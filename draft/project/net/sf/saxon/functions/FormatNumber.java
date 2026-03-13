/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import net.sf.saxon.expr.ArithmeticExpression;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.StatefulSystemFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.DecimalSymbols;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.StringValue;

public class FormatNumber
extends SystemFunction
implements Callable,
StatefulSystemFunction {
    private StructuredQName decimalFormatName;
    private String picture;
    private DecimalSymbols decimalSymbols;
    private SubPicture[] subPictures;

    @Override
    public Expression fixArguments(Expression ... arguments) throws XPathException {
        if (arguments[1] instanceof Literal && (arguments.length == 2 || arguments[2] instanceof Literal)) {
            DecimalFormatManager dfm = this.getRetainedStaticContext().getDecimalFormatManager();
            assert (dfm != null);
            this.picture = ((Literal)arguments[1]).getValue().getStringValue();
            if (arguments.length == 3 && !Literal.isEmptySequence(arguments[2])) {
                try {
                    String lexicalName = ((Literal)arguments[2]).getValue().getStringValue();
                    this.decimalFormatName = StructuredQName.fromLexicalQName(lexicalName, false, true, this.getRetainedStaticContext());
                } catch (XPathException e) {
                    XPathException err = new XPathException("Invalid decimal format name. " + e.getMessage());
                    err.setErrorCode("FODF1280");
                    throw err;
                }
            }
            if (this.decimalFormatName == null) {
                this.decimalSymbols = dfm.getDefaultDecimalFormat();
            } else {
                this.decimalSymbols = dfm.getNamedDecimalFormat(this.decimalFormatName);
                if (this.decimalSymbols == null) {
                    throw new XPathException("Decimal format " + this.decimalFormatName.getDisplayName() + " has not been defined", "FODF1280");
                }
            }
            this.subPictures = FormatNumber.getSubPictures(this.picture, this.decimalSymbols);
        }
        return null;
    }

    private static SubPicture[] getSubPictures(String picture, DecimalSymbols dfs) throws XPathException {
        int[] picture4 = StringValue.expand(picture);
        SubPicture[] pics = new SubPicture[2];
        if (picture4.length == 0) {
            XPathException err = new XPathException("format-number() picture is zero-length");
            err.setErrorCode("FODF1310");
            throw err;
        }
        int sep = -1;
        for (int c = 0; c < picture4.length; ++c) {
            if (picture4[c] != dfs.getPatternSeparator()) continue;
            if (c == 0) {
                FormatNumber.grumble("first subpicture is zero-length");
            } else if (sep >= 0) {
                FormatNumber.grumble("more than one pattern separator");
            } else if (sep == picture4.length - 1) {
                FormatNumber.grumble("second subpicture is zero-length");
            }
            sep = c;
        }
        if (sep < 0) {
            pics[0] = new SubPicture(picture4, dfs);
            pics[1] = null;
        } else {
            int[] pic0 = new int[sep];
            System.arraycopy(picture4, 0, pic0, 0, sep);
            int[] pic1 = new int[picture4.length - sep - 1];
            System.arraycopy(picture4, sep + 1, pic1, 0, picture4.length - sep - 1);
            pics[0] = new SubPicture(pic0, dfs);
            pics[1] = new SubPicture(pic1, dfs);
        }
        return pics;
    }

    private static CharSequence formatNumber(NumericValue number, SubPicture[] subPictures, DecimalSymbols dfs) {
        SubPicture pic;
        NumericValue absN = number;
        String minusSign = "";
        int signum = number.signum();
        if (signum == 0 && number.isNegativeZero()) {
            signum = -1;
        }
        if (signum < 0) {
            absN = number.negate();
            if (subPictures[1] == null) {
                pic = subPictures[0];
                minusSign = "" + FormatNumber.unicodeChar(dfs.getMinusSign());
            } else {
                pic = subPictures[1];
            }
        } else {
            pic = subPictures[0];
        }
        return pic.format(absN, dfs, minusSign);
    }

    private static void grumble(String s) throws XPathException {
        throw new XPathException("format-number picture: " + s, "FODF1310");
    }

    public static BigDecimal adjustToDecimal(double value, int precision) {
        String zeros = precision == 1 ? "00000" : "000000000";
        String nines = precision == 1 ? "99999" : "999999999";
        BigDecimal initial = BigDecimal.valueOf(value);
        BigDecimal trial = null;
        FastStringBuffer fsb = new FastStringBuffer(16);
        BigDecimalValue.decimalToString(initial, fsb);
        String s = fsb.toString();
        int start = s.charAt(0) == '-' ? 1 : 0;
        int p = s.indexOf(".");
        int i = s.lastIndexOf(zeros);
        if (i > 0) {
            if (p < 0 || i < p) {
                FastStringBuffer sb = new FastStringBuffer(s.length());
                sb.append(s.substring(0, i));
                for (int n = i; n < s.length(); ++n) {
                    sb.cat(s.charAt(n) == '.' ? (char)'.' : '0');
                }
                trial = new BigDecimal(sb.toString());
            } else {
                trial = new BigDecimal(s.substring(0, i));
            }
        } else {
            i = s.indexOf(nines);
            if (i >= 0) {
                if (i == start) {
                    FastStringBuffer sb = new FastStringBuffer(s.length() + 1);
                    if (start == 1) {
                        sb.cat('-');
                    }
                    sb.cat('1');
                    for (int n = start; n < s.length(); ++n) {
                        sb.cat(s.charAt(n) == '.' ? (char)'.' : '0');
                    }
                    trial = new BigDecimal(sb.toString());
                } else {
                    while (i >= 0 && (s.charAt(i) == '9' || s.charAt(i) == '.')) {
                        --i;
                    }
                    if (i < 0 || s.charAt(i) == '-') {
                        return initial;
                    }
                    if (p < 0 || i < p) {
                        FastStringBuffer sb = new FastStringBuffer(s.length());
                        sb.append(s.substring(0, i));
                        sb.cat((char)(s.charAt(i) + '\u0001'));
                        for (int n = i; n < s.length(); ++n) {
                            sb.cat(s.charAt(n) == '.' ? (char)'.' : '0');
                        }
                        trial = new BigDecimal(sb.toString());
                    } else {
                        String s2 = s.substring(0, i) + (char)(s.charAt(i) + '\u0001');
                        trial = new BigDecimal(s2);
                    }
                }
            }
        }
        if (trial != null && (precision == 1 ? (double)trial.floatValue() == value : trial.doubleValue() == value)) {
            return trial;
        }
        return initial;
    }

    private static CharSequence unicodeChar(int ch) {
        if (ch < 65536) {
            return "" + (char)ch;
        }
        char[] sb = new char[]{(char)((ch -= 65536) / 1024 + 55296), (char)(ch % 1024 + 56320)};
        return new CharSlice(sb, 0, 2);
    }

    private static int[] insert(int[] array, int used, int value, int position) {
        if (used + 1 > array.length) {
            array = Arrays.copyOf(array, used + 10);
        }
        System.arraycopy(array, position, array, position + 1, used - position);
        array[position] = value;
        return array;
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        DecimalSymbols dfs;
        int numArgs = arguments.length;
        DecimalFormatManager dfm = this.getRetainedStaticContext().getDecimalFormatManager();
        AtomicValue av0 = (AtomicValue)arguments[0].head();
        if (av0 == null) {
            av0 = DoubleValue.NaN;
        }
        NumericValue number = (NumericValue)av0;
        if (this.picture != null) {
            CharSequence result = FormatNumber.formatNumber(number, this.subPictures, this.decimalSymbols);
            return new StringValue(result);
        }
        if (numArgs == 2) {
            dfs = dfm.getDefaultDecimalFormat();
        } else {
            Item arg2 = arguments[2].head();
            if (arg2 == null) {
                dfs = dfm.getDefaultDecimalFormat();
            } else {
                String lexicalName = arg2.getStringValue();
                dfs = this.getNamedDecimalFormat(dfm, lexicalName);
            }
        }
        String format = arguments[1].head().getStringValue();
        SubPicture[] pics = FormatNumber.getSubPictures(format, dfs);
        return new StringValue(FormatNumber.formatNumber(number, pics, dfs));
    }

    protected DecimalSymbols getNamedDecimalFormat(DecimalFormatManager dfm, String lexicalName) throws XPathException {
        StructuredQName qName;
        try {
            qName = StructuredQName.fromLexicalQName(lexicalName, false, true, this.getRetainedStaticContext());
        } catch (XPathException e) {
            XPathException err = new XPathException("Invalid decimal format name. " + e.getMessage());
            err.setErrorCode("FODF1280");
            throw err;
        }
        DecimalSymbols dfs = dfm.getNamedDecimalFormat(qName);
        if (dfs == null) {
            XPathException err = new XPathException("format-number function: decimal-format '" + lexicalName + "' is not defined");
            err.setErrorCode("FODF1280");
            throw err;
        }
        return dfs;
    }

    private static boolean isInDigitFamily(int ch, int zeroDigit) {
        return ch >= zeroDigit && ch < zeroDigit + 10;
    }

    public static String formatExponential(DoubleValue value) {
        try {
            DecimalSymbols dfs = new DecimalSymbols(HostLanguage.XSLT, 31);
            dfs.setInfinity("INF");
            SubPicture[] pics = FormatNumber.getSubPictures("0.0##########################e0", dfs);
            return FormatNumber.formatNumber(value, pics, dfs).toString();
        } catch (XPathException e) {
            return value.getStringValue();
        }
    }

    @Override
    public FormatNumber copy() {
        FormatNumber copy = (FormatNumber)SystemFunction.makeFunction(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), this.getArity());
        copy.decimalFormatName = this.decimalFormatName;
        copy.picture = this.picture;
        copy.decimalSymbols = this.decimalSymbols;
        copy.subPictures = this.subPictures;
        return copy;
    }

    private static class SubPicture {
        int minWholePartSize = 0;
        int maxWholePartSize = 0;
        int minFractionPartSize = 0;
        int maxFractionPartSize = 0;
        int minExponentSize = 0;
        int scalingFactor = 0;
        boolean isPercent = false;
        boolean isPerMille = false;
        String prefix = "";
        String suffix = "";
        int[] wholePartGroupingPositions = null;
        int[] fractionalPartGroupingPositions = null;
        boolean regular;
        boolean is31 = true;

        public SubPicture(int[] pic, DecimalSymbols dfs) throws XPathException {
            int i;
            int percentSign = dfs.getPercent();
            int perMilleSign = dfs.getPerMille();
            int decimalSeparator = dfs.getDecimalSeparator();
            int groupingSeparator = dfs.getGroupingSeparator();
            int digitSign = dfs.getDigit();
            int zeroDigit = dfs.getZeroDigit();
            int exponentSeparator = dfs.getExponentSeparator();
            ArrayList<Integer> wholePartPositions = null;
            ArrayList<Integer> fractionalPartPositions = null;
            boolean foundDigit = false;
            boolean foundDecimalSeparator = false;
            boolean foundExponentSeparator = false;
            boolean foundExponentSeparator2 = false;
            for (int ch : pic) {
                if (ch != digitSign && ch != zeroDigit && !FormatNumber.isInDigitFamily(ch, zeroDigit)) continue;
                foundDigit = true;
                break;
            }
            if (!foundDigit) {
                FormatNumber.grumble("subpicture contains no digit or zero-digit sign");
            }
            int phase = 0;
            block42: for (int c : pic) {
                if (c == percentSign || c == perMilleSign) {
                    if (this.isPercent || this.isPerMille) {
                        FormatNumber.grumble("Cannot have more than one percent or per-mille character in a sub-picture");
                    }
                    this.isPercent = c == percentSign;
                    this.isPerMille = c == perMilleSign;
                    switch (phase) {
                        case 0: {
                            this.prefix = this.prefix + FormatNumber.unicodeChar(c);
                            break;
                        }
                        case 1: 
                        case 2: 
                        case 3: 
                        case 4: 
                        case 5: {
                            if (foundExponentSeparator) {
                                FormatNumber.grumble("Cannot have exponent-separator as well as percent or per-mille character in a sub-picture");
                            }
                        }
                        case 6: {
                            phase = 6;
                            this.suffix = this.suffix + FormatNumber.unicodeChar(c);
                        }
                    }
                    continue;
                }
                if (c == digitSign) {
                    switch (phase) {
                        case 0: 
                        case 1: {
                            phase = 1;
                            ++this.maxWholePartSize;
                            break;
                        }
                        case 2: {
                            FormatNumber.grumble("Digit sign must not appear after a zero-digit sign in the integer part of a sub-picture");
                            break;
                        }
                        case 3: 
                        case 4: {
                            phase = 4;
                            ++this.maxFractionPartSize;
                            break;
                        }
                        case 5: {
                            FormatNumber.grumble("Digit sign must not appear in the exponent part of a sub-picture");
                            break;
                        }
                        case 6: {
                            if (foundExponentSeparator2) {
                                FormatNumber.grumble("There must only be one exponent separator in a sub-picture");
                                break;
                            }
                            FormatNumber.grumble("Passive character must not appear between active characters in a sub-picture");
                        }
                    }
                    continue;
                }
                if (c == zeroDigit || FormatNumber.isInDigitFamily(c, zeroDigit)) {
                    switch (phase) {
                        case 0: 
                        case 1: 
                        case 2: {
                            phase = 2;
                            ++this.minWholePartSize;
                            ++this.maxWholePartSize;
                            break;
                        }
                        case 3: {
                            ++this.minFractionPartSize;
                            ++this.maxFractionPartSize;
                            break;
                        }
                        case 4: {
                            FormatNumber.grumble("Zero digit sign must not appear after a digit sign in the fractional part of a sub-picture");
                            break;
                        }
                        case 5: {
                            ++this.minExponentSize;
                            break;
                        }
                        case 6: {
                            if (foundExponentSeparator2) {
                                FormatNumber.grumble("There must only be one exponent separator in a sub-picture");
                                break;
                            }
                            FormatNumber.grumble("Passive character must not appear between active characters in a sub-picture");
                        }
                    }
                    continue;
                }
                if (c == decimalSeparator) {
                    if (foundDecimalSeparator) {
                        FormatNumber.grumble("There must only be one decimal separator in a sub-picture");
                    }
                    switch (phase) {
                        case 0: 
                        case 1: 
                        case 2: {
                            phase = 3;
                            foundDecimalSeparator = true;
                            break;
                        }
                        case 3: 
                        case 4: 
                        case 5: {
                            if (!foundExponentSeparator) break;
                            FormatNumber.grumble("Decimal separator must not appear in the exponent part of a sub-picture");
                            break;
                        }
                        case 6: {
                            FormatNumber.grumble("Decimal separator cannot come after a character in the suffix");
                        }
                    }
                    continue;
                }
                if (c == groupingSeparator) {
                    switch (phase) {
                        case 0: 
                        case 1: 
                        case 2: {
                            if (wholePartPositions == null) {
                                wholePartPositions = new ArrayList<Integer>(3);
                            }
                            if (wholePartPositions.contains(this.maxWholePartSize)) {
                                FormatNumber.grumble("Sub-picture cannot contain adjacent grouping separators");
                            }
                            wholePartPositions.add(this.maxWholePartSize);
                            break;
                        }
                        case 3: 
                        case 4: {
                            if (this.maxFractionPartSize == 0) {
                                FormatNumber.grumble("Grouping separator cannot be adjacent to decimal separator");
                            }
                            if (fractionalPartPositions == null) {
                                fractionalPartPositions = new ArrayList<Integer>(3);
                            }
                            if (fractionalPartPositions.contains(this.maxFractionPartSize)) {
                                FormatNumber.grumble("Sub-picture cannot contain adjacent grouping separators");
                            }
                            fractionalPartPositions.add(this.maxFractionPartSize);
                            break;
                        }
                        case 5: {
                            if (!foundExponentSeparator) break;
                            FormatNumber.grumble("Grouping separator must not appear in the exponent part of a sub-picture");
                            break;
                        }
                        case 6: {
                            FormatNumber.grumble("Grouping separator found in suffix of sub-picture");
                        }
                    }
                    continue;
                }
                if (c == exponentSeparator) {
                    switch (phase) {
                        case 0: {
                            this.prefix = this.prefix + FormatNumber.unicodeChar(c);
                            break;
                        }
                        case 1: 
                        case 2: 
                        case 3: 
                        case 4: {
                            phase = 5;
                            foundExponentSeparator = true;
                            break;
                        }
                        case 5: {
                            if (!foundExponentSeparator) break;
                            foundExponentSeparator2 = true;
                            phase = 6;
                            this.suffix = this.suffix + FormatNumber.unicodeChar(exponentSeparator);
                            break;
                        }
                        case 6: {
                            this.suffix = this.suffix + FormatNumber.unicodeChar(c);
                        }
                    }
                    continue;
                }
                switch (phase) {
                    case 0: {
                        this.prefix = this.prefix + FormatNumber.unicodeChar(c);
                        continue block42;
                    }
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: {
                        if (this.minExponentSize == 0 && foundExponentSeparator) {
                            phase = 6;
                            this.suffix = this.suffix + FormatNumber.unicodeChar(exponentSeparator);
                            this.suffix = this.suffix + FormatNumber.unicodeChar(c);
                            continue block42;
                        }
                    }
                    case 6: {
                        phase = 6;
                        this.suffix = this.suffix + FormatNumber.unicodeChar(c);
                    }
                }
            }
            this.scalingFactor = this.minWholePartSize;
            if (this.maxWholePartSize == 0 && this.maxFractionPartSize == 0) {
                FormatNumber.grumble("Mantissa contains no digit or zero-digit sign");
            }
            if (this.minWholePartSize == 0 && this.maxFractionPartSize == 0) {
                if (this.minExponentSize != 0) {
                    this.minFractionPartSize = 1;
                    this.maxFractionPartSize = 1;
                } else {
                    this.minWholePartSize = 1;
                }
            }
            if (this.minExponentSize != 0 && this.minWholePartSize == 0 && this.maxWholePartSize != 0) {
                this.minWholePartSize = 1;
            }
            if (this.minWholePartSize == 0 && this.minFractionPartSize == 0) {
                this.minFractionPartSize = 1;
            }
            if (wholePartPositions != null) {
                int n = wholePartPositions.size();
                this.wholePartGroupingPositions = new int[n];
                for (i = 0; i < n; ++i) {
                    this.wholePartGroupingPositions[i] = this.maxWholePartSize - (Integer)wholePartPositions.get(n - i - 1);
                }
                if (n == 1) {
                    this.regular = this.wholePartGroupingPositions[0] * 2 >= this.maxWholePartSize;
                } else if (n > 1) {
                    this.regular = true;
                    int first = this.wholePartGroupingPositions[0];
                    for (int i2 = 1; i2 < n; ++i2) {
                        if (this.wholePartGroupingPositions[i2] == (i2 + 1) * first) continue;
                        this.regular = false;
                        break;
                    }
                    if (this.regular && this.maxWholePartSize - this.wholePartGroupingPositions[n - 1] > first) {
                        this.regular = false;
                    }
                    if (this.regular) {
                        this.wholePartGroupingPositions = new int[1];
                        this.wholePartGroupingPositions[0] = first;
                    }
                }
                if (this.wholePartGroupingPositions[0] == 0) {
                    FormatNumber.grumble("Cannot have a grouping separator at the end of the integer part");
                }
            }
            if (fractionalPartPositions != null) {
                int n = fractionalPartPositions.size();
                this.fractionalPartGroupingPositions = new int[n];
                for (i = 0; i < n; ++i) {
                    this.fractionalPartGroupingPositions[i] = (Integer)fractionalPartPositions.get(i);
                }
            }
        }

        public CharSequence format(NumericValue value, DecimalSymbols dfs, String minusSign) {
            int p;
            int expS;
            int i;
            if (value.isNaN()) {
                return dfs.getNaN();
            }
            int multiplier = 1;
            if (this.isPercent) {
                multiplier = 100;
            } else if (this.isPerMille) {
                multiplier = 1000;
            }
            if (multiplier != 1) {
                try {
                    value = (NumericValue)ArithmeticExpression.compute(value, 2, new Int64Value(multiplier), null);
                } catch (XPathException e) {
                    value = new DoubleValue(Double.POSITIVE_INFINITY);
                }
            }
            if ((value instanceof DoubleValue || value instanceof FloatValue) && Double.isInfinite(value.getDoubleValue())) {
                return minusSign + this.prefix + dfs.getInfinity() + this.suffix;
            }
            FastStringBuffer sb = new FastStringBuffer(16);
            if (value instanceof DoubleValue || value instanceof FloatValue) {
                BigDecimal dec = FormatNumber.adjustToDecimal(value.getDoubleValue(), 2);
                this.formatDecimal(dec, sb);
            } else if (value instanceof IntegerValue) {
                if (this.minExponentSize != 0) {
                    this.formatDecimal(((IntegerValue)value).getDecimalValue(), sb);
                } else {
                    this.formatInteger(value, sb);
                }
            } else if (value instanceof BigDecimalValue) {
                this.formatDecimal(((BigDecimalValue)value).getDecimalValue(), sb);
            }
            int[] ib = StringValue.expand(sb);
            int ibused = ib.length;
            int point = sb.indexOf('.');
            if (point == -1) {
                point = sb.length();
            } else {
                ib[point] = dfs.getDecimalSeparator();
                if (this.maxFractionPartSize == 0) {
                    --ibused;
                }
            }
            if (dfs.getZeroDigit() != 48) {
                int newZero = dfs.getZeroDigit();
                for (i = 0; i < ibused; ++i) {
                    int c = ib[i];
                    if (c < 48 || c > 57) continue;
                    ib[i] = c - 48 + newZero;
                }
            }
            if (dfs.getExponentSeparator() != 101 && (expS = sb.indexOf('e')) != -1) {
                ib[expS] = dfs.getExponentSeparator();
            }
            if (this.wholePartGroupingPositions != null) {
                if (this.regular) {
                    int g = this.wholePartGroupingPositions[0];
                    for (p = point - g; p > 0; p -= g) {
                        ib = FormatNumber.insert(ib, ibused++, dfs.getGroupingSeparator(), p);
                    }
                } else {
                    int[] g = this.wholePartGroupingPositions;
                    p = g.length;
                    for (int j = 0; j < p; ++j) {
                        int wholePartGroupingPosition = g[j];
                        int p2 = point - wholePartGroupingPosition;
                        if (p2 <= 0) continue;
                        ib = FormatNumber.insert(ib, ibused++, dfs.getGroupingSeparator(), p2);
                    }
                }
            }
            if (this.fractionalPartGroupingPositions != null) {
                for (int i2 = 0; i2 < this.fractionalPartGroupingPositions.length && (p = point + 1 + this.fractionalPartGroupingPositions[i2] + i2) < ibused; ++i2) {
                    ib = FormatNumber.insert(ib, ibused++, dfs.getGroupingSeparator(), p);
                }
            }
            FastStringBuffer res = new FastStringBuffer(this.prefix.length() + minusSign.length() + this.suffix.length() + ibused);
            res.append(minusSign);
            res.append(this.prefix);
            for (i = 0; i < ibused; ++i) {
                res.appendWideChar(ib[i]);
            }
            res.append(this.suffix);
            return res;
        }

        private void formatDecimal(BigDecimal dval, FastStringBuffer fsb) {
            int intDigits;
            int exponent = 0;
            if (this.minExponentSize == 0) {
                dval = dval.setScale(this.maxFractionPartSize, RoundingMode.HALF_EVEN);
            } else if (dval.signum() != 0) {
                exponent = dval.precision() - dval.scale() - this.scalingFactor;
                dval = dval.movePointLeft(exponent);
                dval = dval.setScale(this.maxFractionPartSize, RoundingMode.HALF_EVEN);
            }
            BigDecimalValue.decimalToString(dval, fsb);
            int point = fsb.indexOf('.');
            if (point >= 0) {
                for (int zz = this.maxFractionPartSize - this.minFractionPartSize; zz > 0 && fsb.charAt(fsb.length() - 1) == '0'; --zz) {
                    fsb.setLength(fsb.length() - 1);
                }
                intDigits = point;
                if (fsb.charAt(fsb.length() - 1) == '.') {
                    fsb.setLength(fsb.length() - 1);
                }
            } else {
                intDigits = fsb.length();
                if (this.minFractionPartSize > 0) {
                    fsb.cat('.');
                    for (int i = 0; i < this.minFractionPartSize; ++i) {
                        fsb.cat('0');
                    }
                }
            }
            if (this.minWholePartSize == 0 && intDigits == 1 && fsb.charAt(0) == '0') {
                fsb.removeCharAt(0);
            } else {
                fsb.prependRepeated('0', this.minWholePartSize - intDigits);
            }
            if (this.minExponentSize != 0) {
                int length;
                fsb.cat('e');
                IntegerValue exp = (IntegerValue)IntegerValue.makeIntegerValue(exponent);
                String expStr = exp.toString();
                char first = expStr.charAt(0);
                if (first == '-') {
                    fsb.cat('-');
                    expStr = expStr.substring(1);
                }
                if ((length = expStr.length()) < this.minExponentSize) {
                    int zz = this.minExponentSize - length;
                    for (int i = 0; i < zz; ++i) {
                        fsb.cat('0');
                    }
                }
                fsb.append(expStr);
            }
        }

        private void formatInteger(NumericValue value, FastStringBuffer fsb) {
            if (this.minWholePartSize != 0 || value.compareTo(0L) != 0) {
                fsb.cat(value.getStringValueCS());
                int leadingZeroes = this.minWholePartSize - fsb.length();
                fsb.prependRepeated('0', leadingZeroes);
            }
            if (this.minFractionPartSize != 0) {
                fsb.cat('.');
                for (int i = 0; i < this.minFractionPartSize; ++i) {
                    fsb.cat('0');
                }
            }
        }
    }
}


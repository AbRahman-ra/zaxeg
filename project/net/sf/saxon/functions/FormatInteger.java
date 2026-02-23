/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import java.util.function.Function;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.number.Alphanumeric;
import net.sf.saxon.expr.number.IrregularGroupFormatter;
import net.sf.saxon.expr.number.NumberFormatter;
import net.sf.saxon.expr.number.NumericGroupFormatter;
import net.sf.saxon.expr.number.RegularGroupFormatter;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.StatefulSystemFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Numberer;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.regex.ARegularExpression;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.regex.charclass.Categories;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntSet;

public class FormatInteger
extends SystemFunction
implements StatefulSystemFunction {
    private static final RegularExpression badHashPattern;
    private static final RegularExpression modifierPattern;
    private static final RegularExpression decimalDigitPattern;
    public static final String preface = "In the picture string for format-integer, ";
    private Function<IntegerValue, String> formatter = null;

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        boolean opt = true;
        if (!(arguments[1] instanceof Literal)) {
            opt = false;
        }
        if (arguments.length == 3 && !(arguments[2] instanceof Literal)) {
            opt = false;
        }
        if (!opt) {
            return super.makeOptimizedFunctionCall(visitor, contextInfo, arguments);
        }
        Configuration config = visitor.getConfiguration();
        String language = arguments.length == 3 ? ((Literal)arguments[2]).getValue().getStringValue() : config.getDefaultLanguage();
        Numberer numb = config.makeNumberer(language, null);
        this.formatter = this.makeFormatter(numb, ((Literal)arguments[1]).getValue().getStringValue());
        return super.makeOptimizedFunctionCall(visitor, contextInfo, arguments);
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.formatInteger((IntegerValue)arguments[0].head(), (StringValue)arguments[1].head(), arguments.length == 2 ? null : (StringValue)arguments[2].head(), context);
    }

    private StringValue formatInteger(IntegerValue num, StringValue picture, StringValue language, XPathContext context) throws XPathException {
        Configuration config = context.getConfiguration();
        if (num == null) {
            return StringValue.EMPTY_STRING;
        }
        Function<IntegerValue, String> localFormatter = this.formatter;
        if (localFormatter == null) {
            String languageVal = language != null ? language.getStringValue() : config.getDefaultLanguage();
            Numberer numb = config.makeNumberer(languageVal, null);
            localFormatter = this.makeFormatter(numb, picture.getStringValue());
        }
        try {
            return new StringValue(localFormatter.apply(num));
        } catch (UncheckedXPathException e) {
            throw e.getXPathException();
        }
    }

    private Function<IntegerValue, String> makeFormatter(Numberer numb, String pic) throws XPathException {
        String letterValue;
        String modifier;
        String primaryToken;
        if (pic.isEmpty()) {
            throw new XPathException("In the picture string for format-integer, the picture cannot be empty", "FODF1310");
        }
        int lastSemicolon = pic.lastIndexOf(59);
        if (lastSemicolon >= 0) {
            primaryToken = pic.substring(0, lastSemicolon);
            if (primaryToken.isEmpty()) {
                throw new XPathException("In the picture string for format-integer, the primary format token cannot be empty", "FODF1310");
            }
            String string = modifier = lastSemicolon < pic.length() - 1 ? pic.substring(lastSemicolon + 1) : "";
            if (!modifierPattern.matches(modifier)) {
                throw new XPathException("In the picture string for format-integer, the modifier is invalid", "FODF1310");
            }
        } else {
            primaryToken = pic;
            modifier = "";
        }
        boolean ordinal = modifier.startsWith("o");
        boolean alphabetic = modifier.endsWith("a");
        int leftParen = modifier.indexOf(40);
        int rightParen = modifier.lastIndexOf(41);
        String parenthetical = leftParen < 0 ? "" : modifier.substring(leftParen + 1, rightParen);
        String string = letterValue = alphabetic ? "alphabetic" : "traditional";
        String ordinalValue = ordinal ? ("".equals(parenthetical) ? "yes" : parenthetical) : "";
        UnicodeString primary = UnicodeString.makeUnicodeString(primaryToken);
        Categories.Category isDecimalDigit = Categories.getCategory("Nd");
        boolean isDecimalDigitPattern = false;
        for (int i = 0; i < primary.uLength(); ++i) {
            if (!isDecimalDigit.test(primary.uCharAt(i))) continue;
            isDecimalDigitPattern = true;
            break;
        }
        if (isDecimalDigitPattern) {
            if (!decimalDigitPattern.matches(primaryToken)) {
                throw new XPathException("In the picture string for format-integer, the primary format token contains a decimal digit but does not meet the rules for a decimal digit pattern", "FODF1310");
            }
            NumericGroupFormatter picGroupFormat = FormatInteger.getPicSeparators(primaryToken);
            UnicodeString adjustedPicture = picGroupFormat.getAdjustedPicture();
            return num -> {
                try {
                    String s = numb.format(num.abs().longValue(), adjustedPicture, picGroupFormat, letterValue, ordinalValue);
                    return num.signum() < 0 ? "-" + s : s;
                } catch (XPathException e) {
                    throw new UncheckedXPathException(e);
                }
            };
        }
        UnicodeString token = UnicodeString.makeUnicodeString(primaryToken);
        return num -> {
            try {
                String s = numb.format(num.abs().longValue(), token, null, letterValue, ordinalValue);
                return num.signum() < 0 ? "-" + s : s;
            } catch (XPathException e) {
                throw new UncheckedXPathException(e);
            }
        };
    }

    public static NumericGroupFormatter getPicSeparators(String pic) throws XPathException {
        UnicodeString picExpanded = UnicodeString.makeUnicodeString(pic);
        IntHashSet groupingPositions = new IntHashSet(5);
        ArrayList<Integer> separatorList = new ArrayList<Integer>();
        int groupingPosition = 0;
        int firstGroupingPos = 0;
        int lastGroupingPos = 0;
        boolean regularCheck = true;
        int zeroDigit = -1;
        if (badHashPattern.matches(pic)) {
            throw new XPathException("In the picture string for format-integer, the picture is not valid (it uses '#' where disallowed)", "FODF1310");
        }
        block7: for (int i = picExpanded.uLength() - 1; i >= 0; --i) {
            int codePoint = picExpanded.uCharAt(i);
            switch (Character.getType(codePoint)) {
                case 9: {
                    if (zeroDigit == -1) {
                        zeroDigit = Alphanumeric.getDigitFamily(codePoint);
                    } else if (zeroDigit != Alphanumeric.getDigitFamily(codePoint)) {
                        throw new XPathException("In the picture string for format-integer, the picture mixes digits from different digit families", "FODF1310");
                    }
                    ++groupingPosition;
                    continue block7;
                }
                case 1: 
                case 2: 
                case 4: 
                case 5: 
                case 10: 
                case 11: {
                    continue block7;
                }
                default: {
                    if (i == picExpanded.uLength() - 1) {
                        throw new XPathException("In the picture string for format-integer, the picture cannot end with a separator", "FODF1310");
                    }
                    if (codePoint == 35) {
                        ++groupingPosition;
                        if (i == 0) continue block7;
                        switch (Character.getType(picExpanded.uCharAt(i - 1))) {
                            case 1: 
                            case 2: 
                            case 4: 
                            case 5: 
                            case 9: 
                            case 10: 
                            case 11: {
                                throw new XPathException("In the picture string for format-integer, the picture cannot contain alphanumeric character(s) before character '#'", "FODF1310");
                            }
                        }
                        continue block7;
                    }
                    boolean added = groupingPositions.add(groupingPosition);
                    if (!added) {
                        throw new XPathException("In the picture string for format-integer, the picture contains consecutive separators", "FODF1310");
                    }
                    separatorList.add(codePoint);
                    if (groupingPositions.size() == 1) {
                        firstGroupingPos = groupingPosition;
                    } else {
                        if (groupingPosition != firstGroupingPos * groupingPositions.size()) {
                            regularCheck = false;
                        }
                        if ((Integer)separatorList.get(0) != codePoint) {
                            regularCheck = false;
                        }
                    }
                    if (i == 0) {
                        throw new XPathException("In the picture string for format-integer, the picture cannot begin with a separator", "FODF1310");
                    }
                    lastGroupingPos = groupingPosition;
                }
            }
        }
        if (regularCheck && groupingPositions.size() >= 1 && picExpanded.uLength() - lastGroupingPos - groupingPositions.size() > firstGroupingPos) {
            regularCheck = false;
        }
        UnicodeString adjustedPic = FormatInteger.extractSeparators(picExpanded, groupingPositions);
        if (groupingPositions.isEmpty()) {
            return new RegularGroupFormatter(0, "", adjustedPic);
        }
        if (regularCheck) {
            if (separatorList.isEmpty()) {
                return new RegularGroupFormatter(0, "", adjustedPic);
            }
            FastStringBuffer sb = new FastStringBuffer(4);
            sb.appendWideChar((Integer)separatorList.get(0));
            return new RegularGroupFormatter(firstGroupingPos, sb.toString(), adjustedPic);
        }
        return new IrregularGroupFormatter(groupingPositions, separatorList, adjustedPic);
    }

    private static UnicodeString extractSeparators(UnicodeString arr, IntSet excludePositions) {
        FastStringBuffer fsb = new FastStringBuffer(arr.uLength());
        for (int i = 0; i < arr.uLength(); ++i) {
            if (!NumberFormatter.isLetterOrDigit(arr.uCharAt(i))) continue;
            fsb.appendWideChar(arr.uCharAt(i));
        }
        return UnicodeString.makeUnicodeString(fsb);
    }

    @Override
    public SystemFunction copy() {
        FormatInteger fi2 = (FormatInteger)SystemFunction.makeFunction(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), this.getArity());
        fi2.formatter = this.formatter;
        return fi2;
    }

    static {
        try {
            badHashPattern = new ARegularExpression("((\\d+|\\w+)#+.*)|(#+[^\\d]+)", "", "XP20", null, null);
            modifierPattern = new ARegularExpression("([co](\\(.*\\))?)?[at]?", "", "XP20", null, null);
            decimalDigitPattern = new ARegularExpression("^((\\p{Nd}|#|[^\\p{N}\\p{L}])+?)$", "", "XP20", null, null);
        } catch (Exception e) {
            throw new AssertionError((Object)e);
        }
    }
}


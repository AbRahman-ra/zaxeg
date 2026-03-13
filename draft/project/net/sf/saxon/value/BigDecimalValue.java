/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.regex.Pattern;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.Whitespace;

public final class BigDecimalValue
extends DecimalValue {
    public static final int DIVIDE_PRECISION = 18;
    private BigDecimal value;
    private Double doubleValue;
    public static final BigDecimal BIG_DECIMAL_ONE_MILLION = BigDecimal.valueOf(1000000L);
    public static final BigDecimal BIG_DECIMAL_ONE_BILLION = BigDecimal.valueOf(1000000000L);
    public static final BigDecimalValue ZERO = new BigDecimalValue(BigDecimal.valueOf(0L));
    public static final BigDecimalValue ONE = new BigDecimalValue(BigDecimal.valueOf(1L));
    public static final BigDecimalValue TWO = new BigDecimalValue(BigDecimal.valueOf(2L));
    public static final BigDecimalValue THREE = new BigDecimalValue(BigDecimal.valueOf(3L));
    public static final BigDecimal MAX_INT = BigDecimal.valueOf(Integer.MAX_VALUE);
    private static final Pattern decimalPattern = Pattern.compile("(\\-|\\+)?((\\.[0-9]+)|([0-9]+(\\.[0-9]*)?))");

    public BigDecimalValue(BigDecimal value) {
        this.value = value.stripTrailingZeros();
        this.typeLabel = BuiltInAtomicType.DECIMAL;
    }

    public static ConversionResult makeDecimalValue(CharSequence in, boolean validate) {
        try {
            return BigDecimalValue.parse(in);
        } catch (NumberFormatException err) {
            ValidationFailure e = new ValidationFailure("Cannot convert string " + Err.wrap(Whitespace.trim(in), 4) + " to xs:decimal: " + err.getMessage());
            e.setErrorCode("FORG0001");
            return e;
        }
    }

    public static BigDecimalValue parse(CharSequence in) throws NumberFormatException {
        FastStringBuffer digits = new FastStringBuffer(in.length());
        int scale = 0;
        int state = 0;
        boolean foundDigit = false;
        int len = in.length();
        block7: for (int i = 0; i < len; ++i) {
            char c = in.charAt(i);
            switch (c) {
                case '\t': 
                case '\n': 
                case '\r': 
                case ' ': {
                    if (state == 0) continue block7;
                    state = 5;
                    continue block7;
                }
                case '+': {
                    if (state != 0) {
                        throw new NumberFormatException("unexpected sign");
                    }
                    state = 1;
                    continue block7;
                }
                case '-': {
                    if (state != 0) {
                        throw new NumberFormatException("unexpected sign");
                    }
                    state = 1;
                    digits.cat(c);
                    continue block7;
                }
                case '0': 
                case '1': 
                case '2': 
                case '3': 
                case '4': 
                case '5': 
                case '6': 
                case '7': 
                case '8': 
                case '9': {
                    if (state == 0) {
                        state = 1;
                    } else if (state >= 3) {
                        ++scale;
                    }
                    if (state == 5) {
                        throw new NumberFormatException("contains embedded whitespace");
                    }
                    digits.cat(c);
                    foundDigit = true;
                    continue block7;
                }
                case '.': {
                    if (state == 5) {
                        throw new NumberFormatException("contains embedded whitespace");
                    }
                    if (state >= 3) {
                        throw new NumberFormatException("more than one decimal point");
                    }
                    state = 3;
                    continue block7;
                }
                default: {
                    throw new NumberFormatException("invalid character '" + c + "'");
                }
            }
        }
        if (!foundDigit) {
            throw new NumberFormatException("no digits in value");
        }
        while (scale > 0 && digits.charAt(digits.length() - 1) == '0') {
            digits.setLength(digits.length() - 1);
            --scale;
        }
        if (digits.isEmpty() || digits.length() == 1 && digits.charAt(0) == '-') {
            return ZERO;
        }
        BigInteger bigInt = new BigInteger(digits.toString());
        BigDecimal bigDec = new BigDecimal(bigInt, scale);
        return new BigDecimalValue(bigDec);
    }

    public static boolean castableAsDecimal(CharSequence in) {
        CharSequence trimmed = Whitespace.trimWhitespace(in);
        return decimalPattern.matcher(trimmed).matches();
    }

    public BigDecimalValue(double in) throws ValidationException {
        try {
            BigDecimal d = new BigDecimal(in);
            this.value = d.stripTrailingZeros();
        } catch (NumberFormatException err) {
            ValidationFailure e = new ValidationFailure("Cannot convert double " + Err.wrap(in + "", 4) + " to decimal");
            e.setErrorCode("FOCA0002");
            throw e.makeException();
        }
        this.typeLabel = BuiltInAtomicType.DECIMAL;
    }

    public BigDecimalValue(long in) {
        this.value = BigDecimal.valueOf(in);
        this.typeLabel = BuiltInAtomicType.DECIMAL;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        BigDecimalValue v = new BigDecimalValue(this.value);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.DECIMAL;
    }

    @Override
    public double getDoubleValue() {
        if (this.doubleValue == null) {
            double d = this.value.doubleValue();
            this.doubleValue = d;
            return d;
        }
        return this.doubleValue;
    }

    @Override
    public float getFloatValue() {
        return (float)this.value.doubleValue();
    }

    @Override
    public long longValue() throws XPathException {
        return (long)this.value.doubleValue();
    }

    @Override
    public BigDecimal getDecimalValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        BigDecimal round = this.value.setScale(0, RoundingMode.DOWN);
        long value = round.longValue();
        if (value > Integer.MIN_VALUE && value < Integer.MAX_VALUE) {
            return (int)value;
        }
        return Double.valueOf(this.getDoubleValue()).hashCode();
    }

    @Override
    public boolean effectiveBooleanValue() {
        return this.value.signum() != 0;
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        String s = this.getStringValue();
        if (s.indexOf(46) < 0) {
            s = s + ".0";
        }
        return s;
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        return BigDecimalValue.decimalToString(this.value, new FastStringBuffer(16));
    }

    public static FastStringBuffer decimalToString(BigDecimal value, FastStringBuffer fsb) {
        int scale = value.scale();
        if (scale == 0) {
            fsb.append(value.toString());
            return fsb;
        }
        if (scale < 0) {
            String s = value.abs().unscaledValue().toString();
            if (s.equals("0")) {
                fsb.cat('0');
                return fsb;
            }
            if (value.signum() < 0) {
                fsb.cat('-');
            }
            fsb.append(s);
            for (int i = 0; i < -scale; ++i) {
                fsb.cat('0');
            }
            return fsb;
        }
        String s = value.abs().unscaledValue().toString();
        if (s.equals("0")) {
            fsb.cat('0');
            return fsb;
        }
        int len = s.length();
        if (value.signum() < 0) {
            fsb.cat('-');
        }
        if (scale >= len) {
            fsb.append("0.");
            for (int i = len; i < scale; ++i) {
                fsb.cat('0');
            }
            fsb.append(s);
        } else {
            fsb.append(s.substring(0, len - scale));
            fsb.cat('.');
            fsb.append(s.substring(len - scale));
        }
        return fsb;
    }

    @Override
    public NumericValue negate() {
        return new BigDecimalValue(this.value.negate());
    }

    @Override
    public NumericValue floor() {
        return new BigDecimalValue(this.value.setScale(0, RoundingMode.FLOOR));
    }

    @Override
    public NumericValue ceiling() {
        return new BigDecimalValue(this.value.setScale(0, RoundingMode.CEILING));
    }

    @Override
    public NumericValue round(int scale) {
        if (scale >= this.value.scale()) {
            return this;
        }
        switch (this.value.signum()) {
            case -1: {
                return new BigDecimalValue(this.value.setScale(scale, RoundingMode.HALF_DOWN));
            }
            case 0: {
                return this;
            }
            case 1: {
                return new BigDecimalValue(this.value.setScale(scale, RoundingMode.HALF_UP));
            }
        }
        return this;
    }

    @Override
    public NumericValue roundHalfToEven(int scale) {
        if (scale >= this.value.scale()) {
            return this;
        }
        BigDecimal scaledValue = this.value.setScale(scale, RoundingMode.HALF_EVEN);
        return new BigDecimalValue(scaledValue.stripTrailingZeros());
    }

    @Override
    public int signum() {
        return this.value.signum();
    }

    @Override
    public boolean isWholeNumber() {
        return this.value.scale() == 0 || this.value.compareTo(this.value.setScale(0, RoundingMode.DOWN)) == 0;
    }

    @Override
    public int asSubscript() {
        if (this.isWholeNumber() && this.value.compareTo(BigDecimal.ZERO) > 0 && this.value.compareTo(MAX_INT) <= 0) {
            try {
                return (int)this.longValue();
            } catch (XPathException e) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public NumericValue abs() {
        if (this.value.signum() > 0) {
            return this;
        }
        return new BigDecimalValue(this.value.negate());
    }

    @Override
    public int compareTo(NumericValue other) {
        if (NumericValue.isInteger(other)) {
            try {
                return this.value.compareTo(other.getDecimalValue());
            } catch (XPathException err) {
                throw new AssertionError((Object)"Conversion of integer to decimal should never fail");
            }
        }
        if (other instanceof BigDecimalValue) {
            return this.value.compareTo(((BigDecimalValue)other).value);
        }
        if (other instanceof FloatValue) {
            return -other.compareTo(this);
        }
        return super.compareTo(other);
    }

    @Override
    public int compareTo(long other) {
        if (other == 0L) {
            return this.value.signum();
        }
        return this.value.compareTo(BigDecimal.valueOf(other));
    }

    @Override
    public Comparable getSchemaComparable() {
        return new DecimalComparable(this);
    }

    @Override
    public boolean isIdentical(AtomicValue v) {
        return v instanceof DecimalValue && this.equals(v);
    }

    protected static class DecimalComparable
    implements Comparable {
        protected BigDecimalValue value;

        public DecimalComparable(BigDecimalValue value) {
            this.value = value;
        }

        public BigDecimal asBigDecimal() {
            return this.value.getDecimalValue();
        }

        public int compareTo(Object o) {
            if (o instanceof DecimalComparable) {
                return this.asBigDecimal().compareTo(((DecimalComparable)o).asBigDecimal());
            }
            if (o instanceof Int64Value.Int64Comparable) {
                return this.asBigDecimal().compareTo(BigDecimal.valueOf(((Int64Value.Int64Comparable)o).asLong()));
            }
            if (o instanceof BigIntegerValue.BigIntegerComparable) {
                return this.asBigDecimal().compareTo(new BigDecimal(((BigIntegerValue.BigIntegerComparable)o).asBigInteger()));
            }
            return Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return this.compareTo(o) == 0;
        }

        public int hashCode() {
            if (this.value.isWholeNumber()) {
                IntegerValue iv = Converter.DecimalToInteger.INSTANCE.convert(this.value);
                return iv.getSchemaComparable().hashCode();
            }
            return this.value.hashCode();
        }
    }
}


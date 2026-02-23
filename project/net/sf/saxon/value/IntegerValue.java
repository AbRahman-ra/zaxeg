/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import net.sf.saxon.functions.FormatNumber;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.Whitespace;

public abstract class IntegerValue
extends DecimalValue {
    private static long NO_LIMIT = -9999L;
    private static long MAX_UNSIGNED_LONG = -9998L;
    private static long[] ranges = new long[]{533L, NO_LIMIT, NO_LIMIT, 536L, Long.MIN_VALUE, Long.MAX_VALUE, 537L, Integer.MIN_VALUE, Integer.MAX_VALUE, 538L, -32768L, 32767L, 539L, -128L, 127L, 540L, 0L, NO_LIMIT, 541L, 1L, NO_LIMIT, 534L, NO_LIMIT, 0L, 535L, NO_LIMIT, -1L, 542L, 0L, MAX_UNSIGNED_LONG, 543L, 0L, 0xFFFFFFFFL, 544L, 0L, 65535L, 545L, 0L, 255L};

    public static IntegerValue makeIntegerValue(BigInteger value) {
        if (value.compareTo(BigIntegerValue.MAX_LONG) > 0 || value.compareTo(BigIntegerValue.MIN_LONG) < 0) {
            return new BigIntegerValue(value);
        }
        return Int64Value.makeIntegerValue(value.longValue());
    }

    public static ConversionResult makeIntegerValue(double value) {
        if (Double.isNaN(value)) {
            ValidationFailure err = new ValidationFailure("Cannot convert double NaN to an integer");
            err.setErrorCode("FOCA0002");
            return err;
        }
        if (Double.isInfinite(value)) {
            ValidationFailure err = new ValidationFailure("Cannot convert double INF to an integer");
            err.setErrorCode("FOCA0002");
            return err;
        }
        if (value > 9.223372036854776E18 || value < -9.223372036854776E18) {
            if (value == Math.floor(value)) {
                return new BigIntegerValue(FormatNumber.adjustToDecimal(value, 2).toBigInteger());
            }
            return new BigIntegerValue(new BigDecimal(value).toBigInteger());
        }
        return Int64Value.makeIntegerValue((long)value);
    }

    public static ConversionResult makeIntegerValue(DoubleValue doubleValue) {
        double value = doubleValue.getDoubleValue();
        return IntegerValue.makeIntegerValue(value);
    }

    public abstract ValidationFailure convertToSubType(BuiltInAtomicType var1, boolean var2);

    public abstract ValidationFailure validateAgainstSubType(BuiltInAtomicType var1);

    public static boolean checkRange(long value, BuiltInAtomicType type) {
        int fp = type.getFingerprint();
        for (int i = 0; i < ranges.length; i += 3) {
            if (ranges[i] != (long)fp) continue;
            long min = ranges[i + 1];
            if (min != NO_LIMIT && value < min) {
                return false;
            }
            long max = ranges[i + 2];
            return max == NO_LIMIT || max == MAX_UNSIGNED_LONG || value <= max;
        }
        throw new IllegalArgumentException("No range information found for integer subtype " + type.getDescription());
    }

    public static IntegerValue getMinInclusive(BuiltInAtomicType type) {
        int fp = type.getFingerprint();
        for (int i = 0; i < ranges.length; i += 3) {
            if (ranges[i] != (long)fp) continue;
            long min = ranges[i + 1];
            if (min == NO_LIMIT) {
                return null;
            }
            return Int64Value.makeIntegerValue(min);
        }
        return null;
    }

    public static IntegerValue getMaxInclusive(BuiltInAtomicType type) {
        int fp = type.getFingerprint();
        for (int i = 0; i < ranges.length; i += 3) {
            if (ranges[i] != (long)fp) continue;
            long max = ranges[i + 2];
            if (max == NO_LIMIT) {
                return null;
            }
            if (max == MAX_UNSIGNED_LONG) {
                return IntegerValue.makeIntegerValue(BigIntegerValue.MAX_UNSIGNED_LONG);
            }
            return Int64Value.makeIntegerValue(max);
        }
        return null;
    }

    public static boolean checkBigRange(BigInteger big, BuiltInAtomicType type) {
        for (int i = 0; i < ranges.length; i += 3) {
            if (ranges[i] != (long)type.getFingerprint()) continue;
            long min = ranges[i + 1];
            if (min != NO_LIMIT && BigInteger.valueOf(min).compareTo(big) > 0) {
                return false;
            }
            long max = ranges[i + 2];
            if (max == NO_LIMIT) {
                return true;
            }
            if (max == MAX_UNSIGNED_LONG) {
                return BigIntegerValue.MAX_UNSIGNED_LONG.compareTo(big) >= 0;
            }
            return BigInteger.valueOf(max).compareTo(big) >= 0;
        }
        throw new IllegalArgumentException("No range information found for integer subtype " + type.getDescription());
    }

    public static ConversionResult stringToInteger(CharSequence s) {
        int start;
        int len = s.length();
        int last = len - 1;
        for (start = 0; start < len && s.charAt(start) <= ' '; ++start) {
        }
        while (last > start && s.charAt(last) <= ' ') {
            --last;
        }
        if (start > last) {
            return new ValidationFailure("Cannot convert zero-length string to an integer");
        }
        if (last - start < 16) {
            boolean negative = false;
            long value = 0L;
            int i = start;
            if (s.charAt(i) == '+') {
                ++i;
            } else if (s.charAt(i) == '-') {
                negative = true;
                ++i;
            }
            if (i > last) {
                return new ValidationFailure("Cannot convert string " + Err.wrap(s, 4) + " to integer: no digits after the sign");
            }
            while (i <= last) {
                char d;
                if ((d = s.charAt(i++)) >= '0' && d <= '9') {
                    value = 10L * value + (long)(d - 48);
                    continue;
                }
                return new ValidationFailure("Cannot convert string " + Err.wrap(s, 4) + " to an integer");
            }
            return Int64Value.makeIntegerValue(negative ? -value : value);
        }
        try {
            CharSequence t = Whitespace.trimWhitespace(s);
            if (t.charAt(0) == '+') {
                t = t.subSequence(1, t.length());
            }
            if (t.length() < 16) {
                return new Int64Value(Long.parseLong(t.toString()));
            }
            return new BigIntegerValue(new BigInteger(t.toString()));
        } catch (NumberFormatException err) {
            return new ValidationFailure("Cannot convert string " + Err.wrap(s, 4) + " to an integer");
        }
    }

    public static ValidationFailure castableAsInteger(CharSequence input) {
        CharSequence s = Whitespace.trimWhitespace(input);
        int last = s.length() - 1;
        if (last < 0) {
            return new ValidationFailure("Cannot convert empty string to an integer");
        }
        int i = 0;
        if (s.charAt(i) == '+' || s.charAt(i) == '-') {
            ++i;
        }
        if (i > last) {
            return new ValidationFailure("Cannot convert string " + Err.wrap(s, 4) + " to integer: no digits after the sign");
        }
        while (i <= last) {
            char d;
            if ((d = s.charAt(i++)) >= '0' && d <= '9') continue;
            return new ValidationFailure("Cannot convert string " + Err.wrap(s, 4) + " to an integer: contains a character that is not a digit");
        }
        return null;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.INTEGER;
    }

    @Override
    public abstract BigDecimal getDecimalValue();

    @Override
    public boolean isWholeNumber() {
        return true;
    }

    public abstract IntegerValue plus(IntegerValue var1);

    public abstract IntegerValue minus(IntegerValue var1);

    public abstract IntegerValue times(IntegerValue var1);

    public abstract NumericValue div(IntegerValue var1) throws XPathException;

    public NumericValue div(IntegerValue other, Location locator) throws XPathException {
        try {
            return this.div(other);
        } catch (XPathException err) {
            err.maybeSetLocation(locator);
            throw err;
        }
    }

    public abstract IntegerValue mod(IntegerValue var1) throws XPathException;

    public IntegerValue mod(IntegerValue other, Location locator) throws XPathException {
        try {
            return this.mod(other);
        } catch (XPathException err) {
            err.maybeSetLocation(locator);
            throw err;
        }
    }

    public abstract IntegerValue idiv(IntegerValue var1) throws XPathException;

    public IntegerValue idiv(IntegerValue other, Location locator) throws XPathException {
        try {
            return this.idiv(other);
        } catch (XPathException err) {
            err.maybeSetLocation(locator);
            throw err;
        }
    }

    public abstract BigInteger asBigInteger();

    protected static int signum(int i) {
        return i >> 31 | -i >>> 31;
    }

    @Override
    public boolean isIdentical(AtomicValue v) {
        return v instanceof IntegerValue && this.equals(v);
    }
}


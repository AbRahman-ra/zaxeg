/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import net.sf.saxon.expr.Calculator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;

public final class Int64Value
extends IntegerValue {
    public static final Int64Value MINUS_ONE = new Int64Value(-1L);
    public static final Int64Value ZERO = new Int64Value(0L);
    public static final Int64Value PLUS_ONE = new Int64Value(1L);
    public static final Int64Value MAX_LONG = new Int64Value(Long.MAX_VALUE);
    public static final Int64Value MIN_LONG = new Int64Value(Long.MIN_VALUE);
    private long value;
    private static final Int64Value[] SMALL_INTEGERS = new Int64Value[]{new Int64Value(0L), new Int64Value(1L), new Int64Value(2L), new Int64Value(3L), new Int64Value(4L), new Int64Value(5L), new Int64Value(6L), new Int64Value(7L), new Int64Value(8L), new Int64Value(9L), new Int64Value(10L), new Int64Value(11L), new Int64Value(12L), new Int64Value(13L), new Int64Value(14L), new Int64Value(15L), new Int64Value(16L), new Int64Value(17L), new Int64Value(18L), new Int64Value(19L), new Int64Value(20L)};

    public Int64Value(long value) {
        this.value = value;
        this.typeLabel = BuiltInAtomicType.INTEGER;
    }

    public Int64Value(long val, BuiltInAtomicType type, boolean check) throws XPathException {
        this.value = val;
        this.typeLabel = type;
        if (check && !Int64Value.checkRange(this.value, type)) {
            XPathException err = new XPathException("Integer value " + val + " is out of range for the requested type " + type.getDescription());
            err.setErrorCode("XPTY0004");
            err.setIsTypeError(true);
            throw err;
        }
    }

    public static Int64Value makeIntegerValue(long value) {
        if (value <= 20L && value >= 0L) {
            return SMALL_INTEGERS[(int)value];
        }
        return new Int64Value(value);
    }

    public static Int64Value makeDerived(long val, AtomicType type) {
        Int64Value v = new Int64Value(val);
        v.typeLabel = type;
        return v;
    }

    public static Int64Value signum(long val) {
        if (val == 0L) {
            return ZERO;
        }
        return val < 0L ? MINUS_ONE : PLUS_ONE;
    }

    @Override
    public int asSubscript() {
        if (this.value > 0L && this.value <= Integer.MAX_VALUE) {
            return (int)this.value;
        }
        return -1;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        if (typeLabel.getPrimitiveType() == 533) {
            Int64Value v = new Int64Value(this.value);
            v.typeLabel = typeLabel;
            return v;
        }
        return new BigDecimalValue(this.value);
    }

    @Override
    public ValidationFailure convertToSubType(BuiltInAtomicType subtype, boolean validate) {
        if (!validate) {
            this.setSubType(subtype);
            return null;
        }
        if (this.checkRange(subtype)) {
            return null;
        }
        ValidationFailure err = new ValidationFailure("String " + this.value + " cannot be converted to integer subtype " + subtype.getDescription());
        err.setErrorCode("FORG0001");
        return err;
    }

    @Override
    public ValidationFailure validateAgainstSubType(BuiltInAtomicType type) {
        if (Int64Value.checkRange(this.value, type)) {
            return null;
        }
        ValidationFailure err = new ValidationFailure("Value " + this.value + " cannot be converted to integer subtype " + type.getDescription());
        err.setErrorCode("FORG0001");
        return err;
    }

    public void setSubType(AtomicType type) {
        this.typeLabel = type;
    }

    public boolean checkRange(BuiltInAtomicType type) {
        this.typeLabel = type;
        return Int64Value.checkRange(this.value, type);
    }

    @Override
    public Comparable getSchemaComparable() {
        return new Int64Comparable(this);
    }

    @Override
    public int hashCode() {
        if (this.value > Integer.MIN_VALUE && this.value < Integer.MAX_VALUE) {
            return (int)this.value;
        }
        return Double.valueOf(this.getDoubleValue()).hashCode();
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public boolean effectiveBooleanValue() {
        return this.value != 0L;
    }

    @Override
    public int compareTo(NumericValue other) {
        if (other instanceof Int64Value) {
            return Long.compare(this.value, ((Int64Value)other).value);
        }
        if (other instanceof BigIntegerValue) {
            return BigInteger.valueOf(this.value).compareTo(((BigIntegerValue)other).asBigInteger());
        }
        if (other instanceof BigDecimalValue) {
            return new BigDecimal(this.value).compareTo(((BigDecimalValue)other).getDecimalValue());
        }
        return super.compareTo(other);
    }

    @Override
    public int compareTo(long other) {
        return Long.compare(this.value, other);
    }

    @Override
    public String getPrimitiveStringValue() {
        return Long.toString(this.value);
    }

    @Override
    public double getDoubleValue() {
        return this.value;
    }

    @Override
    public float getFloatValue() {
        return this.value;
    }

    @Override
    public BigDecimal getDecimalValue() {
        return BigDecimal.valueOf(this.value);
    }

    @Override
    public NumericValue negate() {
        if (this.value == Long.MIN_VALUE) {
            return BigIntegerValue.makeIntegerValue(BigInteger.valueOf(this.value)).negate();
        }
        return new Int64Value(-this.value);
    }

    @Override
    public NumericValue floor() {
        return this;
    }

    @Override
    public NumericValue ceiling() {
        return this;
    }

    @Override
    public NumericValue round(int scale) {
        if (scale >= 0 || this.value == 0L) {
            return this;
        }
        if (scale < -15) {
            return new BigIntegerValue(this.value).round(scale);
        }
        long absolute = Math.abs(this.value);
        long factor = 1L;
        for (long i = 1L; i <= (long)(-scale); ++i) {
            factor *= 10L;
        }
        long modulus = absolute % factor;
        long rval = absolute - modulus;
        long d = modulus * 2L;
        if (this.value > 0L) {
            if (d >= factor) {
                rval += factor;
            }
        } else {
            if (d > factor) {
                rval += factor;
            }
            rval = -rval;
        }
        return new Int64Value(rval);
    }

    @Override
    public NumericValue roundHalfToEven(int scale) {
        if (scale >= 0) {
            return this;
        }
        if (scale < -15) {
            return new BigIntegerValue(this.value).roundHalfToEven(scale);
        }
        long absolute = Math.abs(this.value);
        long factor = 1L;
        for (long i = 1L; i <= (long)(-scale); ++i) {
            factor *= 10L;
        }
        long modulus = absolute % factor;
        long rval = absolute - modulus;
        long d = modulus * 2L;
        if (d > factor) {
            rval += factor;
        } else if (d >= factor && rval % (2L * factor) != 0L) {
            rval += factor;
        }
        if (this.value < 0L) {
            rval = -rval;
        }
        return new Int64Value(rval);
    }

    @Override
    public int signum() {
        if (this.value > 0L) {
            return 1;
        }
        if (this.value == 0L) {
            return 0;
        }
        return -1;
    }

    @Override
    public NumericValue abs() {
        if (this.value > 0L) {
            return this;
        }
        if (this.value == Long.MIN_VALUE) {
            return new BigIntegerValue(new BigInteger("9223372036854775808"));
        }
        return Int64Value.makeIntegerValue(-this.value);
    }

    @Override
    public IntegerValue plus(IntegerValue other) {
        if (other instanceof Int64Value) {
            long topa = this.value >> 60 & 0xFL;
            if (topa != 0L && topa != 15L) {
                return new BigIntegerValue(this.value).plus(new BigIntegerValue(((Int64Value)other).value));
            }
            long topb = ((Int64Value)other).value >> 60 & 0xFL;
            if (topb != 0L && topb != 15L) {
                return new BigIntegerValue(this.value).plus(new BigIntegerValue(((Int64Value)other).value));
            }
            return Int64Value.makeIntegerValue(this.value + ((Int64Value)other).value);
        }
        return new BigIntegerValue(this.value).plus(other);
    }

    @Override
    public IntegerValue minus(IntegerValue other) {
        if (other instanceof Int64Value) {
            long topa = this.value >> 60 & 0xFL;
            if (topa != 0L && topa != 15L) {
                return new BigIntegerValue(this.value).minus(new BigIntegerValue(((Int64Value)other).value));
            }
            long topb = ((Int64Value)other).value >> 60 & 0xFL;
            if (topb != 0L && topb != 15L) {
                return new BigIntegerValue(this.value).minus(new BigIntegerValue(((Int64Value)other).value));
            }
            return Int64Value.makeIntegerValue(this.value - ((Int64Value)other).value);
        }
        return new BigIntegerValue(this.value).minus(other);
    }

    @Override
    public IntegerValue times(IntegerValue other) {
        if (other instanceof Int64Value) {
            if (this.isLong() || ((Int64Value)other).isLong()) {
                return new BigIntegerValue(this.value).times(new BigIntegerValue(((Int64Value)other).value));
            }
            return Int64Value.makeIntegerValue(this.value * ((Int64Value)other).value);
        }
        return new BigIntegerValue(this.value).times(other);
    }

    @Override
    public NumericValue div(IntegerValue other) throws XPathException {
        if (other instanceof Int64Value) {
            long quotient = ((Int64Value)other).value;
            if (quotient == 0L) {
                throw new XPathException("Integer division by zero", "FOAR0001");
            }
            if (this.isLong() || ((Int64Value)other).isLong()) {
                return new BigIntegerValue(this.value).div(new BigIntegerValue(quotient));
            }
            if (this.value % quotient == 0L) {
                return Int64Value.makeIntegerValue(this.value / quotient);
            }
            return Calculator.decimalDivide(new BigDecimalValue(this.value), new BigDecimalValue(quotient));
        }
        return new BigIntegerValue(this.value).div(other);
    }

    @Override
    public IntegerValue mod(IntegerValue other) throws XPathException {
        if (other instanceof Int64Value) {
            long quotient = ((Int64Value)other).value;
            if (quotient == 0L) {
                throw new XPathException("Integer modulo zero", "FOAR0001");
            }
            if (this.isLong() || ((Int64Value)other).isLong()) {
                return new BigIntegerValue(this.value).mod(new BigIntegerValue(((Int64Value)other).value));
            }
            return Int64Value.makeIntegerValue(this.value % quotient);
        }
        return new BigIntegerValue(this.value).mod(other);
    }

    @Override
    public IntegerValue idiv(IntegerValue other) throws XPathException {
        if (other instanceof Int64Value) {
            if (this.isLong() || ((Int64Value)other).isLong()) {
                return new BigIntegerValue(this.value).idiv(new BigIntegerValue(((Int64Value)other).value));
            }
            try {
                return Int64Value.makeIntegerValue(this.value / ((Int64Value)other).value);
            } catch (ArithmeticException err) {
                XPathException e = "/ by zero".equals(err.getMessage()) ? new XPathException("Integer division by zero", "FOAR0001") : new XPathException("Integer division failure", err);
                throw e;
            }
        }
        return new BigIntegerValue(this.value).idiv(other);
    }

    private boolean isLong() {
        long top = this.value >> 31;
        return top != 0L;
    }

    @Override
    public BigInteger asBigInteger() {
        return BigInteger.valueOf(this.value);
    }

    protected static class Int64Comparable
    implements Comparable {
        protected Int64Value value;

        public Int64Comparable(Int64Value value) {
            this.value = value;
        }

        public long asLong() {
            return this.value.longValue();
        }

        public int compareTo(Object o) {
            if (o instanceof Int64Comparable) {
                long long1;
                long long0 = this.value.longValue();
                if (long0 <= (long1 = ((Int64Comparable)o).value.longValue())) {
                    if (long0 == long1) {
                        return 0;
                    }
                    return -1;
                }
                return 1;
            }
            if (o instanceof BigIntegerValue.BigIntegerComparable) {
                return this.value.asBigInteger().compareTo(((BigIntegerValue.BigIntegerComparable)o).asBigInteger());
            }
            if (o instanceof BigDecimalValue.DecimalComparable) {
                return this.value.getDecimalValue().compareTo(((BigDecimalValue.DecimalComparable)o).asBigDecimal());
            }
            return Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            if (o instanceof Int64Comparable) {
                return this.asLong() == ((Int64Comparable)o).asLong();
            }
            return this.compareTo(o) == 0;
        }

        public int hashCode() {
            return (int)this.asLong();
        }
    }
}


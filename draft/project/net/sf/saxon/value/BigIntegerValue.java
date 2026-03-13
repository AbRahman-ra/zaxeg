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
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;

public final class BigIntegerValue
extends IntegerValue {
    private BigInteger value;
    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
    public static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    public static final BigInteger MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
    public static final BigInteger MAX_UNSIGNED_LONG = new BigInteger("18446744073709551615");
    public static final BigIntegerValue ZERO = new BigIntegerValue(BigInteger.ZERO);

    public BigIntegerValue(BigInteger value) {
        this.value = value;
        this.typeLabel = BuiltInAtomicType.INTEGER;
    }

    public BigIntegerValue(BigInteger value, AtomicType typeLabel) {
        this.value = value;
        this.typeLabel = typeLabel;
    }

    public BigIntegerValue(long value) {
        this.value = BigInteger.valueOf(value);
        this.typeLabel = BuiltInAtomicType.INTEGER;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        if (typeLabel.getPrimitiveType() == 533) {
            BigIntegerValue v = new BigIntegerValue(this.value);
            v.typeLabel = typeLabel;
            return v;
        }
        return new BigDecimalValue(new BigDecimal(this.value));
    }

    @Override
    public ValidationFailure convertToSubType(BuiltInAtomicType type, boolean validate) {
        if (!validate) {
            this.typeLabel = type;
            return null;
        }
        if (IntegerValue.checkBigRange(this.value, type)) {
            this.typeLabel = type;
            return null;
        }
        ValidationFailure err = new ValidationFailure("Integer value is out of range for subtype " + type.getDisplayName());
        err.setErrorCode("FORG0001");
        return err;
    }

    @Override
    public ValidationFailure validateAgainstSubType(BuiltInAtomicType type) {
        if (IntegerValue.checkBigRange(this.value, type)) {
            this.typeLabel = type;
            return null;
        }
        ValidationFailure err = new ValidationFailure("Integer value is out of range for subtype " + type.getDisplayName());
        err.setErrorCode("FORG0001");
        return err;
    }

    @Override
    public int hashCode() {
        if (this.value.compareTo(MIN_INT) >= 0 && this.value.compareTo(MAX_INT) <= 0) {
            return this.value.intValue();
        }
        return Double.valueOf(this.getDoubleValue()).hashCode();
    }

    @Override
    public long longValue() {
        return this.value.longValue();
    }

    @Override
    public BigInteger asBigInteger() {
        return this.value;
    }

    public boolean isWithinLongRange() {
        return this.value.compareTo(MIN_LONG) >= 0 && this.value.compareTo(MAX_LONG) <= 0;
    }

    public BigDecimal asDecimal() {
        return new BigDecimal(this.value);
    }

    @Override
    public boolean effectiveBooleanValue() {
        return this.value.compareTo(BigInteger.ZERO) != 0;
    }

    @Override
    public int compareTo(NumericValue other) {
        if (other instanceof BigIntegerValue) {
            return this.value.compareTo(((BigIntegerValue)other).value);
        }
        if (other instanceof Int64Value) {
            return this.value.compareTo(BigInteger.valueOf(((Int64Value)other).longValue()));
        }
        if (other instanceof BigDecimalValue) {
            return this.asDecimal().compareTo(((BigDecimalValue)other).getDecimalValue());
        }
        return super.compareTo(other);
    }

    @Override
    public int compareTo(long other) {
        if (other == 0L) {
            return this.value.signum();
        }
        return this.value.compareTo(BigInteger.valueOf(other));
    }

    @Override
    public String getPrimitiveStringValue() {
        return this.value.toString();
    }

    @Override
    public double getDoubleValue() {
        return this.value.doubleValue();
    }

    @Override
    public BigDecimal getDecimalValue() {
        return new BigDecimal(this.value);
    }

    @Override
    public float getFloatValue() {
        return (float)this.getDoubleValue();
    }

    @Override
    public NumericValue negate() {
        return new BigIntegerValue(this.value.negate());
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
        if (scale >= 0) {
            return this;
        }
        BigInteger factor = BigInteger.valueOf(10L).pow(-scale);
        BigInteger[] pair = this.value.divideAndRemainder(factor);
        int up = pair[1].compareTo(factor.divide(BigInteger.valueOf(2L)));
        if (up >= 0) {
            pair[0] = pair[0].add(BigInteger.valueOf(1L));
        }
        return BigIntegerValue.makeIntegerValue(pair[0].multiply(factor));
    }

    @Override
    public NumericValue roundHalfToEven(int scale) {
        if (scale >= 0) {
            return this;
        }
        BigInteger factor = BigInteger.valueOf(10L).pow(-scale);
        BigInteger[] pair = this.value.divideAndRemainder(factor);
        int up = pair[1].compareTo(factor.divide(BigInteger.valueOf(2L)));
        if (up > 0) {
            pair[0] = pair[0].add(BigInteger.valueOf(1L));
        } else if (up == 0 && pair[0].mod(BigInteger.valueOf(2L)).signum() != 0) {
            pair[0] = pair[0].add(BigInteger.valueOf(1L));
        }
        return BigIntegerValue.makeIntegerValue(pair[0].multiply(factor));
    }

    @Override
    public int signum() {
        return this.value.signum();
    }

    @Override
    public NumericValue abs() {
        if (this.value.signum() >= 0) {
            return this;
        }
        return new BigIntegerValue(this.value.abs());
    }

    @Override
    public boolean isWholeNumber() {
        return true;
    }

    @Override
    public int asSubscript() {
        if (this.value.compareTo(BigInteger.ZERO) > 0 && this.value.compareTo(MAX_INT) <= 0) {
            return (int)this.longValue();
        }
        return -1;
    }

    @Override
    public IntegerValue plus(IntegerValue other) {
        if (other instanceof BigIntegerValue) {
            return BigIntegerValue.makeIntegerValue(this.value.add(((BigIntegerValue)other).value));
        }
        return BigIntegerValue.makeIntegerValue(this.value.add(BigInteger.valueOf(((Int64Value)other).longValue())));
    }

    @Override
    public IntegerValue minus(IntegerValue other) {
        if (other instanceof BigIntegerValue) {
            return BigIntegerValue.makeIntegerValue(this.value.subtract(((BigIntegerValue)other).value));
        }
        return BigIntegerValue.makeIntegerValue(this.value.subtract(BigInteger.valueOf(((Int64Value)other).longValue())));
    }

    @Override
    public IntegerValue times(IntegerValue other) {
        if (other instanceof BigIntegerValue) {
            return BigIntegerValue.makeIntegerValue(this.value.multiply(((BigIntegerValue)other).value));
        }
        return BigIntegerValue.makeIntegerValue(this.value.multiply(BigInteger.valueOf(((Int64Value)other).longValue())));
    }

    @Override
    public NumericValue div(IntegerValue other) throws XPathException {
        BigInteger oi = other instanceof BigIntegerValue ? ((BigIntegerValue)other).value : BigInteger.valueOf(other.longValue());
        BigDecimalValue a = new BigDecimalValue(new BigDecimal(this.value));
        BigDecimalValue b = new BigDecimalValue(new BigDecimal(oi));
        return Calculator.decimalDivide(a, b);
    }

    @Override
    public IntegerValue mod(IntegerValue other) throws XPathException {
        try {
            if (other instanceof BigIntegerValue) {
                return BigIntegerValue.makeIntegerValue(this.value.remainder(((BigIntegerValue)other).value));
            }
            return BigIntegerValue.makeIntegerValue(this.value.remainder(BigInteger.valueOf(other.longValue())));
        } catch (ArithmeticException err) {
            XPathException e = BigInteger.valueOf(other.longValue()).signum() == 0 ? new XPathException("Integer modulo zero", "FOAR0001") : new XPathException("Integer mod operation failure", err);
            throw e;
        }
    }

    @Override
    public IntegerValue idiv(IntegerValue other) throws XPathException {
        BigInteger oi = other instanceof BigIntegerValue ? ((BigIntegerValue)other).value : BigInteger.valueOf(other.longValue());
        try {
            return BigIntegerValue.makeIntegerValue(this.value.divide(oi));
        } catch (ArithmeticException err) {
            XPathException e = "/ by zero".equals(err.getMessage()) ? new XPathException("Integer division by zero", "FOAR0001") : new XPathException("Integer division failure", err);
            throw e;
        }
    }

    @Override
    public Comparable getSchemaComparable() {
        return new BigIntegerComparable(this);
    }

    @Override
    public IntegerValue reduce() {
        if (this.compareTo(Long.MAX_VALUE) < 0 && this.compareTo(Long.MIN_VALUE) > 0) {
            Int64Value iv = new Int64Value(this.longValue());
            iv.setTypeLabel(this.typeLabel);
            return iv;
        }
        return this;
    }

    @Override
    public BigIntegerValue asAtomic() {
        return this;
    }

    protected static class BigIntegerComparable
    implements Comparable {
        protected BigIntegerValue value;

        public BigIntegerComparable(BigIntegerValue value) {
            this.value = value;
        }

        public BigInteger asBigInteger() {
            return this.value.asBigInteger();
        }

        public int compareTo(Object o) {
            if (o instanceof Int64Value.Int64Comparable) {
                return this.asBigInteger().compareTo(BigInteger.valueOf(((Int64Value.Int64Comparable)o).asLong()));
            }
            if (o instanceof BigIntegerComparable) {
                return this.asBigInteger().compareTo(((BigIntegerComparable)o).asBigInteger());
            }
            if (o instanceof BigDecimalValue.DecimalComparable) {
                return this.value.getDecimalValue().compareTo(((BigDecimalValue.DecimalComparable)o).asBigDecimal());
            }
            return Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return this.compareTo(o) == 0;
        }

        public int hashCode() {
            BigInteger big = this.value.asBigInteger();
            if (big.compareTo(MAX_LONG) < 0 && big.compareTo(MIN_LONG) > 0) {
                Int64Value iv = new Int64Value(big.longValue());
                return iv.hashCode();
            }
            return big.hashCode();
        }
    }
}


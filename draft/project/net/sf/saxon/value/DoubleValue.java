/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.AtomicSortComparer;
import net.sf.saxon.expr.sort.DoubleSortComparer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.FloatingPointConverter;
import net.sf.saxon.value.NumericValue;

public final class DoubleValue
extends NumericValue {
    public static final DoubleValue ZERO = new DoubleValue(0.0);
    public static final DoubleValue NEGATIVE_ZERO = new DoubleValue(-0.0);
    public static final DoubleValue ONE = new DoubleValue(1.0);
    public static final DoubleValue NaN = new DoubleValue(Double.NaN);
    private double value;

    public DoubleValue(double value) {
        this.value = value;
        this.typeLabel = BuiltInAtomicType.DOUBLE;
    }

    public DoubleValue(double value, AtomicType type) {
        this.value = value;
        this.typeLabel = type;
    }

    public static DoubleValue makeDoubleValue(double value) {
        return new DoubleValue(value);
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        DoubleValue v = new DoubleValue(this.value);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.DOUBLE;
    }

    @Override
    public double getDoubleValue() {
        return this.value;
    }

    @Override
    public float getFloatValue() {
        return (float)this.value;
    }

    @Override
    public BigDecimal getDecimalValue() throws ValidationException {
        try {
            return new BigDecimal(this.value);
        } catch (NumberFormatException e) {
            throw new ValidationException(e);
        }
    }

    @Override
    public long longValue() throws XPathException {
        return (long)this.value;
    }

    @Override
    public int hashCode() {
        if (this.value > -2.147483648E9 && this.value < 2.147483647E9) {
            return (int)this.value;
        }
        return Double.valueOf(this.value).hashCode();
    }

    @Override
    public boolean isNaN() {
        return Double.isNaN(this.value);
    }

    @Override
    public boolean effectiveBooleanValue() {
        return this.value != 0.0 && !Double.isNaN(this.value);
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        return DoubleValue.doubleToString(this.value);
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        FastStringBuffer fsb = new FastStringBuffer(16);
        return FloatingPointConverter.appendDouble(fsb, this.value, true);
    }

    public static CharSequence doubleToString(double value) {
        return FloatingPointConverter.appendDouble(new FastStringBuffer(16), value, false);
    }

    @Override
    public NumericValue negate() {
        return new DoubleValue(-this.value);
    }

    @Override
    public NumericValue floor() {
        return new DoubleValue(Math.floor(this.value));
    }

    @Override
    public NumericValue ceiling() {
        return new DoubleValue(Math.ceil(this.value));
    }

    @Override
    public NumericValue round(int scale) {
        if (Double.isNaN(this.value)) {
            return this;
        }
        if (Double.isInfinite(this.value)) {
            return this;
        }
        if (this.value == 0.0) {
            return this;
        }
        if (scale == 0 && this.value > -9.223372036854776E18 && this.value < 9.223372036854776E18) {
            if (this.value >= -0.5 && this.value < 0.0) {
                return new DoubleValue(-0.0);
            }
            return new DoubleValue(Math.round(this.value));
        }
        double factor = Math.pow(10.0, scale + 1);
        double d = Math.abs(this.value * factor);
        if (Double.isInfinite(d)) {
            BigDecimal dec = new BigDecimal(this.value);
            dec = dec.setScale(scale, RoundingMode.HALF_UP);
            return new DoubleValue(dec.doubleValue());
        }
        double rem = d % 10.0;
        if (rem >= 5.0) {
            d += 10.0 - rem;
        } else if (rem < 5.0) {
            d -= rem;
        }
        d /= factor;
        if (this.value < 0.0) {
            d = -d;
        }
        return new DoubleValue(d);
    }

    @Override
    public NumericValue roundHalfToEven(int scale) {
        if (Double.isNaN(this.value)) {
            return this;
        }
        if (Double.isInfinite(this.value)) {
            return this;
        }
        if (this.value == 0.0) {
            return this;
        }
        double factor = Math.pow(10.0, scale + 1);
        double d = Math.abs(this.value * factor);
        if (Double.isInfinite(d)) {
            BigDecimal dec = new BigDecimal(this.value);
            dec = dec.setScale(scale, RoundingMode.HALF_EVEN);
            return new DoubleValue(dec.doubleValue());
        }
        double rem = d % 10.0;
        d = rem > 5.0 ? (d += 10.0 - rem) : (rem < 5.0 ? (d -= rem) : (d % 20.0 == 15.0 ? (d += 5.0) : (d -= 5.0)));
        d /= factor;
        if (this.value < 0.0) {
            d = -d;
        }
        return new DoubleValue(d);
    }

    @Override
    public int signum() {
        if (Double.isNaN(this.value)) {
            return 0;
        }
        return this.value > 0.0 ? 1 : (this.value == 0.0 ? 0 : -1);
    }

    @Override
    public boolean isNegativeZero() {
        return this.value == 0.0 && (Double.doubleToLongBits(this.value) & Long.MIN_VALUE) != 0L;
    }

    @Override
    public boolean isWholeNumber() {
        return this.value == Math.floor(this.value) && !Double.isInfinite(this.value);
    }

    @Override
    public int asSubscript() {
        if (this.isWholeNumber() && this.value > 0.0 && this.value <= 2.147483647E9) {
            return (int)this.value;
        }
        return -1;
    }

    @Override
    public NumericValue abs() {
        if (this.value > 0.0) {
            return this;
        }
        return new DoubleValue(Math.abs(this.value));
    }

    @Override
    public int compareTo(long other) {
        double otherDouble = other;
        if (this.value == otherDouble) {
            return 0;
        }
        return this.value < otherDouble ? -1 : 1;
    }

    @Override
    public Comparable getSchemaComparable() {
        return Double.valueOf(this.value == 0.0 ? 0.0 : this.value);
    }

    @Override
    public AtomicMatchKey asMapKey() {
        if (this.isNaN()) {
            return AtomicSortComparer.COLLATION_KEY_NaN;
        }
        if (Double.isInfinite(this.value)) {
            return this;
        }
        try {
            return new BigDecimalValue(this.value);
        } catch (ValidationException e) {
            throw new AssertionError((Object)e);
        }
    }

    @Override
    public boolean isIdentical(AtomicValue v) {
        return v instanceof DoubleValue && DoubleSortComparer.getInstance().comparesEqual(this, v);
    }
}


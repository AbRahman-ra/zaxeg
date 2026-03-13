/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.AtomicSortComparer;
import net.sf.saxon.expr.sort.DoubleSortComparer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatingPointConverter;
import net.sf.saxon.value.NumericValue;

public final class FloatValue
extends NumericValue {
    public static final FloatValue ZERO = new FloatValue(0.0f);
    public static final FloatValue NEGATIVE_ZERO = new FloatValue(-0.0f);
    public static final FloatValue ONE = new FloatValue(1.0f);
    public static final FloatValue NaN = new FloatValue(Float.NaN);
    private float value;

    public FloatValue(float value) {
        this.value = value;
        this.typeLabel = BuiltInAtomicType.FLOAT;
    }

    public static FloatValue makeFloatValue(float value) {
        return new FloatValue(value);
    }

    public FloatValue(float value, AtomicType type) {
        this.value = value;
        this.typeLabel = type;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        FloatValue v = new FloatValue(this.value);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.FLOAT;
    }

    @Override
    public float getFloatValue() {
        return this.value;
    }

    @Override
    public double getDoubleValue() {
        return this.value;
    }

    @Override
    public BigDecimal getDecimalValue() throws ValidationException {
        return new BigDecimal(this.value);
    }

    @Override
    public long longValue() throws XPathException {
        return (long)this.value;
    }

    @Override
    public int hashCode() {
        if (this.value > -2.14748365E9f && this.value < 2.14748365E9f) {
            return (int)this.value;
        }
        return Double.valueOf(this.getDoubleValue()).hashCode();
    }

    @Override
    public boolean isNaN() {
        return Float.isNaN(this.value);
    }

    @Override
    public boolean effectiveBooleanValue() {
        return (double)this.value != 0.0 && !Float.isNaN(this.value);
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        return FloatValue.floatToString(this.value);
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        FastStringBuffer fsb = new FastStringBuffer(16);
        return FloatingPointConverter.appendFloat(fsb, this.value, true);
    }

    public static CharSequence floatToString(float value) {
        return FloatingPointConverter.appendFloat(new FastStringBuffer(16), value, false);
    }

    @Override
    public NumericValue negate() {
        return new FloatValue(-this.value);
    }

    @Override
    public NumericValue floor() {
        return new FloatValue((float)Math.floor(this.value));
    }

    @Override
    public NumericValue ceiling() {
        return new FloatValue((float)Math.ceil(this.value));
    }

    @Override
    public NumericValue round(int scale) {
        if (Float.isNaN(this.value)) {
            return this;
        }
        if (Float.isInfinite(this.value)) {
            return this;
        }
        if ((double)this.value == 0.0) {
            return this;
        }
        if (scale == 0 && this.value > -2.14748365E9f && this.value < 2.14748365E9f) {
            if ((double)this.value >= -0.5 && (double)this.value < 0.0) {
                return new FloatValue(-0.0f);
            }
            return new FloatValue(Math.round(this.value));
        }
        DoubleValue d = new DoubleValue(this.getDoubleValue());
        d = (DoubleValue)d.round(scale);
        return new FloatValue(d.getFloatValue());
    }

    @Override
    public NumericValue roundHalfToEven(int scale) {
        DoubleValue d = new DoubleValue(this.getDoubleValue());
        d = (DoubleValue)d.roundHalfToEven(scale);
        return new FloatValue(d.getFloatValue());
    }

    @Override
    public int signum() {
        if (Float.isNaN(this.value)) {
            return 0;
        }
        return this.compareTo(0L);
    }

    @Override
    public boolean isNegativeZero() {
        return (double)this.value == 0.0 && (Float.floatToIntBits(this.value) & Integer.MIN_VALUE) != 0;
    }

    @Override
    public boolean isWholeNumber() {
        return (double)this.value == Math.floor(this.value) && !Float.isInfinite(this.value);
    }

    @Override
    public int asSubscript() {
        if (this.isWholeNumber() && this.value > 0.0f && this.value <= 2.14748365E9f) {
            return (int)this.value;
        }
        return -1;
    }

    @Override
    public NumericValue abs() {
        if ((double)this.value > 0.0) {
            return this;
        }
        return new FloatValue(Math.abs(this.value));
    }

    @Override
    public int compareTo(NumericValue other) {
        if (other instanceof FloatValue) {
            float otherFloat = ((FloatValue)other).value;
            if (this.value == otherFloat) {
                return 0;
            }
            if (this.value < otherFloat) {
                return -1;
            }
            return 1;
        }
        if (other instanceof DoubleValue) {
            return super.compareTo(other);
        }
        return this.compareTo(Converter.NumericToFloat.INSTANCE.convert(other));
    }

    @Override
    public int compareTo(long other) {
        float otherFloat = other;
        if (this.value == otherFloat) {
            return 0;
        }
        return this.value < otherFloat ? -1 : 1;
    }

    @Override
    public Comparable getSchemaComparable() {
        return Float.valueOf(this.value == 0.0f ? 0.0f : this.value);
    }

    @Override
    public AtomicMatchKey asMapKey() {
        if (this.isNaN()) {
            return AtomicSortComparer.COLLATION_KEY_NaN;
        }
        if (Double.isInfinite(this.value)) {
            return new DoubleValue(this.value);
        }
        try {
            return new BigDecimalValue(this.value);
        } catch (ValidationException e) {
            throw new AssertionError((Object)e);
        }
    }

    @Override
    public boolean isIdentical(AtomicValue v) {
        return v instanceof FloatValue && DoubleSortComparer.getInstance().comparesEqual(this, (FloatValue)v);
    }

    @Override
    public FloatValue asAtomic() {
        return this;
    }
}


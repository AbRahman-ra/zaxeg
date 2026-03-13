/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;

public abstract class NumericValue
extends AtomicValue
implements Comparable<NumericValue>,
AtomicMatchKey {
    public static NumericValue parseNumber(String in) {
        if (in.indexOf(101) >= 0 || in.indexOf(69) >= 0) {
            try {
                return new DoubleValue(Double.parseDouble(in));
            } catch (NumberFormatException e) {
                return DoubleValue.NaN;
            }
        }
        if (in.indexOf(46) >= 0) {
            ConversionResult v = BigDecimalValue.makeDecimalValue(in, true);
            if (v instanceof ValidationFailure) {
                return DoubleValue.NaN;
            }
            return (NumericValue)v;
        }
        ConversionResult v = Int64Value.stringToInteger(in);
        if (v instanceof ValidationFailure) {
            return DoubleValue.NaN;
        }
        return (NumericValue)v;
    }

    public abstract double getDoubleValue();

    public abstract float getFloatValue();

    public abstract BigDecimal getDecimalValue() throws ValidationException;

    @Override
    public abstract boolean effectiveBooleanValue();

    public static boolean isInteger(AtomicValue value) {
        return value instanceof IntegerValue;
    }

    public abstract long longValue() throws XPathException;

    public abstract NumericValue negate();

    public abstract NumericValue floor();

    public abstract NumericValue ceiling();

    public abstract NumericValue round(int var1);

    public abstract NumericValue roundHalfToEven(int var1);

    public abstract int signum();

    public boolean isNegativeZero() {
        return false;
    }

    public abstract boolean isWholeNumber();

    public abstract int asSubscript();

    public abstract NumericValue abs();

    @Override
    public final AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return this;
    }

    @Override
    public int compareTo(NumericValue other) {
        double b;
        double a = this.getDoubleValue();
        if (a == (b = other.getDoubleValue())) {
            return 0;
        }
        if (a < b) {
            return -1;
        }
        return 1;
    }

    @Override
    public abstract int compareTo(long var1);

    @Override
    public final boolean equals(Object other) {
        return other instanceof NumericValue && this.compareTo((NumericValue)other) == 0;
    }

    public abstract int hashCode();

    @Override
    public String toString() {
        return this.getStringValue();
    }
}


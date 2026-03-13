/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.DurationValue;

public final class YearMonthDurationValue
extends DurationValue
implements Comparable<YearMonthDurationValue> {
    private YearMonthDurationValue() {
        this.typeLabel = BuiltInAtomicType.YEAR_MONTH_DURATION;
    }

    public static ConversionResult makeYearMonthDurationValue(CharSequence s) {
        ConversionResult d = DurationValue.makeDuration(s, true, false);
        if (d instanceof ValidationFailure) {
            return d;
        }
        DurationValue dv = (DurationValue)d;
        return YearMonthDurationValue.fromMonths((dv.getYears() * 12 + dv.getMonths()) * dv.signum());
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        YearMonthDurationValue v = YearMonthDurationValue.fromMonths(this.getLengthInMonths());
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.YEAR_MONTH_DURATION;
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        int y = this.getYears();
        int m = this.getMonths();
        FastStringBuffer sb = new FastStringBuffer(32);
        if (this.negative) {
            sb.cat('-');
        }
        sb.cat('P');
        if (y != 0) {
            sb.append(y + "Y");
        }
        if (m != 0 || y == 0) {
            sb.append(m + "M");
        }
        return sb;
    }

    public int getLengthInMonths() {
        return this.months * (this.negative ? -1 : 1);
    }

    public static YearMonthDurationValue fromMonths(int months) {
        YearMonthDurationValue mdv = new YearMonthDurationValue();
        mdv.negative = months < 0;
        mdv.months = months < 0 ? -months : months;
        mdv.seconds = 0L;
        mdv.nanoseconds = 0;
        return mdv;
    }

    @Override
    public YearMonthDurationValue multiply(long factor) throws XPathException {
        if (Math.abs(factor) < 30000L && Math.abs(this.months) < 30000) {
            return YearMonthDurationValue.fromMonths((int)factor * this.getLengthInMonths());
        }
        return this.multiply((double)factor);
    }

    @Override
    public YearMonthDurationValue multiply(double n) throws XPathException {
        if (Double.isNaN(n)) {
            XPathException err = new XPathException("Cannot multiply a duration by NaN");
            err.setErrorCode("FOCA0005");
            throw err;
        }
        double m = this.getLengthInMonths();
        double product = n * m;
        if (Double.isInfinite(product) || product > 2.147483647E9 || product < -2.147483648E9) {
            XPathException err = new XPathException("Overflow when multiplying a duration by a number");
            err.setErrorCode("FODT0002");
            throw err;
        }
        return YearMonthDurationValue.fromMonths((int)Math.round(product));
    }

    @Override
    public DurationValue divide(double n) throws XPathException {
        if (Double.isNaN(n)) {
            XPathException err = new XPathException("Cannot divide a duration by NaN");
            err.setErrorCode("FOCA0005");
            throw err;
        }
        double m = this.getLengthInMonths();
        double product = m / n;
        if (Double.isInfinite(product) || product > 2.147483647E9 || product < -2.147483648E9) {
            XPathException err = new XPathException("Overflow when dividing a duration by a number");
            err.setErrorCode("FODT0002");
            throw err;
        }
        return YearMonthDurationValue.fromMonths((int)Math.round(product));
    }

    @Override
    public BigDecimalValue divide(DurationValue other) throws XPathException {
        if (other instanceof YearMonthDurationValue) {
            BigDecimal v1 = BigDecimal.valueOf(this.getLengthInMonths());
            BigDecimal v2 = BigDecimal.valueOf(((YearMonthDurationValue)other).getLengthInMonths());
            if (v2.signum() == 0) {
                XPathException err = new XPathException("Divide by zero (durations)");
                err.setErrorCode("FOAR0001");
                throw err;
            }
            return new BigDecimalValue(v1.divide(v2, 20, RoundingMode.HALF_EVEN));
        }
        XPathException err = new XPathException("Cannot divide two durations of different type");
        err.setErrorCode("XPTY0004");
        throw err;
    }

    @Override
    public DurationValue add(DurationValue other) throws XPathException {
        if (other instanceof YearMonthDurationValue) {
            return YearMonthDurationValue.fromMonths(this.getLengthInMonths() + ((YearMonthDurationValue)other).getLengthInMonths());
        }
        XPathException err = new XPathException("Cannot add two durations of different type");
        err.setErrorCode("XPTY0004");
        throw err;
    }

    @Override
    public DurationValue subtract(DurationValue other) throws XPathException {
        if (other instanceof YearMonthDurationValue) {
            return YearMonthDurationValue.fromMonths(this.getLengthInMonths() - ((YearMonthDurationValue)other).getLengthInMonths());
        }
        XPathException err = new XPathException("Cannot subtract two durations of different type");
        err.setErrorCode("XPTY0004");
        throw err;
    }

    @Override
    public DurationValue negate() {
        return YearMonthDurationValue.fromMonths(-this.getLengthInMonths());
    }

    @Override
    public int compareTo(YearMonthDurationValue other) {
        return Integer.compare(this.getLengthInMonths(), other.getLengthInMonths());
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return this;
    }
}


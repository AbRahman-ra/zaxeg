/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Objects;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.DurationValue;

public final class DayTimeDurationValue
extends DurationValue
implements Comparable<DayTimeDurationValue> {
    private DayTimeDurationValue() {
        this.typeLabel = BuiltInAtomicType.DAY_TIME_DURATION;
    }

    public static ConversionResult makeDayTimeDurationValue(CharSequence s) {
        ConversionResult d = DurationValue.makeDuration(s, false, true);
        if (d instanceof ValidationFailure) {
            return d;
        }
        DurationValue dv = (DurationValue)d;
        return Converter.DurationToDayTimeDuration.INSTANCE.convert(dv);
    }

    public DayTimeDurationValue(int sign, int days, int hours, int minutes, long seconds, int microseconds) throws IllegalArgumentException {
        if (days < 0 || hours < 0 || minutes < 0 || seconds < 0L || microseconds < 0) {
            throw new IllegalArgumentException("Negative component value");
        }
        if ((double)days * 86400.0 + (double)hours * 3600.0 + (double)minutes * 60.0 + (double)seconds > 9.223372036854776E18) {
            throw new IllegalArgumentException("Duration seconds limit exceeded");
        }
        this.negative = sign < 0;
        this.months = 0;
        long h = (long)days * 24L + (long)hours;
        long m = h * 60L + (long)minutes;
        long s = m * 60L + seconds;
        if (microseconds > 1000000) {
            s += (long)(microseconds / 1000000);
            microseconds %= 1000000;
        }
        this.seconds = s;
        this.nanoseconds = microseconds * 1000;
        if (s == 0L && microseconds == 0) {
            this.negative = false;
        }
        this.typeLabel = BuiltInAtomicType.DAY_TIME_DURATION;
    }

    public DayTimeDurationValue(int days, int hours, int minutes, long seconds, int nanoseconds) throws IllegalArgumentException {
        boolean someNegative;
        boolean somePositive = days > 0 || hours > 0 || minutes > 0 || seconds > 0L || nanoseconds > 0;
        boolean bl = someNegative = days < 0 || hours < 0 || minutes < 0 || seconds < 0L || nanoseconds < 0;
        if (somePositive && someNegative) {
            throw new IllegalArgumentException("Some component values are positive and others are negative");
        }
        if (someNegative) {
            this.negative = true;
            days = -days;
            hours = -hours;
            minutes = -minutes;
            seconds = -seconds;
            nanoseconds = -nanoseconds;
        }
        if ((double)days * 86400.0 + (double)hours * 3600.0 + (double)minutes * 60.0 + (double)seconds > 9.223372036854776E18) {
            throw new IllegalArgumentException("Duration seconds limit exceeded");
        }
        this.months = 0;
        long h = (long)days * 24L + (long)hours;
        long m = h * 60L + (long)minutes;
        long s = m * 60L + seconds;
        if (nanoseconds > 1000000000) {
            s += (long)(nanoseconds / 1000000000);
            nanoseconds %= 1000000000;
        }
        this.seconds = s;
        this.nanoseconds = nanoseconds;
        this.typeLabel = BuiltInAtomicType.DAY_TIME_DURATION;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        DayTimeDurationValue v = DayTimeDurationValue.fromSeconds(this.getTotalSeconds());
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.DAY_TIME_DURATION;
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        FastStringBuffer sb = new FastStringBuffer(32);
        if (this.negative) {
            sb.cat('-');
        }
        int days = this.getDays();
        int hours = this.getHours();
        int minutes = this.getMinutes();
        int seconds = this.getSeconds();
        sb.cat('P');
        if (days != 0) {
            sb.append(days + "D");
        }
        if (days == 0 || hours != 0 || minutes != 0 || seconds != 0 || this.nanoseconds != 0) {
            sb.cat('T');
        }
        if (hours != 0) {
            sb.append(hours + "H");
        }
        if (minutes != 0) {
            sb.append(minutes + "M");
        }
        if (seconds != 0 || this.nanoseconds != 0 || days == 0 && minutes == 0 && hours == 0) {
            if (this.nanoseconds == 0) {
                sb.append(seconds + "S");
            } else {
                DayTimeDurationValue.formatFractionalSeconds(sb, seconds, (long)seconds * 1000000000L + (long)this.nanoseconds);
            }
        }
        return sb;
    }

    @Override
    public double getLengthInSeconds() {
        double a = (double)this.seconds + (double)this.nanoseconds / 1.0E9;
        return this.negative ? -a : a;
    }

    public long getLengthInMicroseconds() {
        if (this.seconds > 9223372036854L) {
            throw new ArithmeticException("Value is too large to be expressed in microseconds");
        }
        long a = this.seconds * 1000000L + (long)(this.nanoseconds / 1000);
        return this.negative ? -a : a;
    }

    public long getLengthInNanoseconds() {
        if (this.seconds > 9223372036L) {
            throw new ArithmeticException("Value is too large to be expressed in nanoseconds");
        }
        long a = this.seconds * 1000000000L + (long)this.nanoseconds;
        return this.negative ? -a : a;
    }

    public static DayTimeDurationValue fromSeconds(BigDecimal seconds) {
        DayTimeDurationValue sdv = new DayTimeDurationValue();
        boolean bl = sdv.negative = seconds.signum() < 0;
        if (sdv.negative) {
            seconds = seconds.negate();
        }
        BigInteger wholeSeconds = seconds.toBigInteger();
        sdv.seconds = wholeSeconds.longValueExact();
        BigDecimal fractionalPart = seconds.remainder(BigDecimal.ONE);
        BigDecimal nanoseconds = fractionalPart.multiply(BigDecimalValue.BIG_DECIMAL_ONE_BILLION);
        sdv.nanoseconds = nanoseconds.intValue();
        if (sdv.seconds == 0L && sdv.nanoseconds == 0) {
            sdv.negative = false;
        }
        return sdv;
    }

    public static DayTimeDurationValue fromMilliseconds(long milliseconds) throws ValidationException {
        int sign = Long.signum(milliseconds);
        if (sign < 0) {
            milliseconds = -milliseconds;
        }
        try {
            return new DayTimeDurationValue(sign, 0, 0, 0, milliseconds / 1000L, (int)(milliseconds % 1000L) * 1000);
        } catch (IllegalArgumentException err) {
            throw new ValidationFailure("Duration exceeds limits").makeException();
        }
    }

    public static DayTimeDurationValue fromMicroseconds(long microseconds) throws IllegalArgumentException {
        int sign = Long.signum(microseconds);
        if (sign < 0) {
            microseconds = -microseconds;
        }
        return new DayTimeDurationValue(sign, 0, 0, 0, microseconds / 1000000L, (int)(microseconds % 1000000L));
    }

    public static DayTimeDurationValue fromNanoseconds(long nanoseconds) throws IllegalArgumentException {
        return new DayTimeDurationValue(0, 0, 0, nanoseconds / 1000000000L, (int)(nanoseconds % 1000000000L));
    }

    public static DayTimeDurationValue fromJavaDuration(Duration duration) {
        long seconds = duration.getSeconds();
        int nanoseconds = duration.getNano();
        boolean negative = false;
        if (seconds < 0L) {
            return new DayTimeDurationValue(0, 0, 0, seconds, -1000000000 + nanoseconds);
        }
        return new DayTimeDurationValue(0, 0, 0, seconds, nanoseconds);
    }

    public Duration toJavaDuration() {
        if (this.negative) {
            return Duration.ofSeconds(-this.seconds, -this.nanoseconds);
        }
        return Duration.ofSeconds(this.seconds, this.nanoseconds);
    }

    @Override
    public DurationValue multiply(long factor) throws XPathException {
        if (Math.abs(factor) < Integer.MAX_VALUE && Math.abs(this.seconds) < Integer.MAX_VALUE) {
            return new DayTimeDurationValue(0, 0, 0, this.seconds * factor * (long)(this.negative ? -1 : 1), (int)((long)this.nanoseconds * factor * (long)(this.negative ? -1 : 1)));
        }
        return this.multiply(BigDecimal.valueOf(factor));
    }

    @Override
    public DayTimeDurationValue multiply(double n) throws XPathException {
        if (Double.isNaN(n)) {
            XPathException err = new XPathException("Cannot multiply a duration by NaN");
            err.setErrorCode("FOCA0005");
            throw err;
        }
        if (Double.isInfinite(n)) {
            XPathException err = new XPathException("Cannot multiply a duration by infinity");
            err.setErrorCode("FODT0002");
            throw err;
        }
        BigDecimal factor = BigDecimal.valueOf(n);
        return this.multiply(factor);
    }

    private DayTimeDurationValue multiply(BigDecimal factor) throws XPathException {
        BigDecimal secs = this.getTotalSeconds();
        BigDecimal product = secs.multiply(factor);
        try {
            return DayTimeDurationValue.fromSeconds(product);
        } catch (ArithmeticException | IllegalArgumentException err) {
            if (err.getCause() instanceof XPathException) {
                throw (XPathException)err.getCause();
            }
            XPathException err2 = new XPathException("Overflow when multiplying a duration by a number", err);
            err2.setErrorCode("FODT0002");
            throw err2;
        }
    }

    @Override
    public DurationValue divide(double n) throws XPathException {
        if (Double.isNaN(n)) {
            XPathException err = new XPathException("Cannot divide a duration by NaN");
            err.setErrorCode("FOCA0005");
            throw err;
        }
        if (n == 0.0) {
            XPathException err = new XPathException("Cannot divide a duration by zero");
            err.setErrorCode("FODT0002");
            throw err;
        }
        BigDecimal secs = this.getTotalSeconds();
        BigDecimal product = secs.divide(BigDecimal.valueOf(n));
        try {
            return DayTimeDurationValue.fromSeconds(product);
        } catch (ArithmeticException | IllegalArgumentException err) {
            if (err.getCause() instanceof XPathException) {
                throw (XPathException)err.getCause();
            }
            XPathException err2 = new XPathException("Overflow when dividing a duration by a number", err);
            err2.setErrorCode("FODT0002");
            throw err2;
        }
    }

    @Override
    public BigDecimalValue divide(DurationValue other) throws XPathException {
        if (other instanceof DayTimeDurationValue) {
            BigDecimal v1 = this.getTotalSeconds();
            BigDecimal v2 = other.getTotalSeconds();
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
        if (other instanceof DayTimeDurationValue) {
            DayTimeDurationValue d2 = (DayTimeDurationValue)other;
            if (((this.seconds | d2.seconds) & 0xFFFFFFFF00000000L) != 0L) {
                try {
                    BigDecimal v1 = this.getTotalSeconds();
                    BigDecimal v2 = other.getTotalSeconds();
                    return DayTimeDurationValue.fromSeconds(v1.add(v2));
                } catch (IllegalArgumentException e) {
                    XPathException err = new XPathException("Overflow when adding two durations");
                    err.setErrorCode("FODT0002");
                    throw err;
                }
            }
            return DayTimeDurationValue.fromNanoseconds(this.getLengthInNanoseconds() + d2.getLengthInNanoseconds());
        }
        XPathException err = new XPathException("Cannot add two durations of different type");
        err.setErrorCode("XPTY0004");
        throw err;
    }

    @Override
    public DurationValue subtract(DurationValue other) throws XPathException {
        if (other instanceof DayTimeDurationValue) {
            DayTimeDurationValue d2 = (DayTimeDurationValue)other;
            if (((this.seconds | d2.seconds) & 0xFFFFFFFF00000000L) != 0L) {
                try {
                    BigDecimal v1 = this.getTotalSeconds();
                    BigDecimal v2 = other.getTotalSeconds();
                    return DayTimeDurationValue.fromSeconds(v1.subtract(v2));
                } catch (IllegalArgumentException e) {
                    XPathException err = new XPathException("Overflow when subtracting two durations");
                    err.setErrorCode("FODT0002");
                    throw err;
                }
            }
            return DayTimeDurationValue.fromNanoseconds(this.getLengthInNanoseconds() - d2.getLengthInNanoseconds());
        }
        XPathException err = new XPathException("Cannot subtract two durations of different type");
        err.setErrorCode("XPTY0004");
        throw err;
    }

    @Override
    public DurationValue negate() throws IllegalArgumentException {
        DayTimeDurationValue d2 = new DayTimeDurationValue();
        d2.setTypeLabel(this.typeLabel);
        d2.seconds = this.seconds;
        d2.nanoseconds = this.nanoseconds;
        d2.negative = !this.negative;
        return d2;
    }

    @Override
    public int compareTo(DayTimeDurationValue other) {
        Objects.requireNonNull(other);
        if (this.negative != other.negative) {
            return this.negative ? -1 : 1;
        }
        if (this.seconds != other.seconds) {
            return Long.compare(this.seconds, other.seconds) * (this.negative ? -1 : 1);
        }
        return Integer.compare(this.nanoseconds, other.nanoseconds) * (this.negative ? -1 : 1);
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return this;
    }
}


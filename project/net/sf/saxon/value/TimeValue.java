/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.Whitespace;

public final class TimeValue
extends CalendarValue
implements Comparable {
    private byte hour;
    private byte minute;
    private byte second;
    private int nanosecond;

    private TimeValue() {
    }

    public TimeValue(byte hour, byte minute, byte second, int microsecond, int tz) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.nanosecond = microsecond * 1000;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = BuiltInAtomicType.TIME;
    }

    public TimeValue(byte hour, byte minute, byte second, int nanosecond, int tz, String flag) {
        if (!flag.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.nanosecond = nanosecond;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = BuiltInAtomicType.TIME;
    }

    public TimeValue makeTimeValue(byte hour, byte minute, byte second, int nanosecond, int tz) {
        return new TimeValue(hour, minute, second, nanosecond, tz, "");
    }

    public TimeValue(GregorianCalendar calendar, int tz) {
        this.hour = (byte)calendar.get(11);
        this.minute = (byte)calendar.get(12);
        this.second = (byte)calendar.get(13);
        this.nanosecond = calendar.get(14) * 1000000;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = BuiltInAtomicType.TIME;
    }

    public static ConversionResult makeTimeValue(CharSequence s) {
        TimeValue tv = new TimeValue();
        StringTokenizer tok = new StringTokenizer(Whitespace.trimWhitespace(s).toString(), "-:.+Z", true);
        if (!tok.hasMoreElements()) {
            return TimeValue.badTime("too short", s);
        }
        String part = (String)tok.nextElement();
        if (part.length() != 2) {
            return TimeValue.badTime("hour must be two digits", s);
        }
        int value = DurationValue.simpleInteger(part);
        if (value < 0) {
            return TimeValue.badTime("Non-numeric hour component", s);
        }
        tv.hour = (byte)value;
        if (tv.hour > 24) {
            return TimeValue.badTime("hour is out of range", s);
        }
        if (!tok.hasMoreElements()) {
            return TimeValue.badTime("too short", s);
        }
        if (!":".equals(tok.nextElement())) {
            return TimeValue.badTime("wrong delimiter after hour", s);
        }
        if (!tok.hasMoreElements()) {
            return TimeValue.badTime("too short", s);
        }
        part = (String)tok.nextElement();
        if (part.length() != 2) {
            return TimeValue.badTime("minute must be two digits", s);
        }
        value = DurationValue.simpleInteger(part);
        if (value < 0) {
            return TimeValue.badTime("Non-numeric minute component", s);
        }
        tv.minute = (byte)value;
        if (tv.minute > 59) {
            return TimeValue.badTime("minute is out of range", s);
        }
        if (tv.hour == 24 && tv.minute != 0) {
            return TimeValue.badTime("If hour is 24, minute must be 00", s);
        }
        if (!tok.hasMoreElements()) {
            return TimeValue.badTime("too short", s);
        }
        if (!":".equals(tok.nextElement())) {
            return TimeValue.badTime("wrong delimiter after minute", s);
        }
        if (!tok.hasMoreElements()) {
            return TimeValue.badTime("too short", s);
        }
        part = (String)tok.nextElement();
        if (part.length() != 2) {
            return TimeValue.badTime("second must be two digits", s);
        }
        value = DurationValue.simpleInteger(part);
        if (value < 0) {
            return TimeValue.badTime("Non-numeric second component", s);
        }
        tv.second = (byte)value;
        if (tv.second > 59) {
            return TimeValue.badTime("second is out of range", s);
        }
        if (tv.hour == 24 && tv.second != 0) {
            return TimeValue.badTime("If hour is 24, second must be 00", s);
        }
        int tz = 0;
        boolean negativeTz = false;
        int state = 0;
        while (tok.hasMoreElements()) {
            if (state == 9) {
                return TimeValue.badTime("characters after the end", s);
            }
            String delim = (String)tok.nextElement();
            if (".".equals(delim)) {
                if (state != 0) {
                    return TimeValue.badTime("decimal separator occurs twice", s);
                }
                if (!tok.hasMoreElements()) {
                    return TimeValue.badTime("decimal point must be followed by digits", s);
                }
                part = (String)tok.nextElement();
                if (part.length() > 9 && part.matches("^[0-9]+$")) {
                    part = part.substring(0, 9);
                }
                if ((value = DurationValue.simpleInteger(part)) < 0) {
                    return TimeValue.badTime("Non-numeric fractional seconds component", s);
                }
                double fractionalSeconds = Double.parseDouble('.' + part);
                tv.nanosecond = (int)Math.round(fractionalSeconds * 1.0E9);
                if (tv.hour == 24 && tv.nanosecond != 0) {
                    return TimeValue.badTime("If hour is 24, fractional seconds must be 0", s);
                }
                state = 1;
                continue;
            }
            if ("Z".equals(delim)) {
                if (state > 1) {
                    return TimeValue.badTime("Z cannot occur here", s);
                }
                tz = 0;
                state = 9;
                tv.setTimezoneInMinutes(0);
                continue;
            }
            if ("+".equals(delim) || "-".equals(delim)) {
                if (state > 1) {
                    return TimeValue.badTime(delim + " cannot occur here", s);
                }
                state = 2;
                if (!tok.hasMoreElements()) {
                    return TimeValue.badTime("missing timezone", s);
                }
                part = (String)tok.nextElement();
                if (part.length() != 2) {
                    return TimeValue.badTime("timezone hour must be two digits", s);
                }
                value = DurationValue.simpleInteger(part);
                if (value < 0) {
                    return TimeValue.badTime("Non-numeric timezone hour component", s);
                }
                tz = value * 60;
                if (tz > 840) {
                    return TimeValue.badTime("timezone hour is out of range", s);
                }
                if (!"-".equals(delim)) continue;
                negativeTz = true;
                continue;
            }
            if (":".equals(delim)) {
                if (state != 2) {
                    return TimeValue.badTime("colon cannot occur here", s);
                }
                state = 9;
                part = (String)tok.nextElement();
                value = DurationValue.simpleInteger(part);
                if (value < 0) {
                    return TimeValue.badTime("Non-numeric timezone minute component", s);
                }
                int tzminute = value;
                if (part.length() != 2) {
                    return TimeValue.badTime("timezone minute must be two digits", s);
                }
                if (tzminute > 59) {
                    return TimeValue.badTime("timezone minute is out of range", s);
                }
                tz += tzminute;
                if (negativeTz) {
                    tz = -tz;
                }
                tv.setTimezoneInMinutes(tz);
                continue;
            }
            return TimeValue.badTime("timezone format is incorrect", s);
        }
        if (state == 2 || state == 3) {
            return TimeValue.badTime("timezone incomplete", s);
        }
        if (tv.hour == 24) {
            tv.hour = 0;
        }
        tv.typeLabel = BuiltInAtomicType.TIME;
        return tv;
    }

    private static ValidationFailure badTime(String msg, CharSequence value) {
        ValidationFailure err = new ValidationFailure("Invalid time " + Err.wrap(value, 4) + " (" + msg + ")");
        err.setErrorCode("FORG0001");
        return err;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.TIME;
    }

    public byte getHour() {
        return this.hour;
    }

    public byte getMinute() {
        return this.minute;
    }

    public byte getSecond() {
        return this.second;
    }

    public int getMicrosecond() {
        return this.nanosecond / 1000;
    }

    public int getNanosecond() {
        return this.nanosecond;
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        FastStringBuffer sb = new FastStringBuffer(16);
        TimeValue.appendTwoDigits(sb, this.hour);
        sb.cat(':');
        TimeValue.appendTwoDigits(sb, this.minute);
        sb.cat(':');
        TimeValue.appendTwoDigits(sb, this.second);
        if (this.nanosecond != 0) {
            sb.cat('.');
            int ms = this.nanosecond;
            int div = 100000000;
            while (ms > 0) {
                int d = ms / div;
                sb.cat((char)(d + 48));
                ms %= div;
                div /= 10;
            }
        }
        if (this.hasTimezone()) {
            this.appendTimezone(sb);
        }
        return sb;
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        if (this.hasTimezone() && this.getTimezoneInMinutes() != 0) {
            return this.adjustTimezone(0).getStringValueCS();
        }
        return this.getStringValueCS();
    }

    @Override
    public DateTimeValue toDateTime() {
        return new DateTimeValue(1972, 12, 31, this.hour, this.minute, this.second, this.nanosecond, this.getTimezoneInMinutes());
    }

    @Override
    public GregorianCalendar getCalendar() {
        int tz = this.hasTimezone() ? this.getTimezoneInMinutes() * 60000 : 0;
        SimpleTimeZone zone = new SimpleTimeZone(tz, "LLL");
        GregorianCalendar calendar = new GregorianCalendar(zone);
        calendar.setLenient(false);
        if (tz < calendar.getMinimum(15) || tz > calendar.getMaximum(15)) {
            return this.adjustTimezone(0).getCalendar();
        }
        calendar.set(1972, 11, 31, this.hour, this.minute, this.second);
        calendar.set(14, this.nanosecond / 1000000);
        calendar.set(15, tz);
        calendar.set(16, 0);
        calendar.getTime();
        return calendar;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        TimeValue v = new TimeValue(this.hour, this.minute, this.second, this.nanosecond, this.getTimezoneInMinutes(), "");
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public TimeValue adjustTimezone(int timezone) {
        DateTimeValue dt = this.toDateTime().adjustTimezone(timezone);
        return new TimeValue(dt.getHour(), dt.getMinute(), dt.getSecond(), dt.getNanosecond(), dt.getTimezoneInMinutes(), "");
    }

    @Override
    public AtomicValue getComponent(AccessorFn.Component component) throws XPathException {
        switch (component) {
            case HOURS: {
                return Int64Value.makeIntegerValue(this.hour);
            }
            case MINUTES: {
                return Int64Value.makeIntegerValue(this.minute);
            }
            case SECONDS: {
                BigDecimal d = BigDecimal.valueOf(this.nanosecond);
                d = d.divide(BigDecimalValue.BIG_DECIMAL_ONE_BILLION, 6, RoundingMode.HALF_UP);
                d = d.add(BigDecimal.valueOf(this.second));
                return new BigDecimalValue(d);
            }
            case WHOLE_SECONDS: {
                return Int64Value.makeIntegerValue(this.second);
            }
            case MICROSECONDS: {
                return new Int64Value(this.nanosecond / 1000);
            }
            case NANOSECONDS: {
                return new Int64Value(this.nanosecond);
            }
            case TIMEZONE: {
                if (this.hasTimezone()) {
                    return DayTimeDurationValue.fromMilliseconds(60000L * (long)this.getTimezoneInMinutes());
                }
                return null;
            }
        }
        throw new IllegalArgumentException("Unknown component for time: " + (Object)((Object)component));
    }

    public int compareTo(Object other) {
        TimeValue otherTime = (TimeValue)other;
        if (this.getTimezoneInMinutes() == otherTime.getTimezoneInMinutes()) {
            if (this.hour != otherTime.hour) {
                return IntegerValue.signum(this.hour - otherTime.hour);
            }
            if (this.minute != otherTime.minute) {
                return IntegerValue.signum(this.minute - otherTime.minute);
            }
            if (this.second != otherTime.second) {
                return IntegerValue.signum(this.second - otherTime.second);
            }
            if (this.nanosecond != otherTime.nanosecond) {
                return IntegerValue.signum(this.nanosecond - otherTime.nanosecond);
            }
            return 0;
        }
        return this.toDateTime().compareTo(otherTime.toDateTime());
    }

    @Override
    public int compareTo(CalendarValue other, int implicitTimezone) throws NoDynamicContextException {
        if (!(other instanceof TimeValue)) {
            throw new ClassCastException("Time values are not comparable to " + other.getClass());
        }
        TimeValue otherTime = (TimeValue)other;
        if (this.getTimezoneInMinutes() == otherTime.getTimezoneInMinutes()) {
            return this.compareTo(other);
        }
        return this.toDateTime().compareTo(otherTime.toDateTime(), implicitTimezone);
    }

    @Override
    public Comparable getSchemaComparable() {
        return new TimeComparable();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TimeValue && this.compareTo(other) == 0;
    }

    public int hashCode() {
        return DateTimeValue.hashCode(1951, (byte)10, (byte)11, this.hour, this.minute, this.second, this.nanosecond, this.getTimezoneInMinutes());
    }

    @Override
    public TimeValue add(DurationValue duration) throws XPathException {
        if (duration instanceof DayTimeDurationValue) {
            DateTimeValue dt = this.toDateTime().add(duration);
            return new TimeValue(dt.getHour(), dt.getMinute(), dt.getSecond(), dt.getNanosecond(), this.getTimezoneInMinutes(), "");
        }
        XPathException err = new XPathException("Time+Duration arithmetic is supported only for xs:dayTimeDuration");
        err.setErrorCode("XPTY0004");
        err.setIsTypeError(true);
        throw err;
    }

    @Override
    public DayTimeDurationValue subtract(CalendarValue other, XPathContext context) throws XPathException {
        if (!(other instanceof TimeValue)) {
            XPathException err = new XPathException("First operand of '-' is a time, but the second is not");
            err.setIsTypeError(true);
            throw err;
        }
        return super.subtract(other, context);
    }

    private class TimeComparable
    implements Comparable {
        private TimeComparable() {
        }

        public TimeValue asTimeValue() {
            return TimeValue.this;
        }

        public int compareTo(Object o) {
            if (o instanceof TimeComparable) {
                DateTimeValue dt0 = this.asTimeValue().toDateTime();
                DateTimeValue dt1 = ((TimeComparable)o).asTimeValue().toDateTime();
                return dt0.getSchemaComparable().compareTo(dt1.getSchemaComparable());
            }
            return Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return this.compareTo(o) == 0;
        }

        public int hashCode() {
            return TimeValue.this.toDateTime().getSchemaComparable().hashCode();
        }
    }
}


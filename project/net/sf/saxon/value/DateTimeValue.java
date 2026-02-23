/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.lib.ConversionRules;
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
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.TimeValue;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.value.YearMonthDurationValue;

public final class DateTimeValue
extends CalendarValue
implements Comparable,
TemporalAccessor {
    private int year;
    private byte month;
    private byte day;
    private byte hour;
    private byte minute;
    private byte second;
    private int nanosecond;
    private boolean hasNoYearZero;
    public static final DateTimeValue EPOCH = new DateTimeValue(1970, 1, 1, 0, 0, 0, 0, 0, true);

    private DateTimeValue() {
    }

    public static DateTimeValue getCurrentDateTime(XPathContext context) {
        Controller c;
        if (context == null || (c = context.getController()) == null) {
            return DateTimeValue.now();
        }
        return c.getCurrentDateTime();
    }

    public static DateTimeValue now() {
        return DateTimeValue.fromZonedDateTime(ZonedDateTime.now());
    }

    public DateTimeValue(Calendar calendar, boolean tzSpecified) {
        int era = calendar.get(0);
        this.year = calendar.get(1);
        if (era == 0) {
            this.year = 1 - this.year;
        }
        this.month = (byte)(calendar.get(2) + 1);
        this.day = (byte)calendar.get(5);
        this.hour = (byte)calendar.get(11);
        this.minute = (byte)calendar.get(12);
        this.second = (byte)calendar.get(13);
        this.nanosecond = calendar.get(14) * 1000000;
        if (tzSpecified) {
            int tz = (calendar.get(15) + calendar.get(16)) / 60000;
            this.setTimezoneInMinutes(tz);
        }
        this.typeLabel = BuiltInAtomicType.DATE_TIME;
        this.hasNoYearZero = true;
    }

    public static DateTimeValue fromJavaDate(Date suppliedDate) throws XPathException {
        long millis = suppliedDate.getTime();
        return EPOCH.add(DayTimeDurationValue.fromMilliseconds(millis));
    }

    public static DateTimeValue fromJavaTime(long time) throws XPathException {
        return EPOCH.add(DayTimeDurationValue.fromMilliseconds(time));
    }

    public static DateTimeValue fromJavaInstant(long seconds, int nano) throws XPathException {
        return EPOCH.add(DayTimeDurationValue.fromSeconds(new BigDecimal(seconds)).add(DayTimeDurationValue.fromNanoseconds(nano)));
    }

    public static DateTimeValue fromJavaInstant(Instant instant) {
        try {
            return DateTimeValue.fromJavaInstant(instant.getEpochSecond(), instant.getNano());
        } catch (XPathException e) {
            throw new AssertionError();
        }
    }

    public static DateTimeValue fromZonedDateTime(ZonedDateTime zonedDateTime) {
        return DateTimeValue.fromOffsetDateTime(zonedDateTime.toOffsetDateTime());
    }

    public static DateTimeValue fromOffsetDateTime(OffsetDateTime offsetDateTime) {
        LocalDateTime ldt = offsetDateTime.toLocalDateTime();
        ZoneOffset zo = offsetDateTime.getOffset();
        int tz = zo.getTotalSeconds() / 60;
        DateTimeValue dtv = new DateTimeValue(ldt.getYear(), (byte)ldt.getMonthValue(), (byte)ldt.getDayOfMonth(), (byte)ldt.getHour(), (byte)ldt.getMinute(), (byte)ldt.getSecond(), ldt.getNano(), tz);
        dtv.typeLabel = BuiltInAtomicType.DATE_TIME_STAMP;
        dtv.hasNoYearZero = false;
        return dtv;
    }

    public static DateTimeValue fromLocalDateTime(LocalDateTime localDateTime) {
        DateTimeValue dtv = new DateTimeValue(localDateTime.getYear(), (byte)localDateTime.getMonthValue(), (byte)localDateTime.getDayOfMonth(), (byte)localDateTime.getHour(), (byte)localDateTime.getMinute(), (byte)localDateTime.getSecond(), localDateTime.getNano(), Integer.MIN_VALUE);
        dtv.hasNoYearZero = false;
        return dtv;
    }

    public static DateTimeValue makeDateTimeValue(DateValue date, TimeValue time) throws XPathException {
        if (date == null || time == null) {
            return null;
        }
        int tz1 = date.getTimezoneInMinutes();
        int tz2 = time.getTimezoneInMinutes();
        if (tz1 != Integer.MIN_VALUE && tz2 != Integer.MIN_VALUE && tz1 != tz2) {
            XPathException err = new XPathException("Supplied date and time are in different timezones");
            err.setErrorCode("FORG0008");
            throw err;
        }
        DateTimeValue v = date.toDateTime();
        v.hour = time.getHour();
        v.minute = time.getMinute();
        v.second = time.getSecond();
        v.nanosecond = time.getNanosecond();
        v.setTimezoneInMinutes(Math.max(tz1, tz2));
        v.typeLabel = BuiltInAtomicType.DATE_TIME;
        v.hasNoYearZero = date.hasNoYearZero;
        return v;
    }

    public static ConversionResult makeDateTimeValue(CharSequence s, ConversionRules rules) {
        int value;
        DateTimeValue dt = new DateTimeValue();
        dt.hasNoYearZero = !rules.isAllowYearZero();
        StringTokenizer tok = new StringTokenizer(Whitespace.trimWhitespace(s).toString(), "-:.+TZ", true);
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("too short", s);
        }
        String part = (String)tok.nextElement();
        int era = 1;
        if ("+".equals(part)) {
            return DateTimeValue.badDate("Date must not start with '+' sign", s);
        }
        if ("-".equals(part)) {
            era = -1;
            if (!tok.hasMoreElements()) {
                return DateTimeValue.badDate("No year after '-'", s);
            }
            part = (String)tok.nextElement();
        }
        if ((value = DurationValue.simpleInteger(part)) < 0) {
            if (value == -1) {
                return DateTimeValue.badDate("Non-numeric year component", s);
            }
            return DateTimeValue.badDate("Year is outside the range that Saxon can handle", s, "FODT0001");
        }
        dt.year = value * era;
        if (part.length() < 4) {
            return DateTimeValue.badDate("Year is less than four digits", s);
        }
        if (part.length() > 4 && part.charAt(0) == '0') {
            return DateTimeValue.badDate("When year exceeds 4 digits, leading zeroes are not allowed", s);
        }
        if (dt.year == 0 && !rules.isAllowYearZero()) {
            return DateTimeValue.badDate("Year zero is not allowed", s);
        }
        if (era < 0 && !rules.isAllowYearZero()) {
            ++dt.year;
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        if (!"-".equals(tok.nextElement())) {
            return DateTimeValue.badDate("Wrong delimiter after year", s);
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        part = (String)tok.nextElement();
        if (part.length() != 2) {
            return DateTimeValue.badDate("Month must be two digits", s);
        }
        value = DurationValue.simpleInteger(part);
        if (value < 0) {
            return DateTimeValue.badDate("Non-numeric month component", s);
        }
        dt.month = (byte)value;
        if (dt.month < 1 || dt.month > 12) {
            return DateTimeValue.badDate("Month is out of range", s);
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        if (!"-".equals(tok.nextElement())) {
            return DateTimeValue.badDate("Wrong delimiter after month", s);
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        part = (String)tok.nextElement();
        if (part.length() != 2) {
            return DateTimeValue.badDate("Day must be two digits", s);
        }
        value = DurationValue.simpleInteger(part);
        if (value < 0) {
            return DateTimeValue.badDate("Non-numeric day component", s);
        }
        dt.day = (byte)value;
        if (dt.day < 1 || dt.day > 31) {
            return DateTimeValue.badDate("Day is out of range", s);
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        if (!"T".equals(tok.nextElement())) {
            return DateTimeValue.badDate("Wrong delimiter after day", s);
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        part = (String)tok.nextElement();
        if (part.length() != 2) {
            return DateTimeValue.badDate("Hour must be two digits", s);
        }
        value = DurationValue.simpleInteger(part);
        if (value < 0) {
            return DateTimeValue.badDate("Non-numeric hour component", s);
        }
        dt.hour = (byte)value;
        if (dt.hour > 24) {
            return DateTimeValue.badDate("Hour is out of range", s);
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        if (!":".equals(tok.nextElement())) {
            return DateTimeValue.badDate("Wrong delimiter after hour", s);
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        part = (String)tok.nextElement();
        if (part.length() != 2) {
            return DateTimeValue.badDate("Minute must be two digits", s);
        }
        value = DurationValue.simpleInteger(part);
        if (value < 0) {
            return DateTimeValue.badDate("Non-numeric minute component", s);
        }
        dt.minute = (byte)value;
        if (dt.minute > 59) {
            return DateTimeValue.badDate("Minute is out of range", s);
        }
        if (dt.hour == 24 && dt.minute != 0) {
            return DateTimeValue.badDate("If hour is 24, minute must be 00", s);
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        if (!":".equals(tok.nextElement())) {
            return DateTimeValue.badDate("Wrong delimiter after minute", s);
        }
        if (!tok.hasMoreElements()) {
            return DateTimeValue.badDate("Too short", s);
        }
        part = (String)tok.nextElement();
        if (part.length() != 2) {
            return DateTimeValue.badDate("Second must be two digits", s);
        }
        value = DurationValue.simpleInteger(part);
        if (value < 0) {
            return DateTimeValue.badDate("Non-numeric second component", s);
        }
        dt.second = (byte)value;
        if (dt.second > 59) {
            return DateTimeValue.badDate("Second is out of range", s);
        }
        if (dt.hour == 24 && dt.second != 0) {
            return DateTimeValue.badDate("If hour is 24, second must be 00", s);
        }
        int tz = 0;
        boolean negativeTz = false;
        int state = 0;
        while (tok.hasMoreElements()) {
            if (state == 9) {
                return DateTimeValue.badDate("Characters after the end", s);
            }
            String delim = (String)tok.nextElement();
            if (".".equals(delim)) {
                if (state != 0) {
                    return DateTimeValue.badDate("Decimal separator occurs twice", s);
                }
                if (!tok.hasMoreElements()) {
                    return DateTimeValue.badDate("Decimal point must be followed by digits", s);
                }
                part = (String)tok.nextElement();
                if (part.length() > 9 && part.matches("^[0-9]+$")) {
                    part = part.substring(0, 9);
                }
                if ((value = DurationValue.simpleInteger(part)) < 0) {
                    return DateTimeValue.badDate("Non-numeric fractional seconds component", s);
                }
                double fractionalSeconds = Double.parseDouble('.' + part);
                int nanoSeconds = (int)Math.round(fractionalSeconds * 1.0E9);
                if (nanoSeconds == 1000000000) {
                    // empty if block
                }
                dt.nanosecond = --nanoSeconds;
                if (dt.hour == 24 && dt.nanosecond != 0) {
                    return DateTimeValue.badDate("If hour is 24, fractional seconds must be 0", s);
                }
                state = 1;
                continue;
            }
            if ("Z".equals(delim)) {
                if (state > 1) {
                    return DateTimeValue.badDate("Z cannot occur here", s);
                }
                tz = 0;
                state = 9;
                dt.setTimezoneInMinutes(0);
                continue;
            }
            if ("+".equals(delim) || "-".equals(delim)) {
                if (state > 1) {
                    return DateTimeValue.badDate(delim + " cannot occur here", s);
                }
                state = 2;
                if (!tok.hasMoreElements()) {
                    return DateTimeValue.badDate("Missing timezone", s);
                }
                part = (String)tok.nextElement();
                if (part.length() != 2) {
                    return DateTimeValue.badDate("Timezone hour must be two digits", s);
                }
                value = DurationValue.simpleInteger(part);
                if (value < 0) {
                    return DateTimeValue.badDate("Non-numeric timezone hour component", s);
                }
                tz = value;
                if (tz > 14) {
                    return DateTimeValue.badDate("Timezone is out of range (-14:00 to +14:00)", s);
                }
                tz *= 60;
                if (!"-".equals(delim)) continue;
                negativeTz = true;
                continue;
            }
            if (":".equals(delim)) {
                if (state != 2) {
                    return DateTimeValue.badDate("Misplaced ':'", s);
                }
                state = 9;
                part = (String)tok.nextElement();
                value = DurationValue.simpleInteger(part);
                if (value < 0) {
                    return DateTimeValue.badDate("Non-numeric timezone minute component", s);
                }
                int tzminute = value;
                if (part.length() != 2) {
                    return DateTimeValue.badDate("Timezone minute must be two digits", s);
                }
                if (tzminute > 59) {
                    return DateTimeValue.badDate("Timezone minute is out of range", s);
                }
                if (Math.abs(tz) == 840 && tzminute != 0) {
                    return DateTimeValue.badDate("Timezone is out of range (-14:00 to +14:00)", s);
                }
                tz += tzminute;
                if (negativeTz) {
                    tz = -tz;
                }
                dt.setTimezoneInMinutes(tz);
                continue;
            }
            return DateTimeValue.badDate("Timezone format is incorrect", s);
        }
        if (state == 2 || state == 3) {
            return DateTimeValue.badDate("Timezone incomplete", s);
        }
        boolean midnight = false;
        if (dt.hour == 24) {
            dt.hour = 0;
            midnight = true;
        }
        if (!DateValue.isValidDate(dt.year, dt.month, dt.day)) {
            return DateTimeValue.badDate("Non-existent date", s);
        }
        if (midnight) {
            DateValue t = DateValue.tomorrow(dt.year, dt.month, dt.day);
            dt.year = t.getYear();
            dt.month = t.getMonth();
            dt.day = t.getDay();
        }
        dt.typeLabel = BuiltInAtomicType.DATE_TIME;
        return dt;
    }

    public static DateTimeValue parse(CharSequence s) throws DateTimeParseException {
        ConversionResult result = DateTimeValue.makeDateTimeValue(s, ConversionRules.DEFAULT);
        if (result instanceof ValidationFailure) {
            throw new DateTimeParseException(((ValidationFailure)result).getMessage(), s, 0);
        }
        return (DateTimeValue)result;
    }

    private static ValidationFailure badDate(String msg, CharSequence value) {
        ValidationFailure err = new ValidationFailure("Invalid dateTime value " + Err.wrap(value, 4) + " (" + msg + ")");
        err.setErrorCode("FORG0001");
        return err;
    }

    private static ValidationFailure badDate(String msg, CharSequence value, String errorCode) {
        ValidationFailure err = new ValidationFailure("Invalid dateTime value " + Err.wrap(value, 4) + " (" + msg + ")");
        err.setErrorCode(errorCode);
        return err;
    }

    public DateTimeValue(int year, byte month, byte day, byte hour, byte minute, byte second, int nanosecond, int tz) {
        this.hasNoYearZero = false;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.nanosecond = nanosecond;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = BuiltInAtomicType.DATE_TIME;
    }

    public DateTimeValue(int year, byte month, byte day, byte hour, byte minute, byte second, int microsecond, int tz, boolean hasNoYearZero) {
        this.hasNoYearZero = hasNoYearZero;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.nanosecond = microsecond * 1000;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = BuiltInAtomicType.DATE_TIME;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.DATE_TIME;
    }

    public int getYear() {
        return this.year;
    }

    public byte getMonth() {
        return this.month;
    }

    public byte getDay() {
        return this.day;
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
    public DateTimeValue toDateTime() {
        return this;
    }

    public boolean isXsd10Rules() {
        return this.hasNoYearZero;
    }

    @Override
    public void checkValidInJavascript() throws XPathException {
        if (this.year <= 0 || this.year > 9999) {
            throw new XPathException("Year out of range for Saxon-JS", "FODT0001");
        }
    }

    public DateTimeValue adjustToUTC(int implicitTimezone) throws NoDynamicContextException {
        if (this.hasTimezone()) {
            return this.adjustTimezone(0);
        }
        if (implicitTimezone == Integer.MAX_VALUE || implicitTimezone == Integer.MIN_VALUE) {
            throw new NoDynamicContextException("DateTime operation needs access to implicit timezone");
        }
        DateTimeValue dt = this.copyAsSubType(null);
        dt.setTimezoneInMinutes(implicitTimezone);
        return dt.adjustTimezone(0);
    }

    public BigDecimal toJulianInstant() {
        int julianDay = DateValue.getJulianDayNumber(this.year, this.month, this.day);
        long julianSecond = (long)julianDay * 24L * 60L * 60L;
        BigDecimal j = BigDecimal.valueOf(julianSecond += ((long)this.hour * 60L + (long)this.minute) * 60L + (long)this.second);
        if (this.nanosecond == 0) {
            return j;
        }
        return j.add(BigDecimal.valueOf(this.nanosecond).divide(BigDecimalValue.BIG_DECIMAL_ONE_BILLION, 9, RoundingMode.HALF_EVEN));
    }

    public static DateTimeValue fromJulianInstant(BigDecimal instant) {
        BigInteger julianSecond = instant.toBigInteger();
        BigDecimal nanoseconds = instant.subtract(new BigDecimal(julianSecond)).multiply(BigDecimalValue.BIG_DECIMAL_ONE_BILLION);
        long js = julianSecond.longValue();
        long jd = js / 86400L;
        DateValue date = DateValue.dateFromJulianDayNumber((int)jd);
        byte hour = (byte)((js %= 86400L) / 3600L);
        byte minute = (byte)((js %= 3600L) / 60L);
        return new DateTimeValue(date.getYear(), date.getMonth(), date.getDay(), hour, minute, (byte)(js %= 60L), nanoseconds.intValue(), 0);
    }

    @Override
    public GregorianCalendar getCalendar() {
        SimpleTimeZone zone;
        GregorianCalendar calendar;
        int tz = this.hasTimezone() ? this.getTimezoneInMinutes() * 60000 : 0;
        if (tz < (calendar = new GregorianCalendar(zone = new SimpleTimeZone(tz, "LLL"))).getMinimum(15) || tz > calendar.getMaximum(15)) {
            return this.adjustTimezone(0).getCalendar();
        }
        calendar.setGregorianChange(new Date(Long.MIN_VALUE));
        calendar.setLenient(false);
        int yr = this.year;
        if (this.year <= 0) {
            yr = this.hasNoYearZero ? 1 - this.year : 0 - this.year;
            calendar.set(0, 0);
        }
        calendar.set(yr, this.month - 1, this.day, this.hour, this.minute, this.second);
        calendar.set(14, this.nanosecond / 1000000);
        calendar.set(15, tz);
        calendar.set(16, 0);
        return calendar;
    }

    public Instant toJavaInstant() {
        return Instant.from(this);
    }

    public ZonedDateTime toZonedDateTime() {
        if (this.hasTimezone()) {
            return ZonedDateTime.from(this);
        }
        try {
            return ZonedDateTime.from(this.adjustToUTC(0));
        } catch (NoDynamicContextException e) {
            throw new AssertionError((Object)e);
        }
    }

    public OffsetDateTime toOffsetDateTime() {
        if (this.hasTimezone()) {
            return OffsetDateTime.from(this);
        }
        try {
            return OffsetDateTime.from(this.adjustToUTC(0));
        } catch (NoDynamicContextException e) {
            throw new AssertionError((Object)e);
        }
    }

    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.from(this);
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        FastStringBuffer sb = new FastStringBuffer(30);
        int yr = this.year;
        if (this.year <= 0 && (yr = -yr + (this.hasNoYearZero ? 1 : 0)) != 0) {
            sb.cat('-');
        }
        DateTimeValue.appendString(sb, yr, yr > 9999 ? (yr + "").length() : 4);
        sb.cat('-');
        DateTimeValue.appendTwoDigits(sb, this.month);
        sb.cat('-');
        DateTimeValue.appendTwoDigits(sb, this.day);
        sb.cat('T');
        DateTimeValue.appendTwoDigits(sb, this.hour);
        sb.cat(':');
        DateTimeValue.appendTwoDigits(sb, this.minute);
        sb.cat(':');
        DateTimeValue.appendTwoDigits(sb, this.second);
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

    public DateValue toDateValue() {
        return new DateValue(this.year, this.month, this.day, this.getTimezoneInMinutes(), this.hasNoYearZero);
    }

    public TimeValue toTimeValue() {
        return new TimeValue(this.hour, this.minute, this.second, this.nanosecond, this.getTimezoneInMinutes(), "");
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        if (this.hasTimezone() && this.getTimezoneInMinutes() != 0) {
            return this.adjustTimezone(0).getStringValueCS();
        }
        return this.getStringValueCS();
    }

    @Override
    public DateTimeValue copyAsSubType(AtomicType typeLabel) {
        DateTimeValue v = new DateTimeValue(this.year, this.month, this.day, this.hour, this.minute, this.second, this.nanosecond, this.getTimezoneInMinutes());
        v.hasNoYearZero = this.hasNoYearZero;
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public DateTimeValue adjustTimezone(int timezone) {
        DateValue t;
        if (!this.hasTimezone()) {
            DateTimeValue in = this.copyAsSubType(this.typeLabel);
            in.setTimezoneInMinutes(timezone);
            return in;
        }
        int oldtz = this.getTimezoneInMinutes();
        if (oldtz == timezone) {
            return this;
        }
        int tz = timezone - oldtz;
        int h = this.hour;
        int mi = this.minute;
        if ((mi += tz) < 0 || mi > 59) {
            h = (int)((double)h + Math.floor((double)mi / 60.0));
            mi = (mi + 1440) % 60;
        }
        if (h >= 0 && h < 24) {
            DateTimeValue d2 = new DateTimeValue(this.year, this.month, this.day, (byte)h, (byte)mi, this.second, this.nanosecond, timezone);
            d2.hasNoYearZero = this.hasNoYearZero;
            return d2;
        }
        DateTimeValue dt = this;
        while (h < 0) {
            t = DateValue.yesterday(dt.getYear(), dt.getMonth(), dt.getDay());
            dt = new DateTimeValue(t.getYear(), t.getMonth(), t.getDay(), (byte)(h += 24), (byte)mi, this.second, this.nanosecond, timezone);
            dt.hasNoYearZero = this.hasNoYearZero;
        }
        if (h > 23) {
            t = DateValue.tomorrow(this.year, this.month, this.day);
            dt = new DateTimeValue(t.getYear(), t.getMonth(), t.getDay(), (byte)(h -= 24), (byte)mi, this.second, this.nanosecond, timezone);
            dt.hasNoYearZero = this.hasNoYearZero;
        }
        return dt;
    }

    @Override
    public DateTimeValue add(DurationValue duration) throws XPathException {
        if (duration instanceof DayTimeDurationValue) {
            BigDecimal seconds = ((DayTimeDurationValue)duration).getTotalSeconds();
            BigDecimal julian = this.toJulianInstant();
            julian = julian.add(seconds);
            DateTimeValue dt = DateTimeValue.fromJulianInstant(julian);
            dt.setTimezoneInMinutes(this.getTimezoneInMinutes());
            dt.hasNoYearZero = this.hasNoYearZero;
            return dt;
        }
        if (duration instanceof YearMonthDurationValue) {
            int months = ((YearMonthDurationValue)duration).getLengthInMonths();
            int m = this.month - 1 + months;
            int y = this.year + m / 12;
            if ((m %= 12) < 0) {
                m += 12;
                --y;
            }
            ++m;
            int d = this.day;
            while (!DateValue.isValidDate(y, m, d)) {
                --d;
            }
            DateTimeValue dtv = new DateTimeValue(y, (byte)m, (byte)d, this.hour, this.minute, this.second, this.nanosecond, this.getTimezoneInMinutes());
            dtv.hasNoYearZero = this.hasNoYearZero;
            return dtv;
        }
        XPathException err = new XPathException("DateTime arithmetic is not supported on xs:duration, only on its subtypes");
        err.setErrorCode("XPTY0004");
        err.setIsTypeError(true);
        throw err;
    }

    @Override
    public DayTimeDurationValue subtract(CalendarValue other, XPathContext context) throws XPathException {
        if (!(other instanceof DateTimeValue)) {
            XPathException err = new XPathException("First operand of '-' is a dateTime, but the second is not");
            err.setErrorCode("XPTY0004");
            err.setIsTypeError(true);
            throw err;
        }
        return super.subtract(other, context);
    }

    public BigDecimal secondsSinceEpoch() {
        try {
            DateTimeValue dtv = this.adjustToUTC(0);
            BigDecimal d1 = dtv.toJulianInstant();
            BigDecimal d2 = EPOCH.toJulianInstant();
            return d1.subtract(d2);
        } catch (NoDynamicContextException e) {
            throw new AssertionError((Object)e);
        }
    }

    @Override
    public AtomicValue getComponent(AccessorFn.Component component) throws XPathException {
        switch (component) {
            case YEAR_ALLOWING_ZERO: {
                return Int64Value.makeIntegerValue(this.year);
            }
            case YEAR: {
                return Int64Value.makeIntegerValue(this.year > 0 || !this.hasNoYearZero ? (long)this.year : (long)(this.year - 1));
            }
            case MONTH: {
                return Int64Value.makeIntegerValue(this.month);
            }
            case DAY: {
                return Int64Value.makeIntegerValue(this.day);
            }
            case HOURS: {
                return Int64Value.makeIntegerValue(this.hour);
            }
            case MINUTES: {
                return Int64Value.makeIntegerValue(this.minute);
            }
            case SECONDS: {
                BigDecimal d = BigDecimal.valueOf(this.nanosecond);
                d = d.divide(BigDecimalValue.BIG_DECIMAL_ONE_BILLION, 6, 4);
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
        throw new IllegalArgumentException("Unknown component for dateTime: " + (Object)((Object)component));
    }

    @Override
    public boolean isSupported(TemporalField field) {
        if (field.equals(ChronoField.OFFSET_SECONDS)) {
            return this.getTimezoneInMinutes() != Integer.MIN_VALUE;
        }
        if (field instanceof ChronoField) {
            return true;
        }
        return field.isSupportedBy(this);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField)field) {
                case NANO_OF_SECOND: {
                    return this.nanosecond;
                }
                case NANO_OF_DAY: {
                    return (long)(this.hour * 3600 + this.minute * 60 + this.second) * 1000000000L + (long)this.nanosecond;
                }
                case MICRO_OF_SECOND: {
                    return this.nanosecond / 1000;
                }
                case MICRO_OF_DAY: {
                    return (long)(this.hour * 3600 + this.minute * 60 + this.second) * 1000000L + (long)(this.nanosecond / 1000);
                }
                case MILLI_OF_SECOND: {
                    return this.nanosecond / 1000000;
                }
                case MILLI_OF_DAY: {
                    return (long)(this.hour * 3600 + this.minute * 60 + this.second) * 1000L + (long)(this.nanosecond / 1000000);
                }
                case SECOND_OF_MINUTE: {
                    return this.second;
                }
                case SECOND_OF_DAY: {
                    return this.hour * 3600 + this.minute * 60 + this.second;
                }
                case MINUTE_OF_HOUR: {
                    return this.minute;
                }
                case MINUTE_OF_DAY: {
                    return this.hour * 60 + this.minute;
                }
                case HOUR_OF_AMPM: {
                    return this.hour % 12;
                }
                case CLOCK_HOUR_OF_AMPM: {
                    return (this.hour + 11) % 12 + 1;
                }
                case HOUR_OF_DAY: {
                    return this.hour;
                }
                case CLOCK_HOUR_OF_DAY: {
                    return (this.hour + 23) % 24 + 1;
                }
                case AMPM_OF_DAY: {
                    return this.hour / 12;
                }
                case DAY_OF_WEEK: {
                    return DateValue.getDayOfWeek(this.year, this.month, this.day);
                }
                case ALIGNED_DAY_OF_WEEK_IN_MONTH: {
                    return (this.day - 1) % 7 + 1;
                }
                case ALIGNED_DAY_OF_WEEK_IN_YEAR: {
                    return (DateValue.getDayWithinYear(this.year, this.month, this.day) - 1) % 7 + 1;
                }
                case DAY_OF_MONTH: {
                    return this.day;
                }
                case DAY_OF_YEAR: {
                    return DateValue.getDayWithinYear(this.year, this.month, this.day);
                }
                case EPOCH_DAY: {
                    BigDecimal secs = this.secondsSinceEpoch();
                    long days = this.secondsSinceEpoch().longValue() / 86400L;
                    return secs.signum() < 0 ? days - 1L : days;
                }
                case ALIGNED_WEEK_OF_MONTH: {
                    return (this.day - 1) / 7 + 1;
                }
                case ALIGNED_WEEK_OF_YEAR: {
                    return (DateValue.getDayWithinYear(this.year, this.month, this.day) - 1) / 7 + 1;
                }
                case MONTH_OF_YEAR: {
                    return this.month;
                }
                case PROLEPTIC_MONTH: {
                    return this.year * 12 + this.month - 1;
                }
                case YEAR_OF_ERA: {
                    return Math.abs(this.year) + (this.year < 0 ? 1 : 0);
                }
                case YEAR: {
                    return this.year;
                }
                case ERA: {
                    return this.year < 0 ? 0L : 1L;
                }
                case INSTANT_SECONDS: {
                    return this.secondsSinceEpoch().setScale(0, 3).longValue();
                }
                case OFFSET_SECONDS: {
                    int tz = this.getTimezoneInMinutes();
                    if (tz == Integer.MIN_VALUE) {
                        throw new UnsupportedTemporalTypeException("xs:dateTime value has no timezone");
                    }
                    return tz * 60;
                }
            }
            throw new UnsupportedTemporalTypeException(field.toString());
        }
        return field.getFrom(this);
    }

    @Override
    public int compareTo(CalendarValue other, int implicitTimezone) throws NoDynamicContextException {
        if (!(other instanceof DateTimeValue)) {
            throw new ClassCastException("DateTime values are not comparable to " + other.getClass());
        }
        DateTimeValue v2 = (DateTimeValue)other;
        if (this.getTimezoneInMinutes() == v2.getTimezoneInMinutes()) {
            if (this.year != v2.year) {
                return IntegerValue.signum(this.year - v2.year);
            }
            if (this.month != v2.month) {
                return IntegerValue.signum(this.month - v2.month);
            }
            if (this.day != v2.day) {
                return IntegerValue.signum(this.day - v2.day);
            }
            if (this.hour != v2.hour) {
                return IntegerValue.signum(this.hour - v2.hour);
            }
            if (this.minute != v2.minute) {
                return IntegerValue.signum(this.minute - v2.minute);
            }
            if (this.second != v2.second) {
                return IntegerValue.signum(this.second - v2.second);
            }
            if (this.nanosecond != v2.nanosecond) {
                return IntegerValue.signum(this.nanosecond - v2.nanosecond);
            }
            return 0;
        }
        return this.adjustToUTC(implicitTimezone).compareTo(v2.adjustToUTC(implicitTimezone), implicitTimezone);
    }

    public int compareTo(Object v2) {
        try {
            return this.compareTo((DateTimeValue)v2, Integer.MAX_VALUE);
        } catch (Exception err) {
            throw new ClassCastException("DateTime comparison requires access to implicit timezone");
        }
    }

    @Override
    public Comparable getSchemaComparable() {
        return new DateTimeComparable();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DateTimeValue && this.compareTo(o) == 0;
    }

    public int hashCode() {
        return DateTimeValue.hashCode(this.year, this.month, this.day, this.hour, this.minute, this.second, this.nanosecond, this.getTimezoneInMinutes());
    }

    static int hashCode(int year, byte month, byte day, byte hour, byte minute, byte second, int nanosecond, int tzMinutes) {
        DateValue t;
        int tz = tzMinutes == Integer.MIN_VALUE ? 0 : -tzMinutes;
        int h = hour;
        int mi = minute;
        if ((mi += tz) < 0 || mi > 59) {
            h = (int)((double)h + Math.floor((double)mi / 60.0));
            mi = (mi + 1440) % 60;
        }
        while (h < 0) {
            h += 24;
            t = DateValue.yesterday(year, month, day);
            year = t.getYear();
            month = t.getMonth();
            day = t.getDay();
        }
        while (h > 23) {
            h -= 24;
            t = DateValue.tomorrow(year, month, day);
            year = t.getYear();
            month = t.getMonth();
            day = t.getDay();
        }
        return year << 4 ^ month << 28 ^ day << 23 ^ h << 18 ^ mi << 13 ^ second ^ nanosecond;
    }

    private class DateTimeComparable
    implements Comparable {
        private DateTimeComparable() {
        }

        private DateTimeValue asDateTimeValue() {
            return DateTimeValue.this;
        }

        public int compareTo(Object o) {
            if (o instanceof DateTimeComparable) {
                DateTimeValue dt0 = DateTimeValue.this;
                DateTimeValue dt1 = ((DateTimeComparable)o).asDateTimeValue();
                if (dt0.hasTimezone()) {
                    if (dt1.hasTimezone()) {
                        dt0 = dt0.adjustTimezone(0);
                        dt1 = dt1.adjustTimezone(0);
                        return dt0.compareTo(dt1);
                    }
                    DateTimeValue dt1max = dt1.adjustTimezone(840);
                    if (dt0.compareTo(dt1max) < 0) {
                        return -1;
                    }
                    DateTimeValue dt1min = dt1.adjustTimezone(-840);
                    if (dt0.compareTo(dt1min) > 0) {
                        return 1;
                    }
                    return Integer.MIN_VALUE;
                }
                if (dt1.hasTimezone()) {
                    DateTimeValue dt0min = dt0.adjustTimezone(-840);
                    if (dt0min.compareTo(dt1) < 0) {
                        return -1;
                    }
                    DateTimeValue dt0max = dt0.adjustTimezone(840);
                    if (dt0max.compareTo(dt1) > 0) {
                        return 1;
                    }
                    return Integer.MIN_VALUE;
                }
                dt0 = dt0.adjustTimezone(0);
                dt1 = dt1.adjustTimezone(0);
                return dt0.compareTo(dt1);
            }
            return Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return o instanceof DateTimeComparable && DateTimeValue.this.hasTimezone() == ((DateTimeComparable)o).asDateTimeValue().hasTimezone() && this.compareTo(o) == 0;
        }

        public int hashCode() {
            DateTimeValue dt0 = DateTimeValue.this.adjustTimezone(0);
            return dt0.year << 20 ^ dt0.month << 16 ^ dt0.day << 11 ^ dt0.hour << 7 ^ dt0.minute << 2 ^ dt0.second * 1000000000 + dt0.nanosecond;
        }
    }
}


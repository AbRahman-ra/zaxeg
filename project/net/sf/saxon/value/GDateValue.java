/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.Whitespace;

public abstract class GDateValue
extends CalendarValue {
    protected int year;
    protected byte month;
    protected byte day;
    protected boolean hasNoYearZero;
    protected static byte[] daysPerMonth = new byte[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    protected static final short[] monthData = new short[]{306, 337, 0, 31, 61, 92, 122, 153, 184, 214, 245, 275};

    public int getYear() {
        return this.year;
    }

    public byte getMonth() {
        return this.month;
    }

    public byte getDay() {
        return this.day;
    }

    @Override
    public GregorianCalendar getCalendar() {
        int tz = this.hasTimezone() ? this.getTimezoneInMinutes() * 60000 : 0;
        SimpleTimeZone zone = new SimpleTimeZone(tz, "LLL");
        GregorianCalendar calendar = new GregorianCalendar(zone);
        calendar.setGregorianChange(new Date(Long.MIN_VALUE));
        if (tz < calendar.getMinimum(15) || tz > calendar.getMaximum(15)) {
            return this.adjustTimezone(0).getCalendar();
        }
        calendar.clear();
        calendar.setLenient(false);
        int yr = this.year;
        if (this.year <= 0) {
            yr = this.hasNoYearZero ? 1 - this.year : 0 - this.year;
            calendar.set(0, 0);
        }
        calendar.set(yr, this.month - 1, this.day);
        calendar.set(15, tz);
        calendar.set(16, 0);
        calendar.getTime();
        return calendar;
    }

    protected static ConversionResult setLexicalValue(GDateValue d, CharSequence s, boolean allowYearZero) {
        d.hasNoYearZero = !allowYearZero;
        StringTokenizer tok = new StringTokenizer(Whitespace.trimWhitespace(s).toString(), "-:+TZ", true);
        try {
            if (!tok.hasMoreElements()) {
                return GDateValue.badDate("Too short", s);
            }
            String part = (String)tok.nextElement();
            int era = 1;
            if ("+".equals(part)) {
                return GDateValue.badDate("Date must not start with '+' sign", s);
            }
            if ("-".equals(part)) {
                era = -1;
                if (!tok.hasMoreElements()) {
                    return GDateValue.badDate("No year after '-'", s);
                }
                part = (String)tok.nextElement();
            }
            if (part.length() < 4) {
                return GDateValue.badDate("Year is less than four digits", s);
            }
            if (part.length() > 4 && part.charAt(0) == '0') {
                return GDateValue.badDate("When year exceeds 4 digits, leading zeroes are not allowed", s);
            }
            int value = DurationValue.simpleInteger(part);
            if (value < 0) {
                if (value == -1) {
                    return GDateValue.badDate("Non-numeric year component", s);
                }
                return GDateValue.badDate("Year is outside the range that Saxon can handle", s, "FODT0001");
            }
            d.year = value * era;
            if (d.year == 0 && !allowYearZero) {
                return GDateValue.badDate("Year zero is not allowed", s);
            }
            if (era < 0 && !allowYearZero) {
                ++d.year;
            }
            if (!tok.hasMoreElements()) {
                return GDateValue.badDate("Too short", s);
            }
            if (!"-".equals(tok.nextElement())) {
                return GDateValue.badDate("Wrong delimiter after year", s);
            }
            if (!tok.hasMoreElements()) {
                return GDateValue.badDate("Too short", s);
            }
            part = (String)tok.nextElement();
            if (part.length() != 2) {
                return GDateValue.badDate("Month must be two digits", s);
            }
            value = DurationValue.simpleInteger(part);
            if (value < 0) {
                return GDateValue.badDate("Non-numeric month component", s);
            }
            d.month = (byte)value;
            if (d.month < 1 || d.month > 12) {
                return GDateValue.badDate("Month is out of range", s);
            }
            if (!tok.hasMoreElements()) {
                return GDateValue.badDate("Too short", s);
            }
            if (!"-".equals(tok.nextElement())) {
                return GDateValue.badDate("Wrong delimiter after month", s);
            }
            if (!tok.hasMoreElements()) {
                return GDateValue.badDate("Too short", s);
            }
            part = (String)tok.nextElement();
            if (part.length() != 2) {
                return GDateValue.badDate("Day must be two digits", s);
            }
            value = DurationValue.simpleInteger(part);
            if (value < 0) {
                return GDateValue.badDate("Non-numeric day component", s);
            }
            d.day = (byte)value;
            if (d.day < 1 || d.day > 31) {
                return GDateValue.badDate("Day is out of range", s);
            }
            if (tok.hasMoreElements()) {
                String delim = (String)tok.nextElement();
                if ("T".equals(delim)) {
                    return GDateValue.badDate("Value includes time", s);
                }
                if ("Z".equals(delim)) {
                    int tzOffset = 0;
                    if (tok.hasMoreElements()) {
                        return GDateValue.badDate("Continues after 'Z'", s);
                    }
                    d.setTimezoneInMinutes(tzOffset);
                } else if ("+".equals(delim) || "-".equals(delim)) {
                    if (!tok.hasMoreElements()) {
                        return GDateValue.badDate("Missing timezone", s);
                    }
                    part = (String)tok.nextElement();
                    value = DurationValue.simpleInteger(part);
                    if (value < 0) {
                        return GDateValue.badDate("Non-numeric timezone hour component", s);
                    }
                    int tzhour = value;
                    if (part.length() != 2) {
                        return GDateValue.badDate("Timezone hour must be two digits", s);
                    }
                    if (tzhour > 14) {
                        return GDateValue.badDate("Timezone hour is out of range", s);
                    }
                    if (!tok.hasMoreElements()) {
                        return GDateValue.badDate("No minutes in timezone", s);
                    }
                    if (!":".equals(tok.nextElement())) {
                        return GDateValue.badDate("Wrong delimiter after timezone hour", s);
                    }
                    if (!tok.hasMoreElements()) {
                        return GDateValue.badDate("No minutes in timezone", s);
                    }
                    part = (String)tok.nextElement();
                    value = DurationValue.simpleInteger(part);
                    if (value < 0) {
                        return GDateValue.badDate("Non-numeric timezone minute component", s);
                    }
                    int tzminute = value;
                    if (part.length() != 2) {
                        return GDateValue.badDate("Timezone minute must be two digits", s);
                    }
                    if (tzminute > 59) {
                        return GDateValue.badDate("Timezone minute is out of range", s);
                    }
                    if (tok.hasMoreElements()) {
                        return GDateValue.badDate("Continues after timezone", s);
                    }
                    int tzOffset = tzhour * 60 + tzminute;
                    if ("-".equals(delim)) {
                        tzOffset = -tzOffset;
                    }
                    d.setTimezoneInMinutes(tzOffset);
                } else {
                    return GDateValue.badDate("Timezone format is incorrect", s);
                }
            }
            if (!GDateValue.isValidDate(d.year, d.month, d.day)) {
                return GDateValue.badDate("Non-existent date", s);
            }
        } catch (NumberFormatException err) {
            return GDateValue.badDate("Non-numeric component", s);
        }
        return d;
    }

    private static ValidationFailure badDate(String msg, CharSequence value) {
        ValidationFailure err = new ValidationFailure("Invalid date " + Err.wrap(value, 4) + " (" + msg + ")");
        err.setErrorCode("FORG0001");
        return err;
    }

    private static ValidationFailure badDate(String msg, CharSequence value, String errorCode) {
        ValidationFailure err = new ValidationFailure("Invalid date " + Err.wrap(value, 4) + " (" + msg + ")");
        err.setErrorCode(errorCode);
        return err;
    }

    public static boolean isValidDate(int year, int month, int day) {
        return month > 0 && month <= 12 && day > 0 && day <= daysPerMonth[month - 1] || month == 2 && day == 29 && GDateValue.isLeapYear(year);
    }

    public static boolean isLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    @Override
    public void checkValidInJavascript() throws XPathException {
        if (this.year <= 0 || this.year > 9999) {
            throw new XPathException("Year out of range for Saxon-JS", "FODT0001");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GDateValue) {
            GDateValue gdv = (GDateValue)o;
            return this.getPrimitiveType() == gdv.getPrimitiveType() && this.toDateTime().equals(gdv.toDateTime());
        }
        return false;
    }

    public int hashCode() {
        return DateTimeValue.hashCode(this.year, this.month, this.day, (byte)12, (byte)0, (byte)0, 0, this.getTimezoneInMinutes());
    }

    @Override
    public int compareTo(CalendarValue other, int implicitTimezone) throws NoDynamicContextException {
        if (this.getPrimitiveType() != other.getPrimitiveType()) {
            throw new ClassCastException("Cannot compare dates of different types");
        }
        GDateValue v2 = (GDateValue)other;
        if (this.getTimezoneInMinutes() == other.getTimezoneInMinutes()) {
            if (this.year != v2.year) {
                return IntegerValue.signum(this.year - v2.year);
            }
            if (this.month != v2.month) {
                return IntegerValue.signum(this.month - v2.month);
            }
            if (this.day != v2.day) {
                return IntegerValue.signum(this.day - v2.day);
            }
            return 0;
        }
        return this.toDateTime().compareTo(other.toDateTime(), implicitTimezone);
    }

    @Override
    public DateTimeValue toDateTime() {
        return new DateTimeValue(this.year, this.month, this.day, 0, 0, 0, 0, this.getTimezoneInMinutes(), this.hasNoYearZero);
    }

    @Override
    public Comparable getSchemaComparable() {
        return new GDateComparable();
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
            case TIMEZONE: {
                if (this.hasTimezone()) {
                    return DayTimeDurationValue.fromMilliseconds(60000L * (long)this.getTimezoneInMinutes());
                }
                return null;
            }
        }
        throw new IllegalArgumentException("Unknown component for date: " + (Object)((Object)component));
    }

    private class GDateComparable
    implements Comparable {
        private GDateComparable() {
        }

        public GDateValue asGDateValue() {
            return GDateValue.this;
        }

        public int compareTo(Object o) {
            if (o instanceof GDateComparable) {
                if (this.asGDateValue().getPrimitiveType() != ((GDateComparable)o).asGDateValue().getPrimitiveType()) {
                    return Integer.MIN_VALUE;
                }
                DateTimeValue dt0 = GDateValue.this.toDateTime();
                DateTimeValue dt1 = ((GDateComparable)o).asGDateValue().toDateTime();
                return dt0.getSchemaComparable().compareTo(dt1.getSchemaComparable());
            }
            return Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return this.compareTo(o) == 0;
        }

        public int hashCode() {
            return GDateValue.this.toDateTime().getSchemaComparable().hashCode();
        }
    }
}


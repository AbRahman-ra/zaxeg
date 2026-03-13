/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.GregorianCalendar;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.GDateValue;
import net.sf.saxon.value.YearMonthDurationValue;

public class DateValue
extends GDateValue
implements Comparable {
    private DateValue() {
    }

    public DateValue(int year, byte month, byte day) {
        this.hasNoYearZero = true;
        this.year = year;
        this.month = month;
        this.day = day;
        this.typeLabel = BuiltInAtomicType.DATE;
    }

    public DateValue(int year, byte month, byte day, boolean xsd10) {
        this.hasNoYearZero = xsd10;
        this.year = year;
        this.month = month;
        this.day = day;
        this.typeLabel = BuiltInAtomicType.DATE;
    }

    public DateValue(int year, byte month, byte day, int tz, boolean xsd10) {
        this.hasNoYearZero = xsd10;
        this.year = year;
        this.month = month;
        this.day = day;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = BuiltInAtomicType.DATE;
    }

    public DateValue(int year, byte month, byte day, int tz, AtomicType type) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = type;
    }

    public DateValue(CharSequence s) throws ValidationException {
        this(s, ConversionRules.DEFAULT);
    }

    public DateValue(CharSequence s, ConversionRules rules) throws ValidationException {
        DateValue.setLexicalValue(this, s, rules.isAllowYearZero()).asAtomic();
        this.typeLabel = BuiltInAtomicType.DATE;
    }

    public DateValue(LocalDate localDate) {
        this(localDate.getYear(), (byte)localDate.getMonthValue(), (byte)localDate.getDayOfMonth());
    }

    public DateValue(GregorianCalendar calendar, int tz) {
        int era = calendar.get(0);
        this.year = calendar.get(1);
        if (era == 0) {
            this.year = 1 - this.year;
        }
        this.month = (byte)(calendar.get(2) + 1);
        this.day = (byte)calendar.get(5);
        this.setTimezoneInMinutes(tz);
        this.typeLabel = BuiltInAtomicType.DATE;
    }

    public static ConversionResult makeDateValue(CharSequence in, ConversionRules rules) {
        DateValue d = new DateValue();
        d.typeLabel = BuiltInAtomicType.DATE;
        return DateValue.setLexicalValue(d, in, rules.isAllowYearZero());
    }

    public static DateValue parse(CharSequence s) throws DateTimeParseException {
        ConversionResult result = DateValue.makeDateValue(s, ConversionRules.DEFAULT);
        if (result instanceof ValidationFailure) {
            throw new DateTimeParseException(((ValidationFailure)result).getMessage(), s, 0);
        }
        return (DateValue)result;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.DATE;
    }

    public static DateValue tomorrow(int year, byte month, byte day) {
        if (DateValue.isValidDate(year, month, day + 1)) {
            return new DateValue(year, month, (byte)(day + 1), true);
        }
        if (month < 12) {
            return new DateValue(year, (byte)(month + 1), 1, true);
        }
        return new DateValue(year + 1, 1, 1, true);
    }

    public static DateValue yesterday(int year, byte month, byte day) {
        if (day > 1) {
            return new DateValue(year, month, (byte)(day - 1), true);
        }
        if (month > 1) {
            if (month == 3 && DateValue.isLeapYear(year)) {
                return new DateValue(year, 2, 29, true);
            }
            return new DateValue(year, (byte)(month - 1), daysPerMonth[month - 2], true);
        }
        return new DateValue(year - 1, 12, 31, true);
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        FastStringBuffer sb = new FastStringBuffer(16);
        int yr = this.year;
        if (this.year <= 0 && (yr = -yr + (this.hasNoYearZero ? 1 : 0)) != 0) {
            sb.cat('-');
        }
        DateValue.appendString(sb, yr, yr > 9999 ? (yr + "").length() : 4);
        sb.cat('-');
        DateValue.appendTwoDigits(sb, this.month);
        sb.cat('-');
        DateValue.appendTwoDigits(sb, this.day);
        if (this.hasTimezone()) {
            this.appendTimezone(sb);
        }
        return sb;
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        DateValue target = this;
        if (this.hasTimezone()) {
            if (this.getTimezoneInMinutes() > 720) {
                target = this.adjustTimezone(this.getTimezoneInMinutes() - 1440);
            } else if (this.getTimezoneInMinutes() <= -720) {
                target = this.adjustTimezone(this.getTimezoneInMinutes() + 1440);
            }
        }
        return target.getStringValueCS();
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        DateValue v = new DateValue(this.year, this.month, this.day, this.getTimezoneInMinutes(), this.hasNoYearZero);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public DateValue adjustTimezone(int timezone) {
        DateTimeValue dt = this.toDateTime().adjustTimezone(timezone);
        return new DateValue(dt.getYear(), dt.getMonth(), dt.getDay(), dt.getTimezoneInMinutes(), this.hasNoYearZero);
    }

    @Override
    public DateValue add(DurationValue duration) throws XPathException {
        if (duration instanceof DayTimeDurationValue) {
            long microseconds = ((DayTimeDurationValue)duration).getLengthInMicroseconds();
            boolean negative = microseconds < 0L;
            microseconds = Math.abs(microseconds);
            int days = (int)Math.floor((double)microseconds / 8.64E10);
            boolean partDay = microseconds % 86400000000L > 0L;
            int julian = this.getJulianDayNumber();
            DateValue d = DateValue.dateFromJulianDayNumber(julian + (negative ? -days : days));
            if (partDay && negative) {
                d = DateValue.yesterday(d.year, d.month, d.day);
            }
            d.setTimezoneInMinutes(this.getTimezoneInMinutes());
            d.hasNoYearZero = this.hasNoYearZero;
            return d;
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
            return new DateValue(y, (byte)m, (byte)d, this.getTimezoneInMinutes(), this.hasNoYearZero);
        }
        XPathException err = new XPathException("Date arithmetic is not available for xs:duration, only for its subtypes");
        err.setIsTypeError(true);
        err.setErrorCode("XPTY0004");
        throw err;
    }

    @Override
    public DayTimeDurationValue subtract(CalendarValue other, XPathContext context) throws XPathException {
        if (!(other instanceof DateValue)) {
            XPathException err = new XPathException("First operand of '-' is a date, but the second is not");
            err.setIsTypeError(true);
            err.setErrorCode("XPTY0004");
            throw err;
        }
        return super.subtract(other, context);
    }

    public int compareTo(Object v2) {
        try {
            return this.compareTo((DateValue)v2, Integer.MAX_VALUE);
        } catch (Exception err) {
            throw new ClassCastException("Date comparison requires access to implicit timezone");
        }
    }

    public static int getJulianDayNumber(int year, int month, int day) {
        int z = year - (month < 3 ? 1 : 0);
        short f = monthData[month - 1];
        if (z >= 0) {
            return day + f + 365 * z + z / 4 - z / 100 + z / 400 + 1721118;
        }
        int j = day + f + 365 * (z += 12000) + z / 4 - z / 100 + z / 400 + 1721118;
        return j - 4382910;
    }

    public int getJulianDayNumber() {
        return DateValue.getJulianDayNumber(this.year, this.month, this.day);
    }

    public static DateValue dateFromJulianDayNumber(int julianDayNumber) {
        if (julianDayNumber >= 0) {
            int L = julianDayNumber + 68569 + 1;
            int n = 4 * L / 146097;
            int i = 4000 * ((L -= (146097 * n + 3) / 4) + 1) / 1461001;
            L = L - 1461 * i / 4 + 31;
            int j = 80 * L / 2447;
            int d = L - 2447 * j / 80;
            L = j / 11;
            int m = j + 2 - 12 * L;
            int y = 100 * (n - 49) + i + L;
            return new DateValue(y, (byte)m, (byte)d, true);
        }
        DateValue dt = DateValue.dateFromJulianDayNumber(julianDayNumber + 4380000 + 3000 - 120 + 30);
        dt.year -= 12000;
        return dt;
    }

    public static int getDayWithinYear(int year, int month, int day) {
        int j = DateValue.getJulianDayNumber(year, month, day);
        int k = DateValue.getJulianDayNumber(year, 1, 1);
        return j - k + 1;
    }

    public static int getDayOfWeek(int year, int month, int day) {
        int d = DateValue.getJulianDayNumber(year, month, day);
        d -= 2378500;
        while (d <= 0) {
            d += 70000000;
        }
        return (d - 1) % 7 + 1;
    }

    public static int getWeekNumber(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        return date.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public static int getWeekNumberWithinMonth(int year, int month, int day) {
        int firstDay = DateValue.getDayOfWeek(year, month, 1);
        if (firstDay > 4 && firstDay + day <= 8) {
            DateValue lastDayPrevMonth = DateValue.yesterday(year, (byte)month, (byte)1);
            return DateValue.getWeekNumberWithinMonth(lastDayPrevMonth.year, lastDayPrevMonth.month, lastDayPrevMonth.day);
        }
        int inc = firstDay < 5 ? 1 : 0;
        return (day + firstDay - 2) / 7 + inc;
    }

    public LocalDate toLocalDate() {
        return LocalDate.of(this.getYear(), this.getMonth(), (int)this.getDay());
    }
}


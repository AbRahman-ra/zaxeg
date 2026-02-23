/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import javax.xml.datatype.XMLGregorianCalendar;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.GDayValue;
import net.sf.saxon.value.GMonthDayValue;
import net.sf.saxon.value.GMonthValue;
import net.sf.saxon.value.GYearMonthValue;
import net.sf.saxon.value.GYearValue;
import net.sf.saxon.value.SaxonXMLGregorianCalendar;
import net.sf.saxon.value.TimeValue;

public abstract class CalendarValue
extends AtomicValue
implements AtomicMatchKey {
    private int tzMinutes = Integer.MIN_VALUE;
    public static final int NO_TIMEZONE = Integer.MIN_VALUE;
    public static final int MISSING_TIMEZONE = Integer.MAX_VALUE;

    public static ConversionResult makeCalendarValue(CharSequence s, ConversionRules rules) {
        ConversionResult cr;
        ConversionResult firstError = cr = DateTimeValue.makeDateTimeValue(s, rules);
        if (cr instanceof ValidationFailure) {
            cr = DateValue.makeDateValue(s, rules);
        }
        if (cr instanceof ValidationFailure) {
            cr = TimeValue.makeTimeValue(s);
        }
        if (cr instanceof ValidationFailure) {
            cr = GYearValue.makeGYearValue(s, rules);
        }
        if (cr instanceof ValidationFailure) {
            cr = GYearMonthValue.makeGYearMonthValue(s, rules);
        }
        if (cr instanceof ValidationFailure) {
            cr = GMonthValue.makeGMonthValue(s);
        }
        if (cr instanceof ValidationFailure) {
            cr = GMonthDayValue.makeGMonthDayValue(s);
        }
        if (cr instanceof ValidationFailure) {
            cr = GDayValue.makeGDayValue(s);
        }
        if (cr instanceof ValidationFailure) {
            return firstError;
        }
        return cr;
    }

    public final boolean hasTimezone() {
        return this.tzMinutes != Integer.MIN_VALUE;
    }

    public final void setTimezoneInMinutes(int minutes) {
        this.tzMinutes = minutes;
    }

    public abstract DateTimeValue toDateTime();

    public final int getTimezoneInMinutes() {
        return this.tzMinutes;
    }

    public abstract GregorianCalendar getCalendar();

    public XMLGregorianCalendar getXMLGregorianCalendar() {
        return new SaxonXMLGregorianCalendar(this);
    }

    public abstract CalendarValue add(DurationValue var1) throws XPathException;

    public DayTimeDurationValue subtract(CalendarValue other, XPathContext context) throws XPathException {
        DateTimeValue dt1 = this.toDateTime();
        DateTimeValue dt2 = other.toDateTime();
        if (dt1.getTimezoneInMinutes() != dt2.getTimezoneInMinutes()) {
            int tz = Integer.MIN_VALUE;
            if (context == null || (tz = context.getImplicitTimezone()) == Integer.MAX_VALUE) {
                throw new NoDynamicContextException("Implicit timezone required");
            }
            dt1 = dt1.adjustToUTC(tz);
            dt2 = dt2.adjustToUTC(tz);
        }
        BigDecimal d1 = dt1.toJulianInstant();
        BigDecimal d2 = dt2.toJulianInstant();
        BigDecimal difference = d1.subtract(d2);
        return DayTimeDurationValue.fromSeconds(difference);
    }

    public final CalendarValue removeTimezone() {
        CalendarValue c = (CalendarValue)this.copyAsSubType(this.typeLabel);
        c.tzMinutes = Integer.MIN_VALUE;
        return c;
    }

    public abstract CalendarValue adjustTimezone(int var1);

    public final CalendarValue adjustTimezone(DayTimeDurationValue tz) throws XPathException {
        long microseconds = tz.getLengthInMicroseconds();
        if (microseconds % 60000000L != 0L) {
            XPathException err = new XPathException("Timezone is not an integral number of minutes");
            err.setErrorCode("FODT0003");
            throw err;
        }
        int tzminutes = (int)(microseconds / 60000000L);
        if (Math.abs(tzminutes) > 840) {
            XPathException err = new XPathException("Timezone out of range (-14:00 to +14:00)");
            err.setErrorCode("FODT0003");
            throw err;
        }
        return this.adjustTimezone(tzminutes);
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) throws NoDynamicContextException {
        if (ordered && !(this instanceof Comparable)) {
            return null;
        }
        if (this.hasTimezone()) {
            return this;
        }
        if (implicitTimezone == Integer.MAX_VALUE) {
            throw new NoDynamicContextException("Unknown implicit timezone");
        }
        return this.hasTimezone() ? this : this.adjustTimezone(implicitTimezone);
    }

    public AtomicMatchKey getComparisonKey(XPathContext context) {
        try {
            return this.getXPathComparable(false, CodepointCollator.getInstance(), context.getImplicitTimezone());
        } catch (NoDynamicContextException e) {
            return null;
        }
    }

    @Override
    public AtomicMatchKey asMapKey() {
        return new CalendarValueMapKey();
    }

    public abstract int compareTo(CalendarValue var1, int var2) throws NoDynamicContextException;

    @Override
    public boolean isIdentical(AtomicValue v) {
        return super.isIdentical(v) && this.tzMinutes == ((CalendarValue)v).tzMinutes;
    }

    @Override
    public int identityHashCode() {
        return this.hashCode() ^ this.tzMinutes;
    }

    public final void appendTimezone(FastStringBuffer sb) {
        if (this.hasTimezone()) {
            CalendarValue.appendTimezone(this.getTimezoneInMinutes(), sb);
        }
    }

    public static void appendTimezone(int tz, FastStringBuffer sb) {
        if (tz == 0) {
            sb.append("Z");
        } else {
            sb.append(tz > 0 ? "+" : "-");
            tz = Math.abs(tz);
            CalendarValue.appendTwoDigits(sb, tz / 60);
            sb.cat(':');
            CalendarValue.appendTwoDigits(sb, tz % 60);
        }
    }

    static void appendString(FastStringBuffer sb, int value, int size) {
        String s = "000000000" + value;
        sb.append(s.substring(s.length() - size));
    }

    static void appendTwoDigits(FastStringBuffer sb, int value) {
        sb.cat((char)(value / 10 + 48));
        sb.cat((char)(value % 10 + 48));
    }

    private class CalendarValueMapKey
    implements AtomicMatchKey {
        private CalendarValueMapKey() {
        }

        @Override
        public CalendarValue asAtomic() {
            return CalendarValue.this;
        }

        public boolean equals(Object obj) {
            if (obj instanceof CalendarValueMapKey) {
                CalendarValue a = CalendarValue.this;
                CalendarValue b = ((CalendarValueMapKey)obj).asAtomic();
                if (a.hasTimezone() == b.hasTimezone()) {
                    if (a.hasTimezone()) {
                        return a.adjustTimezone(b.tzMinutes).isIdentical(b);
                    }
                    return a.isIdentical(b);
                }
                return false;
            }
            return false;
        }

        public int hashCode() {
            return this.asAtomic().hashCode();
        }
    }
}


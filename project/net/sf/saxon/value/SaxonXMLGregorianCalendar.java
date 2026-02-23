/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.GDayValue;
import net.sf.saxon.value.GMonthDayValue;
import net.sf.saxon.value.GMonthValue;
import net.sf.saxon.value.GYearMonthValue;
import net.sf.saxon.value.GYearValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SaxonDuration;
import net.sf.saxon.value.TimeValue;

public class SaxonXMLGregorianCalendar
extends XMLGregorianCalendar {
    private CalendarValue calendarValue;
    private BigInteger year;
    private int month = Integer.MIN_VALUE;
    private int day = Integer.MIN_VALUE;
    private int hour = Integer.MIN_VALUE;
    private int minute = Integer.MIN_VALUE;
    private int second = Integer.MIN_VALUE;
    private int microsecond = Integer.MIN_VALUE;
    private int tzOffset = Integer.MIN_VALUE;

    public SaxonXMLGregorianCalendar(CalendarValue value) {
        this.clear();
        this.setCalendarValue(value);
    }

    private SaxonXMLGregorianCalendar() {
    }

    public void setCalendarValue(CalendarValue value) {
        this.calendarValue = value;
        try {
            if (value instanceof GYearValue) {
                this.year = BigInteger.valueOf(((Int64Value)value.getComponent(AccessorFn.Component.YEAR)).longValue());
            } else if (value instanceof GYearMonthValue) {
                this.year = BigInteger.valueOf(((Int64Value)value.getComponent(AccessorFn.Component.YEAR)).longValue());
                this.month = (int)((Int64Value)value.getComponent(AccessorFn.Component.MONTH)).longValue();
            } else if (value instanceof GMonthValue) {
                this.month = (int)((Int64Value)value.getComponent(AccessorFn.Component.MONTH)).longValue();
            } else if (value instanceof GMonthDayValue) {
                this.month = (int)((Int64Value)value.getComponent(AccessorFn.Component.MONTH)).longValue();
                this.day = (int)((Int64Value)value.getComponent(AccessorFn.Component.DAY)).longValue();
            } else if (value instanceof GDayValue) {
                this.day = (int)((Int64Value)value.getComponent(AccessorFn.Component.DAY)).longValue();
            } else if (value instanceof DateValue) {
                this.year = BigInteger.valueOf(((Int64Value)value.getComponent(AccessorFn.Component.YEAR)).longValue());
                this.month = (int)((Int64Value)value.getComponent(AccessorFn.Component.MONTH)).longValue();
                this.day = (int)((Int64Value)value.getComponent(AccessorFn.Component.DAY)).longValue();
            } else if (value instanceof TimeValue) {
                this.hour = (int)((Int64Value)value.getComponent(AccessorFn.Component.HOURS)).longValue();
                this.minute = (int)((Int64Value)value.getComponent(AccessorFn.Component.MINUTES)).longValue();
                this.second = (int)((Int64Value)value.getComponent(AccessorFn.Component.WHOLE_SECONDS)).longValue();
                this.microsecond = (int)((Int64Value)value.getComponent(AccessorFn.Component.MICROSECONDS)).longValue();
            } else {
                this.year = BigInteger.valueOf(((Int64Value)value.getComponent(AccessorFn.Component.YEAR)).longValue());
                this.month = (int)((Int64Value)value.getComponent(AccessorFn.Component.MONTH)).longValue();
                this.day = (int)((Int64Value)value.getComponent(AccessorFn.Component.DAY)).longValue();
                this.hour = (int)((Int64Value)value.getComponent(AccessorFn.Component.HOURS)).longValue();
                this.minute = (int)((Int64Value)value.getComponent(AccessorFn.Component.MINUTES)).longValue();
                this.second = (int)((Int64Value)value.getComponent(AccessorFn.Component.WHOLE_SECONDS)).longValue();
                this.microsecond = (int)((Int64Value)value.getComponent(AccessorFn.Component.MICROSECONDS)).longValue();
            }
        } catch (XPathException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void clear() {
        this.year = null;
        this.month = Integer.MIN_VALUE;
        this.day = Integer.MIN_VALUE;
        this.hour = Integer.MIN_VALUE;
        this.minute = Integer.MIN_VALUE;
        this.second = Integer.MIN_VALUE;
        this.microsecond = Integer.MIN_VALUE;
        this.tzOffset = Integer.MIN_VALUE;
    }

    @Override
    public void reset() {
        this.clear();
    }

    @Override
    public void setYear(BigInteger year) {
        this.calendarValue = null;
        this.year = year;
    }

    @Override
    public void setYear(int year) {
        this.calendarValue = null;
        this.year = BigInteger.valueOf(year);
    }

    @Override
    public void setMonth(int month) {
        this.calendarValue = null;
        this.month = month;
    }

    @Override
    public void setDay(int day) {
        this.calendarValue = null;
        this.day = day;
    }

    @Override
    public void setTimezone(int offset) {
        this.calendarValue = null;
        this.tzOffset = offset;
    }

    @Override
    public void setHour(int hour) {
        this.calendarValue = null;
        this.hour = hour;
    }

    @Override
    public void setMinute(int minute) {
        this.calendarValue = null;
        this.minute = minute;
    }

    @Override
    public void setSecond(int second) {
        this.calendarValue = null;
        this.second = second;
    }

    @Override
    public void setMillisecond(int millisecond) {
        this.calendarValue = null;
        this.microsecond = millisecond * 1000;
    }

    @Override
    public void setFractionalSecond(BigDecimal fractional) {
        this.calendarValue = null;
        this.second = fractional.intValue();
        BigInteger micros = fractional.movePointRight(6).toBigInteger();
        micros = micros.remainder(BigInteger.valueOf(1000000L));
        this.microsecond = micros.intValue();
    }

    @Override
    public BigInteger getEon() {
        return this.year.divide(BigInteger.valueOf(1000000000L));
    }

    @Override
    public int getYear() {
        return this.year.intValue();
    }

    @Override
    public BigInteger getEonAndYear() {
        return this.year;
    }

    @Override
    public int getMonth() {
        return this.month;
    }

    @Override
    public int getDay() {
        return this.day;
    }

    @Override
    public int getTimezone() {
        return this.tzOffset;
    }

    @Override
    public int getHour() {
        return this.hour;
    }

    @Override
    public int getMinute() {
        return this.minute;
    }

    @Override
    public int getSecond() {
        return this.second;
    }

    public int getMicrosecond() {
        BigDecimal fractionalSeconds = this.getFractionalSecond();
        if (fractionalSeconds == null) {
            return Integer.MIN_VALUE;
        }
        return this.getFractionalSecond().movePointRight(6).intValue();
    }

    @Override
    public BigDecimal getFractionalSecond() {
        if (this.second == Integer.MIN_VALUE) {
            return null;
        }
        return BigDecimal.valueOf(this.microsecond).movePointLeft(6);
    }

    @Override
    public int compare(XMLGregorianCalendar xmlGregorianCalendar) {
        return this.toCalendarValue().getSchemaComparable().compareTo(((SaxonXMLGregorianCalendar)xmlGregorianCalendar).toCalendarValue().getSchemaComparable());
    }

    @Override
    public XMLGregorianCalendar normalize() {
        return new SaxonXMLGregorianCalendar(this.toCalendarValue().adjustTimezone(0));
    }

    @Override
    public String toXMLFormat() {
        return this.toCalendarValue().getStringValue();
    }

    @Override
    public QName getXMLSchemaType() {
        if (this.second == Integer.MIN_VALUE) {
            if (this.year == null) {
                if (this.month == Integer.MIN_VALUE) {
                    return DatatypeConstants.GDAY;
                }
                if (this.day == Integer.MIN_VALUE) {
                    return DatatypeConstants.GMONTH;
                }
                return DatatypeConstants.GMONTHDAY;
            }
            if (this.day == Integer.MIN_VALUE) {
                if (this.month == Integer.MIN_VALUE) {
                    return DatatypeConstants.GYEAR;
                }
                return DatatypeConstants.GYEARMONTH;
            }
            return DatatypeConstants.DATE;
        }
        if (this.year == null) {
            return DatatypeConstants.TIME;
        }
        return DatatypeConstants.DATETIME;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void add(Duration duration) {
        try {
            CalendarValue cv = this.toCalendarValue().add(((SaxonDuration)duration).getDurationValue());
            this.setCalendarValue(cv);
        } catch (XPathException err) {
            throw new IllegalArgumentException(err.getMessage());
        }
    }

    @Override
    public GregorianCalendar toGregorianCalendar() {
        return this.toCalendarValue().getCalendar();
    }

    @Override
    public GregorianCalendar toGregorianCalendar(TimeZone timezone, Locale aLocale, XMLGregorianCalendar defaults) {
        GregorianCalendar gc = new GregorianCalendar(timezone, aLocale);
        gc.setGregorianChange(new Date(Long.MIN_VALUE));
        gc.set(0, this.year == null ? (defaults.getYear() > 0 ? 1 : -1) : this.year.signum());
        gc.set(1, this.year == null ? defaults.getYear() : this.year.abs().intValue());
        gc.set(2, this.month == Integer.MIN_VALUE ? defaults.getMonth() : this.month);
        gc.set(5, this.day == Integer.MIN_VALUE ? defaults.getDay() : this.day);
        gc.set(10, this.hour == Integer.MIN_VALUE ? defaults.getHour() : this.hour);
        gc.set(12, this.minute == Integer.MIN_VALUE ? defaults.getMinute() : this.minute);
        gc.set(13, this.second == Integer.MIN_VALUE ? defaults.getSecond() : this.second);
        gc.set(14, this.microsecond == Integer.MIN_VALUE ? defaults.getMillisecond() : this.microsecond / 1000);
        return gc;
    }

    @Override
    public TimeZone getTimeZone(int defaultZoneoffset) {
        if (this.tzOffset == Integer.MIN_VALUE) {
            if (defaultZoneoffset == Integer.MIN_VALUE) {
                return new GregorianCalendar().getTimeZone();
            }
            return new SimpleTimeZone(defaultZoneoffset * 60000, "XXX");
        }
        return new SimpleTimeZone(this.tzOffset * 60000, "XXX");
    }

    @Override
    public Object clone() {
        SaxonXMLGregorianCalendar s = new SaxonXMLGregorianCalendar();
        s.setYear(this.year);
        s.setMonth(this.month);
        s.setDay(this.day);
        s.setHour(this.hour);
        s.setMinute(this.minute);
        s.setSecond(this.second);
        s.setMillisecond(this.microsecond / 1000);
        s.setTimezone(this.tzOffset);
        return s;
    }

    public CalendarValue toCalendarValue() {
        if (this.calendarValue != null) {
            return this.calendarValue;
        }
        if (this.second == Integer.MIN_VALUE) {
            if (this.year == null) {
                if (this.month == Integer.MIN_VALUE) {
                    return new GDayValue((byte)this.day, this.tzOffset);
                }
                if (this.day == Integer.MIN_VALUE) {
                    return new GMonthValue((byte)this.month, this.tzOffset);
                }
                return new GMonthDayValue((byte)this.month, (byte)this.day, this.tzOffset);
            }
            if (this.day == Integer.MIN_VALUE) {
                if (this.month == Integer.MIN_VALUE) {
                    return new GYearValue(this.year.intValue(), this.tzOffset, true);
                }
                return new GYearMonthValue(this.year.intValue(), (byte)this.month, this.tzOffset, true);
            }
            return new DateValue(this.year.intValue(), (byte)this.month, (byte)this.day, this.tzOffset, true);
        }
        if (this.year == null) {
            return new TimeValue((byte)this.hour, (byte)this.minute, (byte)this.second, this.getMicrosecond() * 1000, this.tzOffset, "");
        }
        return new DateTimeValue(this.year.intValue(), (byte)this.month, (byte)this.day, (byte)this.hour, (byte)this.minute, (byte)this.second, this.getMicrosecond(), this.tzOffset, true);
    }
}


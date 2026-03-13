/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.YearMonthDurationValue;

public class SaxonDuration
extends Duration {
    private DurationValue duration;

    public SaxonDuration(DurationValue duration) {
        this.duration = duration;
    }

    public DurationValue getDurationValue() {
        return this.duration;
    }

    @Override
    public QName getXMLSchemaType() {
        if (this.duration instanceof DayTimeDurationValue) {
            return new QName("http://www.w3.org/2001/XMLSchema", "dayTimeDuration");
        }
        if (this.duration instanceof YearMonthDurationValue) {
            return new QName("http://www.w3.org/2001/XMLSchema", "yearMonthDuration");
        }
        return new QName("http://www.w3.org/2001/XMLSchema", "duration");
    }

    @Override
    public int getSign() {
        return this.duration.signum();
    }

    @Override
    public Number getField(DatatypeConstants.Field field) {
        if (field == DatatypeConstants.YEARS) {
            return BigInteger.valueOf(((Int64Value)this.duration.getComponent(AccessorFn.Component.YEAR)).longValue());
        }
        if (field == DatatypeConstants.MONTHS) {
            return BigInteger.valueOf(((Int64Value)this.duration.getComponent(AccessorFn.Component.MONTH)).longValue());
        }
        if (field == DatatypeConstants.DAYS) {
            return BigInteger.valueOf(((Int64Value)this.duration.getComponent(AccessorFn.Component.DAY)).longValue());
        }
        if (field == DatatypeConstants.HOURS) {
            return BigInteger.valueOf(((Int64Value)this.duration.getComponent(AccessorFn.Component.HOURS)).longValue());
        }
        if (field == DatatypeConstants.MINUTES) {
            return BigInteger.valueOf(((Int64Value)this.duration.getComponent(AccessorFn.Component.MINUTES)).longValue());
        }
        if (field == DatatypeConstants.SECONDS) {
            return ((BigDecimalValue)this.duration.getComponent(AccessorFn.Component.SECONDS)).getDecimalValue();
        }
        throw new IllegalArgumentException("Invalid field");
    }

    @Override
    public boolean isSet(DatatypeConstants.Field field) {
        return true;
    }

    @Override
    public Duration add(Duration rhs) {
        try {
            return new SaxonDuration(this.duration.add(((SaxonDuration)rhs).duration));
        } catch (XPathException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public Duration subtract(Duration rhs) {
        try {
            return new SaxonDuration(this.duration.subtract(((SaxonDuration)rhs).duration));
        } catch (XPathException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public void addTo(Calendar calendar) {
        int sign = this.getSign();
        if (sign == 0) {
            return;
        }
        calendar.add(1, this.getYears() * sign);
        calendar.add(2, this.getMonths() * sign);
        calendar.add(5, this.getDays() * sign);
        calendar.add(11, this.getHours() * sign);
        calendar.add(12, this.getMinutes() * sign);
        calendar.add(13, (int)((Int64Value)this.duration.getComponent(AccessorFn.Component.WHOLE_SECONDS)).longValue() * sign);
        calendar.add(14, (int)((Int64Value)this.duration.getComponent(AccessorFn.Component.MICROSECONDS)).longValue() * sign / 1000);
    }

    @Override
    public Duration multiply(BigDecimal factor) {
        try {
            return new SaxonDuration(this.duration.multiply(factor.doubleValue()));
        } catch (XPathException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public Duration negate() {
        return new SaxonDuration(this.duration.negate());
    }

    @Override
    public Duration normalizeWith(Calendar startTimeInstant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compare(Duration rhs) {
        if (!(rhs instanceof SaxonDuration)) {
            throw new IllegalArgumentException("Supplied duration is not a SaxonDuration");
        }
        Comparable c0 = this.duration.getSchemaComparable();
        Comparable c1 = ((SaxonDuration)rhs).duration.getSchemaComparable();
        return c0.compareTo(c1);
    }

    @Override
    public int hashCode() {
        return this.duration.hashCode();
    }
}


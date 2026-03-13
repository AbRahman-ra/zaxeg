/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.StringTokenizer;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.Whitespace;

public class DurationValue
extends AtomicValue
implements AtomicMatchKey {
    protected boolean negative = false;
    protected int months = 0;
    protected long seconds = 0L;
    protected int nanoseconds = 0;

    protected DurationValue() {
    }

    public DurationValue(boolean positive, int years, int months, int days, int hours, int minutes, long seconds, int microseconds) throws IllegalArgumentException {
        this(positive, years, months, days, hours, minutes, seconds, microseconds, BuiltInAtomicType.DURATION);
    }

    public DurationValue(boolean positive, int years, int months, int days, int hours, int minutes, long seconds, int microseconds, AtomicType type) {
        boolean bl = this.negative = !positive;
        if (years < 0 || months < 0 || days < 0 || hours < 0 || minutes < 0 || seconds < 0L || microseconds < 0) {
            throw new IllegalArgumentException("Negative component value");
        }
        if ((double)years * 12.0 + (double)months > 2.147483647E9) {
            throw new IllegalArgumentException("Duration months limit exceeded");
        }
        if ((double)days * 86400.0 + (double)hours * 3600.0 + (double)minutes * 60.0 + (double)seconds > 9.223372036854776E18) {
            throw new IllegalArgumentException("Duration seconds limit exceeded");
        }
        this.months = years * 12 + months;
        long h = (long)days * 24L + (long)hours;
        long m = h * 60L + (long)minutes;
        this.seconds = m * 60L + seconds;
        this.nanoseconds = microseconds * 1000;
        this.normalizeZeroDuration();
        this.typeLabel = type;
    }

    public DurationValue(int years, int months, int days, int hours, int minutes, long seconds, int nanoseconds, AtomicType type) {
        boolean someNegative;
        boolean somePositive = years > 0 || months > 0 || days > 0 || hours > 0 || minutes > 0 || seconds > 0L || nanoseconds > 0;
        boolean bl = someNegative = years < 0 || months < 0 || days < 0 || hours < 0 || minutes < 0 || seconds < 0L || nanoseconds < 0;
        if (somePositive && someNegative) {
            throw new IllegalArgumentException("Some component values are positive and some negative");
        }
        if (someNegative) {
            years = -years;
            months = -months;
            days = -days;
            hours = -hours;
            minutes = -minutes;
            seconds = -seconds;
            nanoseconds = -nanoseconds;
        }
        if ((double)years * 12.0 + (double)months > 2.147483647E9) {
            throw new IllegalArgumentException("Duration months limit exceeded");
        }
        if ((double)days * 86400.0 + (double)hours * 3600.0 + (double)minutes * 60.0 + (double)seconds > 9.223372036854776E18) {
            throw new IllegalArgumentException("Duration seconds limit exceeded");
        }
        this.months = years * 12 + months;
        long h = (long)days * 24L + (long)hours;
        long m = h * 60L + (long)minutes;
        this.seconds = m * 60L + seconds;
        this.nanoseconds = nanoseconds;
        this.negative = someNegative;
        this.normalizeZeroDuration();
        this.typeLabel = type;
    }

    protected static void formatFractionalSeconds(FastStringBuffer sb, int seconds, long nanosecs) {
        String mss = nanosecs + "";
        if (seconds == 0) {
            mss = "0000000000" + mss;
            mss = mss.substring(mss.length() - 10);
        }
        sb.append(mss.substring(0, mss.length() - 9));
        sb.cat('.');
        int lastSigDigit = mss.length() - 1;
        while (mss.charAt(lastSigDigit) == '0') {
            --lastSigDigit;
        }
        sb.append(mss.substring(mss.length() - 9, lastSigDigit + 1));
        sb.cat('S');
    }

    protected void normalizeZeroDuration() {
        if (this.months == 0 && this.seconds == 0L && this.nanoseconds == 0) {
            this.negative = false;
        }
    }

    public static ConversionResult makeDuration(CharSequence s) {
        return DurationValue.makeDuration(s, true, true);
    }

    protected static ConversionResult makeDuration(CharSequence s, boolean allowYM, boolean allowDT) {
        int years = 0;
        int months = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        int nanoseconds = 0;
        boolean negative = false;
        StringTokenizer tok = new StringTokenizer(Whitespace.trimWhitespace(s).toString(), "-+.PYMDTHS", true);
        int components = 0;
        if (!tok.hasMoreElements()) {
            return DurationValue.badDuration("empty string", s);
        }
        String part = (String)tok.nextElement();
        if ("+".equals(part)) {
            return DurationValue.badDuration("+ sign not allowed in a duration", s);
        }
        if ("-".equals(part)) {
            negative = true;
            part = (String)tok.nextElement();
        }
        if (!"P".equals(part)) {
            return DurationValue.badDuration("missing 'P'", s);
        }
        int state = 0;
        block10: while (tok.hasMoreElements()) {
            int value;
            part = (String)tok.nextElement();
            if ("T".equals(part)) {
                state = 4;
                if (!tok.hasMoreElements()) {
                    return DurationValue.badDuration("T must be followed by time components", s);
                }
                part = (String)tok.nextElement();
            }
            if ((value = DurationValue.simpleInteger(part)) < 0) {
                if (value == -2) {
                    return DurationValue.badDuration("component of duration exceeds Saxon limits", s, "FODT0002");
                }
                return DurationValue.badDuration("invalid or non-numeric component", s);
            }
            if (!tok.hasMoreElements()) {
                return DurationValue.badDuration("missing unit letter at end", s);
            }
            char delim = ((String)tok.nextElement()).charAt(0);
            switch (delim) {
                case 'Y': {
                    if (state > 0) {
                        return DurationValue.badDuration("Y is out of sequence", s);
                    }
                    if (!allowYM) {
                        return DurationValue.badDuration("Year component is not allowed in dayTimeDuration", s);
                    }
                    years = value;
                    state = 1;
                    ++components;
                    continue block10;
                }
                case 'M': {
                    if (state == 4 || state == 5) {
                        if (!allowDT) {
                            return DurationValue.badDuration("Minute component is not allowed in yearMonthDuration", s);
                        }
                        minutes = value;
                        state = 6;
                        ++components;
                        continue block10;
                    }
                    if (state == 0 || state == 1) {
                        if (!allowYM) {
                            return DurationValue.badDuration("Month component is not allowed in dayTimeDuration", s);
                        }
                        months = value;
                        state = 2;
                        ++components;
                        continue block10;
                    }
                    return DurationValue.badDuration("M is out of sequence", s);
                }
                case 'D': {
                    if (state > 2) {
                        return DurationValue.badDuration("D is out of sequence", s);
                    }
                    if (!allowDT) {
                        return DurationValue.badDuration("Day component is not allowed in yearMonthDuration", s);
                    }
                    days = value;
                    state = 3;
                    ++components;
                    continue block10;
                }
                case 'H': {
                    if (state != 4) {
                        return DurationValue.badDuration("H is out of sequence", s);
                    }
                    if (!allowDT) {
                        return DurationValue.badDuration("Hour component is not allowed in yearMonthDuration", s);
                    }
                    hours = value;
                    state = 5;
                    ++components;
                    continue block10;
                }
                case '.': {
                    if (state < 4 || state > 6) {
                        return DurationValue.badDuration("misplaced decimal point", s);
                    }
                    seconds = value;
                    state = 7;
                    continue block10;
                }
                case 'S': {
                    if (state < 4 || state > 7) {
                        return DurationValue.badDuration("S is out of sequence", s);
                    }
                    if (!allowDT) {
                        return DurationValue.badDuration("Seconds component is not allowed in yearMonthDuration", s);
                    }
                    if (state == 7) {
                        StringBuilder frac = new StringBuilder(part);
                        while (frac.length() < 9) {
                            frac.append("0");
                        }
                        part = frac.toString();
                        if (part.length() > 9) {
                            part = part.substring(0, 9);
                        }
                        if ((value = DurationValue.simpleInteger(part)) < 0) {
                            return DurationValue.badDuration("non-numeric fractional seconds", s);
                        }
                        nanoseconds = value;
                    } else {
                        seconds = value;
                    }
                    state = 8;
                    ++components;
                    continue block10;
                }
            }
            return DurationValue.badDuration("misplaced " + delim, s);
        }
        if (components == 0) {
            return DurationValue.badDuration("Duration specifies no components", s);
        }
        if (negative) {
            years = -years;
            months = -months;
            days = -days;
            hours = -hours;
            minutes = -minutes;
            seconds = -seconds;
            nanoseconds = -nanoseconds;
        }
        try {
            return new DurationValue(years, months, days, hours, minutes, (long)seconds, nanoseconds, BuiltInAtomicType.DURATION);
        } catch (IllegalArgumentException err) {
            return new ValidationFailure(err.getMessage());
        }
    }

    protected static ValidationFailure badDuration(String msg, CharSequence s) {
        ValidationFailure err = new ValidationFailure("Invalid duration value '" + s + "' (" + msg + ')');
        err.setErrorCode("FORG0001");
        return err;
    }

    protected static ValidationFailure badDuration(String msg, CharSequence s, String errorCode) {
        ValidationFailure err = new ValidationFailure("Invalid duration value '" + s + "' (" + msg + ')');
        err.setErrorCode(errorCode);
        return err;
    }

    protected static int simpleInteger(String s) {
        long result = 0L;
        int len = s.length();
        if (len == 0) {
            return -1;
        }
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                if ((result = result * 10L + (long)(c - 48)) <= Integer.MAX_VALUE) continue;
                return -2;
            }
            return -1;
        }
        return (int)result;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        if (this.negative) {
            return new DurationValue(0, -this.months, 0, 0, 0, -this.seconds, -this.nanoseconds, typeLabel);
        }
        return new DurationValue(0, this.months, 0, 0, 0, this.seconds, this.nanoseconds, typeLabel);
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.DURATION;
    }

    public int signum() {
        if (this.negative) {
            return -1;
        }
        if (this.months == 0 && this.seconds == 0L && this.nanoseconds == 0) {
            return 0;
        }
        return 1;
    }

    public int getYears() {
        return this.months / 12;
    }

    public int getMonths() {
        return this.months % 12;
    }

    public int getDays() {
        return (int)(this.seconds / 86400L);
    }

    public int getHours() {
        return (int)(this.seconds % 86400L / 3600L);
    }

    public int getMinutes() {
        return (int)(this.seconds % 3600L / 60L);
    }

    public int getSeconds() {
        return (int)(this.seconds % 60L);
    }

    public int getMicroseconds() {
        return this.nanoseconds / 1000;
    }

    public int getNanoseconds() {
        return this.nanoseconds;
    }

    public int getTotalMonths() {
        return this.negative ? -this.months : this.months;
    }

    public BigDecimal getTotalSeconds() {
        BigDecimal dec = new BigDecimal(this.negative ? -this.seconds : this.seconds);
        if (this.nanoseconds != 0) {
            dec = dec.add(new BigDecimal(BigInteger.valueOf(this.negative ? (long)(-this.nanoseconds) : (long)this.nanoseconds), 9));
        }
        return dec;
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        if (this.months == 0 && this.seconds == 0L && this.nanoseconds == 0) {
            return "PT0S";
        }
        FastStringBuffer sb = new FastStringBuffer(32);
        if (this.negative) {
            sb.cat('-');
        }
        int years = this.getYears();
        int months = this.getMonths();
        int days = this.getDays();
        int hours = this.getHours();
        int minutes = this.getMinutes();
        int seconds = this.getSeconds();
        sb.append("P");
        if (years != 0) {
            sb.append(years + "Y");
        }
        if (months != 0) {
            sb.append(months + "M");
        }
        if (days != 0) {
            sb.append(days + "D");
        }
        if (hours != 0 || minutes != 0 || seconds != 0 || this.nanoseconds != 0) {
            sb.append("T");
        }
        if (hours != 0) {
            sb.append(hours + "H");
        }
        if (minutes != 0) {
            sb.append(minutes + "M");
        }
        if (seconds != 0 || this.nanoseconds != 0) {
            if (seconds != 0 && this.nanoseconds == 0) {
                sb.append(seconds + "S");
            } else {
                DurationValue.formatFractionalSeconds(sb, seconds, (long)seconds * 1000000000L + (long)this.nanoseconds);
            }
        }
        return sb;
    }

    public double getLengthInSeconds() {
        double a = (double)this.months * 30.43684991666667 * 24.0 * 60.0 * 60.0 + (double)this.seconds + (double)this.nanoseconds / 1.0E9;
        return this.negative ? -a : a;
    }

    @Override
    public AtomicValue getComponent(AccessorFn.Component component) {
        switch (component) {
            case YEAR: {
                return Int64Value.makeIntegerValue(this.negative ? (long)(-this.getYears()) : (long)this.getYears());
            }
            case MONTH: {
                return Int64Value.makeIntegerValue(this.negative ? (long)(-this.getMonths()) : (long)this.getMonths());
            }
            case DAY: {
                return Int64Value.makeIntegerValue(this.negative ? (long)(-this.getDays()) : (long)this.getDays());
            }
            case HOURS: {
                return Int64Value.makeIntegerValue(this.negative ? (long)(-this.getHours()) : (long)this.getHours());
            }
            case MINUTES: {
                return Int64Value.makeIntegerValue(this.negative ? (long)(-this.getMinutes()) : (long)this.getMinutes());
            }
            case SECONDS: {
                FastStringBuffer sb = new FastStringBuffer(16);
                String ms = "000000000" + this.nanoseconds;
                ms = ms.substring(ms.length() - 9);
                sb.append((this.negative ? "-" : "") + this.getSeconds() + '.' + ms);
                return BigDecimalValue.parse(sb);
            }
            case WHOLE_SECONDS: {
                return Int64Value.makeIntegerValue(this.negative ? -this.seconds : this.seconds);
            }
            case MICROSECONDS: {
                return new Int64Value((this.negative ? -this.nanoseconds : this.nanoseconds) / 1000);
            }
            case NANOSECONDS: {
                return new Int64Value(this.negative ? (long)(-this.nanoseconds) : (long)this.nanoseconds);
            }
        }
        throw new IllegalArgumentException("Unknown component for duration: " + (Object)((Object)component));
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return ordered ? null : this;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DurationValue) {
            DurationValue d1 = this;
            DurationValue d2 = (DurationValue)other;
            return d1.negative == d2.negative && d1.months == d2.months && d1.seconds == d2.seconds && d1.nanoseconds == d2.nanoseconds;
        }
        return false;
    }

    public int hashCode() {
        return Double.valueOf(this.getLengthInSeconds()).hashCode();
    }

    public DurationValue add(DurationValue other) throws XPathException {
        XPathException err = new XPathException("Only subtypes of xs:duration can be added");
        err.setErrorCode("XPTY0004");
        err.setIsTypeError(true);
        throw err;
    }

    public DurationValue subtract(DurationValue other) throws XPathException {
        XPathException err = new XPathException("Only subtypes of xs:duration can be subtracted");
        err.setErrorCode("XPTY0004");
        err.setIsTypeError(true);
        throw err;
    }

    public DurationValue negate() {
        if (this.negative) {
            return new DurationValue(0, this.months, 0, 0, 0, this.seconds, this.nanoseconds, this.typeLabel);
        }
        return new DurationValue(0, -this.months, 0, 0, 0, -this.seconds, -this.nanoseconds, this.typeLabel);
    }

    public DurationValue multiply(long factor) throws XPathException {
        return this.multiply((double)factor);
    }

    public DurationValue multiply(double factor) throws XPathException {
        XPathException err = new XPathException("Only subtypes of xs:duration can be multiplied by a number");
        err.setErrorCode("XPTY0004");
        err.setIsTypeError(true);
        throw err;
    }

    public DurationValue divide(double factor) throws XPathException {
        XPathException err = new XPathException("Only subtypes of xs:duration can be divided by a number");
        err.setErrorCode("XPTY0004");
        err.setIsTypeError(true);
        throw err;
    }

    public BigDecimalValue divide(DurationValue other) throws XPathException {
        XPathException err = new XPathException("Only subtypes of xs:duration can be divided by another duration");
        err.setErrorCode("XPTY0004");
        err.setIsTypeError(true);
        throw err;
    }

    @Override
    public Comparable getSchemaComparable() {
        return DurationValue.getSchemaComparable(this);
    }

    public static Comparable getSchemaComparable(DurationValue value) {
        int m = value.months;
        long s = value.seconds;
        int n = value.nanoseconds;
        if (value.negative) {
            s = -s;
            m = -m;
            n = -n;
        }
        return new DurationComparable(m, s, n);
    }

    private static class DurationComparable
    implements Comparable<DurationComparable> {
        private int months;
        private long seconds;
        private int nanoseconds;

        public DurationComparable(int m, long s, int nanos) {
            this.months = m;
            this.seconds = s;
            this.nanoseconds = nanos;
        }

        @Override
        public int compareTo(DurationComparable other) {
            if (this.months == other.months) {
                if (this.seconds == other.seconds) {
                    return Integer.compare(this.nanoseconds, other.nanoseconds);
                }
                return Long.compare(this.seconds, other.seconds);
            }
            double oneDay = 86400.0;
            double min0 = (double)this.monthsToDaysMinimum(this.months) * oneDay + (double)this.seconds;
            double max0 = (double)this.monthsToDaysMaximum(this.months) * oneDay + (double)this.seconds;
            double min1 = (double)this.monthsToDaysMinimum(other.months) * oneDay + (double)other.seconds;
            double max1 = (double)this.monthsToDaysMaximum(other.months) * oneDay + (double)other.seconds;
            if (max0 < min1) {
                return -1;
            }
            if (min0 > max1) {
                return 1;
            }
            return Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return o instanceof DurationComparable && this.compareTo((DurationComparable)o) == 0;
        }

        public int hashCode() {
            return this.months ^ (int)this.seconds;
        }

        private int monthsToDaysMinimum(int months) {
            if (months < 0) {
                return -this.monthsToDaysMaximum(-months);
            }
            if (months < 12) {
                int[] shortest = new int[]{0, 28, 59, 89, 120, 150, 181, 212, 242, 273, 303, 334};
                return shortest[months];
            }
            int years = months / 12;
            int remainingMonths = months % 12;
            int yearDays = years * 365 + years % 4 - years % 100 + years % 400 - 1;
            return yearDays + this.monthsToDaysMinimum(remainingMonths);
        }

        private int monthsToDaysMaximum(int months) {
            if (months < 0) {
                return -this.monthsToDaysMinimum(-months);
            }
            if (months < 12) {
                int[] longest = new int[]{0, 31, 62, 92, 123, 153, 184, 215, 245, 276, 306, 337};
                return longest[months];
            }
            int years = months / 12;
            int remainingMonths = months % 12;
            int yearDays = years * 365 + years % 4 - years % 100 + years % 400 + 1;
            return yearDays + this.monthsToDaysMaximum(remainingMonths);
        }
    }
}


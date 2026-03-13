/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Arrays;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.number.AbstractNumberer;
import net.sf.saxon.expr.number.Alphanumeric;
import net.sf.saxon.expr.number.NamedTimeZone;
import net.sf.saxon.expr.number.Numberer_en;
import net.sf.saxon.expr.number.NumericGroupFormatter;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.functions.FormatInteger;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Numberer;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.regex.charclass.Categories;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TimeValue;
import net.sf.saxon.value.Whitespace;

public class FormatDate
extends SystemFunction
implements Callable {
    static final String[] knownCalendars = new String[]{"AD", "AH", "AME", "AM", "AP", "AS", "BE", "CB", "CE", "CL", "CS", "EE", "FE", "ISO", "JE", "KE", "KY", "ME", "MS", "NS", "OS", "RS", "SE", "SH", "SS", "TE", "VE", "VS"};
    private static Pattern componentPattern = Pattern.compile("([YMDdWwFHhmsfZzPCE])\\s*(.*)");
    private static Pattern formatPattern = Pattern.compile("([^,]*)(,.*)?");
    private static Pattern widthPattern = Pattern.compile(",(\\*|[0-9]+)(\\-(\\*|[0-9]+))?");
    private static Pattern alphanumericPattern = Pattern.compile("([A-Za-z0-9]|\\p{L}|\\p{N})*");
    private static Pattern digitsPattern = Pattern.compile("\\p{Nd}+");
    private static Pattern digitsOrOptionalDigitsPattern = Pattern.compile("[#\\p{Nd}]+");
    private static Pattern fractionalDigitsPattern = Pattern.compile("\\p{Nd}+#*");

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private CharSequence adjustCalendar(StringValue calendarVal, CharSequence result, XPathContext context) throws XPathException {
        StructuredQName cal;
        try {
            String c = calendarVal.getStringValue();
            cal = StructuredQName.fromLexicalQName(c, false, true, this.getRetainedStaticContext());
        } catch (XPathException e) {
            XPathException err = new XPathException("Invalid calendar name. " + e.getMessage());
            err.setErrorCode("FOFD1340");
            err.setXPathContext(context);
            throw err;
        }
        if (!cal.hasURI("")) return "[Calendar: AD]" + result;
        String calLocal = cal.getLocalPart();
        if (calLocal.equals("AD")) return result;
        if (calLocal.equals("ISO")) return result;
        if (Arrays.binarySearch(knownCalendars, calLocal) >= 0) {
            return "[Calendar: AD]" + result;
        }
        XPathException err = new XPathException("Unknown no-namespace calendar: " + calLocal);
        err.setErrorCode("FOFD1340");
        err.setXPathContext(context);
        throw err;
    }

    private static CharSequence formatDate(CalendarValue value, String format, String language, String place, XPathContext context) throws XPathException {
        TimeZone tz;
        boolean languageDefaulted;
        Configuration config = context.getConfiguration();
        boolean bl = languageDefaulted = language == null;
        if (language == null) {
            language = config.getDefaultLanguage();
        }
        if (place == null) {
            place = config.getDefaultCountry();
        }
        if (value.hasTimezone() && place.contains("/") && (tz = TimeZone.getTimeZone(place)) != null) {
            int milliOffset = tz.getOffset(value.toDateTime().getCalendar().getTime().getTime());
            value = value.adjustTimezone(milliOffset / 60000);
        }
        Numberer numberer = config.makeNumberer(language, place);
        FastStringBuffer sb = new FastStringBuffer(64);
        if (!languageDefaulted && numberer.getClass() == Numberer_en.class && !language.startsWith("en")) {
            sb.append("[Language: en]");
        }
        if (numberer.defaultedLocale() != null) {
            sb.append("[Language: " + numberer.defaultedLocale().getLanguage() + "]");
        }
        int i = 0;
        while (true) {
            int close;
            if (i < format.length() && format.charAt(i) != '[') {
                sb.cat(format.charAt(i));
                if (format.charAt(i) == ']' && (++i == format.length() || format.charAt(i) != ']')) {
                    XPathException e = new XPathException("Closing ']' in date picture must be written as ']]'");
                    e.setErrorCode("FOFD1340");
                    e.setXPathContext(context);
                    throw e;
                }
                ++i;
                continue;
            }
            if (i == format.length()) break;
            if (++i < format.length() && format.charAt(i) == '[') {
                sb.cat('[');
                ++i;
                continue;
            }
            int n = close = i < format.length() ? format.indexOf("]", i) : -1;
            if (close == -1) {
                XPathException e = new XPathException("Date format contains a '[' with no matching ']'");
                e.setErrorCode("FOFD1340");
                e.setXPathContext(context);
                throw e;
            }
            String componentFormat = format.substring(i, close);
            sb.cat(FormatDate.formatComponent(value, Whitespace.removeAllWhitespace(componentFormat), numberer, place, context));
            i = close + 1;
        }
        return sb;
    }

    private static CharSequence formatComponent(CalendarValue value, CharSequence specifier, Numberer numberer, String country, XPathContext context) throws XPathException {
        boolean ignoreDate = value instanceof TimeValue;
        boolean ignoreTime = value instanceof DateValue;
        DateTimeValue dtvalue = value.toDateTime();
        Matcher matcher = componentPattern.matcher(specifier);
        if (!matcher.matches()) {
            XPathException error = new XPathException("Unrecognized date/time component [" + specifier + ']');
            error.setErrorCode("FOFD1340");
            error.setXPathContext(context);
            throw error;
        }
        String component = matcher.group(1);
        String format = matcher.group(2);
        if (format == null) {
            format = "";
        }
        boolean defaultFormat = false;
        if ("".equals(format) || format.startsWith(",")) {
            defaultFormat = true;
            switch (component.charAt(0)) {
                case 'F': {
                    format = "Nn" + format;
                    break;
                }
                case 'P': {
                    format = 'n' + format;
                    break;
                }
                case 'C': 
                case 'E': {
                    format = 'N' + format;
                    break;
                }
                case 'm': 
                case 's': {
                    format = "01" + format;
                    break;
                }
                case 'Z': 
                case 'z': {
                    break;
                }
                default: {
                    format = '1' + format;
                }
            }
        }
        switch (component.charAt(0)) {
            case 'Y': {
                if (ignoreDate) {
                    XPathException error = new XPathException("In format-time(): an xs:time value does not contain a year component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                int year = dtvalue.getYear();
                if (year < 0) {
                    year = 0 - year;
                }
                return FormatDate.formatNumber(component, year, format, defaultFormat, numberer, context);
            }
            case 'M': {
                if (ignoreDate) {
                    XPathException error = new XPathException("In format-time(): an xs:time value does not contain a month component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                byte month = dtvalue.getMonth();
                return FormatDate.formatNumber(component, month, format, defaultFormat, numberer, context);
            }
            case 'D': {
                if (ignoreDate) {
                    XPathException error = new XPathException("In format-time(): an xs:time value does not contain a day component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                byte day = dtvalue.getDay();
                return FormatDate.formatNumber(component, day, format, defaultFormat, numberer, context);
            }
            case 'd': {
                if (ignoreDate) {
                    XPathException error = new XPathException("In format-time(): an xs:time value does not contain a day component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                int day = DateValue.getDayWithinYear(dtvalue.getYear(), dtvalue.getMonth(), dtvalue.getDay());
                return FormatDate.formatNumber(component, day, format, defaultFormat, numberer, context);
            }
            case 'W': {
                if (ignoreDate) {
                    XPathException error = new XPathException("In format-time(): cannot obtain the week number from an xs:time value");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                int week = DateValue.getWeekNumber(dtvalue.getYear(), dtvalue.getMonth(), dtvalue.getDay());
                return FormatDate.formatNumber(component, week, format, defaultFormat, numberer, context);
            }
            case 'w': {
                if (ignoreDate) {
                    XPathException error = new XPathException("In format-time(): cannot obtain the week number from an xs:time value");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                int week = DateValue.getWeekNumberWithinMonth(dtvalue.getYear(), dtvalue.getMonth(), dtvalue.getDay());
                return FormatDate.formatNumber(component, week, format, defaultFormat, numberer, context);
            }
            case 'H': {
                if (ignoreTime) {
                    XPathException error = new XPathException("In format-date(): an xs:date value does not contain an hour component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                Int64Value hour = (Int64Value)value.getComponent(AccessorFn.Component.HOURS);
                assert (hour != null);
                return FormatDate.formatNumber(component, (int)hour.longValue(), format, defaultFormat, numberer, context);
            }
            case 'h': {
                if (ignoreTime) {
                    XPathException error = new XPathException("In format-date(): an xs:date value does not contain an hour component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                Int64Value hour = (Int64Value)value.getComponent(AccessorFn.Component.HOURS);
                assert (hour != null);
                int hr = (int)hour.longValue();
                if (hr > 12) {
                    hr -= 12;
                }
                if (hr == 0) {
                    hr = 12;
                }
                return FormatDate.formatNumber(component, hr, format, defaultFormat, numberer, context);
            }
            case 'm': {
                if (ignoreTime) {
                    XPathException error = new XPathException("In format-date(): an xs:date value does not contain a minutes component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                Int64Value minutes = (Int64Value)value.getComponent(AccessorFn.Component.MINUTES);
                assert (minutes != null);
                return FormatDate.formatNumber(component, (int)minutes.longValue(), format, defaultFormat, numberer, context);
            }
            case 's': {
                if (ignoreTime) {
                    XPathException error = new XPathException("In format-date(): an xs:date value does not contain a seconds component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                IntegerValue seconds = (IntegerValue)value.getComponent(AccessorFn.Component.WHOLE_SECONDS);
                assert (seconds != null);
                return FormatDate.formatNumber(component, (int)seconds.longValue(), format, defaultFormat, numberer, context);
            }
            case 'f': {
                if (ignoreTime) {
                    XPathException error = new XPathException("In format-date(): an xs:date value does not contain a fractional seconds component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                Int64Value micros = (Int64Value)value.getComponent(AccessorFn.Component.MICROSECONDS);
                assert (micros != null);
                return FormatDate.formatNumber(component, (int)micros.longValue(), format, defaultFormat, numberer, context);
            }
            case 'Z': 
            case 'z': {
                DateTimeValue dtv;
                if (value instanceof TimeValue) {
                    int tzoffset;
                    DateTimeValue now = DateTimeValue.getCurrentDateTime(context);
                    int year = now.getYear();
                    DateTimeValue baseDate = new DateTimeValue(year, 1, 1, 0, 0, 0, 0, tzoffset = value.getTimezoneInMinutes(), false);
                    Boolean b = NamedTimeZone.inSummerTime(baseDate, country);
                    if (b != null && b.booleanValue()) {
                        baseDate = new DateTimeValue(year, 7, 1, 0, 0, 0, 0, tzoffset, false);
                    }
                    dtv = DateTimeValue.makeDateTimeValue(baseDate.toDateValue(), (TimeValue)value);
                } else {
                    dtv = value.toDateTime();
                }
                return FormatDate.formatTimeZone(dtv, component.charAt(0), format, country);
            }
            case 'F': {
                if (ignoreDate) {
                    XPathException error = new XPathException("In format-time(): an xs:time value does not contain day-of-week component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                int day = DateValue.getDayOfWeek(dtvalue.getYear(), dtvalue.getMonth(), dtvalue.getDay());
                return FormatDate.formatNumber(component, day, format, defaultFormat, numberer, context);
            }
            case 'P': {
                if (ignoreTime) {
                    XPathException error = new XPathException("In format-date(): an xs:date value does not contain an am/pm component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                int minuteOfDay = dtvalue.getHour() * 60 + dtvalue.getMinute();
                return FormatDate.formatNumber(component, minuteOfDay, format, defaultFormat, numberer, context);
            }
            case 'C': {
                return numberer.getCalendarName("AD");
            }
            case 'E': {
                if (ignoreDate) {
                    XPathException error = new XPathException("In format-time(): an xs:time value does not contain an AD/BC component");
                    error.setErrorCode("FOFD1350");
                    error.setXPathContext(context);
                    throw error;
                }
                int year = dtvalue.getYear();
                return numberer.getEraName(year);
            }
        }
        XPathException e = new XPathException("Unknown format-date/time component specifier '" + format.charAt(0) + '\'');
        e.setErrorCode("FOFD1340");
        e.setXPathContext(context);
        throw e;
    }

    private static CharSequence formatNumber(String component, int value, String format, boolean defaultFormat, Numberer numberer, XPathContext context) throws XPathException {
        int len;
        NumericGroupFormatter picGroupFormat;
        int len2;
        int comma = format.lastIndexOf(44);
        String widths = "";
        if (comma >= 0) {
            widths = format.substring(comma);
            format = format.substring(0, comma);
        }
        String primary = format;
        String modifier = null;
        if (primary.endsWith("t")) {
            primary = primary.substring(0, primary.length() - 1);
            modifier = "t";
        } else if (primary.endsWith("o")) {
            primary = primary.substring(0, primary.length() - 1);
            modifier = "o";
        }
        String letterValue = "t".equals(modifier) ? "traditional" : null;
        String ordinal = "o".equals(modifier) ? numberer.getOrdinalSuffixForDateTime(component) : null;
        int min = 1;
        int max = Integer.MAX_VALUE;
        if (digitsPattern.matcher(primary).matches() && (len2 = StringValue.getStringLength(primary)) > 1) {
            min = len2;
            max = len2;
        }
        if ("Y".equals(component)) {
            max = 0;
            min = 0;
            if (!widths.isEmpty()) {
                max = FormatDate.getWidths(widths)[1];
            } else if (digitsPattern.matcher(primary).find()) {
                UnicodeString uPrimary = UnicodeString.makeUnicodeString(primary);
                for (int i = 0; i < uPrimary.uLength(); ++i) {
                    int c = uPrimary.uCharAt(i);
                    if (c == 35) {
                        ++max;
                        continue;
                    }
                    if ((c < 48 || c > 57) && !Categories.ESCAPE_d.test(c)) continue;
                    ++min;
                    ++max;
                }
            }
            if (max <= 1) {
                max = Integer.MAX_VALUE;
            }
            if (max < 4 || max < Integer.MAX_VALUE && value > 9999) {
                value %= (int)Math.pow(10.0, max);
            }
        }
        if (primary.equals("I") || primary.equals("i")) {
            int[] range = FormatDate.getWidths(widths);
            min = range[0];
            String roman = numberer.format(value, UnicodeString.makeUnicodeString(primary), null, letterValue, ordinal);
            StringBuilder s = new StringBuilder(roman);
            for (int len3 = StringValue.getStringLength(roman); len3 < min; ++len3) {
                s.append(' ');
            }
            return s.toString();
        }
        if (!widths.isEmpty()) {
            int[] range = FormatDate.getWidths(widths);
            min = Math.max(min, range[0]);
            max = max == Integer.MAX_VALUE ? range[1] : Math.max(max, range[1]);
            if (defaultFormat && primary.endsWith("1") && min != primary.length()) {
                FastStringBuffer sb = new FastStringBuffer(min + 1);
                for (int i = 1; i < min; ++i) {
                    sb.cat('0');
                }
                sb.cat('1');
                primary = sb.toString();
            }
        }
        if ("P".equals(component)) {
            if (!("N".equals(primary) || "n".equals(primary) || "Nn".equals(primary))) {
                primary = "n";
            }
            if (max == Integer.MAX_VALUE) {
                max = 4;
            }
        } else if ("Y".equals(component)) {
            if (max < Integer.MAX_VALUE) {
                value %= (int)Math.pow(10.0, max);
            }
        } else if ("f".equals(component)) {
            StringBuilder s;
            UnicodeString uFormat = UnicodeString.makeUnicodeString(format);
            if (!digitsPattern.matcher(primary).find()) {
                return FormatDate.formatNumber(component, value, "1", defaultFormat, numberer, context);
            }
            if (!digitsOrOptionalDigitsPattern.matcher(primary).matches()) {
                UnicodeString reverseFormat = FormatDate.reverse(uFormat);
                UnicodeString reverseValue = FormatDate.reverse(UnicodeString.makeUnicodeString("" + value));
                CharSequence reverseResult = FormatDate.formatNumber("s", Integer.parseInt(reverseValue.toString()), reverseFormat.toString(), false, numberer, context);
                UnicodeString correctedResult = FormatDate.reverse(UnicodeString.makeUnicodeString(reverseResult));
                if (correctedResult.uLength() > max) {
                    correctedResult = correctedResult.uSubstring(0, max);
                }
                return correctedResult.toString();
            }
            if (!fractionalDigitsPattern.matcher(primary).matches()) {
                throw new XPathException("Invalid picture for fractional seconds: " + primary, "FOFD1340");
            }
            if (value == 0) {
                s = new StringBuilder("0");
            } else {
                s = new StringBuilder((1000000 + value + "").substring(1));
                if (s.length() > max) {
                    s = new StringBuilder(s.substring(0, max));
                }
            }
            while (s.length() < min) {
                s.append('0');
            }
            while (s.length() > min && s.charAt(s.length() - 1) == '0') {
                s = new StringBuilder(s.substring(0, s.length() - 1));
            }
            int zeroDigit = Alphanumeric.getDigitFamily(uFormat.uCharAt(0));
            if (zeroDigit >= 0 && zeroDigit != 48) {
                int[] digits = new int[10];
                for (int z = 0; z <= 9; ++z) {
                    digits[z] = zeroDigit + z;
                }
                long n = Long.parseLong(s.toString());
                int requiredLength = s.length();
                s = new StringBuilder(AbstractNumberer.convertDigitSystem(n, digits, requiredLength).toString());
            }
            return s.toString();
        }
        if ("N".equals(primary) || "n".equals(primary) || "Nn".equals(primary)) {
            String s = "";
            if ("M".equals(component)) {
                s = numberer.monthName(value, min, max);
            } else if ("F".equals(component)) {
                s = numberer.dayName(value, min, max);
            } else if ("P".equals(component)) {
                s = numberer.halfDayName(value, min, max);
            } else {
                primary = "1";
            }
            if ("N".equals(primary)) {
                return s.toUpperCase();
            }
            if ("n".equals(primary)) {
                return s.toLowerCase();
            }
            return s;
        }
        try {
            picGroupFormat = FormatInteger.getPicSeparators(primary);
        } catch (XPathException e) {
            if ("FODF1310".equals(e.getErrorCodeLocalPart())) {
                e.setErrorCode("FOFD1340");
            }
            throw e;
        }
        UnicodeString adjustedPicture = picGroupFormat.getAdjustedPicture();
        String s = numberer.format(value, adjustedPicture, picGroupFormat, letterValue, ordinal);
        if (len < min) {
            int zeroDigit = Alphanumeric.getDigitFamily(adjustedPicture.uCharAt(0));
            FastStringBuffer fsb = new FastStringBuffer(s);
            for (len = StringValue.getStringLength(s); len < min; ++len) {
                fsb.prependWideChar(zeroDigit);
            }
            s = fsb.toString();
        }
        return s;
    }

    private static UnicodeString reverse(UnicodeString in) {
        int[] out = new int[in.uLength()];
        int i = in.uLength() - 1;
        int j = 0;
        while (i >= 0) {
            out[j] = in.uCharAt(i);
            --i;
            ++j;
        }
        return UnicodeString.makeUnicodeString(out);
    }

    private static int[] getWidths(String widths) throws XPathException {
        try {
            int min = -1;
            int max = -1;
            if (!"".equals(widths)) {
                Matcher widthMatcher = widthPattern.matcher(widths);
                if (widthMatcher.matches()) {
                    String smin = widthMatcher.group(1);
                    min = smin == null || "".equals(smin) || "*".equals(smin) ? 1 : Integer.parseInt(smin);
                    String smax = widthMatcher.group(3);
                    max = smax == null || "".equals(smax) || "*".equals(smax) ? Integer.MAX_VALUE : Integer.parseInt(smax);
                    if (min < 1) {
                        throw new XPathException("Invalid min value in format picture " + Err.wrap(widths, 4), "FOFD1340");
                    }
                    if (max < 1 || max < min) {
                        throw new XPathException("Invalid max value in format picture " + Err.wrap(widths, 4), "FOFD1340");
                    }
                } else {
                    throw new XPathException("Unrecognized width specifier in format picture " + Err.wrap(widths, 4), "FOFD1340");
                }
            }
            if (min > max) {
                XPathException e = new XPathException("Minimum width in date/time picture exceeds maximum width");
                e.setErrorCode("FOFD1340");
                throw e;
            }
            int[] result = new int[]{min, max};
            return result;
        } catch (NumberFormatException err) {
            XPathException e = new XPathException("Invalid integer used as width in date/time picture");
            e.setErrorCode("FOFD1340");
            throw e;
        }
    }

    private static String formatTimeZone(DateTimeValue value, char component, String format, String country) throws XPathException {
        int[] expandedFormat;
        int comma = format.lastIndexOf(44);
        String widthModifier = "";
        if (comma >= 0) {
            widthModifier = format.substring(comma);
            format = format.substring(0, comma);
        }
        if (!value.hasTimezone()) {
            if (format.equals("Z")) {
                return "J";
            }
            return "";
        }
        if (format.isEmpty() && !widthModifier.isEmpty()) {
            int[] widths = FormatDate.getWidths(widthModifier);
            int min = widths[0];
            int max = widths[1];
            format = min <= 1 ? (max >= 4 ? "0:00" : "0") : (min <= 4 ? (max >= 5 ? "00:00" : "00") : "00:00");
        }
        if (format.isEmpty()) {
            format = "00:00";
        }
        int tz = value.getTimezoneInMinutes();
        boolean useZforZero = format.endsWith("t");
        if (useZforZero && tz == 0) {
            return "Z";
        }
        if (useZforZero) {
            format = format.substring(0, format.length() - 1);
        }
        int digits = 0;
        int separators = 0;
        int separatorChar = 58;
        int zeroDigit = -1;
        for (int ch : expandedFormat = StringValue.expand(format)) {
            if (Character.isDigit(ch)) {
                ++digits;
                if (zeroDigit >= 0) continue;
                zeroDigit = Alphanumeric.getDigitFamily(ch);
                continue;
            }
            ++separators;
            separatorChar = ch;
        }
        int[] buffer = new int[10];
        int used = 0;
        if (digits > 0) {
            int hourDigits;
            if (component == 'z') {
                buffer[0] = 71;
                buffer[1] = 77;
                buffer[2] = 84;
                used = 3;
            }
            boolean negative = tz < 0;
            tz = Math.abs(tz);
            buffer[used++] = negative ? 45 : 43;
            int hour = tz / 60;
            int minute = tz % 60;
            boolean includeMinutes = minute != 0 || digits >= 3 || separators > 0;
            boolean includeSep = minute != 0 && digits <= 2 || separators > 0 && (minute != 0 || digits >= 3);
            int n = hourDigits = digits <= 2 ? digits : digits - 2;
            if (hour > 9 || hourDigits >= 2) {
                buffer[used++] = zeroDigit + hour / 10;
            }
            buffer[used++] = hour % 10 + zeroDigit;
            if (includeSep) {
                buffer[used++] = separatorChar;
            }
            if (includeMinutes) {
                buffer[used++] = minute / 10 + zeroDigit;
                buffer[used++] = minute % 10 + zeroDigit;
            }
            return StringValue.contract(buffer, used).toString();
        }
        if (format.equals("Z")) {
            int hour = tz / 60;
            int minute = tz % 60;
            if (hour < -12 || hour > 12 || minute != 0) {
                return FormatDate.formatTimeZone(value, 'Z', "00:00", country);
            }
            return Character.toString("YXWVUTSRQPONZABCDEFGHIKLM".charAt(hour + 12));
        }
        if (format.charAt(0) == 'N' || format.charAt(0) == 'n') {
            return FormatDate.getNamedTimeZone(value, country, format);
        }
        return FormatDate.formatTimeZone(value, 'Z', "00:00", country);
    }

    private static String getNamedTimeZone(DateTimeValue value, String country, String format) throws XPathException {
        int min = 1;
        int comma = format.indexOf(44);
        if (comma > 0) {
            String widths = format.substring(comma);
            int[] range = FormatDate.getWidths(widths);
            min = range[0];
        }
        if (format.charAt(0) == 'N' || format.charAt(0) == 'n') {
            if (min <= 5) {
                String tzname = NamedTimeZone.getTimeZoneNameForDate(value, country);
                if (format.charAt(0) == 'n') {
                    tzname = tzname.toLowerCase();
                }
                return tzname;
            }
            return NamedTimeZone.getOlsenTimeZoneName(value, country);
        }
        FastStringBuffer sbz = new FastStringBuffer(8);
        value.appendTimezone(sbz);
        return sbz.toString();
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        TimeZone zone;
        String place;
        CalendarValue value = (CalendarValue)arguments[0].head();
        if (value == null) {
            return ZeroOrOne.empty();
        }
        String format = arguments[1].head().getStringValue();
        StringValue calendarVal = null;
        AtomicValue countryVal = null;
        AtomicValue languageVal = null;
        if (this.getArity() > 2) {
            languageVal = (StringValue)arguments[2].head();
            calendarVal = (StringValue)arguments[3].head();
            countryVal = (StringValue)arguments[4].head();
        }
        String language = languageVal == null ? null : languageVal.getStringValue();
        String string = place = countryVal == null ? null : countryVal.getStringValue();
        if (place != null && place.contains("/") && value.hasTimezone() && !(value instanceof TimeValue) && (zone = NamedTimeZone.getNamedTimeZone(place)) != null) {
            int offset = zone.getOffset(value.toDateTime().getCalendar().getTimeInMillis());
            value = value.adjustTimezone(offset / 60000);
        }
        CharSequence result = FormatDate.formatDate(value, format, language, place, context);
        if (calendarVal != null) {
            result = this.adjustCalendar(calendarVal, result, context);
        }
        return new ZeroOrOne<StringValue>(new StringValue(result));
    }
}


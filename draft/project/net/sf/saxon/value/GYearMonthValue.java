/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.GDateValue;
import net.sf.saxon.value.Whitespace;

public class GYearMonthValue
extends GDateValue {
    private static Pattern regex = Pattern.compile("(-?[0-9]+-[0-9][0-9])(Z|[+-][0-9][0-9]:[0-9][0-9])?");

    private GYearMonthValue() {
    }

    public static ConversionResult makeGYearMonthValue(CharSequence value, ConversionRules rules) {
        Matcher m = regex.matcher(Whitespace.trimWhitespace(value));
        if (!m.matches()) {
            return new ValidationFailure("Cannot convert '" + value + "' to a gYearMonth");
        }
        GYearMonthValue g = new GYearMonthValue();
        String base = m.group(1);
        String tz = m.group(2);
        String date = base + "-01" + (tz == null ? "" : tz);
        g.typeLabel = BuiltInAtomicType.G_YEAR_MONTH;
        return GYearMonthValue.setLexicalValue(g, date, rules.isAllowYearZero());
    }

    public GYearMonthValue(int year, byte month, int tz, boolean xsd10) {
        this(year, month, tz, BuiltInAtomicType.G_YEAR_MONTH);
        this.hasNoYearZero = xsd10;
    }

    public GYearMonthValue(int year, byte month, int tz, AtomicType type) {
        this.year = year;
        this.month = month;
        this.day = 1;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = type;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        GYearMonthValue v = new GYearMonthValue(this.year, this.month, this.getTimezoneInMinutes(), this.hasNoYearZero);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.G_YEAR_MONTH;
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        FastStringBuffer sb = new FastStringBuffer(16);
        int yr = this.year;
        if (this.year <= 0 && (yr = -yr + (this.hasNoYearZero ? 1 : 0)) != 0) {
            sb.cat('-');
        }
        GYearMonthValue.appendString(sb, yr, yr > 9999 ? (yr + "").length() : 4);
        sb.cat('-');
        GYearMonthValue.appendTwoDigits(sb, this.month);
        if (this.hasTimezone()) {
            this.appendTimezone(sb);
        }
        return sb;
    }

    @Override
    public CalendarValue add(DurationValue duration) throws XPathException {
        XPathException err = new XPathException("Cannot add a duration to an xs:gYearMonth");
        err.setErrorCode("XPTY0004");
        throw err;
    }

    @Override
    public CalendarValue adjustTimezone(int tz) {
        DateTimeValue dt = this.toDateTime().adjustTimezone(tz);
        return new GYearMonthValue(dt.getYear(), dt.getMonth(), dt.getTimezoneInMinutes(), this.hasNoYearZero);
    }
}


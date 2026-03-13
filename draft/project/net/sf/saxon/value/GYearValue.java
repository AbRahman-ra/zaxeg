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

public class GYearValue
extends GDateValue {
    private static Pattern regex = Pattern.compile("(-?[0-9]+)(Z|[+-][0-9][0-9]:[0-9][0-9])?");

    private GYearValue() {
    }

    public static ConversionResult makeGYearValue(CharSequence value, ConversionRules rules) {
        GYearValue g = new GYearValue();
        Matcher m = regex.matcher(Whitespace.trimWhitespace(value));
        if (!m.matches()) {
            return new ValidationFailure("Cannot convert '" + value + "' to a gYear");
        }
        String base = m.group(1);
        String tz = m.group(2);
        String date = base + "-01-01" + (tz == null ? "" : tz);
        g.typeLabel = BuiltInAtomicType.G_YEAR;
        return GYearValue.setLexicalValue(g, date, rules.isAllowYearZero());
    }

    public GYearValue(int year, int tz, boolean xsd10) {
        this(year, tz, BuiltInAtomicType.G_YEAR);
        this.hasNoYearZero = xsd10;
    }

    public GYearValue(int year, int tz, AtomicType type) {
        this.year = year;
        this.month = 1;
        this.day = 1;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = type;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        GYearValue v = new GYearValue(this.year, this.getTimezoneInMinutes(), true);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.G_YEAR;
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        FastStringBuffer sb = new FastStringBuffer(16);
        int yr = this.year;
        if (this.year <= 0 && (yr = -yr + (this.hasNoYearZero ? 1 : 0)) != 0) {
            sb.cat('-');
        }
        GYearValue.appendString(sb, yr, yr > 9999 ? (yr + "").length() : 4);
        if (this.hasTimezone()) {
            this.appendTimezone(sb);
        }
        return sb;
    }

    @Override
    public CalendarValue add(DurationValue duration) throws XPathException {
        XPathException err = new XPathException("Cannot add a duration to an xs:gYear");
        err.setErrorCode("XPTY0004");
        throw err;
    }

    @Override
    public CalendarValue adjustTimezone(int tz) {
        DateTimeValue dt = this.toDateTime().adjustTimezone(tz);
        return new GYearValue(dt.getYear(), dt.getTimezoneInMinutes(), this.hasNoYearZero);
    }
}


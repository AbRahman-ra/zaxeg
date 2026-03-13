/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class GMonthValue
extends GDateValue {
    private static Pattern regex = Pattern.compile("--([0-9][0-9])(Z|[+-][0-9][0-9]:[0-9][0-9])?");

    private GMonthValue() {
    }

    public static ConversionResult makeGMonthValue(CharSequence value) {
        GMonthValue g = new GMonthValue();
        Matcher m = regex.matcher(Whitespace.trimWhitespace(value));
        if (!m.matches()) {
            return new ValidationFailure("Cannot convert '" + value + "' to a gMonth");
        }
        String base = m.group(1);
        String tz = m.group(2);
        String date = "2000-" + base + "-01" + (tz == null ? "" : tz);
        g.typeLabel = BuiltInAtomicType.G_MONTH;
        return GMonthValue.setLexicalValue(g, date, true);
    }

    public GMonthValue(byte month, int tz) {
        this(month, tz, BuiltInAtomicType.G_MONTH);
    }

    public GMonthValue(byte month, int tz, AtomicType type) {
        this.year = 2000;
        this.month = month;
        this.day = 1;
        this.setTimezoneInMinutes(tz);
        this.typeLabel = type;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        GMonthValue v = new GMonthValue(this.month, this.getTimezoneInMinutes());
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.G_MONTH;
    }

    @Override
    public CharSequence getPrimitiveStringValue() {
        FastStringBuffer sb = new FastStringBuffer(16);
        sb.append("--");
        GMonthValue.appendTwoDigits(sb, this.month);
        if (this.hasTimezone()) {
            this.appendTimezone(sb);
        }
        return sb;
    }

    @Override
    public CalendarValue add(DurationValue duration) throws XPathException {
        XPathException err = new XPathException("Cannot add a duration to an xs:gMonth");
        err.setErrorCode("XPTY0004");
        throw err;
    }

    @Override
    public CalendarValue adjustTimezone(int tz) {
        DateTimeValue dt = this.toDateTime().adjustTimezone(tz);
        return new GMonthValue(dt.getMonth(), dt.getTimezoneInMinutes());
    }
}


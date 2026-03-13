/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Locale;
import net.sf.saxon.expr.number.NumericGroupFormatter;
import net.sf.saxon.regex.UnicodeString;

public interface Numberer {
    public void setCountry(String var1);

    public String getCountry();

    public Locale defaultedLocale();

    public String format(long var1, UnicodeString var3, int var4, String var5, String var6, String var7);

    public String format(long var1, UnicodeString var3, NumericGroupFormatter var4, String var5, String var6);

    public String monthName(int var1, int var2, int var3);

    public String dayName(int var1, int var2, int var3);

    public String halfDayName(int var1, int var2, int var3);

    public String getOrdinalSuffixForDateTime(String var1);

    public String getEraName(int var1);

    public String getCalendarName(String var1);
}


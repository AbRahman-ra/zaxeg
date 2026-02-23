/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.number;

import net.sf.saxon.expr.number.NumericGroupFormatter;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.tree.util.FastStringBuffer;

public class RegularGroupFormatter
extends NumericGroupFormatter {
    private int groupSize;
    private String groupSeparator;

    public RegularGroupFormatter(int grpSize, String grpSep, UnicodeString adjustedPicture) {
        this.groupSize = grpSize;
        this.groupSeparator = grpSep;
        this.adjustedPicture = adjustedPicture;
    }

    @Override
    public String format(FastStringBuffer value) {
        if (this.groupSize > 0 && this.groupSeparator.length() > 0) {
            UnicodeString valueEx = UnicodeString.makeUnicodeString(value);
            FastStringBuffer temp = new FastStringBuffer(16);
            int i = valueEx.uLength() - 1;
            int j = 0;
            while (i >= 0) {
                if (j != 0 && j % this.groupSize == 0) {
                    temp.prepend(this.groupSeparator);
                }
                temp.prependWideChar(valueEx.uCharAt(i));
                --i;
                ++j;
            }
            return temp.toString();
        }
        return value.toString();
    }

    @Override
    public String getSeparator() {
        return this.groupSeparator;
    }
}


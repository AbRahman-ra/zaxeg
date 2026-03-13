/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.number;

import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.tree.util.FastStringBuffer;

public abstract class NumericGroupFormatter {
    protected UnicodeString adjustedPicture;

    public UnicodeString getAdjustedPicture() {
        return this.adjustedPicture;
    }

    public abstract String format(FastStringBuffer var1);

    public abstract String getSeparator();
}


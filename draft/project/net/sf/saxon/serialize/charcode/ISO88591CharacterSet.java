/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.charcode;

import net.sf.saxon.serialize.charcode.CharacterSet;

public class ISO88591CharacterSet
implements CharacterSet {
    private static ISO88591CharacterSet theInstance = new ISO88591CharacterSet();

    private ISO88591CharacterSet() {
    }

    public static ISO88591CharacterSet getInstance() {
        return theInstance;
    }

    @Override
    public final boolean inCharset(int c) {
        return c <= 255;
    }

    @Override
    public String getCanonicalName() {
        return "ISO-8859-1";
    }
}


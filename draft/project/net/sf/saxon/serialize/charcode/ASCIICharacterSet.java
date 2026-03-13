/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.charcode;

import net.sf.saxon.serialize.charcode.CharacterSet;

public class ASCIICharacterSet
implements CharacterSet {
    public static final ASCIICharacterSet theInstance = new ASCIICharacterSet();

    private ASCIICharacterSet() {
    }

    public static ASCIICharacterSet getInstance() {
        return theInstance;
    }

    @Override
    public final boolean inCharset(int c) {
        return c <= 127;
    }

    @Override
    public String getCanonicalName() {
        return "US-ASCII";
    }
}


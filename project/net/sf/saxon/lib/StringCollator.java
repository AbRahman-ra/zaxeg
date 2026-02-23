/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.expr.sort.AtomicMatchKey;

public interface StringCollator {
    public String getCollationURI();

    public int compareStrings(CharSequence var1, CharSequence var2);

    public boolean comparesEqual(CharSequence var1, CharSequence var2);

    public AtomicMatchKey getCollationKey(CharSequence var1);
}


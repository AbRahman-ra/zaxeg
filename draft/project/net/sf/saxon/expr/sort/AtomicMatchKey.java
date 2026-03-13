/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;

public interface AtomicMatchKey {
    public static final AtomicMatchKey NaN_MATCH_KEY = new QNameValue("", "http://saxon.sf.net/", "+NaN+");

    public AtomicValue asAtomic();
}


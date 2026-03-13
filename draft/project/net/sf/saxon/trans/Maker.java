/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.trans.XPathException;

public interface Maker<T> {
    public T make() throws XPathException;
}


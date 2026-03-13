/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.trans.XPathException;

public abstract class TuplePull {
    public abstract boolean nextTuple(XPathContext var1) throws XPathException;

    public void close() {
    }
}


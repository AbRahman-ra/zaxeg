/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.trans.XPathException;

public class ComparisonException
extends ClassCastException {
    XPathException cause;

    public ComparisonException(XPathException cause) {
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return this.cause.getMessage();
    }

    @Override
    public XPathException getCause() {
        return this.cause;
    }
}


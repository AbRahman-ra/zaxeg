/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.trans.XPathException;

public class UncheckedXPathException
extends RuntimeException {
    private XPathException cause;

    public UncheckedXPathException(XPathException cause) {
        this.cause = cause;
    }

    public XPathException getXPathException() {
        return this.cause;
    }

    @Override
    public String getMessage() {
        return this.cause.getMessage();
    }
}


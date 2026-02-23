/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class ErrorIterator
implements SequenceIterator {
    private XPathException exception;

    public ErrorIterator(XPathException exception) {
        this.exception = exception;
    }

    @Override
    public Item next() throws XPathException {
        throw this.exception;
    }

    @Override
    public void close() {
    }
}


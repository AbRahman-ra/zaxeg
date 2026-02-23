/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.trans.XPathException;

public abstract class TuplePush {
    private Outputter outputter;

    protected TuplePush(Outputter outputter) {
        this.outputter = outputter;
    }

    protected Outputter getOutputter() {
        return this.outputter;
    }

    public abstract void processTuple(XPathContext var1) throws XPathException;

    public void close() throws XPathException {
    }
}


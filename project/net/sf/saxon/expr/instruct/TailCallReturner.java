/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.trans.XPathException;

public interface TailCallReturner {
    public TailCall processLeavingTail(Outputter var1, XPathContext var2) throws XPathException;
}


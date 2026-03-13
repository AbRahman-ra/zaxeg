/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public abstract class ContextAccessorFunction
extends SystemFunction {
    public abstract Function bindContext(XPathContext var1) throws XPathException;

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.bindContext(context).call(context, arguments);
    }
}


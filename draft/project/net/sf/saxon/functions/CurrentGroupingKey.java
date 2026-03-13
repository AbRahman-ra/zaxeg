/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CurrentGroupingKeyCall;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class CurrentGroupingKey
extends SystemFunction {
    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        return new CurrentGroupingKeyCall();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        throw new XPathException("Dynamic call on current-grouping-key() fails (the current group is absent)", "XTDE1071");
    }
}


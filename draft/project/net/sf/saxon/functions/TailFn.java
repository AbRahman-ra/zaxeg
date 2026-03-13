/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.TailExpression;
import net.sf.saxon.expr.TailIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;

public class TailFn
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(TailIterator.make(arguments[0].iterate(), 2));
    }

    @Override
    public TailExpression makeFunctionCall(Expression[] arguments) {
        return new TailExpression(arguments[0], 2);
    }
}


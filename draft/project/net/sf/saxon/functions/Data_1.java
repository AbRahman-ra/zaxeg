/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;

public class Data_1
extends SystemFunction {
    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        return Atomizer.makeAtomizer(arguments[0], null);
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Sequence arg = arguments[0];
        if (arg instanceof Item) {
            return ((Item)arg).atomize();
        }
        SequenceIterator a = Atomizer.getAtomizingIterator(arg.iterate(), false);
        return SequenceTool.toLazySequence(a);
    }
}


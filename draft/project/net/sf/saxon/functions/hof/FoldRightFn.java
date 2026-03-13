/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.Reverse;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public class FoldRightFn
extends SystemFunction {
    @Override
    public ItemType getResultItemType(Expression[] args) {
        ItemType functionArgType = args[2].getItemType();
        if (functionArgType instanceof AnyFunctionType) {
            return ((AnyFunctionType)functionArgType).getResultType().getPrimaryType();
        }
        return AnyItemType.getInstance();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.evalFoldRight((Function)arguments[2].head(), arguments[1].materialize(), arguments[0].iterate(), context);
    }

    private Sequence evalFoldRight(Function function, Sequence zero, SequenceIterator base, XPathContext context) throws XPathException {
        Item item;
        SequenceIterator reverseBase = Reverse.getReverseIterator(base);
        Sequence[] args = new Sequence[2];
        while ((item = reverseBase.next()) != null) {
            args[0] = item;
            args[1] = zero.materialize();
            try {
                zero = FoldRightFn.dynamicCall(function, context, args);
            } catch (XPathException e) {
                e.maybeSetContext(context);
                throw e;
            }
        }
        return zero;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;

public class CallableWithBoundFocus
implements Callable {
    private Callable target;
    private XPathContext boundContext;

    public CallableWithBoundFocus(Callable target, XPathContext context) {
        this.target = target;
        this.boundContext = context.newContext();
        if (context.getCurrentIterator() == null) {
            this.boundContext.setCurrentIterator(null);
        } else {
            ManualIterator iter = new ManualIterator(context.getContextItem(), context.getCurrentIterator().position());
            iter.setLastPositionFinder(context::getLast);
            this.boundContext.setCurrentIterator(iter);
        }
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.target.call(this.boundContext, arguments);
    }
}


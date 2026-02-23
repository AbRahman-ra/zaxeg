/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.type.FunctionItemType;

public class SystemFunctionWithBoundContextItem
extends AbstractFunction {
    private SystemFunction target;
    private Item contextItem;

    public SystemFunctionWithBoundContextItem(SystemFunction target, XPathContext context) {
        this.target = target;
        Item contextItem = context.getContextItem();
        if (contextItem instanceof NodeInfo && contextItem.isStreamed()) {
            contextItem = null;
        }
        this.contextItem = contextItem;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        XPathContextMinor c2 = context.newMinorContext();
        c2.setCurrentIterator(new ManualIterator(this.contextItem));
        return this.target.call(c2, arguments);
    }

    @Override
    public int getArity() {
        return this.target.getArity();
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        return this.target.getFunctionItemType();
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.target.getFunctionName();
    }

    @Override
    public String getDescription() {
        return this.target.getDescription();
    }
}


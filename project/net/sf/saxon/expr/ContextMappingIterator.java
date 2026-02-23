/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.ContextMappingFunction;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public final class ContextMappingIterator
implements SequenceIterator {
    private FocusIterator base;
    private ContextMappingFunction action;
    private XPathContext context;
    private SequenceIterator stepIterator = null;

    public ContextMappingIterator(ContextMappingFunction action, XPathContext context) {
        this.base = context.getCurrentIterator();
        this.action = action;
        this.context = context;
    }

    @Override
    public Item next() throws XPathException {
        Item nextItem;
        block3: {
            while (true) {
                if (this.stepIterator != null) {
                    nextItem = this.stepIterator.next();
                    if (nextItem != null) break block3;
                    this.stepIterator = null;
                }
                if (this.base.next() == null) break;
                this.stepIterator = this.action.map(this.context);
                nextItem = this.stepIterator.next();
                if (nextItem == null) {
                    this.stepIterator = null;
                    continue;
                }
                break block3;
                break;
            }
            this.stepIterator = null;
            return null;
        }
        return nextItem;
    }

    @Override
    public void close() {
        this.base.close();
        if (this.stepIterator != null) {
            this.stepIterator.close();
        }
    }
}


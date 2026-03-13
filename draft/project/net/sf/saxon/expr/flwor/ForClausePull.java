/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.ForClause;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;

public class ForClausePull
extends TuplePull {
    protected TuplePull base;
    protected ForClause forClause;
    protected FocusIterator currentIteration;

    public ForClausePull(TuplePull base, ForClause forClause) {
        this.base = base;
        this.forClause = forClause;
    }

    @Override
    public boolean nextTuple(XPathContext context) throws XPathException {
        while (true) {
            Item next;
            if (this.currentIteration == null) {
                if (!this.base.nextTuple(context)) {
                    return false;
                }
                this.currentIteration = new FocusTrackingIterator(this.forClause.getSequence().iterate(context));
            }
            if ((next = this.currentIteration.next()) != null) {
                context.setLocalVariable(this.forClause.getRangeVariable().getLocalSlotNumber(), next);
                if (this.forClause.getPositionVariable() != null) {
                    context.setLocalVariable(this.forClause.getPositionVariable().getLocalSlotNumber(), new Int64Value(this.currentIteration.position()));
                }
                return true;
            }
            this.currentIteration = null;
        }
    }

    @Override
    public void close() {
        this.base.close();
        if (this.currentIteration != null) {
            this.currentIteration.close();
        }
    }
}


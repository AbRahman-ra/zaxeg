/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.ForClause;
import net.sf.saxon.expr.flwor.ForClausePull;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;

public class ForClauseOuterPull
extends ForClausePull {
    public ForClauseOuterPull(TuplePull base, ForClause forClause) {
        super(base, forClause);
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
                next = this.currentIteration.next();
                if (next == null) {
                    context.setLocalVariable(this.forClause.getRangeVariable().getLocalSlotNumber(), EmptySequence.getInstance());
                    if (this.forClause.getPositionVariable() != null) {
                        context.setLocalVariable(this.forClause.getPositionVariable().getLocalSlotNumber(), Int64Value.ZERO);
                    }
                    this.currentIteration = null;
                    return true;
                }
            } else {
                next = this.currentIteration.next();
            }
            if (next != null) {
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


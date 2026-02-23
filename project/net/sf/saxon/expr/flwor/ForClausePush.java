/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.ForClause;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;

public class ForClausePush
extends TuplePush {
    protected TuplePush destination;
    protected ForClause forClause;

    public ForClausePush(Outputter outputter, TuplePush destination, ForClause forClause) {
        super(outputter);
        this.destination = destination;
        this.forClause = forClause;
    }

    @Override
    public void processTuple(XPathContext context) throws XPathException {
        Item next;
        SequenceIterator iter = this.forClause.getSequence().iterate(context);
        int pos = 0;
        while ((next = iter.next()) != null) {
            context.setLocalVariable(this.forClause.getRangeVariable().getLocalSlotNumber(), next);
            if (this.forClause.getPositionVariable() != null) {
                context.setLocalVariable(this.forClause.getPositionVariable().getLocalSlotNumber(), new Int64Value(++pos));
            }
            this.destination.processTuple(context);
        }
    }

    @Override
    public void close() throws XPathException {
        this.destination.close();
    }
}


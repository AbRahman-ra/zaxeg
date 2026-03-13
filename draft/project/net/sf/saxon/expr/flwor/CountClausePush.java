/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.CountClause;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;

public class CountClausePush
extends TuplePush {
    TuplePush destination;
    int slot;
    int count = 0;

    public CountClausePush(Outputter outputter, TuplePush destination, CountClause countClause) {
        super(outputter);
        this.destination = destination;
        this.slot = countClause.getRangeVariable().getLocalSlotNumber();
    }

    @Override
    public void processTuple(XPathContext context) throws XPathException {
        context.setLocalVariable(this.slot, new Int64Value(++this.count));
        this.destination.processTuple(context);
    }

    @Override
    public void close() throws XPathException {
        this.destination.close();
    }
}


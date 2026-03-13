/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.LetClause;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class LetClausePush
extends TuplePush {
    TuplePush destination;
    LetClause letClause;

    public LetClausePush(Outputter outputter, TuplePush destination, LetClause letClause) {
        super(outputter);
        this.destination = destination;
        this.letClause = letClause;
    }

    @Override
    public void processTuple(XPathContext context) throws XPathException {
        Sequence val = this.letClause.getEvaluator().evaluate(this.letClause.getSequence(), context);
        context.setLocalVariable(this.letClause.getRangeVariable().getLocalSlotNumber(), val);
        this.destination.processTuple(context);
    }

    @Override
    public void close() throws XPathException {
        this.destination.close();
    }
}


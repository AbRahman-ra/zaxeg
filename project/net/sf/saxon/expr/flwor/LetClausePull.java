/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.LetClause;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class LetClausePull
extends TuplePull {
    TuplePull base;
    LetClause letClause;

    public LetClausePull(TuplePull base, LetClause letClause) {
        this.base = base;
        this.letClause = letClause;
    }

    @Override
    public boolean nextTuple(XPathContext context) throws XPathException {
        if (!this.base.nextTuple(context)) {
            return false;
        }
        Sequence val = this.letClause.getEvaluator().evaluate(this.letClause.getSequence(), context);
        context.setLocalVariable(this.letClause.getRangeVariable().getLocalSlotNumber(), val);
        return true;
    }

    @Override
    public void close() {
        this.base.close();
    }
}


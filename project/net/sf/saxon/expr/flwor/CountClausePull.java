/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.CountClause;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;

public class CountClausePull
extends TuplePull {
    TuplePull base;
    int slot;
    int count = 0;

    public CountClausePull(TuplePull base, CountClause countClause) {
        this.base = base;
        this.slot = countClause.getRangeVariable().getLocalSlotNumber();
    }

    @Override
    public boolean nextTuple(XPathContext context) throws XPathException {
        if (!this.base.nextTuple(context)) {
            this.count = 0;
            context.setLocalVariable(this.slot, Int64Value.ZERO);
            return false;
        }
        context.setLocalVariable(this.slot, new Int64Value(++this.count));
        return true;
    }
}


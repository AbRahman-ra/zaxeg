/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.trans.XPathException;

public class WhereClausePull
extends TuplePull {
    TuplePull base;
    Expression predicate;

    public WhereClausePull(TuplePull base, Expression predicate) {
        this.base = base;
        this.predicate = predicate;
    }

    @Override
    public boolean nextTuple(XPathContext context) throws XPathException {
        while (this.base.nextTuple(context)) {
            if (!this.predicate.effectiveBooleanValue(context)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        this.base.close();
    }
}


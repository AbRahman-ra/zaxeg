/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.trans.XPathException;

public class WhereClausePush
extends TuplePush {
    TuplePush destination;
    Expression predicate;

    public WhereClausePush(Outputter outputter, TuplePush destination, Expression predicate) {
        super(outputter);
        this.destination = destination;
        this.predicate = predicate;
    }

    @Override
    public void processTuple(XPathContext context) throws XPathException {
        if (this.predicate.effectiveBooleanValue(context)) {
            this.destination.processTuple(context);
        }
    }

    @Override
    public void close() throws XPathException {
        this.destination.close();
    }
}


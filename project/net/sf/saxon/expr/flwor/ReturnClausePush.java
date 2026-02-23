/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.trans.XPathException;

public class ReturnClausePush
extends TuplePush {
    private Expression returnExpr;

    public ReturnClausePush(Outputter outputter, Expression returnExpr) {
        super(outputter);
        this.returnExpr = returnExpr;
    }

    @Override
    public void processTuple(XPathContext context) throws XPathException {
        this.returnExpr.process(this.getOutputter(), context);
    }

    @Override
    public void close() throws XPathException {
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class ReturnClauseIterator
implements SequenceIterator {
    private TuplePull base;
    private Expression action;
    private XPathContext context;
    private SequenceIterator results = null;

    public ReturnClauseIterator(TuplePull base, FLWORExpression flwor, XPathContext context) {
        this.base = base;
        this.action = flwor.getReturnClause();
        this.context = context;
    }

    @Override
    public Item next() throws XPathException {
        Item nextItem;
        block3: {
            while (true) {
                if (this.results != null) {
                    nextItem = this.results.next();
                    if (nextItem != null) break block3;
                    this.results = null;
                }
                if (!this.base.nextTuple(this.context)) break;
                this.results = this.action.iterate(this.context);
                nextItem = this.results.next();
                if (nextItem == null) {
                    this.results = null;
                    continue;
                }
                break block3;
                break;
            }
            this.results = null;
            return null;
        }
        return nextItem;
    }

    @Override
    public void close() {
        if (this.results != null) {
            this.results.close();
        }
        this.base.close();
    }
}


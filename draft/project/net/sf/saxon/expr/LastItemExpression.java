/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SingleItemFilter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ReversibleIterator;

public final class LastItemExpression
extends SingleItemFilter {
    public LastItemExpression(Expression base) {
        super(base);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        LastItemExpression exp = new LastItemExpression(this.getBaseExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        SequenceIterator forwards = this.getBaseExpression().iterate(context);
        if (forwards instanceof ReversibleIterator) {
            return ((ReversibleIterator)forwards).getReverseIterator().next();
        }
        Item current = null;
        Item item;
        while ((item = forwards.next()) != null) {
            current = item;
        }
        return current;
    }

    @Override
    public String getExpressionName() {
        return "lastOf";
    }
}


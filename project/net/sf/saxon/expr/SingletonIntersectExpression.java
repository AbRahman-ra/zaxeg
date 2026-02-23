/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.VennExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingletonIterator;

public class SingletonIntersectExpression
extends VennExpression {
    public SingletonIntersectExpression(Expression p1, int op, Expression p2) {
        super(p1, op, p2);
    }

    @Override
    public Expression simplify() throws XPathException {
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        SingletonIntersectExpression exp = new SingletonIntersectExpression(this.getLhsExpression().copy(rebindings), this.operator, this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public SequenceIterator iterate(XPathContext c) throws XPathException {
        NodeInfo n;
        NodeInfo m = (NodeInfo)this.getLhsExpression().evaluateItem(c);
        if (m == null) {
            return EmptyIterator.getInstance();
        }
        SequenceIterator iter = this.getRhsExpression().iterate(c);
        while ((n = (NodeInfo)iter.next()) != null) {
            if (!n.equals(m)) continue;
            return SingletonIterator.makeIterator(m);
        }
        return EmptyIterator.getInstance();
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext c) throws XPathException {
        NodeInfo m = (NodeInfo)this.getLhsExpression().evaluateItem(c);
        return m != null && SingletonIntersectExpression.containsNode(this.getRhsExpression().iterate(c), m);
    }

    public static boolean containsNode(SequenceIterator iter, NodeInfo m) throws XPathException {
        NodeInfo n;
        while ((n = (NodeInfo)iter.next()) != null) {
            if (!n.equals(m)) continue;
            iter.close();
            return true;
        }
        return false;
    }

    @Override
    public String getExpressionName() {
        return "singleton-intersect";
    }

    @Override
    protected String displayOperator() {
        return "among";
    }

    @Override
    protected String tag() {
        return "among";
    }
}


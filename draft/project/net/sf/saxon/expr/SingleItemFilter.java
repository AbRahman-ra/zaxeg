/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Cardinality;

public abstract class SingleItemFilter
extends UnaryExpression {
    public SingleItemFilter(Expression base) {
        super(base);
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SAME_FOCUS_ACTION;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().optimize(visitor, contextInfo);
        Expression base = this.getBaseExpression();
        if (!Cardinality.allowsMany(base.getCardinality())) {
            return base;
        }
        return super.optimize(visitor, contextInfo);
    }

    @Override
    public int computeCardinality() {
        return 24576;
    }
}


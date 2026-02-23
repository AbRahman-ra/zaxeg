/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.AndExpression;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;

public class OrExpression
extends BooleanExpression {
    public OrExpression(Expression p1, Expression p2) {
        super(p1, 9, p2);
    }

    @Override
    protected Expression preEvaluate() {
        if (Literal.hasEffectiveBooleanValue(this.getLhsExpression(), true) || Literal.hasEffectiveBooleanValue(this.getRhsExpression(), true)) {
            return Literal.makeLiteral(BooleanValue.TRUE, this);
        }
        if (Literal.hasEffectiveBooleanValue(this.getLhsExpression(), false)) {
            return this.forceToBoolean(this.getRhsExpression());
        }
        if (Literal.hasEffectiveBooleanValue(this.getRhsExpression(), false)) {
            return this.forceToBoolean(this.getLhsExpression());
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression e2;
        Expression e = super.optimize(visitor, contextItemType);
        if (e != this) {
            return e;
        }
        if (!(this.getParentExpression() instanceof OrExpression) && (e2 = visitor.obtainOptimizer().tryGeneralComparison(visitor, contextItemType, this)) != null && e2 != this) {
            return e2;
        }
        return this;
    }

    @Override
    public double getCost() {
        return this.getLhsExpression().getCost() + this.getRhsExpression().getCost() / 2.0;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        OrExpression exp = new OrExpression(this.getLhsExpression().copy(rebindings), this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public Expression negate() {
        Expression not0 = SystemFunction.makeCall("not", this.getRetainedStaticContext(), this.getLhsExpression());
        Expression not1 = SystemFunction.makeCall("not", this.getRetainedStaticContext(), this.getRhsExpression());
        AndExpression result = new AndExpression(not0, not1);
        ExpressionTool.copyLocationInfo(this, result);
        return result;
    }

    @Override
    protected String tag() {
        return "or";
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext c) throws XPathException {
        return this.getLhsExpression().effectiveBooleanValue(c) || this.getRhsExpression().effectiveBooleanValue(c);
    }
}


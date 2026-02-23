/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Collection;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OrExpression;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.BooleanValue;

public class AndExpression
extends BooleanExpression {
    public AndExpression(Expression p1, Expression p2) {
        super(p1, 10, p2);
    }

    @Override
    protected Expression preEvaluate() {
        if (Literal.isConstantBoolean(this.getLhsExpression(), false) || Literal.isConstantBoolean(this.getRhsExpression(), false)) {
            return Literal.makeLiteral(BooleanValue.FALSE, this);
        }
        if (Literal.hasEffectiveBooleanValue(this.getLhsExpression(), true)) {
            return this.forceToBoolean(this.getRhsExpression());
        }
        if (Literal.hasEffectiveBooleanValue(this.getRhsExpression(), true)) {
            return this.forceToBoolean(this.getLhsExpression());
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression t = super.optimize(visitor, contextInfo);
        if (t != this) {
            return t;
        }
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (this.getRhsExpression() instanceof UserFunctionCall && th.isSubType(this.getRhsExpression().getItemType(), BuiltInAtomicType.BOOLEAN) && !ExpressionTool.isLoopingSubexpression(this, null)) {
            Expression cond = Choose.makeConditional(this.getLhsExpression(), this.getRhsExpression(), Literal.makeLiteral(BooleanValue.FALSE, this));
            ExpressionTool.copyLocationInfo(this, cond);
            return cond;
        }
        return this;
    }

    @Override
    public double getCost() {
        return this.getLhsExpression().getCost() + this.getRhsExpression().getCost() / 2.0;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        AndExpression a2 = new AndExpression(this.getLhsExpression().copy(rebindings), this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, a2);
        return a2;
    }

    @Override
    public Expression negate() {
        Expression not0 = SystemFunction.makeCall("not", this.getRetainedStaticContext(), this.getLhsExpression());
        Expression not1 = SystemFunction.makeCall("not", this.getRetainedStaticContext(), this.getRhsExpression());
        return new OrExpression(not0, not1);
    }

    @Override
    protected String tag() {
        return "and";
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext c) throws XPathException {
        return this.getLhsExpression().effectiveBooleanValue(c) && this.getRhsExpression().effectiveBooleanValue(c);
    }

    public static Expression distribute(Collection<Expression> exprs) {
        Expression result = null;
        if (exprs != null) {
            boolean first = true;
            for (Expression e : exprs) {
                if (first) {
                    first = false;
                    result = e;
                    continue;
                }
                result = new AndExpression(result, e);
            }
        }
        return result;
    }
}


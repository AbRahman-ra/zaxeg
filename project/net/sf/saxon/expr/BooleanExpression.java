/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.List;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Negatable;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.BooleanFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.BooleanValue;

public abstract class BooleanExpression
extends BinaryExpression
implements Negatable {
    public BooleanExpression(Expression p1, int operator, Expression p2) {
        super(p1, operator, p2);
    }

    @Override
    public String getExpressionName() {
        return Token.tokens[this.getOperator()] + "-expression";
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        XPathException err0 = TypeChecker.ebvError(this.getLhsExpression(), th);
        if (err0 != null) {
            err0.setLocator(this.getLocation());
            throw err0;
        }
        XPathException err1 = TypeChecker.ebvError(this.getRhsExpression(), th);
        if (err1 != null) {
            err1.setLocator(this.getLocation());
            throw err1;
        }
        if (this.getLhsExpression() instanceof Literal && !(((Literal)this.getLhsExpression()).getValue() instanceof BooleanValue)) {
            this.setLhsExpression(Literal.makeLiteral(BooleanValue.get(this.getLhsExpression().effectiveBooleanValue(visitor.makeDynamicContext())), this));
        }
        if (this.getRhsExpression() instanceof Literal && !(((Literal)this.getRhsExpression()).getValue() instanceof BooleanValue)) {
            this.setRhsExpression(Literal.makeLiteral(BooleanValue.get(this.getRhsExpression().effectiveBooleanValue(visitor.makeDynamicContext())), this));
        }
        return this.preEvaluate();
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression op1;
        this.optimizeChildren(visitor, contextItemType);
        boolean forStreaming = visitor.isOptimizeForStreaming();
        this.setLhsExpression(ExpressionTool.unsortedIfHomogeneous(this.getLhsExpression(), forStreaming));
        this.setRhsExpression(ExpressionTool.unsortedIfHomogeneous(this.getRhsExpression(), forStreaming));
        Expression op0 = BooleanFn.rewriteEffectiveBooleanValue(this.getLhsExpression(), visitor, contextItemType);
        if (op0 != null) {
            this.setLhsExpression(op0);
        }
        if ((op1 = BooleanFn.rewriteEffectiveBooleanValue(this.getRhsExpression(), visitor, contextItemType)) != null) {
            this.setRhsExpression(op1);
        }
        return this.preEvaluate();
    }

    protected abstract Expression preEvaluate();

    protected Expression forceToBoolean(Expression in) {
        if (in.getItemType() == BuiltInAtomicType.BOOLEAN && in.getCardinality() == 16384) {
            return in;
        }
        return SystemFunction.makeCall("boolean", this.getRetainedStaticContext(), in);
    }

    @Override
    public boolean isNegatable(TypeHierarchy th) {
        return true;
    }

    @Override
    public abstract Expression negate();

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        return BooleanValue.get(this.effectiveBooleanValue(context));
    }

    @Override
    public abstract boolean effectiveBooleanValue(XPathContext var1) throws XPathException;

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.BOOLEAN;
    }

    public static void listAndComponents(Expression exp, List<Expression> list) {
        if (exp instanceof BooleanExpression && ((BooleanExpression)exp).getOperator() == 10) {
            for (Operand o : exp.operands()) {
                BooleanExpression.listAndComponents(o.getChildExpression(), list);
            }
        } else {
            list.add(exp);
        }
    }

    @Override
    protected OperandRole getOperandRole(int arg) {
        return OperandRole.INSPECT;
    }
}


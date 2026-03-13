/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.ComparisonExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.BooleanValue;

public abstract class CompareToConstant
extends UnaryExpression
implements ComparisonExpression {
    protected int operator;

    public CompareToConstant(Expression p0) {
        super(p0);
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SINGLE_ATOMIC;
    }

    @Override
    public Expression getLhsExpression() {
        return this.getBaseExpression();
    }

    @Override
    public Operand getLhs() {
        return this.getOperand();
    }

    @Override
    public abstract Expression getRhsExpression();

    @Override
    public Operand getRhs() {
        return new Operand(this, this.getRhsExpression(), OperandRole.SINGLE_ATOMIC);
    }

    public int getComparisonOperator() {
        return this.operator;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public int computeSpecialProperties() {
        return 0x800000;
    }

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        return BooleanValue.get(this.effectiveBooleanValue(context));
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().optimize(visitor, contextInfo);
        if (this.getLhsExpression() instanceof Literal) {
            return Literal.makeLiteral(BooleanValue.get(this.effectiveBooleanValue(null)), this);
        }
        return this;
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    @Override
    public int getSingletonOperator() {
        return this.operator;
    }

    @Override
    public boolean convertsUntypedToOther() {
        return true;
    }

    boolean interpretComparisonResult(int c) {
        switch (this.operator) {
            case 50: {
                return c == 0;
            }
            case 51: {
                return c != 0;
            }
            case 52: {
                return c > 0;
            }
            case 53: {
                return c < 0;
            }
            case 54: {
                return c >= 0;
            }
            case 55: {
                return c <= 0;
            }
        }
        throw new UnsupportedOperationException("Unknown operator " + this.operator);
    }
}


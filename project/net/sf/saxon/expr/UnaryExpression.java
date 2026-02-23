/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Iterator;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.jiter.MonoIterator;
import net.sf.saxon.type.ItemType;

public abstract class UnaryExpression
extends Expression {
    private Operand operand;

    public UnaryExpression(Expression p0) {
        this.operand = new Operand(this, p0, this.getOperandRole());
        ExpressionTool.copyLocationInfo(p0, this);
    }

    public Expression getBaseExpression() {
        return this.operand.getChildExpression();
    }

    public void setBaseExpression(Expression child) {
        this.operand.setChildExpression(child);
    }

    public Operand getOperand() {
        return this.operand;
    }

    @Override
    public Iterable<Operand> operands() {
        return new Iterable<Operand>(){

            @Override
            public Iterator<Operand> iterator() {
                return new MonoIterator<Operand>(UnaryExpression.this.operand);
            }
        };
    }

    protected abstract OperandRole getOperandRole();

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.operand.typeCheck(visitor, contextInfo);
        try {
            if (this.getBaseExpression() instanceof Literal) {
                Literal e2 = Literal.makeLiteral(this.iterate(visitor.getStaticContext().makeEarlyEvaluationContext()).materialize(), this);
                ExpressionTool.copyLocationInfo(this, e2);
                return e2;
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.operand.optimize(visitor, contextInfo);
        Expression base = this.getBaseExpression();
        try {
            if (base instanceof Literal) {
                return Literal.makeLiteral(this.iterate(visitor.getStaticContext().makeEarlyEvaluationContext()).materialize(), this);
            }
        } catch (XPathException xPathException) {
            // empty catch block
        }
        return this;
    }

    @Override
    public int computeSpecialProperties() {
        return this.getBaseExpression().getSpecialProperties();
    }

    @Override
    public int computeCardinality() {
        return this.getBaseExpression().getCardinality();
    }

    @Override
    public ItemType getItemType() {
        return this.getBaseExpression().getItemType();
    }

    @Override
    public boolean equals(Object other) {
        return other != null && this.getClass().equals(other.getClass()) && this.getBaseExpression().isEqual(((UnaryExpression)other).getBaseExpression());
    }

    @Override
    public int computeHashCode() {
        return ("UnaryExpression " + this.getClass()).hashCode() ^ this.getBaseExpression().hashCode();
    }

    @Override
    public String toString() {
        return this.getExpressionName() + "(" + this.getBaseExpression() + ")";
    }

    @Override
    public String toShortString() {
        return this.getExpressionName() + "(" + this.getBaseExpression().toShortString() + ")";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        String name = this.getExpressionName();
        if (name == null) {
            out.startElement("unaryOperator", this);
            String op = this.displayOperator(out.getConfiguration());
            if (op != null) {
                out.emitAttribute("op", op);
            }
        } else {
            out.startElement(name, this);
        }
        this.emitExtraAttributes(out);
        this.getBaseExpression().export(out);
        out.endElement();
    }

    protected void emitExtraAttributes(ExpressionPresenter out) {
    }

    protected String displayOperator(Configuration config) {
        return null;
    }
}


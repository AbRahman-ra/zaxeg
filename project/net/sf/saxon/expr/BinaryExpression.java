/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.jiter.PairIterator;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.value.Cardinality;

public abstract class BinaryExpression
extends Expression {
    private Operand lhs;
    private Operand rhs;
    protected int operator;

    public BinaryExpression(Expression p0, int op, Expression p1) {
        this.operator = op;
        this.lhs = new Operand(this, p0, this.getOperandRole(0));
        this.rhs = new Operand(this, p1, this.getOperandRole(1));
        this.adoptChildExpression(p0);
        this.adoptChildExpression(p1);
    }

    @Override
    public final Iterable<Operand> operands() {
        return new Iterable<Operand>(){

            @Override
            public Iterator<Operand> iterator() {
                return new PairIterator<Operand>(BinaryExpression.this.lhs, BinaryExpression.this.rhs);
            }
        };
    }

    protected OperandRole getOperandRole(int arg) {
        return OperandRole.SINGLE_ATOMIC;
    }

    public Operand getLhs() {
        return this.lhs;
    }

    public final Expression getLhsExpression() {
        return this.lhs.getChildExpression();
    }

    public void setLhsExpression(Expression child) {
        this.lhs.setChildExpression(child);
    }

    public Operand getRhs() {
        return this.rhs;
    }

    public final Expression getRhsExpression() {
        return this.rhs.getChildExpression();
    }

    public void setRhsExpression(Expression child) {
        this.rhs.setChildExpression(child);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.resetLocalStaticProperties();
        this.lhs.typeCheck(visitor, contextInfo);
        this.rhs.typeCheck(visitor, contextInfo);
        try {
            if (this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal) {
                GroundedValue v = this.evaluateItem(visitor.getStaticContext().makeEarlyEvaluationContext()).materialize();
                return Literal.makeLiteral(v, this);
            }
        } catch (XPathException xPathException) {
            // empty catch block
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.lhs.optimize(visitor, contextItemType);
        this.rhs.optimize(visitor, contextItemType);
        try {
            Item item;
            Optimizer opt = visitor.obtainOptimizer();
            if (opt.isOptionSet(32768) && this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal && (item = this.evaluateItem(visitor.getStaticContext().makeEarlyEvaluationContext())) != null) {
                GroundedValue v = item.materialize();
                return Literal.makeLiteral(v, this);
            }
        } catch (XPathException xPathException) {
            // empty catch block
        }
        return this;
    }

    @Override
    public void setFlattened(boolean flattened) {
        this.getLhsExpression().setFlattened(flattened);
        this.getRhsExpression().setFlattened(flattened);
    }

    public int getOperator() {
        return this.operator;
    }

    @Override
    public int computeCardinality() {
        Expression lhs = this.getLhsExpression();
        Expression rhs = this.getRhsExpression();
        if (!Cardinality.allowsZero(lhs.getCardinality()) && lhs.getItemType() instanceof AtomicType && !Cardinality.allowsZero(rhs.getCardinality()) && rhs.getItemType() instanceof AtomicType) {
            return 16384;
        }
        return 24576;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | 0x800000;
    }

    protected static boolean isCommutative(int operator) {
        return operator == 10 || operator == 9 || operator == 1 || operator == 23 || operator == 15 || operator == 17 || operator == 6 || operator == 50 || operator == 22 || operator == 51;
    }

    protected static boolean isAssociative(int operator) {
        return operator == 10 || operator == 9 || operator == 1 || operator == 23 || operator == 15 || operator == 17;
    }

    protected static boolean isInverse(int op1, int op2) {
        return op1 != op2 && op1 == Token.inverse(op2);
    }

    @Override
    public int getImplementationMethod() {
        return 3;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BinaryExpression && this.hasCompatibleStaticContext((Expression)other)) {
            BinaryExpression b = (BinaryExpression)other;
            Expression lhs1 = this.getLhsExpression();
            Expression rhs1 = this.getRhsExpression();
            Expression lhs2 = b.getLhsExpression();
            Expression rhs2 = b.getRhsExpression();
            if (this.operator == b.operator) {
                if (lhs1.isEqual(lhs2) && rhs1.isEqual(rhs2)) {
                    return true;
                }
                if (BinaryExpression.isCommutative(this.operator) && lhs1.isEqual(rhs2) && rhs1.isEqual(lhs2)) {
                    return true;
                }
                if (BinaryExpression.isAssociative(this.operator) && this.pairwiseEqual(this.flattenExpression(new ArrayList<Expression>(4)), b.flattenExpression(new ArrayList<Expression>(4)))) {
                    return true;
                }
            }
            return BinaryExpression.isInverse(this.operator, b.operator) && lhs1.isEqual(rhs2) && rhs1.isEqual(lhs2);
        }
        return false;
    }

    private List<Expression> flattenExpression(List<Expression> list) {
        int i;
        int h;
        if (this.getLhsExpression() instanceof BinaryExpression && ((BinaryExpression)this.getLhsExpression()).operator == this.operator) {
            ((BinaryExpression)this.getLhsExpression()).flattenExpression(list);
        } else {
            h = this.getLhsExpression().hashCode();
            list.add(this.getLhsExpression());
            for (i = list.size() - 1; i > 0 && h > list.get(i - 1).hashCode(); --i) {
                list.set(i, list.get(i - 1));
                list.set(i - 1, this.getLhsExpression());
            }
        }
        if (this.getRhsExpression() instanceof BinaryExpression && ((BinaryExpression)this.getRhsExpression()).operator == this.operator) {
            ((BinaryExpression)this.getRhsExpression()).flattenExpression(list);
        } else {
            h = this.getRhsExpression().hashCode();
            list.add(this.getRhsExpression());
            for (i = list.size() - 1; i > 0 && h > list.get(i - 1).hashCode(); --i) {
                list.set(i, list.get(i - 1));
                list.set(i - 1, this.getRhsExpression());
            }
        }
        return list;
    }

    private boolean pairwiseEqual(List a, List b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); ++i) {
            if (a.get(i).equals(b.get(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public int computeHashCode() {
        int op = Math.min(this.operator, Token.inverse(this.operator));
        return ("BinaryExpression " + op).hashCode() ^ this.getLhsExpression().hashCode() ^ this.getRhsExpression().hashCode();
    }

    @Override
    public String toString() {
        return ExpressionTool.parenthesize(this.getLhsExpression()) + " " + this.displayOperator() + " " + ExpressionTool.parenthesize(this.getRhsExpression());
    }

    @Override
    public String toShortString() {
        return this.parenthesize(this.getLhsExpression()) + " " + this.displayOperator() + " " + this.parenthesize(this.getRhsExpression());
    }

    private String parenthesize(Expression operand) {
        String operandStr = operand.toShortString();
        if (operand instanceof BinaryExpression && XPathParser.operatorPrecedence(((BinaryExpression)operand).operator) < XPathParser.operatorPrecedence(this.operator)) {
            operandStr = "(" + operandStr + ")";
        }
        return operandStr;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement(this.tag(), this);
        out.emitAttribute("op", this.displayOperator());
        this.explainExtraAttributes(out);
        this.getLhsExpression().export(out);
        this.getRhsExpression().export(out);
        out.endElement();
    }

    protected String tag() {
        return "operator";
    }

    protected void explainExtraAttributes(ExpressionPresenter out) {
    }

    protected String displayOperator() {
        return Token.tokens[this.operator];
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.arrays;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.oper.OperandArray;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class SquareArrayConstructor
extends Expression {
    private OperandArray operanda;

    public SquareArrayConstructor(List<Expression> children) {
        Expression[] kids = children.toArray(new Expression[0]);
        for (Expression e : children) {
            this.adoptChildExpression(e);
        }
        this.setOperanda(new OperandArray((Expression)this, kids, OperandRole.NAVIGATE));
    }

    protected void setOperanda(OperandArray operanda) {
        this.operanda = operanda;
    }

    public OperandArray getOperanda() {
        return this.operanda;
    }

    public Operand getOperand(int i) {
        return this.operanda.getOperand(i);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operanda.operands();
    }

    @Override
    public String getExpressionName() {
        return "SquareArrayConstructor";
    }

    @Override
    public String getStreamerName() {
        return "ArrayBlock";
    }

    @Override
    public int computeSpecialProperties() {
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SquareArrayConstructor)) {
            return false;
        }
        SquareArrayConstructor ab2 = (SquareArrayConstructor)other;
        if (ab2.getOperanda().getNumberOfOperands() != this.getOperanda().getNumberOfOperands()) {
            return false;
        }
        for (int i = 0; i < this.getOperanda().getNumberOfOperands(); ++i) {
            if (this.getOperanda().getOperand(i).equals(ab2.getOperanda().getOperand(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public int computeHashCode() {
        int h = -2020896096;
        for (Operand o : this.operands()) {
            h ^= o.getChildExpression().hashCode();
        }
        return h;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression e = super.typeCheck(visitor, contextInfo);
        if (e != this) {
            return e;
        }
        return this.preEvaluate(visitor);
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression e = super.optimize(visitor, contextInfo);
        if (e != this) {
            return e;
        }
        return this.preEvaluate(visitor);
    }

    private Expression preEvaluate(ExpressionVisitor visitor) {
        boolean allFixed = false;
        for (Operand o : this.operands()) {
            if (o.getChildExpression() instanceof Literal) continue;
            return this;
        }
        try {
            return Literal.makeLiteral(this.evaluateItem(visitor.makeDynamicContext()), this);
        } catch (XPathException e) {
            return this;
        }
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ArrayList<Expression> m2 = new ArrayList<Expression>(this.getOperanda().getNumberOfOperands());
        for (Operand o : this.operands()) {
            m2.add(o.getChildExpression().copy(rebindings));
        }
        SquareArrayConstructor b2 = new SquareArrayConstructor(m2);
        ExpressionTool.copyLocationInfo(this, b2);
        return b2;
    }

    @Override
    public final ItemType getItemType() {
        ItemType contentType = null;
        int contentCardinality = 16384;
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        for (Expression e : this.getOperanda().operandExpressions()) {
            if (contentType == null) {
                contentType = e.getItemType();
                contentCardinality = e.getCardinality();
                continue;
            }
            contentType = Type.getCommonSuperType(contentType, e.getItemType(), th);
            contentCardinality = Cardinality.union(contentCardinality, e.getCardinality());
        }
        if (contentType == null) {
            contentType = ErrorType.getInstance();
        }
        return new ArrayItemType(SequenceType.makeSequenceType(contentType, contentCardinality));
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.FUNCTION;
    }

    @Override
    public final int computeCardinality() {
        return 16384;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("arrayBlock", this);
        for (Operand o : this.operands()) {
            o.getChildExpression().export(out);
        }
        out.endElement();
    }

    @Override
    public String toShortString() {
        int n = this.getOperanda().getNumberOfOperands();
        switch (n) {
            case 0: {
                return "[]";
            }
            case 1: {
                return "[" + this.getOperanda().getOperand(0).getChildExpression().toShortString() + "]";
            }
            case 2: {
                return "[" + this.getOperanda().getOperand(0).getChildExpression().toShortString() + ", " + this.getOperanda().getOperand(1).getChildExpression().toShortString() + "]";
            }
        }
        return "[" + this.getOperanda().getOperand(0).getChildExpression().toShortString() + ", ...]";
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        ArrayList<GroundedValue> value = new ArrayList<GroundedValue>(this.getOperanda().getNumberOfOperands());
        for (Operand o : this.operands()) {
            GroundedValue s = ExpressionTool.eagerEvaluate(o.getChildExpression(), context);
            value.add(s);
        }
        return new SimpleArrayItem(value);
    }
}


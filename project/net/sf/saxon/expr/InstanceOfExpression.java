/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.DocumentNodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public final class InstanceOfExpression
extends UnaryExpression {
    ItemType targetType;
    int targetCardinality;

    public InstanceOfExpression(Expression source, SequenceType target) {
        super(source);
        this.targetType = target.getPrimaryType();
        if (this.targetType == null) {
            throw new IllegalArgumentException("Primary item type must not be null");
        }
        this.targetCardinality = target.getCardinality();
    }

    @Override
    protected OperandRole getOperandRole() {
        return this.targetType instanceof DocumentNodeTest ? OperandRole.ABSORB : OperandRole.INSPECT;
    }

    public ItemType getRequiredItemType() {
        return this.targetType;
    }

    public int getRequiredCardinality() {
        return this.targetCardinality;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        Expression operand = this.getBaseExpression();
        if (operand instanceof Literal) {
            Literal lit = Literal.makeLiteral(this.evaluateItem(visitor.getStaticContext().makeEarlyEvaluationContext()), this);
            ExpressionTool.copyLocationInfo(this, lit);
            return lit;
        }
        if (Cardinality.subsumes(this.targetCardinality, operand.getCardinality())) {
            TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
            Affinity relation = th.relationship(operand.getItemType(), this.targetType);
            if (relation == Affinity.SAME_TYPE || relation == Affinity.SUBSUMED_BY) {
                Literal lit = Literal.makeLiteral(BooleanValue.TRUE, this);
                ExpressionTool.copyLocationInfo(this, lit);
                return lit;
            }
            if (!(relation != Affinity.DISJOINT || Cardinality.allowsZero(this.targetCardinality) && Cardinality.allowsZero(operand.getCardinality()))) {
                Literal lit = Literal.makeLiteral(BooleanValue.FALSE, this);
                ExpressionTool.copyLocationInfo(this, lit);
                return lit;
            }
        } else if ((this.targetCardinality & operand.getCardinality()) == 0) {
            Literal lit = Literal.makeLiteral(BooleanValue.FALSE, this);
            ExpressionTool.copyLocationInfo(this, lit);
            return lit;
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression e = super.optimize(visitor, contextInfo);
        if (e != this) {
            return e;
        }
        if (Cardinality.subsumes(this.targetCardinality, this.getBaseExpression().getCardinality())) {
            TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
            Affinity relation = th.relationship(this.getBaseExpression().getItemType(), this.targetType);
            if (relation == Affinity.SAME_TYPE || relation == Affinity.SUBSUMED_BY) {
                return Literal.makeLiteral(BooleanValue.TRUE, this);
            }
            if (!(relation != Affinity.DISJOINT || Cardinality.allowsZero(this.targetCardinality) && Cardinality.allowsZero(this.getBaseExpression().getCardinality()))) {
                return Literal.makeLiteral(BooleanValue.FALSE, this);
            }
        }
        return this;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && this.targetType == ((InstanceOfExpression)other).targetType && this.targetCardinality == ((InstanceOfExpression)other).targetCardinality;
    }

    @Override
    public int computeHashCode() {
        return super.computeHashCode() ^ this.targetType.hashCode() ^ this.targetCardinality;
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        InstanceOfExpression exp = new InstanceOfExpression(this.getBaseExpression().copy(rebindings), SequenceType.makeSequenceType(this.targetType, this.targetCardinality));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.BOOLEAN;
    }

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        return BooleanValue.get(this.effectiveBooleanValue(context));
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        SequenceIterator iter = this.getBaseExpression().iterate(context);
        return this.isInstance(iter, context);
    }

    private boolean isInstance(SequenceIterator iter, XPathContext context) throws XPathException {
        Item item;
        int count = 0;
        while ((item = iter.next()) != null) {
            ++count;
            if (!this.targetType.matches(item, context.getConfiguration().getTypeHierarchy())) {
                iter.close();
                return false;
            }
            if (count != 2 || Cardinality.allowsMany(this.targetCardinality)) continue;
            iter.close();
            return false;
        }
        return count != 0 || (this.targetCardinality & 0x2000) != 0;
    }

    @Override
    public String getExpressionName() {
        return "instance";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("instance", this);
        SequenceType st = SequenceType.makeSequenceType(this.targetType, this.targetCardinality);
        out.emitAttribute("of", st.toAlphaCode());
        this.getBaseExpression().export(out);
        out.endElement();
    }

    @Override
    public String toString() {
        String occ = Cardinality.getOccurrenceIndicator(this.targetCardinality);
        return "(" + this.getBaseExpression().toString() + " instance of " + this.targetType.toString() + occ + ")";
    }

    @Override
    public String toShortString() {
        String occ = Cardinality.getOccurrenceIndicator(this.targetCardinality);
        return this.getBaseExpression().toShortString() + " instance of " + this.targetType.toString() + occ;
    }

    @Override
    public String getStreamerName() {
        return "InstanceOf";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.SingleItemFilter;
import net.sf.saxon.expr.TailIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MemoSequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.NumericValue;

public class SubscriptExpression
extends SingleItemFilter {
    private Operand subscriptOp;

    public SubscriptExpression(Expression base, Expression subscript) {
        super(base);
        this.subscriptOp = new Operand(this, subscript, OperandRole.SINGLE_ATOMIC);
    }

    public Expression getSubscript() {
        return this.subscriptOp.getChildExpression();
    }

    public void setSubscript(Expression subscript) {
        this.subscriptOp.setChildExpression(subscript);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().optimize(visitor, contextInfo);
        if (Literal.isConstantOne(this.getSubscript())) {
            return FirstItemExpression.makeFirstItemExpression(this.getBaseExpression());
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        SubscriptExpression exp = new SubscriptExpression(this.getBaseExpression().copy(rebindings), this.getSubscript().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.getOperand(), this.subscriptOp);
    }

    public Expression getSubscriptExpression() {
        return this.getSubscript();
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SubscriptExpression && this.getBaseExpression().isEqual(((SubscriptExpression)other).getBaseExpression()) && this.getSubscript().isEqual(((SubscriptExpression)other).getSubscript());
    }

    @Override
    public int computeHashCode() {
        return this.getBaseExpression().hashCode() ^ this.getSubscript().hashCode();
    }

    @Override
    public int computeCardinality() {
        return 24576;
    }

    @Override
    public String getStreamerName() {
        return "SubscriptExpression";
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        NumericValue index = (NumericValue)this.getSubscript().evaluateItem(context);
        if (index == null) {
            return null;
        }
        int intindex = index.asSubscript();
        if (intindex != -1) {
            Item item;
            SequenceIterator iter = this.getBaseExpression().iterate(context);
            if (intindex == 1) {
                item = iter.next();
            } else if (iter instanceof MemoSequence.ProgressiveIterator) {
                MemoSequence mem = ((MemoSequence.ProgressiveIterator)iter).getMemoSequence();
                item = mem.itemAt(intindex - 1);
            } else if (iter.getProperties().contains((Object)SequenceIterator.Property.GROUNDED)) {
                GroundedValue value = iter.materialize();
                item = value.itemAt(intindex - 1);
            } else {
                SequenceIterator tail = TailIterator.make(iter, intindex);
                item = tail.next();
                tail.close();
            }
            return item;
        }
        return null;
    }

    @Override
    public String getExpressionName() {
        return "subscript";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("subscript", this);
        this.getBaseExpression().export(destination);
        this.getSubscript().export(destination);
        destination.endElement();
    }

    @Override
    public String toString() {
        return ExpressionTool.parenthesize(this.getBaseExpression()) + "[" + this.getSubscript() + "]";
    }

    @Override
    public String toShortString() {
        return ExpressionTool.parenthesize(this.getBaseExpression()) + "[" + this.getSubscript().toShortString() + "]";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public class ConsumingOperand
extends UnaryExpression {
    public ConsumingOperand(Expression subExpression) {
        super(subExpression);
    }

    @Override
    protected OperandRole getOperandRole() {
        return new OperandRole(0, OperandUsage.ABSORPTION);
    }

    @Override
    public ItemType getItemType() {
        return this.getBaseExpression().getItemType();
    }

    @Override
    public int getIntrinsicDependencies() {
        return this.getBaseExpression().getIntrinsicDependencies();
    }

    @Override
    public int computeCardinality() {
        return this.getBaseExpression().getCardinality();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ConsumingOperand exp = new ConsumingOperand(this.getBaseExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getImplementationMethod() {
        return 3;
    }

    public Sequence evaluate(XPathContext c) throws XPathException {
        if (c.getStackFrame().holdsDynamicValue()) {
            return c.getStackFrame().popDynamicValue();
        }
        return new LazySequence(this.getBaseExpression().iterate(c));
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return this.evaluate(context).iterate();
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        return this.evaluate(context).head();
    }

    @Override
    public String getExpressionName() {
        return "consume";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("consume", this);
        this.getBaseExpression().export(destination);
        destination.endElement();
    }

    @Override
    public String toString() {
        return "consume(" + this.getBaseExpression().toString() + ")";
    }

    @Override
    public String toShortString() {
        return "consume(" + this.getBaseExpression().toShortString() + ")";
    }
}


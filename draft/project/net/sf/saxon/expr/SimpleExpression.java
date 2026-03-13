/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Arrays;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.oper.OperandArray;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;

public abstract class SimpleExpression
extends Expression
implements Callable {
    private OperandArray operanda;

    protected void setOperanda(OperandArray operanda) {
        this.operanda = operanda;
    }

    protected OperandArray getOperanda() {
        return this.operanda;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operanda.operands();
    }

    public void setArguments(Expression[] sub) {
        if (this.getOperanda() != null && this.getOperanda().getNumberOfOperands() > 0) {
            throw new IllegalArgumentException("Cannot replace existing argument array");
        }
        Expression[] sub2 = Arrays.copyOf(sub, sub.length);
        Object[] roles = new OperandRole[sub.length];
        Arrays.fill(roles, OperandRole.NAVIGATE);
        this.setOperanda(new OperandArray((Expression)this, sub2, (OperandRole[])roles));
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        try {
            SimpleExpression se2 = (SimpleExpression)this.getClass().newInstance();
            Expression[] a2 = new Expression[this.operanda.getNumberOfOperands()];
            int i = 0;
            for (Operand o : this.operands()) {
                a2[i++] = o.getChildExpression().copy(rebindings);
            }
            OperandArray o2 = new OperandArray((Expression)se2, a2, this.operanda.getRoles());
            se2.setOperanda(o2);
            return se2;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new UnsupportedOperationException(this.getClass().getName() + ".copy()");
        }
    }

    protected SimpleExpression copyOperandsFrom(SimpleExpression se1) {
        Expression[] a2 = new Expression[se1.operanda.getNumberOfOperands()];
        int i = 0;
        for (Operand o : se1.operands()) {
            a2[i++] = o.getChildExpression().copy(new RebindingMap());
        }
        OperandArray o2 = new OperandArray((Expression)this, a2, se1.operanda.getRoles());
        this.setOperanda(o2);
        return this;
    }

    @Override
    public ItemType getItemType() {
        return Type.ITEM_TYPE;
    }

    @Override
    public int computeCardinality() {
        if ((this.getImplementationMethod() & 1) == 0) {
            return 49152;
        }
        return 24576;
    }

    @Override
    public final Item evaluateItem(XPathContext context) throws XPathException {
        return this.call(context, this.evaluateArguments(context)).head();
    }

    @Override
    public final SequenceIterator iterate(XPathContext context) throws XPathException {
        return this.call(context, this.evaluateArguments(context)).iterate();
    }

    @Override
    public final void process(Outputter output, XPathContext context) throws XPathException {
        SequenceIterator iter = this.call(context, this.evaluateArguments(context)).iterate();
        iter.forEachOrFail(it -> output.append(it, this.getLocation(), 524288));
    }

    private Sequence[] evaluateArguments(XPathContext context) throws XPathException {
        Sequence[] iters = SequenceTool.makeSequenceArray(this.getOperanda().getNumberOfOperands());
        int i = 0;
        for (Operand o : this.operands()) {
            iters[i++] = SequenceTool.toLazySequence(o.getChildExpression().iterate(context));
        }
        return iters;
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        throw new XPathException("In general, stylesheets using extension instructions cannot be exported");
    }

    public String getExpressionType() {
        return this.getClass().getName();
    }
}


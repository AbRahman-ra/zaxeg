/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ForExpression;
import net.sf.saxon.expr.MappingIterator;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.LookaheadIteratorImpl;
import net.sf.saxon.value.EmptySequence;

public class OuterForExpression
extends ForExpression {
    @Override
    protected int getRangeVariableCardinality() {
        return 24576;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression sequence0 = this.getSequence();
        this.getSequenceOp().optimize(visitor, contextItemType);
        Expression action0 = this.getAction();
        this.getActionOp().optimize(visitor, contextItemType);
        if (sequence0 != this.getSequence() || action0 != this.getAction()) {
            return this.optimize(visitor, contextItemType);
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        OuterForExpression forExp = new OuterForExpression();
        ExpressionTool.copyLocationInfo(this, forExp);
        forExp.setRequiredType(this.requiredType);
        forExp.setVariableQName(this.variableName);
        forExp.setSequence(this.getSequence().copy(rebindings));
        rebindings.put(this, forExp);
        Expression newAction = this.getAction().copy(rebindings);
        forExp.setAction(newAction);
        forExp.variableName = this.variableName;
        return forExp;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator base = this.getSequence().iterate(context);
        LookaheadIterator ahead = LookaheadIteratorImpl.makeLookaheadIterator(base);
        if (ahead.hasNext()) {
            ForExpression.MappingAction map = new ForExpression.MappingAction(context, this.getLocalSlotNumber(), this.getAction());
            return new MappingIterator(ahead, map);
        }
        context.setLocalVariable(this.getLocalSlotNumber(), EmptySequence.getInstance());
        return this.getAction().iterate(context);
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        SequenceIterator base = this.getSequence().iterate(context);
        boolean position = true;
        int slot = this.getLocalSlotNumber();
        LookaheadIterator ahead = LookaheadIteratorImpl.makeLookaheadIterator(base);
        if (ahead.hasNext()) {
            Item item;
            while ((item = ahead.next()) != null) {
                context.setLocalVariable(slot, item);
                this.getAction().process(output, context);
            }
        } else {
            context.setLocalVariable(this.getLocalSlotNumber(), EmptySequence.getInstance());
            this.getAction().process(output, context);
        }
    }

    @Override
    public String getExpressionName() {
        return "outerFor";
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        SequenceIterator base = this.getSequence().iterate(context);
        boolean position = true;
        int slot = this.getLocalSlotNumber();
        LookaheadIterator ahead = LookaheadIteratorImpl.makeLookaheadIterator(base);
        if (ahead.hasNext()) {
            Item item;
            while ((item = ahead.next()) != null) {
                context.setLocalVariable(slot, item);
                this.getAction().evaluatePendingUpdates(context, pul);
            }
        } else {
            context.setLocalVariable(this.getLocalSlotNumber(), EmptySequence.getInstance());
            this.getAction().evaluatePendingUpdates(context, pul);
        }
    }

    @Override
    protected void explainSpecializedAttributes(ExpressionPresenter out) {
        out.emitAttribute("outer", "true");
    }
}


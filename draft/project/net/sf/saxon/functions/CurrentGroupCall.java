/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.ForEachGroup;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public class CurrentGroupCall
extends Expression
implements Callable {
    private boolean isInHigherOrderOperand = false;
    private ItemType itemType = AnyItemType.getInstance();
    private ForEachGroup controllingInstruction = null;

    @Override
    public Expression getScopingExpression() {
        return this.getControllingInstruction();
    }

    public void setControllingInstruction(ForEachGroup instruction, ItemType itemType, boolean isHigherOrder) {
        this.resetLocalStaticProperties();
        this.controllingInstruction = instruction;
        this.isInHigherOrderOperand = isHigherOrder;
        this.itemType = itemType;
    }

    @Override
    public void resetLocalStaticProperties() {
        super.resetLocalStaticProperties();
        this.controllingInstruction = null;
        this.itemType = AnyItemType.getInstance();
    }

    public ForEachGroup getControllingInstruction() {
        if (this.controllingInstruction == null) {
            this.controllingInstruction = CurrentGroupCall.findControllingInstruction(this);
        }
        return this.controllingInstruction;
    }

    public static ForEachGroup findControllingInstruction(Expression exp) {
        Expression child = exp;
        for (Expression parent = exp.getParentExpression(); parent != null; parent = parent.getParentExpression()) {
            if (parent instanceof ForEachGroup && (child == ((ForEachGroup)parent).getActionExpression() || child == ((ForEachGroup)parent).getSortKeyDefinitionList())) {
                return (ForEachGroup)parent;
            }
            child = parent;
        }
        return null;
    }

    public boolean isInHigherOrderOperand() {
        return this.isInHigherOrderOperand;
    }

    @Override
    public ItemType getItemType() {
        if (this.itemType == AnyItemType.getInstance() && this.controllingInstruction != null) {
            this.itemType = this.controllingInstruction.getSelectExpression().getItemType();
        }
        return this.itemType;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 32;
    }

    @Override
    protected int computeCardinality() {
        return 57344;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("currentGroup");
        out.endElement();
    }

    @Override
    public int computeSpecialProperties() {
        if (this.getControllingInstruction() == null) {
            return 0;
        }
        return this.controllingInstruction.getSelectExpression().getSpecialProperties();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        CurrentGroupCall cg = new CurrentGroupCall();
        cg.isInHigherOrderOperand = this.isInHigherOrderOperand;
        cg.itemType = this.itemType;
        cg.controllingInstruction = this.controllingInstruction;
        return cg;
    }

    @Override
    public SequenceIterator iterate(XPathContext c) throws XPathException {
        GroupIterator gi = c.getCurrentGroupIterator();
        if (gi == null) {
            XPathException err = new XPathException("There is no current group", "XTDE1061");
            err.setLocation(this.getLocation());
            throw err;
        }
        return gi.iterateCurrentGroup();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(this.iterate(context));
    }

    @Override
    public String toString() {
        return "current-group()";
    }

    @Override
    public String toShortString() {
        return this.toString();
    }

    @Override
    public String getStreamerName() {
        return "CurrentGroup";
    }
}


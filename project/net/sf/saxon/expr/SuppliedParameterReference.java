/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.StandardDiagnostics;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;

public class SuppliedParameterReference
extends Expression {
    int slotNumber;
    SequenceType type;

    public SuppliedParameterReference(int slot) {
        this.slotNumber = slot;
    }

    public int getSlotNumber() {
        return this.slotNumber;
    }

    public void setSuppliedType(SequenceType type) {
        this.type = type;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this;
    }

    @Override
    public ItemType getItemType() {
        if (this.type != null) {
            return this.type.getPrimaryType();
        }
        return AnyItemType.getInstance();
    }

    @Override
    public int getIntrinsicDependencies() {
        return 128;
    }

    @Override
    public int computeCardinality() {
        if (this.type != null) {
            return this.type.getCardinality();
        }
        return 57344;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        SuppliedParameterReference exp = new SuppliedParameterReference(this.slotNumber);
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getImplementationMethod() {
        return 3;
    }

    public Sequence evaluateVariable(XPathContext c) {
        if (this.slotNumber == -1) {
            return c.getStackFrame().popDynamicValue();
        }
        try {
            return c.evaluateLocalVariable(this.slotNumber);
        } catch (AssertionError e) {
            new StandardDiagnostics().printStackTrace(c, c.getConfiguration().getLogger(), 2);
            throw new AssertionError((Object)(((Throwable)((Object)e)).getMessage() + ". No value has been set for parameter " + this.slotNumber));
        }
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return this.evaluateVariable(context).iterate();
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        return this.evaluateVariable(context).head();
    }

    @Override
    public String getExpressionName() {
        return "supplied";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("supplied", this);
        destination.emitAttribute("slot", this.slotNumber + "");
        destination.endElement();
    }

    @Override
    public String toString() {
        return "suppliedParam(" + this.slotNumber + ")";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sxpath;

import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public final class XPathVariable
implements LocalBinding {
    private StructuredQName name;
    private SequenceType requiredType = SequenceType.ANY_SEQUENCE;
    private Sequence defaultValue;
    private int slotNumber;

    private XPathVariable() {
    }

    protected static XPathVariable make(StructuredQName name) {
        XPathVariable v = new XPathVariable();
        v.name = name;
        return v;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public final boolean isAssignable() {
        return false;
    }

    public void setRequiredType(SequenceType requiredType) {
        this.requiredType = requiredType;
    }

    @Override
    public SequenceType getRequiredType() {
        return this.requiredType;
    }

    @Override
    public IntegerValue[] getIntegerBoundsForVariable() {
        return null;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    @Override
    public int getLocalSlotNumber() {
        return this.slotNumber;
    }

    @Override
    public StructuredQName getVariableQName() {
        return this.name;
    }

    @Override
    public void addReference(VariableReference ref, boolean isLoopingReference) {
    }

    public void setDefaultValue(Sequence defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Sequence getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public Sequence evaluateVariable(XPathContext context) {
        return context.evaluateLocalVariable(this.slotNumber);
    }

    @Override
    public void setIndexedVariable() {
    }

    @Override
    public boolean isIndexedVariable() {
        return false;
    }
}


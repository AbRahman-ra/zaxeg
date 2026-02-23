/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public class LocalVariableBinding
implements LocalBinding {
    private StructuredQName variableName;
    private SequenceType requiredType;
    private int slotNumber = -999;
    private int refCount = 0;

    public LocalVariableBinding(StructuredQName name, SequenceType type) {
        this.variableName = name;
        this.requiredType = type;
    }

    public LocalVariableBinding copy() {
        LocalVariableBinding lb2 = new LocalVariableBinding(this.variableName, this.requiredType);
        lb2.slotNumber = this.slotNumber;
        lb2.refCount = this.refCount;
        return lb2;
    }

    @Override
    public StructuredQName getVariableQName() {
        return this.variableName;
    }

    public void setRequiredType(SequenceType type) {
        this.requiredType = type;
    }

    @Override
    public SequenceType getRequiredType() {
        return this.requiredType;
    }

    @Override
    public IntegerValue[] getIntegerBoundsForVariable() {
        return null;
    }

    public int getNominalReferenceCount() {
        return this.refCount;
    }

    @Override
    public void addReference(VariableReference ref, boolean isLoopingReference) {
        if (this.refCount != 10000) {
            this.refCount += isLoopingReference ? 10 : 1;
        }
    }

    @Override
    public void setIndexedVariable() {
        this.refCount = 10000;
    }

    @Override
    public boolean isIndexedVariable() {
        return this.refCount == 10000;
    }

    public void setVariableQName(StructuredQName variableName) {
        this.variableName = variableName;
    }

    public void setSlotNumber(int nr) {
        this.slotNumber = nr;
    }

    @Override
    public int getLocalSlotNumber() {
        return this.slotNumber;
    }

    @Override
    public Sequence evaluateVariable(XPathContext context) {
        return context.evaluateLocalVariable(this.slotNumber);
    }

    @Override
    public boolean isAssignable() {
        return false;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }
}


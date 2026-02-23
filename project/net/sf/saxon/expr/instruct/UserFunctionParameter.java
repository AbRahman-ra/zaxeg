/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.FunctionStreamability;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public class UserFunctionParameter
implements LocalBinding {
    private SequenceType requiredType;
    private StructuredQName variableQName;
    private int slotNumber;
    private int referenceCount = 999;
    private boolean isIndexed = false;
    private FunctionStreamability functionStreamability = FunctionStreamability.UNCLASSIFIED;

    @Override
    public final boolean isGlobal() {
        return false;
    }

    @Override
    public final boolean isAssignable() {
        return false;
    }

    public void setSlotNumber(int slot) {
        this.slotNumber = slot;
    }

    @Override
    public int getLocalSlotNumber() {
        return this.slotNumber;
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

    public void setVariableQName(StructuredQName name) {
        this.variableQName = name;
    }

    @Override
    public StructuredQName getVariableQName() {
        return this.variableQName;
    }

    @Override
    public void addReference(VariableReference ref, boolean isLoopingReference) {
    }

    public int getReferenceCount() {
        return this.referenceCount;
    }

    public void setIndexedVariable(boolean indexed) {
        this.isIndexed = indexed;
    }

    @Override
    public void setIndexedVariable() {
        this.setIndexedVariable(true);
    }

    @Override
    public boolean isIndexedVariable() {
        return this.isIndexed;
    }

    @Override
    public Sequence evaluateVariable(XPathContext context) {
        return context.evaluateLocalVariable(this.slotNumber);
    }

    public void setFunctionStreamability(FunctionStreamability ability) {
        this.functionStreamability = ability;
    }

    public FunctionStreamability getFunctionStreamability() {
        return this.functionStreamability;
    }
}


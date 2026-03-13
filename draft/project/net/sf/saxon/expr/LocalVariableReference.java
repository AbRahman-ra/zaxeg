/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;

public class LocalVariableReference
extends VariableReference {
    int slotNumber = -999;

    public LocalVariableReference(StructuredQName name) {
        super(name);
    }

    public LocalVariableReference(LocalBinding binding) {
        super(binding);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        if (this.binding == null) {
            throw new UnsupportedOperationException("Cannot copy a variable reference whose binding is unknown");
        }
        LocalVariableReference ref = new LocalVariableReference(this.getVariableName());
        ref.copyFrom(this);
        ref.slotNumber = this.slotNumber;
        Binding newBinding = rebindings.get(this.binding);
        if (newBinding != null) {
            ref.binding = newBinding;
        }
        ref.binding.addReference(ref, this.isInLoop());
        return ref;
    }

    public void setBinding(LocalBinding binding) {
        this.binding = binding;
    }

    @Override
    public LocalBinding getBinding() {
        return (LocalBinding)super.getBinding();
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public int getSlotNumber() {
        return this.slotNumber;
    }

    @Override
    public Sequence evaluateVariable(XPathContext c) throws XPathException {
        try {
            return c.getStackFrame().slots[this.slotNumber];
        } catch (ArrayIndexOutOfBoundsException err) {
            if (this.slotNumber == -999) {
                if (this.binding != null) {
                    try {
                        this.slotNumber = this.getBinding().getLocalSlotNumber();
                        return c.getStackFrame().slots[this.slotNumber];
                    } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                        // empty catch block
                    }
                }
                throw new ArrayIndexOutOfBoundsException("Local variable $" + this.getDisplayName() + " has not been allocated a stack frame slot");
            }
            int actual = c.getStackFrame().slots.length;
            throw new ArrayIndexOutOfBoundsException("Local variable $" + this.getDisplayName() + " uses slot " + this.slotNumber + " but " + (actual == 0 ? "no" : "only " + c.getStackFrame().slots.length) + " slots are allocated on the stack frame");
        }
    }

    @Override
    public String getExpressionName() {
        return "locVarRef";
    }
}


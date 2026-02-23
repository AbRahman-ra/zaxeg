/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Arrays;
import java.util.Stack;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.om.Sequence;

public class StackFrame {
    protected SlotManager map;
    protected Sequence[] slots;
    protected Stack<Sequence> dynamicStack;
    public static final StackFrame EMPTY = new StackFrame(SlotManager.EMPTY, new Sequence[0]);

    public StackFrame(SlotManager map, Sequence[] slots) {
        this.map = map;
        this.slots = slots;
    }

    public SlotManager getStackFrameMap() {
        return this.map;
    }

    public Sequence[] getStackFrameValues() {
        return this.slots;
    }

    public void setStackFrameValues(Sequence[] values) {
        this.slots = values;
    }

    public StackFrame copy() {
        Sequence[] v2 = Arrays.copyOf(this.slots, this.slots.length);
        StackFrame s = new StackFrame(this.map, v2);
        if (this.dynamicStack != null) {
            s.dynamicStack = new Stack();
            s.dynamicStack.addAll(this.dynamicStack);
        }
        return s;
    }

    public void pushDynamicValue(Sequence value) {
        if (this == EMPTY) {
            throw new IllegalStateException("Immutable stack frame");
        }
        if (this.dynamicStack == null) {
            this.dynamicStack = new Stack();
        }
        this.dynamicStack.push(value);
    }

    public Sequence popDynamicValue() {
        return this.dynamicStack.pop();
    }

    public boolean holdsDynamicValue() {
        return this.dynamicStack != null && !this.dynamicStack.empty();
    }
}


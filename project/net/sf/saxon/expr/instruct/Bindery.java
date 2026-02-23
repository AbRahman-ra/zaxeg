/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;

public final class Bindery {
    private GroundedValue[] globals;
    private long[] busy;

    public Bindery(PackageData pack) {
        this.allocateGlobals(pack.getGlobalSlotManager());
    }

    private void allocateGlobals(SlotManager map) {
        int n = map.getNumberOfVariables() + 1;
        this.globals = new GroundedValue[n];
        this.busy = new long[n];
        for (int i = 0; i < n; ++i) {
            this.globals[i] = null;
            this.busy[i] = -1L;
        }
    }

    public void setGlobalVariable(GlobalVariable binding, GroundedValue value) {
        this.globals[binding.getBinderySlotNumber()] = value;
    }

    public boolean setExecuting(GlobalVariable binding) throws XPathException {
        long thisThread = Thread.currentThread().getId();
        int slot = binding.getBinderySlotNumber();
        long busyThread = this.busy[slot];
        if (busyThread != -1L) {
            if (busyThread == thisThread) {
                throw new XPathException.Circularity("Circular definition of variable " + binding.getVariableQName().getDisplayName());
            }
            for (int i = 0; i < 10; ++i) {
                try {
                    Thread.sleep(20 * i);
                } catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                if (this.busy[slot] != -1L) continue;
                return false;
            }
            return true;
        }
        this.busy[slot] = thisThread;
        return true;
    }

    public void setNotExecuting(GlobalVariable binding) {
        int slot = binding.getBinderySlotNumber();
        this.busy[slot] = -1L;
    }

    public synchronized GroundedValue saveGlobalVariableValue(GlobalVariable binding, GroundedValue value) {
        int slot = binding.getBinderySlotNumber();
        if (this.globals[slot] != null) {
            return this.globals[slot];
        }
        this.busy[slot] = -1L;
        this.globals[slot] = value;
        return value;
    }

    public GroundedValue getGlobalVariableValue(GlobalVariable binding) {
        return this.globals[binding.getBinderySlotNumber()];
    }

    public GroundedValue getGlobalVariable(int slot) {
        return this.globals[slot];
    }

    public GroundedValue[] getGlobalVariables() {
        return this.globals;
    }

    public static class FailureValue
    extends ObjectValue<XPathException> {
        public FailureValue(XPathException err) {
            super(err);
        }
    }
}


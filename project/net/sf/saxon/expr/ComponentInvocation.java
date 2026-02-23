/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Component;
import net.sf.saxon.trans.SymbolicName;

public interface ComponentInvocation {
    public Component getFixedTarget();

    public void setBindingSlot(int var1);

    public int getBindingSlot();

    public SymbolicName getSymbolicName();
}


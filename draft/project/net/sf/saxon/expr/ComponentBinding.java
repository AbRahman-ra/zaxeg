/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Component;
import net.sf.saxon.trans.SymbolicName;

public class ComponentBinding {
    private SymbolicName symbolicName;
    private Component target;

    public ComponentBinding(SymbolicName name, Component target) {
        this.symbolicName = name;
        this.target = target;
    }

    public SymbolicName getSymbolicName() {
        return this.symbolicName;
    }

    public Component getTarget() {
        return this.target;
    }
}


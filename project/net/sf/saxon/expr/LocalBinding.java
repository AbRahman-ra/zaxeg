/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Binding;

public interface LocalBinding
extends Binding {
    public int getLocalSlotNumber();

    public void setIndexedVariable();

    public boolean isIndexedVariable();
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Binding;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.value.SequenceType;

public interface BindingReference {
    public void setStaticType(SequenceType var1, GroundedValue var2, int var3);

    public void fixup(Binding var1);
}


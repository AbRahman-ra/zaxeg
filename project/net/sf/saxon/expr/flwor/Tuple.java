/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.ObjectValue;

public class Tuple
extends ObjectValue<Sequence[]> {
    public Tuple(Sequence[] members) {
        super(members);
    }

    public Sequence[] getMembers() {
        return (Sequence[])this.getObject();
    }
}


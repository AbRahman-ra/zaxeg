/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.util.IdentityHashMap;
import java.util.Map;
import net.sf.saxon.expr.Binding;

public class RebindingMap {
    private Map<Binding, Binding> map = null;

    public void put(Binding oldBinding, Binding newBinding) {
        if (this.map == null) {
            this.map = new IdentityHashMap<Binding, Binding>();
        }
        this.map.put(oldBinding, newBinding);
    }

    public Binding get(Binding oldBinding) {
        return this.map == null ? null : this.map.get(oldBinding);
    }
}


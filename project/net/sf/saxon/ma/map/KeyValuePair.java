/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.value.AtomicValue;

public class KeyValuePair {
    public AtomicValue key;
    public GroundedValue value;

    public KeyValuePair(AtomicValue key, GroundedValue value) {
        this.key = key;
        this.value = value;
    }
}


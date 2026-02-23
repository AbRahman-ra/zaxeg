/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntIterator;

public class IntSingletonIterator
implements IntIterator {
    private int value;
    boolean gone = false;

    public IntSingletonIterator(int value) {
        this.value = value;
    }

    @Override
    public boolean hasNext() {
        return !this.gone;
    }

    @Override
    public int next() {
        this.gone = true;
        return this.value;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntIterator;

public class IntStepIterator
implements IntIterator {
    private int current;
    private int step;
    private int limit;

    public IntStepIterator(int start, int step, int limit) {
        this.current = start;
        this.step = step;
        this.limit = limit;
    }

    @Override
    public boolean hasNext() {
        return this.step > 0 ? this.current <= this.limit : this.current >= this.limit;
    }

    @Override
    public int next() {
        int n = this.current;
        this.current += this.step;
        return n;
    }
}


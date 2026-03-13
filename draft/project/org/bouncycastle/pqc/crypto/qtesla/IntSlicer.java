/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.qtesla;

final class IntSlicer {
    private final int[] values;
    private int base;

    IntSlicer(int[] nArray, int n) {
        this.values = nArray;
        this.base = n;
    }

    final int at(int n) {
        return this.values[this.base + n];
    }

    final int at(int n, int n2) {
        int n3 = n2;
        this.values[this.base + n] = n3;
        return n3;
    }

    final int at(int n, long l) {
        int n2 = (int)l;
        this.values[this.base + n] = n2;
        return n2;
    }

    final IntSlicer from(int n) {
        return new IntSlicer(this.values, this.base + n);
    }

    final void incBase(int n) {
        this.base += n;
    }

    final IntSlicer copy() {
        return new IntSlicer(this.values, this.base);
    }
}


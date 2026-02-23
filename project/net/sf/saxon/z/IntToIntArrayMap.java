/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntToIntMap;

public class IntToIntArrayMap
implements IntToIntMap {
    private int[] keys;
    private int[] values;
    private int used = 0;
    private int defaultValue = Integer.MIN_VALUE;

    public IntToIntArrayMap() {
        this.keys = new int[8];
        this.values = new int[8];
    }

    public IntToIntArrayMap(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity <= 0");
        }
        this.keys = new int[capacity];
        this.values = new int[capacity];
    }

    @Override
    public void clear() {
        this.used = 0;
    }

    @Override
    public boolean find(int key) {
        for (int i = 0; i < this.used; ++i) {
            if (this.keys[i] != key) continue;
            return true;
        }
        return false;
    }

    @Override
    public int get(int key) {
        for (int i = 0; i < this.used; ++i) {
            if (this.keys[i] != key) continue;
            return this.values[i];
        }
        return this.defaultValue;
    }

    @Override
    public int getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public IntIterator keyIterator() {
        return new KeyIterator();
    }

    @Override
    public void put(int key, int value) {
        for (int i = 0; i < this.used; ++i) {
            if (this.keys[i] != key) continue;
            this.values[i] = value;
            return;
        }
        if (this.used >= this.keys.length) {
            int[] k2 = new int[this.used * 2];
            System.arraycopy(this.keys, 0, k2, 0, this.used);
            this.keys = k2;
            int[] v2 = new int[this.used * 2];
            System.arraycopy(this.values, 0, v2, 0, this.used);
            this.values = v2;
        }
        this.keys[this.used] = key;
        this.values[this.used++] = value;
    }

    @Override
    public boolean remove(int key) {
        for (int i = 0; i < this.used; ++i) {
            if (this.keys[i] != key) continue;
            this.values[i] = this.defaultValue;
            return true;
        }
        return false;
    }

    @Override
    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public int size() {
        return this.used;
    }

    private class KeyIterator
    implements IntIterator {
        private int i = 0;
        private static final long serialVersionUID = 1720894017771245276L;

        @Override
        public boolean hasNext() {
            return this.i < IntToIntArrayMap.this.used;
        }

        @Override
        public int next() {
            return IntToIntArrayMap.this.keys[this.i++];
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntToIntMap;

public class IntToIntHashMap
implements IntToIntMap {
    private static final int NBIT = 30;
    private static final int NMAX = 0x40000000;
    private double _factor;
    private int _defaultValue = Integer.MAX_VALUE;
    private int _nmax;
    private int _n;
    private int _nlo;
    private int _nhi;
    private int _shift;
    private int _mask;
    private int[] _key;
    private int[] _value;
    private boolean[] _filled;

    public IntToIntHashMap() {
        this(8, 0.25);
    }

    public IntToIntHashMap(int capacity) {
        this(capacity, 0.25);
    }

    public IntToIntHashMap(int capacity, double factor) {
        this._factor = factor;
        this.setCapacity(capacity);
    }

    @Override
    public void setDefaultValue(int defaultValue) {
        this._defaultValue = defaultValue;
    }

    @Override
    public int getDefaultValue() {
        return this._defaultValue;
    }

    @Override
    public void clear() {
        this._n = 0;
        for (int i = 0; i < this._nmax; ++i) {
            this._filled[i] = false;
        }
    }

    @Override
    public boolean find(int key) {
        return this._filled[this.indexOf(key)];
    }

    @Override
    public int get(int key) {
        int i = this.indexOf(key);
        return this._filled[i] ? this._value[i] : this._defaultValue;
    }

    @Override
    public int size() {
        return this._n;
    }

    @Override
    public boolean remove(int key) {
        int i = this.indexOf(key);
        if (!this._filled[i]) {
            return false;
        }
        --this._n;
        while (true) {
            int r;
            this._filled[i] = false;
            int j = i;
            do {
                if (this._filled[i = i - 1 & this._mask]) continue;
                return true;
            } while (i <= (r = this.hash(this._key[i])) && r < j || r < j && j < i || j < i && i <= r);
            this._key[j] = this._key[i];
            this._value[j] = this._value[i];
            this._filled[j] = this._filled[i];
        }
    }

    @Override
    public void put(int key, int value) {
        int i = this.indexOf(key);
        if (this._filled[i]) {
            this._value[i] = value;
        } else {
            this._key[i] = key;
            this._value[i] = value;
            this._filled[i] = true;
            this.grow();
        }
    }

    @Override
    public IntIterator keyIterator() {
        return new IntToIntHashMapKeyIterator();
    }

    private int hash(int key) {
        return 1327217885 * key >> this._shift & this._mask;
    }

    private int indexOf(int key) {
        int i = this.hash(key);
        while (this._filled[i]) {
            if (this._key[i] == key) {
                return i;
            }
            i = i - 1 & this._mask;
        }
        return i;
    }

    private void grow() {
        ++this._n;
        if (this._n > 0x40000000) {
            throw new RuntimeException("number of keys mapped exceeds 1073741824");
        }
        if (this._nlo < this._n && this._n <= this._nhi) {
            this.setCapacity(this._n);
        }
    }

    private void setCapacity(int capacity) {
        int nmax;
        if (capacity < this._n) {
            capacity = this._n;
        }
        double factor = this._factor < 0.01 ? 0.01 : (this._factor > 0.99 ? 0.99 : this._factor);
        int nbit = 1;
        for (nmax = 2; (double)nmax * factor < (double)capacity && nmax < 0x40000000; nmax *= 2) {
            ++nbit;
        }
        int nold = this._nmax;
        if (nmax == nold) {
            return;
        }
        this._nmax = nmax;
        this._nlo = (int)((double)nmax * factor);
        this._nhi = (int)(1.073741824E9 * factor);
        this._shift = 31 - nbit;
        this._mask = nmax - 1;
        int[] key = this._key;
        int[] value = this._value;
        boolean[] filled = this._filled;
        this._n = 0;
        this._key = new int[nmax];
        this._value = new int[nmax];
        this._filled = new boolean[nmax];
        if (key != null) {
            for (int i = 0; i < nold; ++i) {
                if (!filled[i]) continue;
                this.put(key[i], value[i]);
            }
        }
    }

    public String toString() {
        FastStringBuffer buffer = new FastStringBuffer(256);
        buffer.append("{");
        IntIterator keys = this.keyIterator();
        int count = 0;
        while (keys.hasNext()) {
            int k = keys.next();
            int v = this.get(k);
            buffer.append(" " + k + ":" + v + ",");
            if (count++ < 100) continue;
            buffer.append("....");
            break;
        }
        buffer.setCharAt(buffer.length() - 1, '}');
        return buffer.toString();
    }

    private class IntToIntHashMapKeyIterator
    implements IntIterator {
        private int i = 0;

        @Override
        public boolean hasNext() {
            while (this.i < IntToIntHashMap.this._key.length) {
                if (IntToIntHashMap.this._filled[this.i]) {
                    return true;
                }
                ++this.i;
            }
            return false;
        }

        @Override
        public int next() {
            return IntToIntHashMap.this._key[this.i++];
        }
    }
}


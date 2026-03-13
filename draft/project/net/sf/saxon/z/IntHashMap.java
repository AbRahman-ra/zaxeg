/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;

public class IntHashMap<T> {
    private static final int NBIT = 30;
    private static final int NMAX = 0x40000000;
    private double _factor;
    private int _nmax;
    private int _n;
    private int _nlo;
    private int _nhi;
    private int _shift;
    private int _mask;
    private int[] _key;
    private T[] _value;

    public IntHashMap() {
        this(8, 0.25);
    }

    public IntHashMap(int capacity) {
        this(capacity, 0.25);
    }

    public IntHashMap(int capacity, double factor) {
        this._factor = factor;
        this.setCapacity(capacity);
    }

    public void clear() {
        this._n = 0;
        for (int i = 0; i < this._nmax; ++i) {
            this._value[i] = null;
        }
    }

    public T get(int key) {
        return this._value[this.indexOf(key)];
    }

    public int size() {
        return this._n;
    }

    public boolean remove(int key) {
        int i = this.indexOf(key);
        if (this._value[i] == null) {
            return false;
        }
        --this._n;
        while (true) {
            int r;
            this._value[i] = null;
            int j = i;
            do {
                if (this._value[i = i - 1 & this._mask] != null) continue;
                return true;
            } while (i <= (r = this.hash(this._key[i])) && r < j || r < j && j < i || j < i && i <= r);
            this._key[j] = this._key[i];
            this._value[j] = this._value[i];
        }
    }

    public T put(int key, T value) {
        if (value == null) {
            throw new NullPointerException("IntHashMap does not allow null values");
        }
        int i = this.indexOf(key);
        T old = this._value[i];
        if (old != null) {
            this._value[i] = value;
        } else {
            this._key[i] = key;
            this._value[i] = value;
            this.grow();
        }
        return old;
    }

    private int hash(int key) {
        return 1327217885 * key >> this._shift & this._mask;
    }

    private int indexOf(int key) {
        int i = this.hash(key);
        while (this._value[i] != null) {
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
        T[] value = this._value;
        this._n = 0;
        this._key = new int[nmax];
        this._value = new Object[nmax];
        if (key != null) {
            for (int i = 0; i < nold; ++i) {
                if (value[i] == null) continue;
                this.put(key[i], value[i]);
            }
        }
    }

    public IntIterator keyIterator() {
        return new IntHashMapKeyIterator();
    }

    public Iterator<T> valueIterator() {
        return new IntHashMapValueIterator();
    }

    public Iterable<T> valueSet() {
        return new Iterable<T>(){

            @Override
            public Iterator<T> iterator() {
                return IntHashMap.this.valueIterator();
            }
        };
    }

    public IntHashMap<T> copy() {
        IntHashMap<T> n = new IntHashMap<T>(this.size());
        IntIterator it = this.keyIterator();
        while (it.hasNext()) {
            int k = it.next();
            n.put(k, this.get(k));
        }
        return n;
    }

    public void display(PrintStream ps) {
        IntHashMapKeyIterator iter = new IntHashMapKeyIterator();
        while (iter.hasNext()) {
            int key = iter.next();
            T value = this.get(key);
            ps.println(key + " -> " + value.toString());
        }
    }

    public IntSet keySet() {
        return new IntSet(){

            @Override
            public void clear() {
                throw new UnsupportedOperationException("Immutable set");
            }

            @Override
            public IntSet copy() {
                IntHashSet s = new IntHashSet();
                IntIterator ii = this.iterator();
                while (ii.hasNext()) {
                    s.add(ii.next());
                }
                return s;
            }

            @Override
            public IntSet mutableCopy() {
                return this.copy();
            }

            @Override
            public boolean isMutable() {
                return false;
            }

            @Override
            public int size() {
                return IntHashMap.this._n;
            }

            @Override
            public boolean isEmpty() {
                return IntHashMap.this._n == 0;
            }

            @Override
            public boolean contains(int key) {
                return IntHashMap.this._value[IntHashMap.this.indexOf(key)] != null;
            }

            @Override
            public boolean remove(int value) {
                throw new UnsupportedOperationException("Immutable set");
            }

            @Override
            public boolean add(int value) {
                throw new UnsupportedOperationException("Immutable set");
            }

            @Override
            public IntIterator iterator() {
                return new IntHashMapKeyIterator();
            }

            @Override
            public IntSet union(IntSet other) {
                return this.copy().union(other);
            }

            @Override
            public IntSet intersect(IntSet other) {
                return this.copy().intersect(other);
            }

            @Override
            public IntSet except(IntSet other) {
                return this.copy().except(other);
            }

            @Override
            public boolean containsAll(IntSet other) {
                return this.copy().containsAll(other);
            }

            public String toString() {
                return IntHashSet.toString(this.iterator());
            }
        };
    }

    private class IntHashMapValueIterator
    implements Iterator<T> {
        private int i = 0;

        @Override
        public boolean hasNext() {
            while (this.i < IntHashMap.this._key.length) {
                if (IntHashMap.this._value[this.i] != null) {
                    return true;
                }
                ++this.i;
            }
            return false;
        }

        @Override
        public T next() {
            Object temp = IntHashMap.this._value[this.i++];
            if (temp == null) {
                throw new NoSuchElementException();
            }
            return temp;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    private class IntHashMapKeyIterator
    implements IntIterator {
        private int i = 0;

        @Override
        public boolean hasNext() {
            while (this.i < IntHashMap.this._key.length) {
                if (IntHashMap.this._value[this.i] != null) {
                    return true;
                }
                ++this.i;
            }
            return false;
        }

        @Override
        public int next() {
            return IntHashMap.this._key[this.i++];
        }
    }
}


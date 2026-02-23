/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import java.util.Arrays;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntComplementSet;
import net.sf.saxon.z.IntEmptySet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntUniversalSet;

public class IntHashSet
implements IntSet {
    private static final int NBIT = 30;
    private static final int MAX_SIZE = 0x40000000;
    private final int ndv;
    private int _nmax;
    private int _size;
    private int _nlo;
    private int _nhi;
    private int _shift;
    private int _mask;
    private int[] _values;

    public IntHashSet() {
        this(8, Integer.MIN_VALUE);
    }

    public IntHashSet(int capacity) {
        this(capacity, Integer.MIN_VALUE);
    }

    public IntHashSet(int capacity, int noDataValue) {
        this.ndv = noDataValue;
        this.setCapacity(capacity);
    }

    @Override
    public IntSet copy() {
        if (this._size == 0) {
            return IntEmptySet.getInstance();
        }
        IntHashSet s = new IntHashSet(this._size, this.ndv);
        s._nmax = this._nmax;
        s._size = this._size;
        s._nlo = this._nlo;
        s._nhi = this._nhi;
        s._shift = this._shift;
        s._mask = this._mask;
        s._values = new int[this._values.length];
        System.arraycopy(this._values, 0, s._values, 0, this._values.length);
        return s;
    }

    @Override
    public IntSet mutableCopy() {
        return this.copy();
    }

    @Override
    public void clear() {
        this._size = 0;
        for (int i = 0; i < this._nmax; ++i) {
            this._values[i] = this.ndv;
        }
    }

    @Override
    public int size() {
        return this._size;
    }

    @Override
    public boolean isEmpty() {
        return this._size == 0;
    }

    public int[] getValues() {
        int index = 0;
        int[] values = new int[this._size];
        for (int _value : this._values) {
            if (_value == this.ndv) continue;
            values[index++] = _value;
        }
        return values;
    }

    @Override
    public boolean contains(int value) {
        return this._values[this.indexOf(value)] != this.ndv;
    }

    @Override
    public boolean remove(int value) {
        int i = this.indexOf(value);
        if (this._values[i] == this.ndv) {
            return false;
        }
        --this._size;
        while (true) {
            int r;
            this._values[i] = this.ndv;
            int j = i;
            do {
                if (this._values[i = i - 1 & this._mask] != this.ndv) continue;
                return true;
            } while (i <= (r = this.hash(this._values[i])) && r < j || r < j && j < i || j < i && i <= r);
            this._values[j] = this._values[i];
        }
    }

    @Override
    public boolean add(int value) {
        if (value == this.ndv) {
            throw new IllegalArgumentException("Can't add the 'no data' value");
        }
        int i = this.indexOf(value);
        if (this._values[i] == this.ndv) {
            ++this._size;
            this._values[i] = value;
            if (this._size > 0x40000000) {
                throw new RuntimeException("Too many elements (> 1073741824)");
            }
            if (this._nlo < this._size && this._size <= this._nhi) {
                this.setCapacity(this._size);
            }
            return true;
        }
        return false;
    }

    private int hash(int key) {
        return 1327217885 * key >> this._shift & this._mask;
    }

    private int indexOf(int value) {
        int i = this.hash(value);
        while (this._values[i] != this.ndv) {
            if (this._values[i] == value) {
                return i;
            }
            i = i - 1 & this._mask;
        }
        return i;
    }

    private void setCapacity(int capacity) {
        int nmax;
        if (capacity < this._size) {
            capacity = this._size;
        }
        int nbit = 1;
        for (nmax = 2; nmax < capacity * 4 && nmax < 0x40000000; nmax *= 2) {
            ++nbit;
        }
        int nold = this._nmax;
        if (nmax == nold) {
            return;
        }
        this._nmax = nmax;
        this._nlo = nmax / 4;
        this._nhi = 0x10000000;
        this._shift = 31 - nbit;
        this._mask = nmax - 1;
        this._size = 0;
        int[] values = this._values;
        this._values = new int[nmax];
        Arrays.fill(this._values, this.ndv);
        if (values != null) {
            for (int i = 0; i < nold; ++i) {
                int value = values[i];
                if (value == this.ndv) continue;
                ++this._size;
                this._values[this.indexOf((int)value)] = value;
            }
        }
    }

    @Override
    public IntIterator iterator() {
        return new IntHashSetIterator();
    }

    public static boolean containsSome(IntSet one, IntSet two) {
        if (two instanceof IntEmptySet) {
            return false;
        }
        if (two instanceof IntUniversalSet) {
            return !one.isEmpty();
        }
        if (two instanceof IntComplementSet) {
            return !((IntComplementSet)two).getExclusions().containsAll(one);
        }
        IntIterator it = two.iterator();
        while (it.hasNext()) {
            if (!one.contains(it.next())) continue;
            return true;
        }
        return false;
    }

    public boolean equals(Object other) {
        if (other instanceof IntSet) {
            IntHashSet s = (IntHashSet)other;
            return this.size() == s.size() && this.containsAll(s);
        }
        return false;
    }

    public int hashCode() {
        int h = 936247625;
        IntIterator it = this.iterator();
        while (it.hasNext()) {
            h += it.next();
        }
        return h;
    }

    public String toString() {
        return IntHashSet.toString(this.iterator());
    }

    public static String toString(IntIterator it) {
        FastStringBuffer sb = new FastStringBuffer(100);
        while (it.hasNext()) {
            if (sb.isEmpty()) {
                sb.append("" + it.next());
                continue;
            }
            sb.append(" " + it.next());
        }
        return sb.toString();
    }

    public void diagnosticDump() {
        System.err.println("Contents of IntHashSet");
        FastStringBuffer sb = new FastStringBuffer(100);
        for (int i = 0; i < this._values.length; ++i) {
            if (i % 10 == 0) {
                System.err.println(sb.toString());
                sb.setLength(0);
            }
            if (this._values[i] == this.ndv) {
                sb.append("*, ");
                continue;
            }
            sb.append(this._values[i] + ", ");
        }
        System.err.println(sb.toString());
        sb.setLength(0);
        System.err.println("size: " + this._size);
        System.err.println("ndv: " + this.ndv);
        System.err.println("nlo: " + this._nlo);
        System.err.println("nhi: " + this._nhi);
        System.err.println("nmax: " + this._nmax);
        System.err.println("shift: " + this._shift);
        System.err.println("mask: " + this._mask);
        System.err.println("Result of iterator:");
        IntIterator iter = this.iterator();
        int i = 0;
        while (iter.hasNext()) {
            if (i++ % 10 == 0) {
                System.err.println(sb.toString());
                sb.setLength(0);
            }
            sb.append(iter.next() + ", ");
        }
        System.err.println(sb.toString());
        System.err.println("=====================");
    }

    public static IntHashSet of(int ... members) {
        IntHashSet is = new IntHashSet(members.length);
        for (int i : members) {
            is.add(i);
        }
        return is;
    }

    private class IntHashSetIterator
    implements IntIterator {
        private int i = 0;

        IntHashSetIterator() {
        }

        @Override
        public boolean hasNext() {
            while (this.i < IntHashSet.this._values.length) {
                if (IntHashSet.this._values[this.i] != IntHashSet.this.ndv) {
                    return true;
                }
                ++this.i;
            }
            return false;
        }

        @Override
        public int next() {
            return IntHashSet.this._values[this.i++];
        }
    }
}


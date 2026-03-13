/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import java.util.Arrays;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntArraySet;
import net.sf.saxon.z.IntComplementSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntUniversalSet;

public class MonotonicIntSet
implements IntSet {
    private int[] contents;
    private int used = 0;

    public MonotonicIntSet() {
        this.contents = new int[4];
        this.used = 0;
    }

    @Override
    public IntSet copy() {
        MonotonicIntSet i2 = new MonotonicIntSet();
        i2.contents = Arrays.copyOf(this.contents, this.used);
        i2.used = this.used;
        return i2;
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
    public void clear() {
        if (this.contents.length > this.used + 20) {
            this.contents = new int[4];
        }
        this.used = 0;
    }

    @Override
    public int size() {
        return this.used;
    }

    @Override
    public boolean isEmpty() {
        return this.used == 0;
    }

    @Override
    public boolean contains(int value) {
        return Arrays.binarySearch(this.contents, 0, this.used, value) >= 0;
    }

    @Override
    public boolean remove(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(int value) {
        if (this.used > 0) {
            int last = this.contents[this.used - 1];
            if (value == last) {
                return false;
            }
            if (value < last) {
                throw new UnsupportedOperationException("Values must be added in monotonic order");
            }
        }
        if (this.used == this.contents.length) {
            this.contents = Arrays.copyOf(this.contents, this.used == 0 ? 4 : this.used * 2);
        }
        this.contents[this.used++] = value;
        return true;
    }

    @Override
    public IntIterator iterator() {
        return new IntArraySet.IntArrayIterator(this.contents, this.used);
    }

    @Override
    public IntSet union(IntSet other) {
        if (this.size() == 0) {
            return other.copy();
        }
        if (other.isEmpty()) {
            return this.copy();
        }
        if (other == IntUniversalSet.getInstance()) {
            return other;
        }
        if (other instanceof IntComplementSet) {
            return other.union(this);
        }
        if (this.equals(other)) {
            return this.copy();
        }
        if (other instanceof MonotonicIntSet) {
            int[] merged = new int[this.size() + other.size()];
            int[] a = this.contents;
            int[] b = ((MonotonicIntSet)other).contents;
            int m = this.used;
            int n = ((MonotonicIntSet)other).used;
            int o = 0;
            int i = 0;
            int j = 0;
            do {
                if (a[i] < b[j]) {
                    merged[o++] = a[i++];
                } else if (b[j] < a[i]) {
                    merged[o++] = b[j++];
                } else {
                    merged[o++] = a[i++];
                    ++j;
                }
                if (i != m) continue;
                System.arraycopy(b, j, merged, o, n - j);
                return MonotonicIntSet.make(merged, o += n - j);
            } while (j != n);
            System.arraycopy(a, i, merged, o, m - i);
            return MonotonicIntSet.make(merged, o += m - i);
        }
        return IntSet.super.union(other);
    }

    public static MonotonicIntSet make(int[] in, int size) {
        return new MonotonicIntSet(in, size);
    }

    private MonotonicIntSet(int[] content, int used) {
        this.contents = content;
        this.used = used;
    }

    public String toString() {
        FastStringBuffer sb = new FastStringBuffer(this.used * 4);
        for (int i = 0; i < this.used; ++i) {
            if (i == this.used - 1) {
                sb.append(this.contents[i] + "");
                continue;
            }
            if (this.contents[i] + 1 != this.contents[i + 1]) {
                sb.append(this.contents[i] + ",");
                continue;
            }
            int j = i + 1;
            while (this.contents[j] == this.contents[j - 1] + 1 && ++j != this.used) {
            }
            sb.append(this.contents[i] + "-" + this.contents[j - 1] + ",");
            i = j;
        }
        return sb.toString();
    }

    public boolean equals(Object other) {
        if (other instanceof MonotonicIntSet) {
            MonotonicIntSet s = (MonotonicIntSet)other;
            if (this.used != s.used) {
                return false;
            }
            for (int i = 0; i < this.used; ++i) {
                if (this.contents[i] == s.contents[i]) continue;
                return false;
            }
            return true;
        }
        return other instanceof IntSet && this.used == ((IntSet)other).size() && this.containsAll((IntSet)other);
    }

    public int hashCode() {
        int h = 936247625;
        IntIterator it = this.iterator();
        while (it.hasNext()) {
            h += it.next();
        }
        return h;
    }
}


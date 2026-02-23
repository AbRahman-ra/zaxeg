/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import java.util.Arrays;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntComplementSet;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntUniversalSet;

public class IntArraySet
implements IntSet {
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    private int[] contents;
    private int hashCode = -1;

    public IntArraySet() {
        this.contents = EMPTY_INT_ARRAY;
    }

    public IntArraySet(IntHashSet input) {
        this.contents = input.getValues();
        Arrays.sort(this.contents);
    }

    public IntArraySet(IntArraySet input) {
        this.contents = new int[input.contents.length];
        System.arraycopy(input.contents, 0, this.contents, 0, this.contents.length);
    }

    @Override
    public IntSet copy() {
        IntArraySet i2 = new IntArraySet();
        i2.contents = new int[this.contents.length];
        System.arraycopy(this.contents, 0, i2.contents, 0, this.contents.length);
        return i2;
    }

    @Override
    public IntSet mutableCopy() {
        return this.copy();
    }

    @Override
    public void clear() {
        this.contents = EMPTY_INT_ARRAY;
        this.hashCode = -1;
    }

    @Override
    public int size() {
        return this.contents.length;
    }

    @Override
    public boolean isEmpty() {
        return this.contents.length == 0;
    }

    public int[] getValues() {
        return this.contents;
    }

    @Override
    public boolean contains(int value) {
        return Arrays.binarySearch(this.contents, value) >= 0;
    }

    @Override
    public boolean remove(int value) {
        this.hashCode = -1;
        int pos = Arrays.binarySearch(this.contents, value);
        if (pos < 0) {
            return false;
        }
        int[] newArray = new int[this.contents.length - 1];
        if (pos > 0) {
            System.arraycopy(this.contents, 0, newArray, 0, pos);
        }
        if (pos < newArray.length) {
            System.arraycopy(this.contents, pos + 1, newArray, pos, this.contents.length - pos);
        }
        this.contents = newArray;
        return true;
    }

    @Override
    public boolean add(int value) {
        this.hashCode = -1;
        if (this.contents.length == 0) {
            this.contents = new int[]{value};
            return true;
        }
        int pos = Arrays.binarySearch(this.contents, value);
        if (pos >= 0) {
            return false;
        }
        pos = -pos - 1;
        int[] newArray = new int[this.contents.length + 1];
        if (pos > 0) {
            System.arraycopy(this.contents, 0, newArray, 0, pos);
        }
        newArray[pos] = value;
        if (pos < this.contents.length) {
            System.arraycopy(this.contents, pos, newArray, pos + 1, newArray.length - pos);
        }
        this.contents = newArray;
        return true;
    }

    public int getFirst() {
        return this.contents[0];
    }

    @Override
    public IntIterator iterator() {
        return new IntArrayIterator(this.contents, this.contents.length);
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
        if (other instanceof IntArraySet) {
            int[] merged = new int[this.size() + other.size()];
            int[] a = this.contents;
            int[] b = ((IntArraySet)other).contents;
            int m = a.length;
            int n = b.length;
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
                return IntArraySet.make(merged, o += n - j);
            } while (j != n);
            System.arraycopy(a, i, merged, o, m - i);
            return IntArraySet.make(merged, o += m - i);
        }
        return IntSet.super.union(other);
    }

    public static IntArraySet make(int[] in, int size) {
        int[] out;
        if (in.length == size) {
            out = in;
        } else {
            out = new int[size];
            System.arraycopy(in, 0, out, 0, size);
        }
        return new IntArraySet(out);
    }

    private IntArraySet(int[] content) {
        this.contents = content;
    }

    public String toString() {
        FastStringBuffer sb = new FastStringBuffer(this.contents.length * 4);
        for (int i = 0; i < this.contents.length; ++i) {
            if (i == this.contents.length - 1) {
                sb.append(this.contents[i] + "");
                continue;
            }
            if (this.contents[i] + 1 != this.contents[i + 1]) {
                sb.append(this.contents[i] + ",");
                continue;
            }
            int j = i + 1;
            while (this.contents[j] == this.contents[j - 1] + 1 && ++j != this.contents.length) {
            }
            sb.append(this.contents[i] + "-" + this.contents[j - 1] + ",");
            i = j - 1;
        }
        return sb.toString();
    }

    public boolean equals(Object other) {
        if (other instanceof IntArraySet) {
            IntArraySet s = (IntArraySet)other;
            return this.hashCode() == other.hashCode() && Arrays.equals(this.contents, s.contents);
        }
        return other instanceof IntSet && this.contents.length == ((IntSet)other).size() && this.containsAll((IntSet)other);
    }

    public int hashCode() {
        if (this.hashCode == -1) {
            int h = 936247625;
            IntIterator it = this.iterator();
            while (it.hasNext()) {
                h += it.next();
            }
            this.hashCode = h;
        }
        return this.hashCode;
    }

    public static class IntArrayIterator
    implements IntIterator {
        private int[] contents;
        private int limit;
        private int i = 0;

        public IntArrayIterator(int[] contents, int limit) {
            this.contents = contents;
            this.limit = limit;
        }

        @Override
        public boolean hasNext() {
            return this.i < this.limit;
        }

        @Override
        public int next() {
            return this.contents[this.i++];
        }
    }
}


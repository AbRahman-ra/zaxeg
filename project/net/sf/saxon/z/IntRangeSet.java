/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import java.util.Arrays;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;

public class IntRangeSet
implements IntSet {
    private int[] startPoints;
    private int[] endPoints;
    private int used = 0;
    private int hashCode = -1;
    private int size = 0;

    public IntRangeSet() {
        this.startPoints = new int[4];
        this.endPoints = new int[4];
        this.used = 0;
        this.size = 0;
        this.hashCode = -1;
    }

    public IntRangeSet(IntRangeSet input) {
        this.startPoints = new int[input.used];
        this.endPoints = new int[input.used];
        this.used = input.used;
        System.arraycopy(input.startPoints, 0, this.startPoints, 0, this.used);
        System.arraycopy(input.endPoints, 0, this.endPoints, 0, this.used);
        this.hashCode = input.hashCode;
    }

    public IntRangeSet(int[] startPoints, int[] endPoints) {
        if (startPoints.length != endPoints.length) {
            throw new IllegalArgumentException("Array lengths differ");
        }
        this.startPoints = startPoints;
        this.endPoints = endPoints;
        this.used = startPoints.length;
        for (int i = 0; i < this.used; ++i) {
            this.size += endPoints[i] - startPoints[i] + 1;
        }
    }

    @Override
    public void clear() {
        this.startPoints = new int[4];
        this.endPoints = new int[4];
        this.used = 0;
        this.hashCode = -1;
    }

    @Override
    public IntSet copy() {
        IntRangeSet s = new IntRangeSet();
        s.startPoints = new int[this.startPoints.length];
        System.arraycopy(this.startPoints, 0, s.startPoints, 0, this.startPoints.length);
        s.endPoints = new int[this.endPoints.length];
        System.arraycopy(this.endPoints, 0, s.endPoints, 0, this.endPoints.length);
        s.used = this.used;
        s.size = this.size;
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
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(int value) {
        if (this.used == 0) {
            return false;
        }
        if (value > this.endPoints[this.used - 1]) {
            return false;
        }
        if (value < this.startPoints[0]) {
            return false;
        }
        int i = 0;
        int j = this.used;
        do {
            int mid;
            if (this.endPoints[mid = i + (j - i) / 2] < value) {
                i = Math.max(mid, i + 1);
                continue;
            }
            if (this.startPoints[mid] > value) {
                j = Math.min(mid, j - 1);
                continue;
            }
            return true;
        } while (i != j);
        return false;
    }

    @Override
    public boolean remove(int value) {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public boolean add(int value) {
        this.hashCode = -1;
        if (this.used == 0) {
            this.ensureCapacity(1);
            this.startPoints[this.used - 1] = value;
            this.endPoints[this.used - 1] = value;
            ++this.size;
            return true;
        }
        if (value > this.endPoints[this.used - 1]) {
            if (value == this.endPoints[this.used - 1] + 1) {
                int n = this.used - 1;
                this.endPoints[n] = this.endPoints[n] + 1;
            } else {
                this.ensureCapacity(this.used + 1);
                this.startPoints[this.used - 1] = value;
                this.endPoints[this.used - 1] = value;
            }
            ++this.size;
            return true;
        }
        if (value < this.startPoints[0]) {
            if (value == this.startPoints[0] - 1) {
                this.startPoints[0] = this.startPoints[0] - 1;
            } else {
                this.ensureCapacity(this.used + 1);
                System.arraycopy(this.startPoints, 0, this.startPoints, 1, this.used - 1);
                System.arraycopy(this.endPoints, 0, this.endPoints, 1, this.used - 1);
                this.startPoints[0] = value;
                this.endPoints[0] = value;
            }
            ++this.size;
            return true;
        }
        int i = 0;
        int j = this.used;
        do {
            int mid;
            if (this.endPoints[mid = i + (j - i) / 2] < value) {
                i = Math.max(mid, i + 1);
                continue;
            }
            if (this.startPoints[mid] > value) {
                j = Math.min(mid, j - 1);
                continue;
            }
            return false;
        } while (i != j);
        if (i > 0 && this.endPoints[i - 1] + 1 == value) {
            --i;
        } else if (i < this.used - 1 && this.startPoints[i + 1] - 1 == value) {
            ++i;
        }
        if (this.endPoints[i] + 1 == value) {
            if (value == this.startPoints[i + 1] - 1) {
                this.endPoints[i] = this.endPoints[i + 1];
                System.arraycopy(this.startPoints, i + 2, this.startPoints, i + 1, this.used - i - 2);
                System.arraycopy(this.endPoints, i + 2, this.endPoints, i + 1, this.used - i - 2);
                --this.used;
            } else {
                int n = i;
                this.endPoints[n] = this.endPoints[n] + 1;
            }
            ++this.size;
            return true;
        }
        if (this.startPoints[i] - 1 == value) {
            if (value == this.endPoints[i - 1] + 1) {
                this.endPoints[i - 1] = this.endPoints[i];
                System.arraycopy(this.startPoints, i + 1, this.startPoints, i, this.used - i - 1);
                System.arraycopy(this.endPoints, i + 1, this.endPoints, i, this.used - i - 1);
                --this.used;
            } else {
                int n = i;
                this.startPoints[n] = this.startPoints[n] - 1;
            }
            ++this.size;
            return true;
        }
        if (value > this.endPoints[i]) {
            ++i;
        }
        this.ensureCapacity(this.used + 1);
        try {
            System.arraycopy(this.startPoints, i, this.startPoints, i + 1, this.used - i - 1);
            System.arraycopy(this.endPoints, i, this.endPoints, i + 1, this.used - i - 1);
        } catch (Exception err) {
            err.printStackTrace();
        }
        this.startPoints[i] = value;
        this.endPoints[i] = value;
        ++this.size;
        return true;
    }

    private void ensureCapacity(int n) {
        if (this.startPoints.length < n) {
            int[] s = new int[this.startPoints.length * 2];
            int[] e = new int[this.startPoints.length * 2];
            System.arraycopy(this.startPoints, 0, s, 0, this.used);
            System.arraycopy(this.endPoints, 0, e, 0, this.used);
            this.startPoints = s;
            this.endPoints = e;
        }
        this.used = n;
    }

    @Override
    public IntIterator iterator() {
        return new IntRangeSetIterator();
    }

    public String toString() {
        FastStringBuffer sb = new FastStringBuffer(this.used * 8);
        for (int i = 0; i < this.used; ++i) {
            sb.append(this.startPoints[i] + "-" + this.endPoints[i] + ",");
        }
        return sb.toString();
    }

    public boolean equals(Object other) {
        if (other instanceof IntSet) {
            if (other instanceof IntRangeSet) {
                return this.used == ((IntRangeSet)other).used && Arrays.equals(this.startPoints, ((IntRangeSet)other).startPoints) && Arrays.equals(this.endPoints, ((IntRangeSet)other).endPoints);
            }
            return this.containsAll((IntSet)other);
        }
        return false;
    }

    public int hashCode() {
        if (this.hashCode == -1) {
            int h = -2090169871;
            for (int i = 0; i < this.used; ++i) {
                h ^= this.startPoints[i] + (this.endPoints[i] << 3);
            }
            this.hashCode = h;
        }
        return this.hashCode;
    }

    public void addRange(int low, int high) {
        if (low == high) {
            this.add(low);
            return;
        }
        this.hashCode = -1;
        if (this.used == 0) {
            this.ensureCapacity(1);
            this.startPoints[this.used - 1] = low;
            this.endPoints[this.used - 1] = high;
            this.size += high - low + 1;
        } else if (low > this.endPoints[this.used - 1]) {
            if (low == this.endPoints[this.used - 1] + 1) {
                this.endPoints[this.used - 1] = high;
            } else {
                this.ensureCapacity(this.used + 1);
                this.startPoints[this.used - 1] = low;
                this.endPoints[this.used - 1] = high;
            }
            this.size += high - low + 1;
        } else if (high < this.startPoints[0]) {
            this.ensureCapacity(this.used + 1);
            System.arraycopy(this.startPoints, 0, this.startPoints, 1, this.used - 1);
            System.arraycopy(this.endPoints, 0, this.endPoints, 1, this.used - 1);
            this.startPoints[0] = low;
            this.endPoints[0] = high;
        } else {
            int i;
            for (i = 1; i < this.used; ++i) {
                if (this.startPoints[i] <= high || this.endPoints[i - 1] >= low) continue;
                this.ensureCapacity(this.used + 1);
                System.arraycopy(this.startPoints, i, this.startPoints, i + 1, this.used - i - 1);
                System.arraycopy(this.endPoints, i, this.endPoints, i + 1, this.used - i - 1);
                this.startPoints[i] = low;
                this.endPoints[i] = high;
                return;
            }
            for (i = low; i <= high; ++i) {
                this.add(i);
            }
        }
    }

    public int[] getStartPoints() {
        return this.startPoints;
    }

    public int[] getEndPoints() {
        return this.endPoints;
    }

    public int getNumberOfRanges() {
        return this.used;
    }

    private class IntRangeSetIterator
    implements IntIterator {
        private int i = -1;
        private int current = Integer.MIN_VALUE;

        @Override
        public boolean hasNext() {
            if (this.i < 0) {
                return IntRangeSet.this.size > 0;
            }
            return this.current < IntRangeSet.this.endPoints[IntRangeSet.this.used - 1];
        }

        @Override
        public int next() {
            if (this.i < 0) {
                this.i = 0;
                this.current = IntRangeSet.this.startPoints[0];
                return this.current;
            }
            if (this.current == IntRangeSet.this.endPoints[this.i]) {
                this.current = IntRangeSet.this.startPoints[++this.i];
                return this.current;
            }
            return ++this.current;
        }
    }
}


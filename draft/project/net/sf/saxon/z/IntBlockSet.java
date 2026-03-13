/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntRangeSet;
import net.sf.saxon.z.IntSet;

public class IntBlockSet
implements IntSet {
    private int startPoint;
    private int endPoint;
    private int hashCode = -1;

    public IntBlockSet(int startPoint, int endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    @Override
    public IntSet copy() {
        return this;
    }

    @Override
    public IntSet mutableCopy() {
        return new IntRangeSet(new int[]{this.startPoint}, new int[]{this.endPoint});
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public int size() {
        return this.endPoint - this.startPoint;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean contains(int value) {
        return value >= this.startPoint && value <= this.endPoint;
    }

    @Override
    public boolean remove(int value) {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear");
    }

    @Override
    public boolean add(int value) {
        throw new UnsupportedOperationException("add");
    }

    @Override
    public IntIterator iterator() {
        return this.mutableCopy().iterator();
    }

    public String toString() {
        return this.startPoint + " - " + this.endPoint;
    }

    public boolean equals(Object other) {
        return this.mutableCopy().equals(other);
    }

    public int hashCode() {
        if (this.hashCode == -1) {
            this.hashCode = 0x836A89F1 ^ this.startPoint + (this.endPoint << 3);
        }
        return this.hashCode;
    }

    public int getStartPoint() {
        return this.startPoint;
    }

    public int getEndPoint() {
        return this.endPoint;
    }
}


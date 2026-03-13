/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;

public class IntEmptySet
implements IntSet {
    private static IntEmptySet THE_INSTANCE = new IntEmptySet();

    public static IntEmptySet getInstance() {
        return THE_INSTANCE;
    }

    private IntEmptySet() {
    }

    @Override
    public IntSet copy() {
        return this;
    }

    @Override
    public IntSet mutableCopy() {
        return new IntHashSet();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("IntEmptySet is immutable");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(int value) {
        return false;
    }

    @Override
    public boolean remove(int value) {
        throw new UnsupportedOperationException("IntEmptySet is immutable");
    }

    @Override
    public boolean add(int value) {
        throw new UnsupportedOperationException("IntEmptySet is immutable");
    }

    @Override
    public IntIterator iterator() {
        return new IntIterator(){

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public int next() {
                return Integer.MIN_VALUE;
            }
        };
    }

    @Override
    public IntSet union(IntSet other) {
        return other.copy();
    }

    @Override
    public IntSet intersect(IntSet other) {
        return this;
    }

    @Override
    public IntSet except(IntSet other) {
        return this;
    }

    @Override
    public boolean containsAll(IntSet other) {
        return other.isEmpty();
    }
}


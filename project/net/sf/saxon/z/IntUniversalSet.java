/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntComplementSet;
import net.sf.saxon.z.IntEmptySet;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;

public class IntUniversalSet
implements IntSet {
    private static IntUniversalSet THE_INSTANCE = new IntUniversalSet();

    public static IntUniversalSet getInstance() {
        return THE_INSTANCE;
    }

    private IntUniversalSet() {
    }

    @Override
    public IntSet copy() {
        return this;
    }

    @Override
    public IntSet mutableCopy() {
        return new IntComplementSet(new IntHashSet());
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("IntUniversalSet is immutable");
    }

    @Override
    public int size() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(int value) {
        return true;
    }

    @Override
    public boolean remove(int value) {
        throw new UnsupportedOperationException("IntUniversalSet is immutable");
    }

    @Override
    public boolean add(int value) {
        throw new UnsupportedOperationException("IntUniversalSet is immutable");
    }

    @Override
    public IntIterator iterator() {
        throw new UnsupportedOperationException("Cannot enumerate an infinite set");
    }

    @Override
    public IntSet union(IntSet other) {
        return this;
    }

    @Override
    public IntSet intersect(IntSet other) {
        return other.copy();
    }

    @Override
    public IntSet except(IntSet other) {
        if (other instanceof IntUniversalSet) {
            return IntEmptySet.getInstance();
        }
        return new IntComplementSet(other.copy());
    }

    @Override
    public boolean containsAll(IntSet other) {
        return true;
    }
}


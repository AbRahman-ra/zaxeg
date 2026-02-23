/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntEmptySet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntUniversalSet;

public class IntComplementSet
implements IntSet {
    private IntSet exclusions;

    public IntComplementSet(IntSet exclusions) {
        this.exclusions = exclusions.copy();
    }

    public IntSet getExclusions() {
        return this.exclusions;
    }

    @Override
    public IntSet copy() {
        return new IntComplementSet(this.exclusions.copy());
    }

    @Override
    public IntSet mutableCopy() {
        return this.copy();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("IntComplementSet cannot be emptied");
    }

    @Override
    public int size() {
        return Integer.MAX_VALUE - this.exclusions.size();
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean contains(int value) {
        return !this.exclusions.contains(value);
    }

    @Override
    public boolean remove(int value) {
        boolean b = this.contains(value);
        if (b) {
            this.exclusions.add(value);
        }
        return b;
    }

    @Override
    public boolean add(int value) {
        boolean b = this.contains(value);
        if (!b) {
            this.exclusions.remove(value);
        }
        return b;
    }

    @Override
    public IntIterator iterator() {
        throw new UnsupportedOperationException("Cannot enumerate an infinite set");
    }

    @Override
    public IntSet union(IntSet other) {
        return new IntComplementSet(this.exclusions.except(other));
    }

    @Override
    public IntSet intersect(IntSet other) {
        if (other.isEmpty()) {
            return IntEmptySet.getInstance();
        }
        if (other == IntUniversalSet.getInstance()) {
            return this.copy();
        }
        if (other instanceof IntComplementSet) {
            return new IntComplementSet(this.exclusions.union(((IntComplementSet)other).exclusions));
        }
        return other.intersect(this);
    }

    @Override
    public IntSet except(IntSet other) {
        return new IntComplementSet(this.exclusions.union(other));
    }

    @Override
    public boolean containsAll(IntSet other) {
        if (other instanceof IntComplementSet) {
            return ((IntComplementSet)other).exclusions.containsAll(this.exclusions);
        }
        if (other instanceof IntUniversalSet) {
            return !this.exclusions.isEmpty();
        }
        IntIterator ii = other.iterator();
        while (ii.hasNext()) {
            if (!this.exclusions.contains(ii.next())) continue;
            return false;
        }
        return true;
    }
}


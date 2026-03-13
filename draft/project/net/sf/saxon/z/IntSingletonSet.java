/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntEmptySet;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntSingletonIterator;

public class IntSingletonSet
implements IntSet {
    private int value;

    public IntSingletonSet(int value) {
        this.value = value;
    }

    public int getMember() {
        return this.value;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("IntSingletonSet is immutable");
    }

    @Override
    public IntSet copy() {
        return this;
    }

    @Override
    public IntSet mutableCopy() {
        IntHashSet intHashSet = new IntHashSet();
        intHashSet.add(this.value);
        return intHashSet;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(int value) {
        return this.value == value;
    }

    @Override
    public boolean remove(int value) {
        throw new UnsupportedOperationException("IntSingletonSet is immutable");
    }

    @Override
    public boolean add(int value) {
        throw new UnsupportedOperationException("IntSingletonSet is immutable");
    }

    @Override
    public IntIterator iterator() {
        return new IntSingletonIterator(this.value);
    }

    @Override
    public IntSet union(IntSet other) {
        IntSet n = other.mutableCopy();
        n.add(this.value);
        return n;
    }

    @Override
    public IntSet intersect(IntSet other) {
        if (other.contains(this.value)) {
            return this;
        }
        return IntEmptySet.getInstance();
    }

    @Override
    public IntSet except(IntSet other) {
        if (other.contains(this.value)) {
            return IntEmptySet.getInstance();
        }
        return this;
    }

    @Override
    public boolean containsAll(IntSet other) {
        if (other.size() > 1) {
            return false;
        }
        IntIterator ii = other.iterator();
        while (ii.hasNext()) {
            if (this.value == ii.next()) continue;
            return false;
        }
        return true;
    }
}


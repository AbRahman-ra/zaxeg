/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import net.sf.saxon.z.IntComplementSet;
import net.sf.saxon.z.IntEmptySet;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntUniversalSet;

public interface IntSet {
    public IntSet copy();

    public IntSet mutableCopy();

    default public boolean isMutable() {
        return true;
    }

    public void clear();

    public int size();

    public boolean isEmpty();

    public boolean contains(int var1);

    public boolean remove(int var1);

    public boolean add(int var1);

    public IntIterator iterator();

    default public boolean containsAll(IntSet other) {
        if (other == IntUniversalSet.getInstance() || other instanceof IntComplementSet) {
            return false;
        }
        IntIterator it = other.iterator();
        while (it.hasNext()) {
            if (this.contains(it.next())) continue;
            return false;
        }
        return true;
    }

    default public IntSet union(IntSet other) {
        if (other == IntUniversalSet.getInstance()) {
            return other;
        }
        if (this.isEmpty()) {
            return other.copy();
        }
        if (other.isEmpty()) {
            return this.copy();
        }
        if (other instanceof IntComplementSet) {
            return other.union(this);
        }
        IntHashSet n = new IntHashSet(this.size() + other.size());
        IntIterator it = this.iterator();
        while (it.hasNext()) {
            n.add(it.next());
        }
        it = other.iterator();
        while (it.hasNext()) {
            n.add(it.next());
        }
        return n;
    }

    default public IntSet intersect(IntSet other) {
        if (this.isEmpty() || other.isEmpty()) {
            return IntEmptySet.getInstance();
        }
        IntHashSet n = new IntHashSet(this.size());
        IntIterator it = this.iterator();
        while (it.hasNext()) {
            int v = it.next();
            if (!other.contains(v)) continue;
            n.add(v);
        }
        return n;
    }

    default public IntSet except(IntSet other) {
        IntHashSet n = new IntHashSet(this.size());
        IntIterator it = this.iterator();
        while (it.hasNext()) {
            int v = it.next();
            if (other.contains(v)) continue;
            n.add(v);
        }
        return n;
    }
}


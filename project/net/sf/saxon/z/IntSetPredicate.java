/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.z;

import java.util.function.IntPredicate;
import net.sf.saxon.z.IntSet;

public class IntSetPredicate
implements IntPredicate {
    private IntSet set;
    public static final IntPredicate ALWAYS_TRUE = i -> true;
    public static final IntPredicate ALWAYS_FALSE = i -> false;

    public IntSetPredicate(IntSet set) {
        if (set == null) {
            throw new NullPointerException();
        }
        this.set = set;
    }

    @Override
    public boolean test(int value) {
        return this.set.contains(value);
    }

    @Override
    public IntPredicate or(IntPredicate other) {
        if (other instanceof IntSetPredicate) {
            return new IntSetPredicate(this.set.union(((IntSetPredicate)other).set));
        }
        return IntPredicate.super.or(other);
    }

    public IntSet getIntSet() {
        return this.set;
    }

    public String toString() {
        return "in {" + this.set + "}";
    }
}


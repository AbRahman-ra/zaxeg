/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.sort.ComparableAtomicValueComparer;

public class DecimalSortComparer
extends ComparableAtomicValueComparer {
    private static DecimalSortComparer THE_INSTANCE = new DecimalSortComparer();

    public static DecimalSortComparer getDecimalSortComparerInstance() {
        return THE_INSTANCE;
    }

    private DecimalSortComparer() {
    }

    @Override
    public String save() {
        return "DecSC";
    }
}


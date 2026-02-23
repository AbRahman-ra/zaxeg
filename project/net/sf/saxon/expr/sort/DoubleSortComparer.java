/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.NumericValue;

public class DoubleSortComparer
implements AtomicComparer {
    private static DoubleSortComparer THE_INSTANCE = new DoubleSortComparer();

    public static DoubleSortComparer getInstance() {
        return THE_INSTANCE;
    }

    private DoubleSortComparer() {
    }

    @Override
    public StringCollator getCollator() {
        return null;
    }

    @Override
    public AtomicComparer provideContext(XPathContext context) {
        return this;
    }

    @Override
    public int compareAtomicValues(AtomicValue a, AtomicValue b) {
        if (a == null) {
            if (b == null) {
                return 0;
            }
            return -1;
        }
        if (b == null) {
            return 1;
        }
        NumericValue an = (NumericValue)a;
        NumericValue bn = (NumericValue)b;
        if (an.isNaN()) {
            return bn.isNaN() ? 0 : -1;
        }
        if (bn.isNaN()) {
            return 1;
        }
        return an.compareTo(bn);
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) {
        return this.compareAtomicValues(a, b) == 0;
    }

    @Override
    public String save() {
        return "DblSC";
    }
}


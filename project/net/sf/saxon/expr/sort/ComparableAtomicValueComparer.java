/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.value.AtomicValue;

public class ComparableAtomicValueComparer
implements AtomicComparer {
    private static ComparableAtomicValueComparer THE_INSTANCE = new ComparableAtomicValueComparer();

    public static ComparableAtomicValueComparer getInstance() {
        return THE_INSTANCE;
    }

    protected ComparableAtomicValueComparer() {
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
            return b == null ? 0 : -1;
        }
        if (b == null) {
            return 1;
        }
        return ((Comparable)((Object)a)).compareTo(b);
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) {
        return a.equals(b);
    }

    @Override
    public String save() {
        return "CAVC";
    }
}


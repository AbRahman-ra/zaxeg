/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.value.AtomicValue;

public class EqualityComparer
implements AtomicComparer {
    public static EqualityComparer THE_INSTANCE = new EqualityComparer();

    public static EqualityComparer getInstance() {
        return THE_INSTANCE;
    }

    private EqualityComparer() {
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
        throw new ClassCastException("Values are not comparable");
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) {
        return a.equals(b);
    }

    @Override
    public String save() {
        return "EQC";
    }
}


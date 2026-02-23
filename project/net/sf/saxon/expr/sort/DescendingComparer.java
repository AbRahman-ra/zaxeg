/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.value.AtomicValue;

public class DescendingComparer
implements AtomicComparer {
    private AtomicComparer baseComparer;

    public DescendingComparer(AtomicComparer base) {
        this.baseComparer = base;
    }

    public AtomicComparer getBaseComparer() {
        return this.baseComparer;
    }

    @Override
    public StringCollator getCollator() {
        return this.baseComparer.getCollator();
    }

    @Override
    public AtomicComparer provideContext(XPathContext context) {
        AtomicComparer newBase = this.baseComparer.provideContext(context);
        if (newBase != this.baseComparer) {
            return new DescendingComparer(newBase);
        }
        return this;
    }

    @Override
    public int compareAtomicValues(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        return 0 - this.baseComparer.compareAtomicValues(a, b);
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        return this.baseComparer.comparesEqual(a, b);
    }

    @Override
    public String save() {
        return "DESC|" + this.baseComparer.save();
    }
}


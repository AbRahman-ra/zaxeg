/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.value.AtomicValue;

public class EmptyGreatestComparer
implements AtomicComparer {
    private AtomicComparer baseComparer;

    public EmptyGreatestComparer(AtomicComparer baseComparer) {
        this.baseComparer = baseComparer;
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
            return new EmptyGreatestComparer(newBase);
        }
        return this;
    }

    @Override
    public int compareAtomicValues(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        if (a == null) {
            if (b == null) {
                return 0;
            }
            return 1;
        }
        if (b == null) {
            return -1;
        }
        if (a.isNaN()) {
            return b.isNaN() ? 0 : 1;
        }
        if (b.isNaN()) {
            return -1;
        }
        return this.baseComparer.compareAtomicValues(a, b);
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        return a == null && b == null || this.baseComparer.comparesEqual(a, b);
    }

    @Override
    public String save() {
        return "EG|" + this.baseComparer.save();
    }
}


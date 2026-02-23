/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class TextComparer
implements AtomicComparer {
    private AtomicComparer baseComparer;

    public TextComparer(AtomicComparer baseComparer) {
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
            return new TextComparer(newBase);
        }
        return this;
    }

    @Override
    public int compareAtomicValues(AtomicValue a, AtomicValue b) throws ClassCastException, NoDynamicContextException {
        return this.baseComparer.compareAtomicValues(this.toStringValue(a), this.toStringValue(b));
    }

    private StringValue toStringValue(AtomicValue a) {
        if (a instanceof StringValue) {
            return (StringValue)a;
        }
        return new StringValue(a == null ? "" : a.getStringValue());
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        return this.compareAtomicValues(a, b) == 0;
    }

    @Override
    public String save() {
        return "TEXT|" + this.baseComparer.save();
    }
}


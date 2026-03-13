/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.value.AtomicValue;

public class CollatingAtomicComparer
implements AtomicComparer {
    private StringCollator collator;

    public CollatingAtomicComparer(StringCollator collator) {
        this.collator = collator == null ? CodepointCollator.getInstance() : collator;
    }

    @Override
    public StringCollator getCollator() {
        return this.collator;
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
        return this.collator.compareStrings(a.getStringValue(), b.getStringValue());
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) {
        return this.compareAtomicValues(a, b) == 0;
    }

    @Override
    public String save() {
        return "CAC|" + this.getCollator().getCollationURI();
    }

    public int hashCode() {
        return this.collator.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof CollatingAtomicComparer && this.collator.equals(((CollatingAtomicComparer)obj).collator);
    }
}


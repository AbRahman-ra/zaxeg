/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.CalendarValue;

public class CalendarValueComparer
implements AtomicComparer {
    private transient XPathContext context;

    public CalendarValueComparer(XPathContext context) {
        this.context = context;
    }

    @Override
    public StringCollator getCollator() {
        return null;
    }

    @Override
    public AtomicComparer provideContext(XPathContext context) {
        return new CalendarValueComparer(context);
    }

    @Override
    public int compareAtomicValues(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        if (a == null) {
            return b == null ? 0 : -1;
        }
        if (b == null) {
            return 1;
        }
        return ((CalendarValue)a).compareTo((CalendarValue)b, this.context.getImplicitTimezone());
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        return this.compareAtomicValues(a, b) == 0;
    }

    @Override
    public String save() {
        return "CalVC";
    }
}


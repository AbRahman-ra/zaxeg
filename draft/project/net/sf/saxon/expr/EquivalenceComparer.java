/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.StringValue;

public class EquivalenceComparer
extends GenericAtomicComparer {
    protected EquivalenceComparer(StringCollator collator, XPathContext context) {
        super(collator, context);
    }

    @Override
    public EquivalenceComparer provideContext(XPathContext context) {
        return new EquivalenceComparer(this.getStringCollator(), context);
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        if (a instanceof StringValue && b instanceof StringValue) {
            return this.getStringCollator().comparesEqual(a.getStringValue(), b.getStringValue());
        }
        if (a instanceof CalendarValue && b instanceof CalendarValue) {
            return ((CalendarValue)a).compareTo((CalendarValue)b, this.getContext().getImplicitTimezone()) == 0;
        }
        if (a.isNaN() && b.isNaN()) {
            return true;
        }
        int implicitTimezone = this.getContext().getImplicitTimezone();
        AtomicMatchKey ac = a.getXPathComparable(false, this.getStringCollator(), implicitTimezone);
        AtomicMatchKey bc = b.getXPathComparable(false, this.getStringCollator(), implicitTimezone);
        return ac.equals(bc);
    }

    @Override
    public String save() {
        return "EQUIV|" + super.save();
    }
}


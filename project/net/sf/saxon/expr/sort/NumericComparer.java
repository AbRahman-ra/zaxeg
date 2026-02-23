/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.type.StringToDouble;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.NumericValue;

public class NumericComparer
implements AtomicComparer {
    private static NumericComparer THE_INSTANCE = new NumericComparer();
    protected StringToDouble converter = StringToDouble.getInstance();

    public static NumericComparer getInstance() {
        return THE_INSTANCE;
    }

    protected NumericComparer() {
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
        double d2;
        double d1;
        if (a instanceof NumericValue) {
            d1 = ((NumericValue)a).getDoubleValue();
        } else if (a == null) {
            d1 = Double.NaN;
        } else {
            try {
                d1 = this.converter.stringToNumber(a.getStringValueCS());
            } catch (NumberFormatException err) {
                d1 = Double.NaN;
            }
        }
        if (b instanceof NumericValue) {
            d2 = ((NumericValue)b).getDoubleValue();
        } else if (b == null) {
            d2 = Double.NaN;
        } else {
            try {
                d2 = this.converter.stringToNumber(b.getStringValueCS());
            } catch (NumberFormatException err) {
                d2 = Double.NaN;
            }
        }
        if (Double.isNaN(d1)) {
            if (Double.isNaN(d2)) {
                return 0;
            }
            return -1;
        }
        if (Double.isNaN(d2)) {
            return 1;
        }
        if (d1 < d2) {
            return -1;
        }
        if (d1 > d2) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) {
        return this.compareAtomicValues(a, b) == 0;
    }

    @Override
    public String save() {
        return "NC";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.CalendarValueComparer;
import net.sf.saxon.expr.sort.CodepointCollatingComparer;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.expr.sort.CollatingAtomicComparer;
import net.sf.saxon.expr.sort.ComparisonException;
import net.sf.saxon.expr.sort.DecimalSortComparer;
import net.sf.saxon.expr.sort.DoubleSortComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;

public class AtomicSortComparer
implements AtomicComparer {
    private StringCollator collator;
    private transient XPathContext context;
    private int itemType;
    public static AtomicMatchKey COLLATION_KEY_NaN = new AtomicMatchKey(){

        @Override
        public AtomicValue asAtomic() {
            return new QNameValue("saxon", "http://saxon.sf.net/collation-key", "NaN");
        }
    };

    public static AtomicComparer makeSortComparer(StringCollator collator, int itemType, XPathContext context) {
        switch (itemType) {
            case 513: 
            case 529: 
            case 631: {
                if (collator instanceof CodepointCollator) {
                    return CodepointCollatingComparer.getInstance();
                }
                return new CollatingAtomicComparer(collator);
            }
            case 515: 
            case 533: {
                return DecimalSortComparer.getDecimalSortComparerInstance();
            }
            case 516: 
            case 517: 
            case 635: {
                return DoubleSortComparer.getInstance();
            }
            case 519: 
            case 520: 
            case 521: {
                return new CalendarValueComparer(context);
            }
        }
        return new AtomicSortComparer(collator, itemType, context);
    }

    protected AtomicSortComparer(StringCollator collator, int itemType, XPathContext context) {
        this.collator = collator;
        if (collator == null) {
            this.collator = CodepointCollator.getInstance();
        }
        this.context = context;
        this.itemType = itemType;
    }

    @Override
    public StringCollator getCollator() {
        return this.collator;
    }

    @Override
    public AtomicComparer provideContext(XPathContext context) {
        return new AtomicSortComparer(this.collator, this.itemType, context);
    }

    public StringCollator getStringCollator() {
        return this.collator;
    }

    public int getItemType() {
        return this.itemType;
    }

    @Override
    public int compareAtomicValues(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        if (a == null) {
            if (b == null) {
                return 0;
            }
            return -1;
        }
        if (b == null) {
            return 1;
        }
        if (a.isNaN()) {
            return b.isNaN() ? 0 : -1;
        }
        if (b.isNaN()) {
            return 1;
        }
        if (a instanceof StringValue && b instanceof StringValue) {
            if (this.collator instanceof CodepointCollator) {
                return CodepointCollator.compareCS(a.getStringValueCS(), b.getStringValueCS());
            }
            return this.collator.compareStrings(a.getStringValue(), b.getStringValue());
        }
        int implicitTimezone = this.context.getImplicitTimezone();
        Comparable ac = (Comparable)((Object)a.getXPathComparable(true, this.collator, implicitTimezone));
        Comparable bc = (Comparable)((Object)b.getXPathComparable(true, this.collator, implicitTimezone));
        if (ac == null || bc == null) {
            return this.compareNonComparables(a, b);
        }
        try {
            return ac.compareTo(bc);
        } catch (ClassCastException e) {
            String message = "Cannot compare " + a.getPrimitiveType().getDisplayName() + " with " + b.getPrimitiveType().getDisplayName();
            if (a instanceof UntypedAtomicValue || b instanceof UntypedAtomicValue) {
                message = message + ". Further information: see http://saxonica.plan.io/issues/3450";
            }
            throw new ClassCastException(message);
        }
    }

    protected int compareNonComparables(AtomicValue a, AtomicValue b) {
        XPathException err = new XPathException("Values are not comparable (" + Type.displayTypeName(a) + ", " + Type.displayTypeName(b) + ')', "XPTY0004");
        throw new ComparisonException(err);
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        return this.compareAtomicValues(a, b) == 0;
    }

    @Override
    public String save() {
        return "AtSC|" + this.itemType + "|" + this.getCollator().getCollationURI();
    }
}


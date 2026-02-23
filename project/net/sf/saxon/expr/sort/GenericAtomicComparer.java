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
import net.sf.saxon.expr.sort.ComparableAtomicValueComparer;
import net.sf.saxon.expr.sort.ComparisonException;
import net.sf.saxon.expr.sort.EqualityComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.StringValue;

public class GenericAtomicComparer
implements AtomicComparer {
    private StringCollator collator;
    private transient XPathContext context;

    public GenericAtomicComparer(StringCollator collator, XPathContext conversionContext) {
        this.collator = collator;
        if (collator == null) {
            this.collator = CodepointCollator.getInstance();
        }
        this.context = conversionContext;
    }

    public static AtomicComparer makeAtomicComparer(BuiltInAtomicType type0, BuiltInAtomicType type1, StringCollator collator, XPathContext context) {
        int fp1;
        int fp0 = type0.getFingerprint();
        if (fp0 == (fp1 = type1.getFingerprint())) {
            switch (fp0) {
                case 519: 
                case 520: 
                case 521: 
                case 522: 
                case 523: 
                case 524: 
                case 525: 
                case 526: {
                    return new CalendarValueComparer(context);
                }
                case 514: 
                case 633: 
                case 634: {
                    return ComparableAtomicValueComparer.getInstance();
                }
                case 527: 
                case 528: {
                    return ComparableAtomicValueComparer.getInstance();
                }
                case 530: 
                case 531: {
                    return EqualityComparer.getInstance();
                }
            }
        }
        if (type0.isPrimitiveNumeric() && type1.isPrimitiveNumeric()) {
            return ComparableAtomicValueComparer.getInstance();
        }
        if (!(fp0 != 513 && fp0 != 631 && fp0 != 529 || fp1 != 513 && fp1 != 631 && fp1 != 529)) {
            if (collator instanceof CodepointCollator) {
                return CodepointCollatingComparer.getInstance();
            }
            return new CollatingAtomicComparer(collator);
        }
        return new GenericAtomicComparer(collator, context);
    }

    @Override
    public StringCollator getCollator() {
        return this.collator;
    }

    @Override
    public GenericAtomicComparer provideContext(XPathContext context) {
        return new GenericAtomicComparer(this.collator, context);
    }

    public StringCollator getStringCollator() {
        return this.collator;
    }

    @Override
    public int compareAtomicValues(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        if (a == null) {
            return b == null ? 0 : -1;
        }
        if (b == null) {
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
            XPathException e = new XPathException("Objects are not comparable (" + Type.displayTypeName(a) + ", " + Type.displayTypeName(b) + ')', "XPTY0004");
            throw new ComparisonException(e);
        }
        return ac.compareTo(bc);
    }

    @Override
    public boolean comparesEqual(AtomicValue a, AtomicValue b) throws NoDynamicContextException {
        if (a instanceof StringValue && b instanceof StringValue) {
            return this.collator.comparesEqual(a.getStringValue(), b.getStringValue());
        }
        if (a instanceof CalendarValue && b instanceof CalendarValue) {
            return ((CalendarValue)a).compareTo((CalendarValue)b, this.context.getImplicitTimezone()) == 0;
        }
        int implicitTimezone = this.context.getImplicitTimezone();
        AtomicMatchKey ac = a.getXPathComparable(false, this.collator, implicitTimezone);
        AtomicMatchKey bc = b.getXPathComparable(false, this.collator, implicitTimezone);
        return ac.equals(bc);
    }

    public XPathContext getContext() {
        return this.context;
    }

    @Override
    public String save() {
        return "GAC|" + this.collator.getCollationURI();
    }

    public int hashCode() {
        return this.collator.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof GenericAtomicComparer && this.collator.equals(((GenericAtomicComparer)obj).collator);
    }
}


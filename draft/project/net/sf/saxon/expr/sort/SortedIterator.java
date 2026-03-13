/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.Arrays;
import java.util.EnumSet;
import net.sf.saxon.expr.ErrorIterator;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.ItemToBeSorted;
import net.sf.saxon.expr.sort.ObjectToBeSorted;
import net.sf.saxon.expr.sort.SortKeyEvaluator;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class SortedIterator
implements SequenceIterator,
LastPositionFinder,
LookaheadIterator {
    protected SequenceIterator base;
    protected SortKeyEvaluator sortKeyEvaluator;
    protected AtomicComparer[] comparators;
    protected ObjectToBeSorted[] values;
    protected int count = -1;
    protected int position = 0;
    protected XPathContext context;
    private HostLanguage hostLanguage;

    protected SortedIterator() {
    }

    public SortedIterator(XPathContext context, SequenceIterator base, SortKeyEvaluator sortKeyEvaluator, AtomicComparer[] comparators, boolean createNewContext) {
        if (createNewContext) {
            this.context = context.newMinorContext();
            this.base = this.context.trackFocus(base);
            this.context.setTemporaryOutputState(195);
        } else {
            this.base = base;
            this.context = context;
        }
        this.sortKeyEvaluator = sortKeyEvaluator;
        this.comparators = new AtomicComparer[comparators.length];
        for (int n = 0; n < comparators.length; ++n) {
            this.comparators[n] = comparators[n].provideContext(context);
        }
    }

    public void setHostLanguage(HostLanguage language) {
        this.hostLanguage = language;
    }

    @Override
    public boolean hasNext() {
        if (this.position < 0) {
            return false;
        }
        if (this.count < 0) {
            if (this.base instanceof LookaheadIterator) {
                return ((LookaheadIterator)this.base).hasNext();
            }
            try {
                this.doSort();
                return this.count > 0;
            } catch (XPathException err) {
                this.count = -1;
                this.base = new FocusTrackingIterator(new ErrorIterator(err));
                return true;
            }
        }
        return this.position < this.count;
    }

    @Override
    public Item next() throws XPathException {
        if (this.position < 0) {
            return null;
        }
        if (this.count < 0) {
            this.doSort();
        }
        if (this.position < this.count) {
            return (Item)this.values[this.position++].value;
        }
        this.position = -1;
        return null;
    }

    @Override
    public int getLength() throws XPathException {
        if (this.count < 0) {
            this.doSort();
        }
        return this.count;
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LAST_POSITION_FINDER);
    }

    protected void buildArray() throws XPathException {
        ObjectToBeSorted[] nk2;
        Item item;
        int allocated = this.base.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER) ? ((LastPositionFinder)((Object)this.base)).getLength() : 100;
        this.values = new ItemToBeSorted[allocated];
        this.count = 0;
        while ((item = this.base.next()) != null) {
            if (this.count == allocated) {
                nk2 = new ObjectToBeSorted[allocated *= 2];
                System.arraycopy(this.values, 0, nk2, 0, this.count);
                this.values = nk2;
            }
            ItemToBeSorted itbs = new ItemToBeSorted(this.comparators.length);
            this.values[this.count] = itbs;
            itbs.value = item;
            for (int n = 0; n < this.comparators.length; ++n) {
                itbs.sortKeyValues[n] = this.sortKeyEvaluator.evaluateSortKey(n, this.context);
            }
            ++this.count;
            itbs.originalPosition = itbs.originalPosition;
        }
        if (allocated * 2 < this.count || allocated - this.count > 2000) {
            nk2 = new ObjectToBeSorted[this.count];
            System.arraycopy(this.values, 0, nk2, 0, this.count);
            this.values = nk2;
        }
    }

    private void doSort() throws XPathException {
        this.buildArray();
        if (this.count < 2) {
            return;
        }
        try {
            Arrays.sort(this.values, 0, this.count, (a, b) -> {
                try {
                    for (int i = 0; i < this.comparators.length; ++i) {
                        int comp = this.comparators[i].compareAtomicValues(a.sortKeyValues[i], b.sortKeyValues[i]);
                        if (comp == 0) continue;
                        return comp;
                    }
                } catch (NoDynamicContextException e) {
                    throw new AssertionError((Object)("Sorting without dynamic context: " + e.getMessage()));
                }
                return a.originalPosition - b.originalPosition;
            });
        } catch (ClassCastException e) {
            XPathException err = new XPathException("Non-comparable types found while sorting: " + e.getMessage());
            if (this.hostLanguage == HostLanguage.XSLT) {
                err.setErrorCode("XTDE1030");
            } else {
                err.setErrorCode("XPTY0004");
            }
            throw err;
        }
    }
}


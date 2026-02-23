/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.Arrays;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.expr.sort.GroupToBeSorted;
import net.sf.saxon.expr.sort.SortKeyEvaluator;
import net.sf.saxon.expr.sort.SortedIterator;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MemoSequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.trans.XPathException;

public class SortedGroupIterator
extends SortedIterator
implements GroupIterator {
    public SortedGroupIterator(XPathContext context, GroupIterator base, SortKeyEvaluator sortKeyEvaluator, AtomicComparer[] comparators) {
        super(context, base, sortKeyEvaluator, comparators, true);
        this.setHostLanguage(HostLanguage.XSLT);
    }

    @Override
    protected void buildArray() throws XPathException {
        Item item;
        int allocated = this.base.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER) ? ((LastPositionFinder)((Object)this.base)).getLength() : 100;
        this.values = new GroupToBeSorted[allocated];
        this.count = 0;
        XPathContextMajor c2 = this.context.newContext();
        c2.setCurrentIterator((FocusIterator)this.base);
        GroupIterator groupIter = (GroupIterator)((FocusTrackingIterator)this.base).getUnderlyingIterator();
        c2.setCurrentGroupIterator(groupIter);
        while ((item = this.base.next()) != null) {
            if (this.count == allocated) {
                this.values = Arrays.copyOf(this.values, allocated *= 2);
            }
            GroupToBeSorted gtbs = new GroupToBeSorted(this.comparators.length);
            this.values[this.count] = gtbs;
            gtbs.value = item;
            for (int n = 0; n < this.comparators.length; ++n) {
                gtbs.sortKeyValues[n] = this.sortKeyEvaluator.evaluateSortKey(n, c2);
            }
            ++this.count;
            gtbs.originalPosition = gtbs.originalPosition;
            gtbs.currentGroupingKey = groupIter.getCurrentGroupingKey();
            gtbs.currentGroup = new MemoSequence(groupIter.iterateCurrentGroup());
        }
    }

    @Override
    public AtomicSequence getCurrentGroupingKey() {
        return ((GroupToBeSorted)this.values[this.position - 1]).currentGroupingKey;
    }

    @Override
    public SequenceIterator iterateCurrentGroup() throws XPathException {
        return ((GroupToBeSorted)this.values[this.position - 1]).currentGroup.iterate();
    }
}


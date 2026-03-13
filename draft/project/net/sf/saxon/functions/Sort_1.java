/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.AtomicSortComparer;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.ma.arrays.ArraySort;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;

public class Sort_1
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        List<ItemToBeSorted> inputList = this.getItemsToBeSorted(arguments[0]);
        StringCollator collation = context.getConfiguration().getCollation(this.getRetainedStaticContext().getDefaultCollationName());
        return this.doSort(inputList, collation, context);
    }

    protected List<ItemToBeSorted> getItemsToBeSorted(Sequence input) throws XPathException {
        Item item;
        ArrayList<ItemToBeSorted> inputList = new ArrayList<ItemToBeSorted>();
        int i = 0;
        SequenceIterator iterator = input.iterate();
        while ((item = iterator.next()) != null) {
            ItemToBeSorted member = new ItemToBeSorted();
            member.value = item;
            member.originalPosition = i++;
            member.sortKey = item.atomize();
            inputList.add(member);
        }
        return inputList;
    }

    protected Sequence doSort(List<ItemToBeSorted> inputList, StringCollator collation, XPathContext context) throws XPathException {
        AtomicComparer atomicComparer = AtomicSortComparer.makeSortComparer(collation, 632, context);
        try {
            inputList.sort((a, b) -> {
                int result = ArraySort.compareSortKeys(a.sortKey, b.sortKey, atomicComparer);
                if (result == 0) {
                    return a.originalPosition - b.originalPosition;
                }
                return result;
            });
        } catch (ClassCastException e) {
            XPathException err = new XPathException("Non-comparable types found while sorting: " + e.getMessage());
            err.setErrorCode("XPTY0004");
            throw err;
        }
        ArrayList<Item> outputList = new ArrayList<Item>(inputList.size());
        for (ItemToBeSorted member : inputList) {
            outputList.add(member.value);
        }
        return new SequenceExtent(outputList);
    }

    public static class ItemToBeSorted {
        public Item value;
        public GroundedValue sortKey;
        public int originalPosition;
    }
}


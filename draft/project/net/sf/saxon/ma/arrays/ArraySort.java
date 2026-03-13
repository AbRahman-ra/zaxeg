/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.arrays;

import java.util.ArrayList;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.AtomicSortComparer;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class ArraySort
extends SystemFunction {
    @Override
    public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue collName;
        ArrayItem array = (ArrayItem)arguments[0].head();
        ArrayList<MemberToBeSorted> inputList = new ArrayList<MemberToBeSorted>(array.arrayLength());
        int i = 0;
        StringCollator collation = arguments.length == 1 ? context.getConfiguration().getCollation(this.getRetainedStaticContext().getDefaultCollationName()) : ((collName = (StringValue)arguments[1].head()) == null ? context.getConfiguration().getCollation(this.getRetainedStaticContext().getDefaultCollationName()) : context.getConfiguration().getCollation(collName.getStringValue(), this.getStaticBaseUriString()));
        Function key = null;
        if (arguments.length == 3) {
            key = (Function)arguments[2].head();
        }
        for (GroundedValue seq : array.members()) {
            MemberToBeSorted member = new MemberToBeSorted();
            member.value = seq;
            member.originalPosition = i++;
            member.sortKey = key != null ? ArraySort.dynamicCall(key, context, new Sequence[]{seq}).materialize() : ArraySort.atomize(seq);
            inputList.add(member);
        }
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
        ArrayList<GroundedValue> outputList = new ArrayList<GroundedValue>(array.arrayLength());
        for (MemberToBeSorted member : inputList) {
            outputList.add(member.value);
        }
        return new SimpleArrayItem(outputList);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    public static int compareSortKeys(GroundedValue a, GroundedValue b, AtomicComparer comparer) {
        UnfailingIterator iteratora = a.iterate();
        UnfailingIterator iteratorb = b.iterate();
        while (true) {
            AtomicValue firsta = (AtomicValue)iteratora.next();
            AtomicValue firstb = (AtomicValue)iteratorb.next();
            if (firsta == null) {
                if (firstb != null) return -1;
                return 0;
            }
            if (firstb == null) {
                return 1;
            }
            try {
                int first = comparer.compareAtomicValues(firsta, firstb);
                if (first != 0) return first;
            } catch (NoDynamicContextException e) {
                throw new AssertionError((Object)e);
            }
        }
    }

    private static GroundedValue atomize(Sequence input) throws XPathException {
        SequenceIterator iterator = input.iterate();
        SequenceIterator mapper = Atomizer.getAtomizingIterator(iterator, false);
        return mapper.materialize();
    }

    private static class MemberToBeSorted {
        public GroundedValue value;
        public GroundedValue sortKey;
        int originalPosition;

        private MemberToBeSorted() {
        }
    }
}


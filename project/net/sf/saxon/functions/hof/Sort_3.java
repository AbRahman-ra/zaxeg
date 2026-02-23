/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import java.util.ArrayList;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.Sort_1;
import net.sf.saxon.functions.Sort_2;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class Sort_3
extends Sort_2 {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Item item;
        Sequence input = arguments[0];
        ArrayList<Sort_1.ItemToBeSorted> inputList = new ArrayList<Sort_1.ItemToBeSorted>();
        int i = 0;
        Function key = (Function)arguments[2].head();
        SequenceIterator iterator = input.iterate();
        while ((item = iterator.next()) != null) {
            Sort_1.ItemToBeSorted member = new Sort_1.ItemToBeSorted();
            member.value = item;
            member.originalPosition = i++;
            member.sortKey = Sort_3.dynamicCall(key, context, new Sequence[]{item}).materialize();
            inputList.add(member);
        }
        return this.doSort(inputList, this.getCollation(context, arguments[1]), context);
    }
}


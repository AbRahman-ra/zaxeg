/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.sort.ObjectToBeSorted;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MemoSequence;

public class GroupToBeSorted
extends ObjectToBeSorted<Item> {
    public AtomicSequence currentGroupingKey;
    public MemoSequence currentGroup;

    public GroupToBeSorted(int numberOfSortKeys) {
        super(numberOfSortKeys);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.sort.ObjectToBeSorted;
import net.sf.saxon.om.Item;

public class ItemToBeSorted
extends ObjectToBeSorted<Item> {
    public ItemToBeSorted(int numberOfSortKeys) {
        super(numberOfSortKeys);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.Comparator;
import net.sf.saxon.om.Item;

public interface ItemOrderComparer
extends Comparator<Item> {
    @Override
    public int compare(Item var1, Item var2);
}


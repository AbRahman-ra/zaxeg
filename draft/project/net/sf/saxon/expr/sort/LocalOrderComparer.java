/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;

public final class LocalOrderComparer
implements ItemOrderComparer {
    private static LocalOrderComparer instance = new LocalOrderComparer();

    public static LocalOrderComparer getInstance() {
        return instance;
    }

    @Override
    public int compare(Item a, Item b) {
        NodeInfo n1 = (NodeInfo)a;
        NodeInfo n2 = (NodeInfo)b;
        return n1.compareOrder(n2);
    }
}


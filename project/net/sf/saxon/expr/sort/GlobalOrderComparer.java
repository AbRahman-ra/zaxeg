/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;

public final class GlobalOrderComparer
implements ItemOrderComparer {
    private static GlobalOrderComparer instance = new GlobalOrderComparer();

    public static GlobalOrderComparer getInstance() {
        return instance;
    }

    @Override
    public int compare(Item a, Item b) {
        long d2;
        if (a == b) {
            return 0;
        }
        long d1 = ((NodeInfo)a).getTreeInfo().getDocumentNumber();
        if (d1 == (d2 = ((NodeInfo)b).getTreeInfo().getDocumentNumber())) {
            return ((NodeInfo)a).compareOrder((NodeInfo)b);
        }
        return Long.signum(d1 - d2);
    }
}


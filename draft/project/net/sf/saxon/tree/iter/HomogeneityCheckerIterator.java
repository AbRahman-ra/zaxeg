/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.ArrayList;
import net.sf.saxon.expr.sort.DocumentOrderIterator;
import net.sf.saxon.expr.sort.GlobalOrderComparer;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;

public class HomogeneityCheckerIterator
implements SequenceIterator {
    SequenceIterator base = null;
    Location loc;
    int state;

    public HomogeneityCheckerIterator(SequenceIterator base, Location loc) {
        this.base = base;
        this.loc = loc;
        this.state = 0;
    }

    @Override
    public void close() {
        this.base.close();
    }

    private XPathException reportMixedItems() {
        XPathException err = new XPathException("Cannot mix nodes and atomic values in the result of a path expression");
        err.setErrorCode("XPTY0018");
        err.setLocator(this.loc);
        return err;
    }

    @Override
    public Item next() throws XPathException {
        Item item = this.base.next();
        if (item == null) {
            return null;
        }
        if (this.state == 0) {
            if (item instanceof NodeInfo) {
                ArrayList<Item> nodes = new ArrayList<Item>(50);
                nodes.add(item);
                while ((item = this.base.next()) != null) {
                    if (!(item instanceof NodeInfo)) {
                        throw this.reportMixedItems();
                    }
                    nodes.add(item);
                }
                this.base = new DocumentOrderIterator(new ListIterator(nodes), GlobalOrderComparer.getInstance());
                this.state = 1;
                return this.base.next();
            }
            this.state = -1;
        } else if (this.state == -1 && item instanceof NodeInfo) {
            throw this.reportMixedItems();
        }
        return item;
    }
}


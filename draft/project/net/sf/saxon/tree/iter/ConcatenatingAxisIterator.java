/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;

public class ConcatenatingAxisIterator
implements AxisIterator {
    AxisIterator first;
    AxisIterator second;
    AxisIterator active;

    public ConcatenatingAxisIterator(AxisIterator first, AxisIterator second) {
        this.first = first;
        this.second = second;
        this.active = first;
    }

    @Override
    public NodeInfo next() {
        NodeInfo n = this.active.next();
        if (n == null && this.active == this.first) {
            this.active = this.second;
            n = this.second.next();
        }
        return n;
    }

    @Override
    public void close() {
        this.first.close();
        this.second.close();
    }
}


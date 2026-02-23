/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.tree.wrapper.WrappingFunction;

public class WrappingIterator
implements AxisIterator {
    AxisIterator base;
    VirtualNode parent;
    NodeInfo current;
    boolean atomizing = false;
    WrappingFunction wrappingFunction;

    public WrappingIterator(AxisIterator base, WrappingFunction function, VirtualNode parent) {
        this.base = base;
        this.wrappingFunction = function;
        this.parent = parent;
    }

    @Override
    public NodeInfo next() {
        NodeInfo n = this.base.next();
        this.current = n instanceof NodeInfo && !this.atomizing ? this.wrappingFunction.makeWrapper(n, this.parent) : n;
        return this.current;
    }

    public NodeInfo current() {
        return this.current;
    }

    @Override
    public void close() {
        this.base.close();
    }
}


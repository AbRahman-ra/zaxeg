/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import java.util.Iterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.NodeWrappingFunction;

public class NodeWrappingAxisIterator<B>
implements AxisIterator,
LookaheadIterator {
    Iterator<? extends B> base;
    private final NodeWrappingFunction<? super B, NodeInfo> wrappingFunction;

    public NodeWrappingAxisIterator(Iterator<? extends B> base, NodeWrappingFunction<? super B, NodeInfo> wrappingFunction) {
        this.base = base;
        this.wrappingFunction = wrappingFunction;
    }

    public Iterator<? extends B> getBaseIterator() {
        return this.base;
    }

    public NodeWrappingFunction<? super B, NodeInfo> getNodeWrappingFunction() {
        return this.wrappingFunction;
    }

    @Override
    public boolean hasNext() {
        return this.base.hasNext();
    }

    @Override
    public NodeInfo next() {
        while (this.base.hasNext()) {
            B next = this.base.next();
            if (this.isIgnorable(next)) continue;
            return this.wrappingFunction.wrap(next);
        }
        return null;
    }

    public boolean isIgnorable(B node) {
        return false;
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD);
    }
}


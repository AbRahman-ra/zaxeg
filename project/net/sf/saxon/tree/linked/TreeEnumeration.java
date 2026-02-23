/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.linked.NodeImpl;

abstract class TreeEnumeration
implements AxisIterator,
LookaheadIterator {
    protected NodeImpl start;
    protected NodeImpl next;
    protected Predicate<? super NodeInfo> nodeTest;
    protected NodeImpl current = null;
    protected int position = 0;

    public TreeEnumeration(NodeImpl origin, Predicate<? super NodeInfo> nodeTest) {
        this.next = origin;
        this.start = origin;
        this.nodeTest = nodeTest;
    }

    protected boolean conforms(NodeImpl node) {
        return node == null || this.nodeTest == null || this.nodeTest.test(node);
    }

    protected final void advance() {
        do {
            this.step();
        } while (!this.conforms(this.next));
    }

    protected abstract void step();

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public final NodeInfo next() {
        if (this.next == null) {
            this.current = null;
            this.position = -1;
            return null;
        }
        this.current = this.next;
        ++this.position;
        this.advance();
        return this.current;
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD);
    }
}


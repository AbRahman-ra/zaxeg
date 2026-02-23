/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.wrapper.SiblingCountingNode;
import net.sf.saxon.value.SequenceExtent;

public class FocusTrackingIterator
implements FocusIterator,
LookaheadIterator,
GroundedIterator,
LastPositionFinder {
    private SequenceIterator base;
    private Item curr;
    private int pos = 0;
    private int last = -1;
    private SiblingMemory siblingMemory;

    public static FocusTrackingIterator track(SequenceIterator base) {
        return new FocusTrackingIterator(base);
    }

    public FocusTrackingIterator(SequenceIterator base) {
        this.base = base;
    }

    public SequenceIterator getUnderlyingIterator() {
        return this.base;
    }

    @Override
    public Item next() throws XPathException {
        this.curr = this.base.next();
        this.pos = this.curr == null ? -1 : ++this.pos;
        return this.curr;
    }

    @Override
    public Item current() {
        return this.curr;
    }

    @Override
    public int position() {
        return this.pos;
    }

    @Override
    public int getLength() throws XPathException {
        if (this.last == -1) {
            if (this.base.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER)) {
                this.last = ((LastPositionFinder)((Object)this.base)).getLength();
            }
            if (this.last == -1) {
                GroundedValue residue = SequenceExtent.makeResidue(this.base);
                this.last = this.pos + residue.getLength();
                this.base = residue.iterate();
            }
        }
        return this.last;
    }

    @Override
    public boolean hasNext() {
        assert (this.base instanceof LookaheadIterator);
        return ((LookaheadIterator)this.base).hasNext();
    }

    @Override
    public GroundedValue materialize() throws XPathException {
        return this.base.materialize();
    }

    @Override
    public GroundedValue getResidue() throws XPathException {
        return new SequenceExtent(this);
    }

    @Override
    public void close() {
        this.base.close();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return this.base.getProperties();
    }

    public int getSiblingPosition(NodeInfo node, NodeTest nodeTest, int max) {
        NodeInfo prior;
        if (node instanceof SiblingCountingNode && nodeTest instanceof AnyNodeTest) {
            return ((SiblingCountingNode)node).getSiblingPosition();
        }
        if (this.siblingMemory == null) {
            this.siblingMemory = new SiblingMemory();
        } else if (this.siblingMemory.mostRecentNodeTest.equals(nodeTest) && node.equals(this.siblingMemory.mostRecentNode)) {
            return this.siblingMemory.mostRecentPosition;
        }
        SiblingMemory s = this.siblingMemory;
        AxisIterator prev = node.iterateAxis(11, nodeTest);
        int count = 1;
        while ((prior = prev.next()) != null) {
            if (prior.equals(s.mostRecentNode) && nodeTest.equals(s.mostRecentNodeTest)) {
                int result = count + s.mostRecentPosition;
                s.mostRecentNode = node;
                s.mostRecentPosition = result;
                return result;
            }
            if (++count <= max) continue;
            return count;
        }
        s.mostRecentNode = node;
        s.mostRecentPosition = count;
        s.mostRecentNodeTest = nodeTest;
        return count;
    }

    private static class SiblingMemory {
        public NodeTest mostRecentNodeTest = null;
        public NodeInfo mostRecentNode = null;
        public int mostRecentPosition = -1;

        private SiblingMemory() {
        }
    }
}


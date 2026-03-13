/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.One;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.value.EmptySequence;

public class SingleNodeIterator
implements AxisIterator,
ReversibleIterator,
LastPositionFinder,
GroundedIterator,
LookaheadIterator {
    private final NodeInfo item;
    private int position = 0;

    private SingleNodeIterator(NodeInfo value) {
        this.item = value;
    }

    public static AxisIterator makeIterator(NodeInfo item) {
        if (item == null) {
            return EmptyIterator.ofNodes();
        }
        return new SingleNodeIterator(item);
    }

    @Override
    public boolean hasNext() {
        return this.position == 0;
    }

    @Override
    public NodeInfo next() {
        if (this.position == 0) {
            this.position = 1;
            return this.item;
        }
        if (this.position == 1) {
            this.position = -1;
            return null;
        }
        return null;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public SequenceIterator getReverseIterator() {
        return new SingleNodeIterator(this.item);
    }

    public NodeInfo getValue() {
        return this.item;
    }

    @Override
    public GroundedValue materialize() {
        return new ZeroOrOne<NodeInfo>(this.item);
    }

    @Override
    public GroundedValue getResidue() {
        return this.item == null ? EmptySequence.getInstance() : new One<NodeInfo>(this.item);
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.LAST_POSITION_FINDER, SequenceIterator.Property.GROUNDED);
    }
}


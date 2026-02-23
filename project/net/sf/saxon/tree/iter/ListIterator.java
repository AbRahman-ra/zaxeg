/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import java.util.List;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReverseListIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceExtent;

public class ListIterator<T extends Item>
implements UnfailingIterator,
LastPositionFinder,
LookaheadIterator,
GroundedIterator,
ReversibleIterator {
    private int index = 0;
    protected List<T> list;

    public ListIterator(List<T> list) {
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return this.index < this.list.size();
    }

    public T next() {
        if (this.index >= this.list.size()) {
            return null;
        }
        return (T)((Item)this.list.get(this.index++));
    }

    @Override
    public int getLength() {
        return this.list.size();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.GROUNDED, SequenceIterator.Property.LAST_POSITION_FINDER);
    }

    @Override
    public GroundedValue materialize() {
        return SequenceExtent.makeSequenceExtent(this.list);
    }

    @Override
    public GroundedValue getResidue() {
        List<T> l2 = this.list;
        if (this.index != 0) {
            l2 = l2.subList(this.index, l2.size());
        }
        return SequenceExtent.makeSequenceExtent(l2);
    }

    @Override
    public SequenceIterator getReverseIterator() {
        return new ReverseListIterator<T>(this.list);
    }

    public static class OfNodes
    extends ListIterator<NodeInfo>
    implements AxisIterator {
        public OfNodes(List<NodeInfo> list) {
            super(list);
        }

        @Override
        public NodeInfo next() {
            return (NodeInfo)super.next();
        }
    }

    public static class Atomic
    extends ListIterator<AtomicValue>
    implements AtomicIterator<AtomicValue> {
        public Atomic(List<AtomicValue> list) {
            super(list);
        }
    }
}


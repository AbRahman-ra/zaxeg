/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.value.SequenceExtent;

public class ArrayIterator<T extends Item>
implements UnfailingIterator,
LastPositionFinder,
LookaheadIterator,
GroundedIterator {
    protected T[] items;
    private int index;
    protected int start;
    protected int end;

    public ArrayIterator(T[] nodes) {
        this.items = nodes;
        this.start = 0;
        this.end = nodes.length;
        this.index = 0;
    }

    public ArrayIterator(T[] items, int start, int end) {
        this.items = items;
        this.end = end;
        this.start = start;
        this.index = start;
    }

    public SequenceIterator makeSliceIterator(int min, int max) {
        int newEnd;
        int newStart;
        Item[] items = this.getArray();
        int currentStart = this.getStartPosition();
        int currentEnd = this.getEndPosition();
        if (min < 1) {
            min = 1;
        }
        if ((newStart = currentStart + (min - 1)) < currentStart) {
            newStart = currentStart;
        }
        int n = newEnd = max == Integer.MAX_VALUE ? currentEnd : newStart + max - min + 1;
        if (newEnd > currentEnd) {
            newEnd = currentEnd;
        }
        if (newEnd <= newStart) {
            return EmptyIterator.emptyIterator();
        }
        return new ArrayIterator(items, newStart, newEnd);
    }

    @Override
    public boolean hasNext() {
        return this.index < this.end;
    }

    public T next() {
        if (this.index >= this.end) {
            this.index = this.end + 1;
            return null;
        }
        return this.items[this.index++];
    }

    @Override
    public int getLength() {
        return this.end - this.start;
    }

    public T[] getArray() {
        return this.items;
    }

    public int getStartPosition() {
        return this.start;
    }

    public int getEndPosition() {
        return this.end;
    }

    @Override
    public GroundedValue materialize() {
        SequenceExtent seq;
        if (this.start == 0 && this.end == this.items.length) {
            seq = new SequenceExtent((Item[])this.items);
        } else {
            List<T> sublist = Arrays.asList(this.items).subList(this.start, this.end);
            seq = new SequenceExtent(sublist);
        }
        return seq.reduce();
    }

    @Override
    public GroundedValue getResidue() {
        SequenceExtent seq;
        if (this.start == 0 && this.index == 0 && this.end == this.items.length) {
            seq = new SequenceExtent((Item[])this.items);
        } else {
            List<T> sublist = Arrays.asList(this.items).subList(this.start + this.index, this.end);
            seq = new SequenceExtent(sublist);
        }
        return seq.reduce();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.GROUNDED, SequenceIterator.Property.LAST_POSITION_FINDER, SequenceIterator.Property.LOOKAHEAD);
    }

    public static class OfNodes
    extends ArrayIterator<NodeInfo>
    implements AxisIterator {
        public OfNodes(NodeInfo[] list) {
            super((Item[])list);
        }
    }
}


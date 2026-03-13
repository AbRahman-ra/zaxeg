/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import java.util.List;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;

public class ReverseListIterator<T extends Item>
implements UnfailingIterator,
ReversibleIterator,
LookaheadIterator,
LastPositionFinder {
    private final List<T> items;
    private int index;

    public ReverseListIterator(List<T> items) {
        this.items = items;
        this.index = items.size() - 1;
    }

    @Override
    public boolean hasNext() {
        return this.index >= 0;
    }

    public T next() {
        if (this.index >= 0) {
            return (T)((Item)this.items.get(this.index--));
        }
        return null;
    }

    @Override
    public int getLength() {
        return this.items.size();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LAST_POSITION_FINDER);
    }

    @Override
    public SequenceIterator getReverseIterator() {
        return new ListIterator<T>(this.items);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;

public interface ConstrainedIterator<T extends Item>
extends SequenceIterator,
UnfailingIterator,
ReversibleIterator,
LastPositionFinder,
GroundedIterator,
LookaheadIterator {
    @Override
    public boolean hasNext();

    public T next();

    @Override
    public int getLength();

    @Override
    public GroundedValue materialize();
}


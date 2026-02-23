/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.AtomicValue;

public class SingleAtomicIterator<T extends AtomicValue>
extends SingletonIterator<T>
implements AtomicIterator<T>,
ReversibleIterator,
LastPositionFinder,
GroundedIterator,
LookaheadIterator {
    public SingleAtomicIterator(T value) {
        super(value);
    }

    @Override
    public SingleAtomicIterator<T> getReverseIterator() {
        return new SingleAtomicIterator<AtomicValue>((AtomicValue)this.getValue());
    }
}


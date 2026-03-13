/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.value.EmptySequence;

public class SingletonIterator<T extends Item>
implements SequenceIterator,
UnfailingIterator,
ReversibleIterator,
LastPositionFinder,
GroundedIterator,
LookaheadIterator {
    private final T item;
    boolean gone = false;

    public SingletonIterator(T value) {
        this.item = value;
    }

    public static <T extends Item> UnfailingIterator makeIterator(T item) {
        if (item == null) {
            return EmptyIterator.emptyIterator();
        }
        return new SingletonIterator<T>(item);
    }

    public static <T extends Item> SingletonIterator<T> rawIterator(T item) {
        assert (item != null);
        return new SingletonIterator<T>(item);
    }

    @Override
    public boolean hasNext() {
        return !this.gone;
    }

    public T next() {
        if (this.gone) {
            return null;
        }
        this.gone = true;
        return this.item;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public SingletonIterator<T> getReverseIterator() {
        return new SingletonIterator<T>(this.item);
    }

    public T getValue() {
        return this.item;
    }

    @Override
    public GroundedValue materialize() {
        if (this.item != null) {
            return this.item;
        }
        return EmptySequence.getInstance();
    }

    @Override
    public GroundedValue getResidue() {
        return this.gone ? EmptySequence.getInstance() : this.materialize();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.GROUNDED, SequenceIterator.Property.LAST_POSITION_FINDER);
    }
}


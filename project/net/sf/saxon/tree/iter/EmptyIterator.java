/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AtomizedValueIterator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;

public class EmptyIterator
implements SequenceIterator,
ReversibleIterator,
LastPositionFinder,
GroundedIterator,
LookaheadIterator,
UnfailingIterator,
AtomizedValueIterator {
    private static final EmptyIterator theInstance = new EmptyIterator();

    public static EmptyIterator getInstance() {
        return theInstance;
    }

    public static EmptyIterator emptyIterator() {
        return theInstance;
    }

    protected EmptyIterator() {
    }

    @Override
    public AtomicSequence nextAtomizedValue() {
        return null;
    }

    @Override
    public Item next() {
        return null;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public EmptyIterator getReverseIterator() {
        return this;
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.GROUNDED, SequenceIterator.Property.LAST_POSITION_FINDER, SequenceIterator.Property.ATOMIZING);
    }

    @Override
    public GroundedValue materialize() {
        return EmptySequence.getInstance();
    }

    @Override
    public GroundedValue getResidue() {
        return EmptySequence.getInstance();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    public static AxisIterator ofNodes() {
        return OfNodes.THE_INSTANCE;
    }

    public static <T extends AtomicValue> AtomicIterator<T> ofAtomic() {
        return OfAtomic.THE_INSTANCE;
    }

    private static class OfAtomic<T extends AtomicValue>
    implements AtomicIterator<T> {
        public static final OfAtomic THE_INSTANCE = new OfAtomic();

        private OfAtomic() {
        }

        @Override
        public T next() {
            return null;
        }
    }

    private static class OfNodes
    extends EmptyIterator
    implements AxisIterator {
        public static final OfNodes THE_INSTANCE = new OfNodes();

        private OfNodes() {
        }

        @Override
        public NodeInfo next() {
            return null;
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;

public class ManualIterator
implements FocusIterator,
UnfailingIterator,
ReversibleIterator,
LastPositionFinder,
GroundedIterator,
LookaheadIterator {
    private Item item;
    private int position;
    private LastPositionFinder lastPositionFinder;

    public ManualIterator() {
        this.item = null;
        this.position = 0;
    }

    public ManualIterator(Item value, int position) {
        this.item = value;
        this.position = position;
    }

    public ManualIterator(Item value) {
        this.item = value;
        this.position = 1;
        this.lastPositionFinder = () -> 1;
    }

    public void setContextItem(Item value) {
        this.item = value;
    }

    public void setLastPositionFinder(LastPositionFinder finder) {
        this.lastPositionFinder = finder;
    }

    public void incrementPosition() {
        ++this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public boolean hasNext() {
        try {
            return this.position() != this.getLength();
        } catch (XPathException e) {
            return false;
        }
    }

    @Override
    public Item next() {
        return null;
    }

    @Override
    public Item current() {
        return this.item;
    }

    @Override
    public int position() {
        return this.position;
    }

    @Override
    public int getLength() throws XPathException {
        if (this.lastPositionFinder == null) {
            throw new XPathException("Saxon streaming restriction: last() cannot be used when consuming a sequence of streamed nodes, even if the items being processed are grounded");
        }
        return this.lastPositionFinder.getLength();
    }

    @Override
    public ManualIterator getReverseIterator() {
        return new ManualIterator(this.item);
    }

    @Override
    public GroundedValue materialize() {
        return this.item;
    }

    @Override
    public GroundedValue getResidue() {
        return this.materialize();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.GROUNDED, SequenceIterator.Property.LAST_POSITION_FINDER);
    }
}


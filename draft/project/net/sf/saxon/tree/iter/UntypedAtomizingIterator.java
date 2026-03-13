/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.AtomicValue;

public class UntypedAtomizingIterator
implements SequenceIterator,
LastPositionFinder,
LookaheadIterator {
    private final SequenceIterator base;

    public UntypedAtomizingIterator(SequenceIterator base) {
        this.base = base;
    }

    @Override
    public AtomicValue next() throws XPathException {
        Item nextSource = this.base.next();
        if (nextSource == null) {
            return null;
        }
        return (AtomicValue)nextSource.atomize();
    }

    @Override
    public void close() {
        this.base.close();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        EnumSet<SequenceIterator.Property> p = EnumSet.copyOf(this.base.getProperties());
        p.retainAll(EnumSet.of(SequenceIterator.Property.LAST_POSITION_FINDER, SequenceIterator.Property.LOOKAHEAD));
        return p;
    }

    @Override
    public int getLength() throws XPathException {
        return ((LastPositionFinder)((Object)this.base)).getLength();
    }

    @Override
    public boolean hasNext() {
        return ((LookaheadIterator)this.base).hasNext();
    }
}


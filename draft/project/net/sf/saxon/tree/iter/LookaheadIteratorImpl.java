/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class LookaheadIteratorImpl
implements LookaheadIterator {
    private SequenceIterator base;
    private Item next;

    private LookaheadIteratorImpl(SequenceIterator base) throws XPathException {
        this.base = base;
        this.next = base.next();
    }

    public static LookaheadIterator makeLookaheadIterator(SequenceIterator base) throws XPathException {
        if (base.getProperties().contains((Object)SequenceIterator.Property.LOOKAHEAD)) {
            return (LookaheadIterator)base;
        }
        return new LookaheadIteratorImpl(base);
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public Item next() throws XPathException {
        Item current = this.next;
        if (this.next != null) {
            this.next = this.base.next();
        }
        return current;
    }

    @Override
    public void close() {
        this.base.close();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD);
    }
}


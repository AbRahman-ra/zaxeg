/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class LazySequence
implements Sequence {
    SequenceIterator iterator;
    boolean used = false;

    public LazySequence(SequenceIterator iterator) {
        this.iterator = iterator;
    }

    @Override
    public Item head() throws XPathException {
        return this.iterate().next();
    }

    @Override
    public synchronized SequenceIterator iterate() throws XPathException {
        if (this.used) {
            throw new IllegalStateException("A LazySequence can only be read once");
        }
        this.used = true;
        return this.iterator;
    }

    @Override
    public Sequence makeRepeatable() throws XPathException {
        return this.materialize();
    }
}


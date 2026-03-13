/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Iterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class IteratorWrapper
implements SequenceIterator {
    Iterator<? extends Item> iterator;

    public IteratorWrapper(Iterator<? extends Item> iterator) {
        this.iterator = iterator;
    }

    @Override
    public Item next() throws XPathException {
        return this.iterator.hasNext() ? this.iterator.next() : null;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class PrependSequenceIterator
implements SequenceIterator {
    Item start;
    SequenceIterator base;

    public PrependSequenceIterator(Item start, SequenceIterator base) {
        this.start = start;
        this.base = base;
    }

    @Override
    public Item next() throws XPathException {
        if (this.start != null) {
            Item temp = this.start;
            this.start = null;
            return temp;
        }
        return this.base.next();
    }

    @Override
    public void close() {
        this.base.close();
    }
}


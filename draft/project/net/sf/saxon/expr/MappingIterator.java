/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.MappingFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class MappingIterator
implements SequenceIterator {
    private SequenceIterator base;
    private MappingFunction action;
    private SequenceIterator results = null;

    public MappingIterator(SequenceIterator base, MappingFunction action) {
        this.base = base;
        this.action = action;
    }

    @Override
    public Item next() throws XPathException {
        Item nextItem;
        block3: {
            while (true) {
                Item nextSource;
                if (this.results != null) {
                    nextItem = this.results.next();
                    if (nextItem != null) break block3;
                    this.results = null;
                }
                if ((nextSource = this.base.next()) == null) break;
                SequenceIterator obj = this.action.map(nextSource);
                if (obj == null) continue;
                this.results = obj;
                nextItem = this.results.next();
                if (nextItem == null) {
                    this.results = null;
                    continue;
                }
                break block3;
                break;
            }
            this.results = null;
            return null;
        }
        return nextItem;
    }

    @Override
    public void close() {
        if (this.results != null) {
            this.results.close();
        }
        this.base.close();
    }
}


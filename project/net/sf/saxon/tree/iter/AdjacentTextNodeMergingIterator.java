/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.EnumSet;
import net.sf.saxon.expr.AdjacentTextNodeMerger;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Orphan;

public class AdjacentTextNodeMergingIterator
implements LookaheadIterator {
    private SequenceIterator base;
    private Item next;

    public AdjacentTextNodeMergingIterator(SequenceIterator base) throws XPathException {
        this.base = base;
        this.next = base.next();
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public Item next() throws XPathException {
        Item current = this.next;
        if (current == null) {
            return null;
        }
        this.next = this.base.next();
        if (AdjacentTextNodeMerger.isTextNode(current)) {
            FastStringBuffer fsb = new FastStringBuffer(256);
            fsb.cat(current.getStringValueCS());
            while (AdjacentTextNodeMerger.isTextNode(this.next)) {
                fsb.cat(this.next.getStringValueCS());
                this.next = this.base.next();
            }
            if (fsb.isEmpty()) {
                return this.next();
            }
            Orphan o = new Orphan(((NodeInfo)current).getConfiguration());
            o.setNodeKind((short)3);
            o.setStringValue(fsb);
            current = o;
            return current;
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


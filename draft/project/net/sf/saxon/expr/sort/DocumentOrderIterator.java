/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;

public final class DocumentOrderIterator
implements SequenceIterator {
    private SequenceIterator iterator;
    private List<NodeInfo> sequence;
    private ItemOrderComparer comparer;
    private NodeInfo current = null;

    public DocumentOrderIterator(SequenceIterator base, ItemOrderComparer comparer) throws XPathException {
        this.comparer = comparer;
        int len = base.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER) ? ((LastPositionFinder)((Object)base)).getLength() : 50;
        this.sequence = new ArrayList<NodeInfo>(len);
        base.forEachOrFail(item -> {
            if (!(item instanceof NodeInfo)) {
                throw new XPathException("Item in input for sorting is not a node: " + Err.depict(item), "XPTY0004");
            }
            this.sequence.add((NodeInfo)item);
        });
        if (this.sequence.size() > 1) {
            this.sequence.sort(comparer);
        }
        this.iterator = new ListIterator<NodeInfo>(this.sequence);
    }

    @Override
    public NodeInfo next() throws XPathException {
        NodeInfo next;
        do {
            if ((next = (NodeInfo)this.iterator.next()) != null) continue;
            this.current = null;
            return null;
        } while (next.equals(this.current));
        this.current = next;
        return this.current;
    }
}


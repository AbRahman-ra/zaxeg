/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class DifferenceEnumeration
implements SequenceIterator {
    private SequenceIterator p1;
    private SequenceIterator p2;
    private NodeInfo nextNode1 = null;
    private NodeInfo nextNode2 = null;
    private ItemOrderComparer comparer;

    public DifferenceEnumeration(SequenceIterator p1, SequenceIterator p2, ItemOrderComparer comparer) throws XPathException {
        this.p1 = p1;
        this.p2 = p2;
        this.comparer = comparer;
        this.nextNode1 = this.next(p1);
        this.nextNode2 = this.next(p2);
    }

    private NodeInfo next(SequenceIterator iter) throws XPathException {
        return (NodeInfo)iter.next();
    }

    @Override
    public NodeInfo next() throws XPathException {
        while (this.nextNode1 != null) {
            if (this.nextNode2 == null) {
                return this.deliver();
            }
            int c = this.comparer.compare(this.nextNode1, this.nextNode2);
            if (c < 0) {
                return this.deliver();
            }
            if (c > 0) {
                this.nextNode2 = this.next(this.p2);
                if (this.nextNode2 != null) continue;
                return this.deliver();
            }
            this.nextNode2 = this.next(this.p2);
            this.nextNode1 = this.next(this.p1);
        }
        return null;
    }

    private NodeInfo deliver() throws XPathException {
        NodeInfo current = this.nextNode1;
        this.nextNode1 = this.next(this.p1);
        return current;
    }

    @Override
    public void close() {
        this.p1.close();
        this.p2.close();
    }
}


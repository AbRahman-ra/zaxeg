/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class IntersectionEnumeration
implements SequenceIterator {
    private SequenceIterator e1;
    private SequenceIterator e2;
    private NodeInfo nextNode1;
    private NodeInfo nextNode2;
    private ItemOrderComparer comparer;

    public IntersectionEnumeration(SequenceIterator p1, SequenceIterator p2, ItemOrderComparer comparer) throws XPathException {
        this.e1 = p1;
        this.e2 = p2;
        this.comparer = comparer;
        this.nextNode1 = this.next(this.e1);
        this.nextNode2 = this.next(this.e2);
    }

    private NodeInfo next(SequenceIterator iter) throws XPathException {
        return (NodeInfo)iter.next();
    }

    @Override
    public NodeInfo next() throws XPathException {
        if (this.nextNode1 == null || this.nextNode2 == null) {
            return null;
        }
        while (this.nextNode1 != null && this.nextNode2 != null) {
            int c = this.comparer.compare(this.nextNode1, this.nextNode2);
            if (c < 0) {
                this.nextNode1 = this.next(this.e1);
                continue;
            }
            if (c > 0) {
                this.nextNode2 = this.next(this.e2);
                continue;
            }
            NodeInfo current = this.nextNode2;
            this.nextNode2 = this.next(this.e2);
            this.nextNode1 = this.next(this.e1);
            return current;
        }
        return null;
    }

    @Override
    public void close() {
        this.e1.close();
        this.e2.close();
    }
}


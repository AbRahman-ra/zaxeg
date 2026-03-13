/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.EnumSet;
import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class UnionEnumeration
implements SequenceIterator,
LookaheadIterator {
    private SequenceIterator e1;
    private SequenceIterator e2;
    private NodeInfo nextNode1 = null;
    private NodeInfo nextNode2 = null;
    private ItemOrderComparer comparer;

    public UnionEnumeration(SequenceIterator p1, SequenceIterator p2, ItemOrderComparer comparer) throws XPathException {
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
    public boolean hasNext() {
        return this.nextNode1 != null || this.nextNode2 != null;
    }

    @Override
    public NodeInfo next() throws XPathException {
        if (this.nextNode1 != null && this.nextNode2 != null) {
            int c = this.comparer.compare(this.nextNode1, this.nextNode2);
            if (c < 0) {
                NodeInfo current = this.nextNode1;
                this.nextNode1 = this.next(this.e1);
                return current;
            }
            if (c > 0) {
                NodeInfo current = this.nextNode2;
                this.nextNode2 = this.next(this.e2);
                return current;
            }
            NodeInfo current = this.nextNode2;
            this.nextNode2 = this.next(this.e2);
            this.nextNode1 = this.next(this.e1);
            return current;
        }
        if (this.nextNode1 != null) {
            NodeInfo current = this.nextNode1;
            this.nextNode1 = this.next(this.e1);
            return current;
        }
        if (this.nextNode2 != null) {
            NodeInfo current = this.nextNode2;
            this.nextNode2 = this.next(this.e2);
            return current;
        }
        return null;
    }

    @Override
    public void close() {
        this.e1.close();
        this.e2.close();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD);
    }
}


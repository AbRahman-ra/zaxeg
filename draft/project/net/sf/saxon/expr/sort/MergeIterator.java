/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.EnumSet;
import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.expr.sort.ItemWithMergeKeys;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;

public class MergeIterator
implements SequenceIterator,
LookaheadIterator {
    private SequenceIterator e1;
    private SequenceIterator e2;
    private ObjectValue<ItemWithMergeKeys> nextItem1 = null;
    private ObjectValue<ItemWithMergeKeys> nextItem2 = null;
    private ItemOrderComparer comparer;

    public MergeIterator(SequenceIterator p1, SequenceIterator p2, ItemOrderComparer comparer) throws XPathException {
        this.e1 = p1;
        this.e2 = p2;
        this.comparer = comparer;
        this.nextItem1 = (ObjectValue)this.e1.next();
        this.nextItem2 = (ObjectValue)this.e2.next();
    }

    @Override
    public boolean hasNext() {
        return this.nextItem1 != null || this.nextItem2 != null;
    }

    @Override
    public ObjectValue<ItemWithMergeKeys> next() throws XPathException {
        if (this.nextItem1 != null && this.nextItem2 != null) {
            int c;
            try {
                c = this.comparer.compare(this.nextItem1, this.nextItem2);
            } catch (ClassCastException e) {
                ItemWithMergeKeys i1 = this.nextItem1.getObject();
                ItemWithMergeKeys i2 = this.nextItem2.getObject();
                AtomicValue a1 = i1.sortKeyValues.get(0);
                AtomicValue a2 = i2.sortKeyValues.get(0);
                XPathException err = new XPathException("Merge key values are of non-comparable types (" + Type.displayTypeName(a1) + " and " + Type.displayTypeName(a2) + ")", "XTTE2230");
                err.setIsTypeError(true);
                throw err;
            }
            if (c <= 0) {
                ObjectValue<ItemWithMergeKeys> current = this.nextItem1;
                this.nextItem1 = (ObjectValue)this.e1.next();
                return current;
            }
            ObjectValue<ItemWithMergeKeys> current = this.nextItem2;
            this.nextItem2 = (ObjectValue)this.e2.next();
            return current;
        }
        if (this.nextItem1 != null) {
            ObjectValue<ItemWithMergeKeys> current = this.nextItem1;
            this.nextItem1 = (ObjectValue)this.e1.next();
            return current;
        }
        if (this.nextItem2 != null) {
            ObjectValue<ItemWithMergeKeys> current = this.nextItem2;
            this.nextItem2 = (ObjectValue)this.e2.next();
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


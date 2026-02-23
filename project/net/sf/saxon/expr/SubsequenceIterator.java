/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.TailIterator;
import net.sf.saxon.om.EnumSetTool;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class SubsequenceIterator
implements SequenceIterator,
LastPositionFinder,
LookaheadIterator {
    private SequenceIterator base;
    private int basePosition = 0;
    private int min;
    private int max;
    private Item nextItem = null;

    private SubsequenceIterator(SequenceIterator base, int min, int max) throws XPathException {
        this.base = base;
        this.min = min;
        if (min < 1) {
            min = 1;
        }
        this.max = max;
        if (max < min) {
            this.nextItem = null;
            return;
        }
        int i = 1;
        while (i++ <= min) {
            this.nextItem = base.next();
            ++this.basePosition;
            if (this.nextItem != null) continue;
            break;
        }
    }

    public static <T extends Item> SequenceIterator make(SequenceIterator base, int min, int max) throws XPathException {
        if (base instanceof ArrayIterator) {
            return ((ArrayIterator)base).makeSliceIterator(min, max);
        }
        if (max == Integer.MAX_VALUE) {
            return TailIterator.make(base, min);
        }
        if (base.getProperties().contains((Object)SequenceIterator.Property.GROUNDED) && min > 4) {
            GroundedValue value = base.materialize();
            value = value.subsequence(min - 1, max - min + 1);
            return value.iterate();
        }
        return new SubsequenceIterator(base, min, max);
    }

    @Override
    public boolean hasNext() {
        return this.nextItem != null;
    }

    @Override
    public Item next() throws XPathException {
        if (this.nextItem == null) {
            return null;
        }
        Item current = this.nextItem;
        if (this.basePosition < this.max) {
            this.nextItem = this.base.next();
            ++this.basePosition;
        } else {
            this.nextItem = null;
            this.base.close();
        }
        return current;
    }

    @Override
    public void close() {
        this.base.close();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        EnumSet<SequenceIterator.Property> p = EnumSetTool.intersect(this.base.getProperties(), EnumSet.of(SequenceIterator.Property.LAST_POSITION_FINDER));
        return EnumSetTool.union(p, EnumSet.of(SequenceIterator.Property.LOOKAHEAD));
    }

    @Override
    public int getLength() throws XPathException {
        int lastBase = ((LastPositionFinder)((Object)this.base)).getLength();
        int z = Math.min(lastBase, this.max);
        return Math.max(z - this.min + 1, 0);
    }
}


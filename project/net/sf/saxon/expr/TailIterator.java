/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.ValueTailIterator;
import net.sf.saxon.om.EnumSetTool;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class TailIterator
implements SequenceIterator,
LastPositionFinder,
LookaheadIterator {
    private SequenceIterator base;
    private int start;

    private TailIterator(SequenceIterator base, int start) {
        this.base = base;
        this.start = start;
    }

    public static <T extends Item> SequenceIterator make(SequenceIterator base, int start) throws XPathException {
        if (start <= 1) {
            return base;
        }
        if (base instanceof ArrayIterator) {
            return ((ArrayIterator)base).makeSliceIterator(start, Integer.MAX_VALUE);
        }
        if (base.getProperties().contains((Object)SequenceIterator.Property.GROUNDED)) {
            GroundedValue value = base.materialize();
            if (start > value.getLength()) {
                return EmptyIterator.emptyIterator();
            }
            return new ValueTailIterator(value, start - 1);
        }
        for (int i = 0; i < start - 1; ++i) {
            Item b = base.next();
            if (b != null) continue;
            return EmptyIterator.emptyIterator();
        }
        return new TailIterator(base, start);
    }

    @Override
    public Item next() throws XPathException {
        return this.base.next();
    }

    @Override
    public boolean hasNext() {
        return ((LookaheadIterator)this.base).hasNext();
    }

    @Override
    public int getLength() throws XPathException {
        int bl = ((LastPositionFinder)((Object)this.base)).getLength() - this.start + 1;
        return bl > 0 ? bl : 0;
    }

    @Override
    public void close() {
        this.base.close();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSetTool.intersect(this.base.getProperties(), EnumSet.of(SequenceIterator.Property.LAST_POSITION_FINDER, SequenceIterator.Property.LOOKAHEAD));
    }
}


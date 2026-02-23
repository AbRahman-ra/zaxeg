/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.EnumSet;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.EnumSetTool;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class ItemMappingIterator
implements SequenceIterator,
LookaheadIterator,
LastPositionFinder {
    private SequenceIterator base;
    private ItemMappingFunction action;
    private boolean oneToOne = false;

    public ItemMappingIterator(SequenceIterator base, ItemMappingFunction action) {
        this.base = base;
        this.action = action;
    }

    public ItemMappingIterator(SequenceIterator base, ItemMappingFunction action, boolean oneToOne) {
        this.base = base;
        this.action = action;
        this.oneToOne = oneToOne;
    }

    public void setOneToOne(boolean oneToOne) {
        this.oneToOne = oneToOne;
    }

    public boolean isOneToOne() {
        return this.oneToOne;
    }

    protected SequenceIterator getBaseIterator() {
        return this.base;
    }

    protected ItemMappingFunction getMappingFunction() {
        return this.action;
    }

    @Override
    public boolean hasNext() {
        return ((LookaheadIterator)this.base).hasNext();
    }

    @Override
    public Item next() throws XPathException {
        Item nextSource;
        Item current;
        do {
            if ((nextSource = this.base.next()) != null) continue;
            return null;
        } while ((current = this.action.mapItem(nextSource)) == null);
        return current;
    }

    @Override
    public void close() {
        this.base.close();
    }

    @Override
    public int getLength() throws XPathException {
        return ((LastPositionFinder)((Object)this.base)).getLength();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        if (this.oneToOne) {
            return EnumSetTool.intersect(this.base.getProperties(), EnumSet.of(SequenceIterator.Property.LAST_POSITION_FINDER, SequenceIterator.Property.LOOKAHEAD));
        }
        return EnumSet.noneOf(SequenceIterator.Property.class);
    }
}


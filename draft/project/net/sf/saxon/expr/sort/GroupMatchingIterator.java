/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.EnumSet;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;

public abstract class GroupMatchingIterator
implements LookaheadIterator,
LastPositionFinder,
GroupIterator {
    protected Expression select;
    protected FocusIterator population;
    protected Pattern pattern;
    protected XPathContext baseContext;
    protected XPathContext runningContext;
    protected List<Item> currentMembers;
    protected Item next;
    protected Item current = null;
    protected int position = 0;

    protected abstract void advance() throws XPathException;

    @Override
    public AtomicSequence getCurrentGroupingKey() {
        return null;
    }

    @Override
    public SequenceIterator iterateCurrentGroup() {
        return new ListIterator<Item>(this.currentMembers);
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public Item next() throws XPathException {
        if (this.next != null) {
            this.current = this.next;
            ++this.position;
            this.advance();
            return this.current;
        }
        this.current = null;
        this.position = -1;
        return null;
    }

    @Override
    public void close() {
        this.population.close();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.LAST_POSITION_FINDER);
    }
}


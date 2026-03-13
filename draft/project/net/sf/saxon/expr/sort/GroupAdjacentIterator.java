/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.functions.Count;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.AtomicValue;

public class GroupAdjacentIterator
implements GroupIterator,
LastPositionFinder,
LookaheadIterator {
    private Expression select;
    private FocusIterator population;
    private Expression keyExpression;
    private StringCollator collator;
    private XPathContext baseContext;
    private XPathContext runningContext;
    private List<AtomicMatchKey> currentComparisonKey;
    private AtomicSequence currentKey;
    private List<Item> currentMembers;
    private List<AtomicMatchKey> nextComparisonKey;
    private List<AtomicValue> nextKey = null;
    private Item next;
    private Item current = null;
    private int position = 0;
    private boolean composite = false;

    public GroupAdjacentIterator(Expression select, Expression keyExpression, XPathContext baseContext, StringCollator collator, boolean composite) throws XPathException {
        this.select = select;
        this.keyExpression = keyExpression;
        this.baseContext = baseContext;
        this.runningContext = baseContext.newMinorContext();
        this.population = this.runningContext.trackFocus(select.iterate(baseContext));
        this.collator = collator;
        this.composite = composite;
        this.next = this.population.next();
        if (this.next != null) {
            this.nextKey = this.getKey(this.runningContext);
            this.nextComparisonKey = this.getComparisonKey(this.nextKey, baseContext);
        }
    }

    @Override
    public int getLength() throws XPathException {
        GroupAdjacentIterator another = new GroupAdjacentIterator(this.select, this.keyExpression, this.baseContext, this.collator, this.composite);
        return Count.steppingCount(another);
    }

    private List<AtomicValue> getKey(XPathContext context) throws XPathException {
        AtomicValue val;
        ArrayList<AtomicValue> key = new ArrayList<AtomicValue>();
        SequenceIterator iter = this.keyExpression.iterate(context);
        while ((val = (AtomicValue)iter.next()) != null) {
            key.add(val);
        }
        return key;
    }

    private List<AtomicMatchKey> getComparisonKey(List<AtomicValue> key, XPathContext keyContext) throws XPathException {
        ArrayList<AtomicMatchKey> ckey = new ArrayList<AtomicMatchKey>(key.size());
        for (AtomicValue aKey : key) {
            AtomicMatchKey comparisonKey = aKey.isNaN() ? AtomicMatchKey.NaN_MATCH_KEY : aKey.getXPathComparable(false, this.collator, keyContext.getImplicitTimezone());
            ckey.add(comparisonKey);
        }
        return ckey;
    }

    private void advance() throws XPathException {
        Item nextCandidate;
        this.currentMembers = new ArrayList<Item>(20);
        this.currentMembers.add(this.current);
        while ((nextCandidate = this.population.next()) != null) {
            List<AtomicValue> newKey = this.getKey(this.runningContext);
            List<AtomicMatchKey> newComparisonKey = this.getComparisonKey(newKey, this.baseContext);
            try {
                if (this.currentComparisonKey.equals(newComparisonKey)) {
                    this.currentMembers.add(nextCandidate);
                    continue;
                }
                this.next = nextCandidate;
                this.nextComparisonKey = newComparisonKey;
                this.nextKey = newKey;
                return;
            } catch (ClassCastException e) {
                String message = "Grouping key values are of non-comparable types";
                XPathException err = new XPathException(message);
                err.setIsTypeError(true);
                err.setXPathContext(this.runningContext);
                throw err;
            }
        }
        this.next = null;
        this.nextKey = null;
    }

    @Override
    public AtomicSequence getCurrentGroupingKey() {
        return this.currentKey;
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
        if (this.next == null) {
            this.current = null;
            this.position = -1;
            return null;
        }
        this.current = this.next;
        this.currentKey = this.nextKey.size() == 1 ? (AtomicSequence)this.nextKey.get(0) : new AtomicArray(this.nextKey);
        this.currentComparisonKey = this.nextComparisonKey;
        ++this.position;
        this.advance();
        return this.current;
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


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.EmptyAtomicSequence;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.AtomicValue;

public class GroupByIterator
implements GroupIterator,
LastPositionFinder,
LookaheadIterator {
    private SequenceIterator population;
    protected Expression keyExpression;
    private StringCollator collator;
    private XPathContext keyContext;
    private int position = 0;
    protected List<List<Item>> groups = new ArrayList<List<Item>>(40);
    protected List<AtomicSequence> groupKeys = new ArrayList<AtomicSequence>(40);
    protected boolean composite;

    public GroupByIterator(SequenceIterator population, Expression keyExpression, XPathContext keyContext, StringCollator collator, boolean composite) throws XPathException {
        this.population = population;
        this.keyExpression = keyExpression;
        this.keyContext = keyContext;
        this.collator = collator;
        this.composite = composite;
        if (composite) {
            this.buildIndexedGroupsComposite();
        } else {
            this.buildIndexedGroups();
        }
    }

    public GroupByIterator() {
    }

    private void buildIndexedGroups() throws XPathException {
        Item item;
        HashMap index = new HashMap(40);
        XPathContextMinor c2 = this.keyContext.newMinorContext();
        FocusIterator focus = c2.trackFocus(this.population);
        int implicitTimezone = c2.getImplicitTimezone();
        while ((item = focus.next()) != null) {
            AtomicValue key;
            SequenceIterator keys = this.keyExpression.iterate(c2);
            boolean firstKey = true;
            while ((key = (AtomicValue)keys.next()) != null) {
                AtomicMatchKey comparisonKey = key.isNaN() ? AtomicMatchKey.NaN_MATCH_KEY : key.getXPathComparable(false, this.collator, implicitTimezone);
                List g = (List)index.get(comparisonKey);
                if (g == null) {
                    ArrayList<Item> newGroup = new ArrayList<Item>(20);
                    newGroup.add(item);
                    this.groups.add(newGroup);
                    this.groupKeys.add(key);
                    index.put(comparisonKey, newGroup);
                } else if (firstKey) {
                    g.add(item);
                } else if (g.get(g.size() - 1) != item) {
                    g.add(item);
                }
                firstKey = false;
            }
        }
    }

    private void buildIndexedGroupsComposite() throws XPathException {
        Item item;
        HashMap index = new HashMap(40);
        XPathContextMinor c2 = this.keyContext.newMinorContext();
        FocusIterator focus = c2.trackFocus(this.population);
        int implicitTimezone = c2.getImplicitTimezone();
        while ((item = focus.next()) != null) {
            AtomicValue key;
            SequenceIterator keys = this.keyExpression.iterate(c2);
            ArrayList<AtomicMatchKey> ckList = new ArrayList<AtomicMatchKey>();
            ArrayList<AtomicValue> compositeKey = new ArrayList<AtomicValue>();
            while ((key = (AtomicValue)keys.next()) != null) {
                compositeKey.add(key);
                AtomicMatchKey comparisonKey = key.isNaN() ? AtomicMatchKey.NaN_MATCH_KEY : key.getXPathComparable(false, this.collator, implicitTimezone);
                ckList.add(comparisonKey);
            }
            List g = (List)index.get(ckList);
            if (g == null) {
                ArrayList<Item> newGroup = new ArrayList<Item>(20);
                newGroup.add(item);
                this.groups.add(newGroup);
                this.groupKeys.add(new AtomicArray(compositeKey));
                index.put(ckList, newGroup);
                continue;
            }
            g.add(item);
        }
    }

    @Override
    public synchronized AtomicSequence getCurrentGroupingKey() {
        AtomicSequence val = this.groupKeys.get(this.position - 1);
        if (val == null) {
            return EmptyAtomicSequence.getInstance();
        }
        return val;
    }

    @Override
    public SequenceIterator iterateCurrentGroup() {
        return new ListIterator<Item>(this.groups.get(this.position - 1));
    }

    public List getCurrentGroup() {
        return this.groups.get(this.position - 1);
    }

    @Override
    public boolean hasNext() {
        return this.position < this.groups.size();
    }

    @Override
    public Item next() throws XPathException {
        if (this.position >= 0 && this.position < this.groups.size()) {
            ++this.position;
            return this.current();
        }
        this.position = -1;
        return null;
    }

    private Item current() {
        if (this.position < 1) {
            return null;
        }
        return this.groups.get(this.position - 1).get(0);
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.LAST_POSITION_FINDER);
    }

    @Override
    public int getLength() throws XPathException {
        return this.groups.size();
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.ReverseListIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceSlice;

public class SequenceExtent
implements GroundedValue {
    private List<? extends Item> value;

    public SequenceExtent(Item[] items) {
        this.value = Arrays.asList(items);
    }

    public SequenceExtent(List<? extends Item> list) {
        this.value = list;
    }

    public SequenceExtent(SequenceIterator iter) throws XPathException {
        int len = !iter.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER) ? 20 : ((LastPositionFinder)((Object)iter)).getLength();
        ArrayList<? extends Item> list = new ArrayList<Item>(len);
        iter.forEachOrFail(list::add);
        this.value = list;
    }

    public static GroundedValue makeSequenceExtent(SequenceIterator iter) throws XPathException {
        return iter.materialize();
    }

    public static GroundedValue fromIterator(SequenceIterator iter) throws XPathException {
        return new SequenceExtent(iter).reduce();
    }

    public static GroundedValue makeResidue(SequenceIterator iter) throws XPathException {
        if (iter.getProperties().contains((Object)SequenceIterator.Property.GROUNDED)) {
            return ((GroundedIterator)iter).getResidue();
        }
        SequenceExtent extent = new SequenceExtent(iter);
        return extent.reduce();
    }

    public static <T extends Item> GroundedValue makeSequenceExtent(List<T> input) {
        int len = input.size();
        if (len == 0) {
            return EmptySequence.getInstance();
        }
        if (len == 1) {
            return (GroundedValue)input.get(0);
        }
        return new SequenceExtent(input);
    }

    @Override
    public String getStringValue() throws XPathException {
        return SequenceTool.getStringValue(this);
    }

    @Override
    public CharSequence getStringValueCS() throws XPathException {
        return SequenceTool.getStringValue(this);
    }

    @Override
    public Item head() {
        return this.itemAt(0);
    }

    @Override
    public int getLength() {
        return this.value.size();
    }

    public int getCardinality() {
        switch (this.value.size()) {
            case 0: {
                return 8192;
            }
            case 1: {
                return 16384;
            }
        }
        return 49152;
    }

    @Override
    public Item itemAt(int n) {
        if (n < 0 || n >= this.getLength()) {
            return null;
        }
        return this.value.get(n);
    }

    @Override
    public ListIterator<? extends Item> iterate() {
        return new ListIterator<Item>(this.value);
    }

    public UnfailingIterator reverseIterate() {
        return new ReverseListIterator<Item>(this.value);
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        int len = this.getLength();
        if (len == 0) {
            return false;
        }
        Item first = this.value.get(0);
        if (first instanceof NodeInfo) {
            return true;
        }
        if (len == 1 && first instanceof AtomicValue) {
            return first.effectiveBooleanValue();
        }
        return ExpressionTool.effectiveBooleanValue(this.iterate());
    }

    @Override
    public GroundedValue subsequence(int start, int length) {
        if (start < 0) {
            start = 0;
        }
        if (start > this.value.size()) {
            return EmptySequence.getInstance();
        }
        return new SequenceSlice(this.value, start, length).reduce();
    }

    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        for (int i = 0; i < this.value.size(); ++i) {
            fsb.append(i == 0 ? "(" : ", ");
            fsb.append(this.value.get(i).toString());
        }
        fsb.cat(')');
        return fsb.toString();
    }

    @Override
    public GroundedValue reduce() {
        int len = this.getLength();
        if (len == 0) {
            return EmptySequence.getInstance();
        }
        if (len == 1) {
            return this.itemAt(0);
        }
        return this;
    }

    @Override
    public Iterable<? extends Item> asIterable() {
        return this.value;
    }

    public Iterator<? extends Item> iterator() {
        return this.value.iterator();
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Iterator;
import java.util.List;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.EmptySequence;

public class SequenceSlice
implements GroundedValue {
    private List<? extends Item> value;
    private int offset;
    private int length;

    public SequenceSlice(List<? extends Item> value, int offset, int length) {
        this.value = value;
        this.offset = offset;
        if (offset < 0 || length < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.length = length > value.size() || offset + length > value.size() ? value.size() - offset : length;
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
        return this.length;
    }

    public int getCardinality() {
        switch (this.getLength()) {
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
        return this.value.get(n + this.offset);
    }

    @Override
    public ListIterator<? extends Item> iterate() {
        return new ListIterator<Item>(this.value.subList(this.offset, this.offset + this.length));
    }

    @Override
    public GroundedValue subsequence(int start, int length) {
        int newStart;
        if (start < 0) {
            start = 0;
        }
        if ((newStart = start + this.offset) > this.value.size()) {
            return EmptySequence.getInstance();
        }
        if (length < 0) {
            return EmptySequence.getInstance();
        }
        int newLength = Integer.min(length, this.length);
        if (newStart + newLength > this.value.size()) {
            newLength = this.value.size() - newStart;
        }
        switch (newLength) {
            case 0: {
                return EmptySequence.getInstance();
            }
            case 1: {
                return this.value.get(newStart);
            }
        }
        return new SequenceSlice(this.value, newStart, newLength);
    }

    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        for (int i = 0; i < this.getLength(); ++i) {
            fsb.append(i == 0 ? "(" : ", ");
            fsb.append(this.itemAt(i).toString());
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
        return this.value.subList(this.offset, this.offset + this.length);
    }

    public Iterator<? extends Item> iterator() {
        return this.value.subList(this.offset, this.offset + this.length).iterator();
    }
}


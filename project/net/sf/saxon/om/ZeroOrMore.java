/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.One;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;

public class ZeroOrMore<T extends Item>
implements GroundedValue,
Iterable<T> {
    private List<T> content;

    public ZeroOrMore(T[] content) {
        this.content = Arrays.asList(content);
    }

    public ZeroOrMore(List<T> content) {
        this.content = content;
    }

    public ZeroOrMore(SequenceIterator iter) throws XPathException {
        this.content = new ArrayList<T>();
        iter.forEachOrFail(item -> this.content.add(item));
    }

    public T head() {
        return (T)(this.content.isEmpty() ? null : (Item)this.content.get(0));
    }

    @Override
    public ListIterator<T> iterate() {
        return new ListIterator<T>(this.content);
    }

    @Override
    public Iterator<T> iterator() {
        return this.content.iterator();
    }

    public T itemAt(int n) {
        if (n >= 0 && n < this.content.size()) {
            return (T)((Item)this.content.get(n));
        }
        return null;
    }

    @Override
    public ZeroOrMore<T> subsequence(int start, int length) {
        if (start < 0) {
            start = 0;
        }
        if (start + length > this.content.size()) {
            length = this.content.size() - start;
        }
        return new ZeroOrMore<T>(this.content.subList(start, start + length));
    }

    @Override
    public int getLength() {
        return this.content.size();
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        return ExpressionTool.effectiveBooleanValue(this.iterate());
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
    public GroundedValue reduce() {
        if (this.content.isEmpty()) {
            return EmptySequence.getInstance();
        }
        if (this.content.size() == 1) {
            Item first = (Item)this.content.get(0);
            if (first instanceof AtomicValue) {
                return first;
            }
            return new One<T>(this.head());
        }
        return this;
    }
}


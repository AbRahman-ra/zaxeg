/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;

public class AtomicArray
implements AtomicSequence {
    private static List<AtomicValue> emptyAtomicList = Collections.emptyList();
    public static AtomicArray EMPTY_ATOMIC_ARRAY = new AtomicArray(emptyAtomicList);
    private List<AtomicValue> content;

    public AtomicArray(List<AtomicValue> content) {
        this.content = content;
    }

    public AtomicArray(SequenceIterator iter) throws XPathException {
        ArrayList<AtomicValue> list = new ArrayList<AtomicValue>(10);
        iter.forEachOrFail(item -> list.add((AtomicValue)item));
        this.content = list;
    }

    @Override
    public AtomicValue head() {
        return this.content.isEmpty() ? null : this.content.get(0);
    }

    @Override
    public AtomicIterator iterate() {
        return new ListIterator.Atomic(this.content);
    }

    @Override
    public AtomicValue itemAt(int n) {
        if (n >= 0 && n < this.content.size()) {
            return this.content.get(n);
        }
        return null;
    }

    @Override
    public int getLength() {
        return this.content.size();
    }

    @Override
    public AtomicArray subsequence(int start, int length) {
        if (start < 0) {
            start = 0;
        }
        if (start + length > this.content.size()) {
            length = this.content.size() - start;
        }
        return new AtomicArray(this.content.subList(start, start + length));
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        return this.getStringValueCS();
    }

    @Override
    public CharSequence getStringValueCS() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        boolean first = true;
        for (AtomicValue av : this.content) {
            if (!first) {
                fsb.cat(' ');
            } else {
                first = false;
            }
            fsb.cat(av.getStringValueCS());
        }
        return fsb.condense();
    }

    @Override
    public String getStringValue() {
        return this.getStringValueCS().toString();
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        return ExpressionTool.effectiveBooleanValue(this.iterate());
    }

    public Comparable getSchemaComparable() {
        if (this.content.size() == 1) {
            return this.content.get(0).getSchemaComparable();
        }
        return new ValueSchemaComparable();
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
    public Iterator<AtomicValue> iterator() {
        return this.content.iterator();
    }

    private class ValueSchemaComparable
    implements Comparable<ValueSchemaComparable> {
        private ValueSchemaComparable() {
        }

        public AtomicArray getValue() {
            return AtomicArray.this;
        }

        @Override
        public int compareTo(ValueSchemaComparable obj) {
            AtomicValue item2;
            AtomicValue item1;
            int c;
            AtomicIterator iter1 = this.getValue().iterate();
            AtomicIterator iter2 = obj.getValue().iterate();
            do {
                item1 = (AtomicValue)iter1.next();
                item2 = (AtomicValue)iter2.next();
                if (item1 == null && item2 == null) {
                    return 0;
                }
                if (item1 == null) {
                    return -1;
                }
                if (item2 != null) continue;
                return 1;
            } while ((c = item1.getSchemaComparable().compareTo(item2.getSchemaComparable())) == 0);
            return c;
        }

        public boolean equals(Object obj) {
            return ValueSchemaComparable.class.isAssignableFrom(obj.getClass()) && this.compareTo((ValueSchemaComparable)obj) == 0;
        }

        public int hashCode() {
            try {
                int hash = 107189858;
                AtomicIterator iter = this.getValue().iterate();
                while (true) {
                    Item item;
                    if ((item = iter.next()) == null) {
                        return hash;
                    }
                    if (!(item instanceof AtomicValue)) continue;
                    hash ^= ((AtomicValue)item).getSchemaComparable().hashCode();
                }
            } catch (XPathException e) {
                return 0;
            }
        }
    }
}


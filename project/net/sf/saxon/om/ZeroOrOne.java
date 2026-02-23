/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ConstrainedIterator;
import net.sf.saxon.value.EmptySequence;

public class ZeroOrOne<T extends Item>
implements GroundedValue {
    private T item;
    private static ZeroOrOne EMPTY = new ZeroOrOne<Object>(null);

    public static <T extends Item> ZeroOrOne<T> empty() {
        return EMPTY;
    }

    public ZeroOrOne(T item) {
        this.item = item;
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.item == null ? "" : this.item.getStringValueCS();
    }

    @Override
    public String getStringValue() {
        return this.item == null ? "" : this.item.getStringValue();
    }

    public T head() {
        return this.item;
    }

    @Override
    public int getLength() {
        return this.item == null ? 0 : 1;
    }

    public T itemAt(int n) {
        if (n == 0 && this.item != null) {
            return this.item;
        }
        return null;
    }

    @Override
    public GroundedValue subsequence(int start, int length) {
        if (this.item != null && start <= 0 && start + length > 0) {
            return this;
        }
        return EmptySequence.getInstance();
    }

    @Override
    public ConstrainedIterator<T> iterate() {
        return new ConstrainedIterator<T>(){
            boolean gone = false;

            @Override
            public boolean hasNext() {
                return ZeroOrOne.this.item != null && !this.gone;
            }

            @Override
            public T next() {
                if (this.gone) {
                    return null;
                }
                this.gone = true;
                return ZeroOrOne.this.item;
            }

            @Override
            public int getLength() {
                return ZeroOrOne.this.item == null ? 0 : 1;
            }

            @Override
            public GroundedValue materialize() {
                return ZeroOrOne.this.item == null ? EmptySequence.getInstance() : ZeroOrOne.this.item;
            }

            @Override
            public GroundedValue getResidue() {
                return this.gone ? EmptySequence.getInstance() : ZeroOrOne.this.item;
            }

            @Override
            public SequenceIterator getReverseIterator() {
                return ZeroOrOne.this.iterate();
            }
        };
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        return ExpressionTool.effectiveBooleanValue(this.item);
    }

    public String toString() {
        return this.item == null ? "null" : this.item.toString();
    }

    @Override
    public GroundedValue reduce() {
        if (this.item == null) {
            return EmptySequence.getInstance();
        }
        return this.item;
    }
}


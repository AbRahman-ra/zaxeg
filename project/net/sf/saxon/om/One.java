/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.tree.iter.ConstrainedIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;

public class One<T extends Item>
extends ZeroOrOne<T> {
    public One(T item) {
        super(item);
        if (item == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public ConstrainedIterator<T> iterate() {
        return new ConstrainedIterator<T>(){
            boolean gone = false;

            @Override
            public boolean hasNext() {
                return !this.gone;
            }

            @Override
            public T next() {
                if (this.gone) {
                    return null;
                }
                this.gone = true;
                return One.this.head();
            }

            @Override
            public int getLength() {
                return 1;
            }

            @Override
            public GroundedValue materialize() {
                return One.this.head();
            }

            @Override
            public GroundedValue getResidue() {
                return this.gone ? EmptySequence.getInstance() : One.this.head();
            }

            @Override
            public SequenceIterator getReverseIterator() {
                return One.this.iterate();
            }
        };
    }

    public static One<BooleanValue> bool(boolean value) {
        return new One<BooleanValue>(BooleanValue.get(value));
    }

    public static One<StringValue> string(String value) {
        return new One<StringValue>(new StringValue(value));
    }

    public static One<IntegerValue> integer(long value) {
        return new One<IntegerValue>(new Int64Value(value));
    }

    public static One<DoubleValue> dbl(double value) {
        return new One<DoubleValue>(new DoubleValue(value));
    }
}


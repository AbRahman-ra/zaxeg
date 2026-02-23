/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Iterator;
import net.sf.saxon.expr.RangeIterator;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;

public class IntegerRange
implements AtomicSequence {
    public long start;
    public long end;

    public IntegerRange(long start, long end) {
        if (end < start) {
            throw new IllegalArgumentException("end < start in IntegerRange");
        }
        if (end - start > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Maximum length of sequence in Saxon is 2147483647");
        }
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return this.start;
    }

    public long getEnd() {
        return this.end;
    }

    @Override
    public AtomicIterator iterate() {
        return new RangeIterator(this.start, this.end);
    }

    @Override
    public IntegerValue itemAt(int n) {
        if (n < 0 || (long)n > this.end - this.start) {
            return null;
        }
        return Int64Value.makeIntegerValue(this.start + (long)n);
    }

    @Override
    public GroundedValue subsequence(int start, int length) {
        if (length <= 0) {
            return EmptySequence.getInstance();
        }
        long newStart = this.start + (long)(start > 0 ? start : 0);
        long newEnd = newStart + (long)length - 1L;
        if (newEnd > this.end) {
            newEnd = this.end;
        }
        if (newEnd >= newStart) {
            return new IntegerRange(newStart, newEnd);
        }
        return EmptySequence.getInstance();
    }

    @Override
    public int getLength() {
        return (int)(this.end - this.start + 1L);
    }

    @Override
    public IntegerValue head() {
        return new Int64Value(this.start);
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        return this.getStringValueCS();
    }

    public Comparable getSchemaComparable() {
        try {
            return new AtomicArray(this.iterate()).getSchemaComparable();
        } catch (XPathException err) {
            throw new AssertionError((Object)err);
        }
    }

    @Override
    public CharSequence getStringValueCS() {
        try {
            return SequenceTool.getStringValue(this);
        } catch (XPathException err) {
            throw new AssertionError((Object)err);
        }
    }

    @Override
    public String getStringValue() {
        return this.getStringValueCS().toString();
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        return ExpressionTool.effectiveBooleanValue(this.iterate());
    }

    @Override
    public GroundedValue reduce() {
        if (this.start == this.end) {
            return this.itemAt(0);
        }
        return this;
    }

    public String toString() {
        return "(" + this.start + " to " + this.end + ")";
    }

    @Override
    public Iterator<AtomicValue> iterator() {
        return new Iterator<AtomicValue>(){
            long current;
            {
                this.current = IntegerRange.this.start;
            }

            @Override
            public boolean hasNext() {
                return this.current <= IntegerRange.this.end;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public IntegerValue next() {
                return new Int64Value(this.current++);
            }
        };
    }
}


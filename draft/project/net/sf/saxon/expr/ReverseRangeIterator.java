/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.RangeIterator;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;

public class ReverseRangeIterator
implements AtomicIterator<IntegerValue>,
ReversibleIterator,
LastPositionFinder,
LookaheadIterator {
    long start;
    long currentValue;
    long limit;

    public ReverseRangeIterator(long start, long end) throws XPathException {
        if (start - end > Integer.MAX_VALUE) {
            throw new XPathException("Saxon limit on sequence length exceeded (2^31)", "XPDY0130");
        }
        this.start = start;
        this.currentValue = start + 1L;
        this.limit = end;
    }

    @Override
    public boolean hasNext() {
        return this.currentValue > this.limit;
    }

    @Override
    public IntegerValue next() {
        if (--this.currentValue < this.limit) {
            return null;
        }
        return Int64Value.makeIntegerValue(this.currentValue);
    }

    @Override
    public int getLength() {
        return (int)(this.start - this.limit + 1L);
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.LAST_POSITION_FINDER);
    }

    @Override
    public AtomicIterator getReverseIterator() {
        return new RangeIterator(this.limit, this.start);
    }
}


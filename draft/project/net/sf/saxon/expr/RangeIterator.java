/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.EnumSet;
import net.sf.saxon.expr.BigRangeIterator;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.ReverseRangeIterator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerRange;
import net.sf.saxon.value.IntegerValue;

public class RangeIterator
implements AtomicIterator<IntegerValue>,
ReversibleIterator,
LastPositionFinder,
LookaheadIterator,
GroundedIterator {
    long start;
    long currentValue;
    long limit;

    public static AtomicIterator<IntegerValue> makeRangeIterator(IntegerValue start, IntegerValue end) throws XPathException {
        if (start == null || end == null) {
            return EmptyIterator.ofAtomic();
        }
        if (start.compareTo(end) > 0) {
            return EmptyIterator.ofAtomic();
        }
        if (start instanceof BigIntegerValue || end instanceof BigIntegerValue) {
            return new BigRangeIterator(start.asBigInteger(), end.asBigInteger());
        }
        long startVal = start.longValue();
        long endVal = end.longValue();
        if (endVal - startVal > Integer.MAX_VALUE) {
            throw new XPathException("Saxon limit on sequence length exceeded (2^31)", "XPDY0130");
        }
        return new RangeIterator(startVal, endVal);
    }

    public RangeIterator(long start, long end) {
        this.start = start;
        this.currentValue = start - 1L;
        this.limit = end;
    }

    @Override
    public boolean hasNext() {
        return this.currentValue < this.limit;
    }

    @Override
    public IntegerValue next() {
        if (++this.currentValue > this.limit) {
            return null;
        }
        return Int64Value.makeIntegerValue(this.currentValue);
    }

    @Override
    public int getLength() {
        return (int)(this.limit - this.start + 1L);
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.LAST_POSITION_FINDER, SequenceIterator.Property.GROUNDED);
    }

    @Override
    public AtomicIterator<IntegerValue> getReverseIterator() {
        try {
            return new ReverseRangeIterator(this.limit, this.start);
        } catch (XPathException err) {
            throw new AssertionError((Object)err);
        }
    }

    @Override
    public GroundedValue materialize() {
        return new IntegerRange(this.start, this.limit);
    }

    @Override
    public GroundedValue getResidue() {
        return new IntegerRange(this.currentValue, this.limit);
    }
}


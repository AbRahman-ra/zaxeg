/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.math.BigInteger;
import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.IntegerValue;

public class BigRangeIterator
implements AtomicIterator<IntegerValue>,
LastPositionFinder,
LookaheadIterator {
    BigInteger start;
    BigInteger currentValue;
    BigInteger limit;

    public BigRangeIterator(BigInteger start, BigInteger end) throws XPathException {
        if (end.subtract(start).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new XPathException("Saxon limit on sequence length exceeded (2^31)", "XPDY0130");
        }
        this.start = start;
        this.currentValue = start.subtract(BigInteger.valueOf(1L));
        this.limit = end;
    }

    @Override
    public boolean hasNext() {
        return this.currentValue.compareTo(this.limit) < 0;
    }

    @Override
    public IntegerValue next() {
        this.currentValue = this.currentValue.add(BigInteger.valueOf(1L));
        if (this.currentValue.compareTo(this.limit) > 0) {
            return null;
        }
        return IntegerValue.makeIntegerValue(this.currentValue);
    }

    @Override
    public int getLength() {
        BigInteger len = this.limit.subtract(this.start).add(BigInteger.valueOf(1L));
        if (len.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new UncheckedXPathException(new XPathException("Sequence exceeds Saxon limit (32-bit integer)"));
        }
        return len.intValue();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.LAST_POSITION_FINDER);
    }
}


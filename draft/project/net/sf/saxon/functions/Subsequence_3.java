/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.ArithmeticExpression;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SubsequenceIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;

public class Subsequence_3
extends SystemFunction
implements Callable {
    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return arguments[0].getSpecialProperties();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(Subsequence_3.subSequence(arguments[0].iterate(), (NumericValue)arguments[1].head(), (NumericValue)arguments[2].head(), context));
    }

    public static SequenceIterator subSequence(SequenceIterator seq, NumericValue startVal, NumericValue lengthVal, XPathContext context) throws XPathException {
        if (startVal instanceof Int64Value && lengthVal instanceof Int64Value) {
            long lstart = startVal.longValue();
            if (lstart > Integer.MAX_VALUE) {
                return EmptyIterator.emptyIterator();
            }
            long llength = lengthVal.longValue();
            if (llength > Integer.MAX_VALUE) {
                llength = Integer.MAX_VALUE;
            }
            if (llength < 1L) {
                return EmptyIterator.emptyIterator();
            }
            long lend = lstart + llength - 1L;
            if (lend < 1L) {
                return EmptyIterator.emptyIterator();
            }
            int start = lstart < 1L ? 1 : (int)lstart;
            return SubsequenceIterator.make(seq, start, (int)lend);
        }
        if (startVal.isNaN()) {
            return EmptyIterator.emptyIterator();
        }
        if (startVal.compareTo(Int64Value.MAX_LONG) > 0) {
            return EmptyIterator.emptyIterator();
        }
        startVal = startVal.round(0);
        if (lengthVal.isNaN()) {
            return EmptyIterator.emptyIterator();
        }
        if ((lengthVal = lengthVal.round(0)).compareTo(Int64Value.ZERO) <= 0) {
            return EmptyIterator.emptyIterator();
        }
        NumericValue rend = (NumericValue)ArithmeticExpression.compute(startVal, 0, lengthVal, context);
        if (rend.isNaN()) {
            return EmptyIterator.emptyIterator();
        }
        if ((rend = (NumericValue)ArithmeticExpression.compute(rend, 1, Int64Value.PLUS_ONE, context)).compareTo(Int64Value.ZERO) <= 0) {
            return EmptyIterator.emptyIterator();
        }
        long lstart = startVal.compareTo(Int64Value.PLUS_ONE) <= 0 ? 1L : startVal.longValue();
        if (lstart > Integer.MAX_VALUE) {
            return EmptyIterator.emptyIterator();
        }
        long lend = rend.compareTo(Int64Value.MAX_LONG) >= 0 ? Integer.MAX_VALUE : rend.longValue();
        return SubsequenceIterator.make(seq, (int)lstart, (int)lend);
    }

    @Override
    public String getStreamerName() {
        return "Subsequence";
    }
}


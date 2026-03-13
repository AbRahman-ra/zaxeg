/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.TailExpression;
import net.sf.saxon.expr.TailIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;

public class Subsequence_2
extends SystemFunction
implements Callable {
    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return arguments[0].getSpecialProperties();
    }

    @Override
    public int getCardinality(Expression[] arguments) {
        return arguments[0].getCardinality() | 0x6000;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(Subsequence_2.subSequence(arguments[0].iterate(), (NumericValue)arguments[1].head()));
    }

    public static SequenceIterator subSequence(SequenceIterator seq, NumericValue startVal) throws XPathException {
        long lstart;
        if (startVal instanceof Int64Value) {
            lstart = startVal.longValue();
            if (lstart <= 1L) {
                return seq;
            }
        } else {
            if (startVal.isNaN()) {
                return EmptyIterator.emptyIterator();
            }
            if ((startVal = startVal.round(0)).compareTo(Int64Value.PLUS_ONE) <= 0) {
                return seq;
            }
            if (startVal.compareTo(Int64Value.MAX_LONG) > 0) {
                return EmptyIterator.emptyIterator();
            }
            lstart = startVal.longValue();
        }
        if (lstart > Integer.MAX_VALUE) {
            return EmptyIterator.emptyIterator();
        }
        return TailIterator.make(seq, (int)lstart);
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        try {
            if (Literal.isAtomic(arguments[1]) && !(arguments[0] instanceof ErrorExpression)) {
                NumericValue start = (NumericValue)((Literal)arguments[1]).getValue();
                long intStart = (start = start.round(0)).longValue();
                if (intStart > Integer.MAX_VALUE) {
                    return super.makeFunctionCall(arguments);
                }
                if (intStart <= 0L) {
                    return arguments[0];
                }
                return new TailExpression(arguments[0], (int)intStart);
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return super.makeFunctionCall(arguments);
    }

    @Override
    public String getStreamerName() {
        return "Subsequence";
    }
}


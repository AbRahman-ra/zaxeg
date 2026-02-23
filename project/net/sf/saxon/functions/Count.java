/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;

public class Count
extends SystemFunction {
    @Override
    public IntegerValue[] getIntegerBounds() {
        return new IntegerValue[]{Int64Value.ZERO, Expression.MAX_SEQUENCE_LENGTH};
    }

    public static int count(SequenceIterator iter) throws XPathException {
        if (iter.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER)) {
            return ((LastPositionFinder)((Object)iter)).getLength();
        }
        int n = 0;
        while (iter.next() != null) {
            ++n;
        }
        return n;
    }

    public static int steppingCount(SequenceIterator iter) throws XPathException {
        int n = 0;
        while (iter.next() != null) {
            ++n;
        }
        return n;
    }

    @Override
    public IntegerValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        Sequence arg = arguments[0];
        int size = arg instanceof GroundedValue ? ((GroundedValue)arg).getLength() : Count.count(arg.iterate());
        return Int64Value.makeIntegerValue(size);
    }

    @Override
    public String getCompilerName() {
        return "CountCompiler";
    }

    @Override
    public String getStreamerName() {
        return "Count";
    }
}


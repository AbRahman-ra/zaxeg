/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.StringValue;

public class Compare
extends CollatingFunctionFixed {
    private static Int64Value compare(StringValue s1, StringValue s2, AtomicComparer comparer) throws XPathException {
        if (s1 == null || s2 == null) {
            return null;
        }
        int result = comparer.compareAtomicValues(s1, s2);
        if (result < 0) {
            return Int64Value.MINUS_ONE;
        }
        if (result > 0) {
            return Int64Value.PLUS_ONE;
        }
        return Int64Value.ZERO;
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue arg0 = (StringValue)arguments[0].head();
        StringValue arg1 = (StringValue)arguments[1].head();
        GenericAtomicComparer comparer = new GenericAtomicComparer(this.getStringCollator(), context);
        Int64Value result = Compare.compare(arg0, arg1, comparer);
        return new ZeroOrOne<Int64Value>(result);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;

public class CodepointEqual
extends SystemFunction
implements Callable {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue op1 = (StringValue)arguments[0].head();
        StringValue op2 = (StringValue)arguments[1].head();
        if (op1 == null || op2 == null) {
            return ZeroOrOne.empty();
        }
        return new ZeroOrOne<BooleanValue>(BooleanValue.get(op1.getStringValue().equals(op2.getStringValue())));
    }
}


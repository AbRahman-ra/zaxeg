/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.CalendarValue;

public class Adjust_1
extends SystemFunction {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        CalendarValue in = (CalendarValue)arguments[0].head();
        if (in == null) {
            return ZeroOrOne.empty();
        }
        return new ZeroOrOne<CalendarValue>(in.adjustTimezone(context.getImplicitTimezone()));
    }
}


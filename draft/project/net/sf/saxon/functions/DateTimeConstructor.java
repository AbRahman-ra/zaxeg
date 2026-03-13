/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.TimeValue;

public class DateTimeConstructor
extends SystemFunction {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        DateValue arg0 = (DateValue)arguments[0].head();
        TimeValue arg1 = (TimeValue)arguments[1].head();
        if (arg0 == null || arg1 == null) {
            return ZeroOrOne.empty();
        }
        return new ZeroOrOne<DateTimeValue>(DateTimeValue.makeDateTimeValue(arg0, arg1));
    }

    @Override
    public String getCompilerName() {
        return "DateTimeConstructorCompiler";
    }
}


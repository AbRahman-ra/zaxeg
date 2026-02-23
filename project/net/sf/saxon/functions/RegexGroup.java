/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.StringValue;

public class RegexGroup
extends SystemFunction {
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        RegexIterator iter = context.getCurrentRegexIterator();
        if (iter == null) {
            return StringValue.EMPTY_STRING;
        }
        NumericValue gp0 = (NumericValue)arguments[0].head();
        String s = iter.getRegexGroup((int)gp0.longValue());
        return StringValue.makeStringValue(s);
    }
}


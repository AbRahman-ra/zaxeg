/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.serialize.HTMLURIEscaper;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class EscapeHtmlUri
extends ScalarSystemFunction {
    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        CharSequence s = arg.getStringValueCS();
        return StringValue.makeStringValue(HTMLURIEscaper.escapeURL(s, false, this.getRetainedStaticContext().getConfiguration()));
    }

    @Override
    public ZeroOrOne resultWhenEmpty() {
        return ZERO_LENGTH_STRING;
    }
}


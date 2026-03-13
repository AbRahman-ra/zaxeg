/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

public class CurrentOutputUri
extends SystemFunction
implements Callable {
    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return super.getSpecialProperties(arguments) | 0x2000000;
    }

    public AnyURIValue evaluateItem(XPathContext context) throws XPathException {
        String uri = context.getCurrentOutputUri();
        return uri == null ? null : new AnyURIValue(uri);
    }

    @Override
    public ZeroOrOne<AnyURIValue> call(XPathContext context, Sequence[] arguments) throws XPathException {
        return new ZeroOrOne<AnyURIValue>(this.evaluateItem(context));
    }
}


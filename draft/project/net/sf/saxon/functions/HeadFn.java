/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;

public class HeadFn
extends SystemFunction {
    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return FirstItemExpression.makeFirstItemExpression(arguments[0]);
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        Item head = arguments[0].head();
        return new ZeroOrOne<Item>(head);
    }
}


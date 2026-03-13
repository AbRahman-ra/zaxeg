/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public abstract class StaticContextAccessor
extends SystemFunction {
    public abstract AtomicValue evaluate(RetainedStaticContext var1);

    @Override
    public AtomicValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.evaluate(this.getRetainedStaticContext());
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return Literal.makeLiteral(this.evaluate(this.getRetainedStaticContext()));
    }

    public static class DefaultCollation
    extends StaticContextAccessor {
        @Override
        public AtomicValue evaluate(RetainedStaticContext rsc) {
            return new StringValue(rsc.getDefaultCollationName());
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.One;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;

public abstract class ScalarSystemFunction
extends SystemFunction {
    public static final One ZERO_LENGTH_STRING = One.string("");

    public abstract AtomicValue evaluate(Item var1, XPathContext var2) throws XPathException;

    public ZeroOrOne resultWhenEmpty() {
        return ZeroOrOne.empty();
    }

    @Override
    public final ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        Item val0 = arguments[0].head();
        if (val0 == null) {
            return this.resultWhenEmpty();
        }
        return new ZeroOrOne<AtomicValue>(this.evaluate(val0, context));
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        SystemFunctionCall call = new SystemFunctionCall(this, arguments){

            @Override
            public AtomicValue evaluateItem(XPathContext context) throws XPathException {
                Item val = this.getArg(0).evaluateItem(context);
                if (val == null) {
                    return (AtomicValue)ScalarSystemFunction.this.resultWhenEmpty().head();
                }
                return ScalarSystemFunction.this.evaluate(val, context);
            }
        };
        call.setRetainedStaticContext(this.getRetainedStaticContext());
        return call;
    }
}


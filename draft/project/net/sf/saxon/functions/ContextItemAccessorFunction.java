/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.functions.ConstantFunction;
import net.sf.saxon.functions.ContextAccessorFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class ContextItemAccessorFunction
extends ContextAccessorFunction {
    @Override
    public Function bindContext(XPathContext context) throws XPathException {
        Item ci = context.getContextItem();
        if (ci == null) {
            Callable callable = (context1, arguments) -> {
                throw new XPathException("Context item for " + this.getFunctionName().getDisplayName() + " is absent", "XPDY0002");
            };
            SpecificFunctionType fit = new SpecificFunctionType(new SequenceType[0], SequenceType.ANY_SEQUENCE);
            return new CallableFunction(0, callable, (FunctionItemType)fit);
        }
        ConstantFunction fn = new ConstantFunction(this.evaluate(ci, context));
        fn.setDetails(this.getDetails());
        fn.setRetainedStaticContext(this.getRetainedStaticContext());
        return fn;
    }

    public GroundedValue evaluate(Item item, XPathContext context) throws XPathException {
        SystemFunction f = SystemFunction.makeFunction(this.getDetails().name.getLocalPart(), this.getRetainedStaticContext(), 1);
        return f.call(context, new Sequence[]{item}).materialize();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.evaluate(context.getContextItem(), context);
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        ContextItemExpression arg = new ContextItemExpression();
        return SystemFunction.makeCall(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), arg);
    }

    public Expression makeContextItemExplicit() {
        Expression[] args = new Expression[]{new ContextItemExpression()};
        return SystemFunction.makeCall(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), args);
    }

    public static class Number_0
    extends ContextItemAccessorFunction {
        @Override
        public Expression makeFunctionCall(Expression[] arguments) {
            ContextItemExpression ci = new ContextItemExpression();
            Expression sv = SystemFunction.makeCall("data", this.getRetainedStaticContext(), ci);
            return SystemFunction.makeCall(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), sv);
        }

        @Override
        public GroundedValue evaluate(Item item, XPathContext context) throws XPathException {
            SystemFunction f = SystemFunction.makeFunction(this.getDetails().name.getLocalPart(), this.getRetainedStaticContext(), 1);
            AtomicSequence val = item.atomize();
            switch (val.getLength()) {
                case 0: {
                    return DoubleValue.NaN;
                }
                case 1: {
                    return f.call(context, new Sequence[]{val.head()}).materialize();
                }
            }
            XPathException err = new XPathException("When number() is called with no arguments, the atomized value of the context node must not be a sequence of several atomic values", "XPTY0004");
            err.setIsTypeError(true);
            throw err;
        }
    }

    public static class StringAccessor
    extends ContextItemAccessorFunction {
        @Override
        public Expression makeFunctionCall(Expression[] arguments) {
            ContextItemExpression ci = new ContextItemExpression();
            Expression sv = SystemFunction.makeCall("string", this.getRetainedStaticContext(), ci);
            return SystemFunction.makeCall(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), sv);
        }

        @Override
        public GroundedValue evaluate(Item item, XPathContext context) throws XPathException {
            SystemFunction f = SystemFunction.makeFunction(this.getDetails().name.getLocalPart(), this.getRetainedStaticContext(), 1);
            StringValue val = new StringValue(item.getStringValueCS());
            return f.call(context, new Sequence[]{val}).materialize();
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.StringValue;

public abstract class DynamicContextAccessor
extends SystemFunction {
    private AtomicValue boundValue;

    public void bindContext(XPathContext context) throws XPathException {
        this.boundValue = this.evaluate(context);
    }

    public abstract AtomicValue evaluate(XPathContext var1) throws XPathException;

    @Override
    public AtomicValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        if (this.boundValue != null) {
            return this.boundValue;
        }
        return this.evaluate(context);
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public Item evaluateItem(XPathContext context) throws XPathException {
                return DynamicContextAccessor.this.evaluate(context);
            }

            @Override
            public int getIntrinsicDependencies() {
                return 1024;
            }
        };
    }

    public static class DefaultLanguage
    extends DynamicContextAccessor {
        @Override
        public AtomicValue evaluate(XPathContext context) throws XPathException {
            String lang = context.getConfiguration().getDefaultLanguage();
            return new StringValue(lang, BuiltInAtomicType.LANGUAGE);
        }
    }

    public static class CurrentTime
    extends DynamicContextAccessor {
        @Override
        public AtomicValue evaluate(XPathContext context) throws XPathException {
            DateTimeValue now = DateTimeValue.getCurrentDateTime(context);
            return now.toTimeValue();
        }
    }

    public static class CurrentDate
    extends DynamicContextAccessor {
        @Override
        public AtomicValue evaluate(XPathContext context) throws XPathException {
            DateTimeValue now = DateTimeValue.getCurrentDateTime(context);
            return now.toDateValue();
        }
    }

    public static class CurrentDateTime
    extends DynamicContextAccessor {
        @Override
        public AtomicValue evaluate(XPathContext context) throws XPathException {
            return DateTimeValue.getCurrentDateTime(context);
        }
    }

    public static class ImplicitTimezone
    extends DynamicContextAccessor {
        @Override
        public AtomicValue evaluate(XPathContext context) throws XPathException {
            DateTimeValue now = DateTimeValue.getCurrentDateTime(context);
            return now.getComponent(AccessorFn.Component.TIMEZONE);
        }
    }
}


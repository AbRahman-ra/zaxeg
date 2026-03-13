/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.functions.ConstantFunction;
import net.sf.saxon.functions.ContextAccessorFunction;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;

public abstract class PositionAndLast
extends ContextAccessorFunction {
    private boolean contextPossiblyUndefined = true;

    @Override
    public int getNetCost() {
        return 0;
    }

    @Override
    public Function bindContext(XPathContext context) {
        Int64Value value;
        try {
            value = this.evaluateItem(context);
        } catch (XPathException e) {
            SymbolicName.F name = new SymbolicName.F(this.getFunctionName(), this.getArity());
            Callable callable = (context1, arguments) -> {
                throw e;
            };
            return new CallableFunction(name, callable, this.getFunctionItemType());
        }
        ConstantFunction fn = new ConstantFunction(value);
        fn.setDetails(this.getDetails());
        fn.setRetainedStaticContext(this.getRetainedStaticContext());
        return fn;
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        return new IntegerValue[]{Int64Value.PLUS_ONE, Expression.MAX_SEQUENCE_LENGTH};
    }

    @Override
    public void supplyTypeInformation(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression[] arguments) throws XPathException {
        super.supplyTypeInformation(visitor, contextInfo, arguments);
        if (contextInfo.getItemType() == ErrorType.getInstance()) {
            XPathException err = new XPathException("The context item is absent at this point");
            err.setErrorCode("XPDY0002");
            throw err;
        }
        this.contextPossiblyUndefined = contextInfo.isPossiblyAbsent();
    }

    public boolean isContextPossiblyUndefined() {
        return this.contextPossiblyUndefined;
    }

    public abstract Int64Value evaluateItem(XPathContext var1) throws XPathException;

    @Override
    public IntegerValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.evaluateItem(context);
    }

    public static class Last
    extends PositionAndLast {
        @Override
        public Int64Value evaluateItem(XPathContext c) throws XPathException {
            return Int64Value.makeIntegerValue(c.getLast());
        }

        @Override
        public String getCompilerName() {
            return "LastCompiler";
        }

        @Override
        public String getStreamerName() {
            return "Last";
        }
    }

    public static class Position
    extends PositionAndLast {
        @Override
        public Int64Value evaluateItem(XPathContext c) throws XPathException {
            FocusIterator currentIterator = c.getCurrentIterator();
            if (currentIterator == null) {
                XPathException e = new XPathException("The context item is absent, so position() is undefined");
                e.setXPathContext(c);
                e.setErrorCode("XPDY0002");
                throw e;
            }
            return Int64Value.makeIntegerValue(currentIterator.position());
        }

        @Override
        public String getCompilerName() {
            return "PositionCompiler";
        }
    }
}


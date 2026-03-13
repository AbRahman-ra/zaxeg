/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Negatable;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.BooleanValue;

public class NotFn
extends SystemFunction {
    @Override
    public void supplyTypeInformation(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType, Expression[] arguments) throws XPathException {
        XPathException err = TypeChecker.ebvError(arguments[0], visitor.getConfiguration().getTypeHierarchy());
        if (err != null) {
            throw err;
        }
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return BooleanValue.get(!ExpressionTool.effectiveBooleanValue(arguments[0].iterate()));
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public boolean effectiveBooleanValue(XPathContext c) throws XPathException {
                try {
                    return !this.getArg(0).effectiveBooleanValue(c);
                } catch (XPathException e) {
                    e.maybeSetLocation(this.getLocation());
                    e.maybeSetContext(c);
                    throw e;
                }
            }
        };
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        TypeHierarchy th = visitor.getStaticContext().getConfiguration().getTypeHierarchy();
        if (arguments[0] instanceof Negatable && ((Negatable)((Object)arguments[0])).isNegatable(th)) {
            return ((Negatable)((Object)arguments[0])).negate();
        }
        if (arguments[0].getItemType() instanceof NodeTest) {
            SystemFunction empty = SystemFunction.makeFunction("empty", this.getRetainedStaticContext(), 1);
            return empty.makeFunctionCall(arguments[0]).optimize(visitor, contextInfo);
        }
        return null;
    }

    @Override
    public String getCompilerName() {
        return "NotFnCompiler";
    }

    @Override
    public String getStreamerName() {
        return "NotFn";
    }
}


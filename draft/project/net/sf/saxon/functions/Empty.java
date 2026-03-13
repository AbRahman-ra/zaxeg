/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.AndExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.VennExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.Aggregate;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.BooleanValue;

public class Empty
extends Aggregate {
    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        VennExpression v;
        int c = arguments[0].getCardinality();
        if (c == 49152) {
            return Literal.makeLiteral(BooleanValue.FALSE, arguments[0]);
        }
        if (c == 8192) {
            return Literal.makeLiteral(BooleanValue.TRUE, arguments[0]);
        }
        if (arguments[0] instanceof VennExpression && !visitor.isOptimizeForStreaming() && (v = (VennExpression)arguments[0]).getOperator() == 1) {
            Expression e0 = SystemFunction.makeCall("empty", this.getRetainedStaticContext(), v.getLhsExpression());
            Expression e1 = SystemFunction.makeCall("empty", this.getRetainedStaticContext(), v.getRhsExpression());
            return new AndExpression(e0, e1).optimize(visitor, contextInfo);
        }
        return null;
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return BooleanValue.get(Empty.empty(arguments[0].iterate()));
    }

    private static boolean empty(SequenceIterator iter) throws XPathException {
        boolean result = iter.getProperties().contains((Object)SequenceIterator.Property.LOOKAHEAD) ? !((LookaheadIterator)iter).hasNext() : iter.next() == null;
        iter.close();
        return result;
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
                VennExpression v;
                Expression e2 = super.optimize(visitor, contextInfo);
                if (e2 != this) {
                    return e2;
                }
                int c = this.getArg(0).getCardinality();
                if (c == 49152) {
                    return Literal.makeLiteral(BooleanValue.FALSE, e2);
                }
                if (c == 8192) {
                    return Literal.makeLiteral(BooleanValue.TRUE, e2);
                }
                this.setArg(0, this.getArg(0).unordered(false, visitor.isOptimizeForStreaming()));
                if (this.getArg(0) instanceof VennExpression && (v = (VennExpression)this.getArg(0)).getOperator() == 1 && !visitor.isOptimizeForStreaming()) {
                    Expression e0 = SystemFunction.makeCall("empty", this.getRetainedStaticContext(), v.getLhsExpression());
                    Expression e1 = SystemFunction.makeCall("empty", this.getRetainedStaticContext(), v.getRhsExpression());
                    return new AndExpression(e0, e1).optimize(visitor, contextInfo);
                }
                return this;
            }

            @Override
            public BooleanValue evaluateItem(XPathContext context) throws XPathException {
                return BooleanValue.get(this.effectiveBooleanValue(context));
            }

            @Override
            public boolean effectiveBooleanValue(XPathContext c) throws XPathException {
                SequenceIterator iter = this.getArg(0).iterate(c);
                boolean result = iter.getProperties().contains((Object)SequenceIterator.Property.LOOKAHEAD) ? !((LookaheadIterator)iter).hasNext() : iter.next() == null;
                iter.close();
                return result;
            }

            @Override
            public int getNetCost() {
                return 0;
            }
        };
    }

    @Override
    public String getCompilerName() {
        return "EmptyCompiler";
    }

    @Override
    public String getStreamerName() {
        return "Empty";
    }
}


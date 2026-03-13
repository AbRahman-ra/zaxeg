/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.Count;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.BooleanValue;

public class BooleanFn
extends SystemFunction {
    @Override
    public void supplyTypeInformation(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType, Expression[] arguments) throws XPathException {
        XPathException err = TypeChecker.ebvError(arguments[0], visitor.getConfiguration().getTypeHierarchy());
        if (err != null) {
            throw err;
        }
    }

    public static Expression rewriteEffectiveBooleanValue(Expression exp, ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        boolean forStreaming = visitor.isOptimizeForStreaming();
        if ((exp = ExpressionTool.unsortedIfHomogeneous(exp, forStreaming)) instanceof Literal) {
            GroundedValue val = ((Literal)exp).getValue();
            if (val instanceof BooleanValue) {
                return exp;
            }
            return Literal.makeLiteral(BooleanValue.get(ExpressionTool.effectiveBooleanValue(val.iterate())), exp);
        }
        if (exp instanceof ValueComparison) {
            ValueComparison vc = (ValueComparison)exp;
            if (vc.getResultWhenEmpty() == null) {
                vc.setResultWhenEmpty(BooleanValue.FALSE);
            }
            return exp;
        }
        if (exp.isCallOn(BooleanFn.class)) {
            return ((SystemFunctionCall)exp).getArg(0);
        }
        if (th.isSubType(exp.getItemType(), BuiltInAtomicType.BOOLEAN) && exp.getCardinality() == 16384) {
            return exp;
        }
        if (exp.isCallOn(Count.class)) {
            Expression exists = SystemFunction.makeCall("exists", exp.getRetainedStaticContext(), ((SystemFunctionCall)exp).getArg(0));
            assert (exists != null);
            ExpressionTool.copyLocationInfo(exp, exists);
            return exists.optimize(visitor, contextItemType);
        }
        if (exp.getItemType() instanceof NodeTest) {
            Expression exists = SystemFunction.makeCall("exists", exp.getRetainedStaticContext(), exp);
            assert (exists != null);
            ExpressionTool.copyLocationInfo(exp, exists);
            return exists.optimize(visitor, contextItemType);
        }
        return null;
    }

    @Override
    public BooleanValue call(XPathContext c, Sequence[] arguments) throws XPathException {
        boolean bValue = ExpressionTool.effectiveBooleanValue(arguments[0].iterate());
        return BooleanValue.get(bValue);
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
                Expression ebv;
                Expression e = super.optimize(visitor, contextItemType);
                if (e == this && (ebv = BooleanFn.rewriteEffectiveBooleanValue(this.getArg(0), visitor, contextItemType)) != null) {
                    if ((ebv = ebv.optimize(visitor, contextItemType)).getItemType() == BuiltInAtomicType.BOOLEAN && ebv.getCardinality() == 16384) {
                        ebv.setParentExpression(this.getParentExpression());
                        return ebv;
                    }
                    this.setArg(0, ebv);
                    this.adoptChildExpression(ebv);
                    return this;
                }
                return e;
            }

            @Override
            public boolean effectiveBooleanValue(XPathContext c) throws XPathException {
                try {
                    return this.getArg(0).effectiveBooleanValue(c);
                } catch (XPathException e) {
                    e.maybeSetLocation(this.getLocation());
                    e.maybeSetContext(c);
                    throw e;
                }
            }

            @Override
            public BooleanValue evaluateItem(XPathContext context) throws XPathException {
                return BooleanValue.get(this.effectiveBooleanValue(context));
            }
        };
    }

    @Override
    public String getCompilerName() {
        return "BooleanFnCompiler";
    }

    @Override
    public String getStreamerName() {
        return "BooleanFn";
    }
}


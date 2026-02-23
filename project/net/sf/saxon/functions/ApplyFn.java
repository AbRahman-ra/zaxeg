/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Properties;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.arrays.SquareArrayConstructor;
import net.sf.saxon.ma.map.MapFunctionSet;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class ApplyFn
extends SystemFunction {
    private String dynamicFunctionCall;

    public void setDynamicFunctionCall(String fnExpr) {
        this.dynamicFunctionCall = fnExpr;
    }

    public boolean isDynamicFunctionCall() {
        return this.dynamicFunctionCall != null;
    }

    @Override
    public ItemType getResultItemType(Expression[] args) {
        ItemType fnType = args[0].getItemType();
        if (fnType instanceof MapType) {
            return ((MapType)fnType).getValueType().getPrimaryType();
        }
        if (fnType instanceof ArrayItemType) {
            return ((ArrayItemType)fnType).getMemberType().getPrimaryType();
        }
        if (fnType instanceof FunctionItemType) {
            return ((FunctionItemType)fnType).getResultType().getPrimaryType();
        }
        if (fnType instanceof AnyFunctionType) {
            return AnyItemType.getInstance();
        }
        return AnyItemType.getInstance();
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        if (arguments.length == 2 && arguments[1] instanceof SquareArrayConstructor) {
            Expression target = arguments[0];
            if (target.getItemType() instanceof MapType) {
                return this.makeGetCall(visitor, MapFunctionSet.getInstance(), contextInfo, arguments);
            }
            if (target.getItemType() instanceof ArrayItemType) {
                return this.makeGetCall(visitor, ArrayFunctionSet.getInstance(), contextInfo, arguments);
            }
        }
        return null;
    }

    private Expression makeGetCall(ExpressionVisitor visitor, BuiltInFunctionSet fnSet, ContextItemStaticInfo contextInfo, Expression[] arguments) throws XPathException {
        Expression target = arguments[0];
        Expression key = ((SquareArrayConstructor)arguments[1]).getOperanda().getOperand(0).getChildExpression();
        Expression getter = fnSet.makeFunction("get", 2).makeFunctionCall(target, key);
        getter.setRetainedStaticContext(target.getRetainedStaticContext());
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(visitor.getStaticContext().isInBackwardsCompatibleMode());
        if (fnSet == MapFunctionSet.getInstance()) {
            RoleDiagnostic role = new RoleDiagnostic(20, "key value supplied when calling a map as a function", 0);
            ((SystemFunctionCall)getter).setArg(1, tc.staticTypeCheck(key, SequenceType.SINGLE_ATOMIC, role, visitor));
        } else {
            RoleDiagnostic role = new RoleDiagnostic(20, "subscript supplied when calling an array as a function", 0);
            ((SystemFunctionCall)getter).setArg(1, tc.staticTypeCheck(key, SequenceType.SINGLE_INTEGER, role, visitor));
        }
        return getter.typeCheck(visitor, contextInfo);
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        int i;
        Function function = (Function)arguments[0].head();
        ArrayItem args = (ArrayItem)arguments[1].head();
        if (function.getArity() != args.arrayLength()) {
            String errorCode = this.isDynamicFunctionCall() ? "XPTY0004" : "FOAP0001";
            XPathException err = new XPathException("Number of arguments required for dynamic call to " + function.getDescription() + " is " + function.getArity() + "; number supplied = " + args.arrayLength(), errorCode);
            err.setIsTypeError(this.isDynamicFunctionCall());
            err.setXPathContext(context);
            throw err;
        }
        TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
        FunctionItemType fit = function.getFunctionItemType();
        Sequence[] argArray = new Sequence[args.arrayLength()];
        if (fit == AnyFunctionType.ANY_FUNCTION) {
            for (i = 0; i < argArray.length; ++i) {
                argArray[i] = args.get(i);
            }
        } else {
            for (i = 0; i < argArray.length; ++i) {
                SequenceType expected = fit.getArgumentTypes()[i];
                RoleDiagnostic role = this.isDynamicFunctionCall() ? new RoleDiagnostic(0, "result of " + this.dynamicFunctionCall, i) : new RoleDiagnostic(0, "fn:apply", i + 1);
                Sequence converted = th.applyFunctionConversionRules(args.get(i), expected, role, Loc.NONE);
                argArray[i] = converted.materialize();
            }
        }
        Sequence rawResult = ApplyFn.dynamicCall(function, context, argArray);
        if (function.isTrustedResultType()) {
            return rawResult;
        }
        RoleDiagnostic resultRole = new RoleDiagnostic(5, "fn:apply", -1);
        return th.applyFunctionConversionRules(rawResult, fit.getResultType(), resultRole, Loc.NONE);
    }

    @Override
    public void exportAttributes(ExpressionPresenter out) {
        out.emitAttribute("dyn", this.dynamicFunctionCall);
    }

    @Override
    public void importAttributes(Properties attributes) {
        this.dynamicFunctionCall = attributes.getProperty("dyn");
    }
}


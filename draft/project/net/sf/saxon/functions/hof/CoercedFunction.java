/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.functions.hof.FunctionLiteral;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class CoercedFunction
extends AbstractFunction {
    private Function targetFunction;
    private SpecificFunctionType requiredType;

    public CoercedFunction(Function targetFunction, SpecificFunctionType requiredType) throws XPathException {
        if (targetFunction.getArity() != requiredType.getArity()) {
            throw new XPathException(CoercedFunction.wrongArityMessage(targetFunction, requiredType.getArity()), "XPTY0004");
        }
        this.targetFunction = targetFunction;
        this.requiredType = requiredType;
    }

    public CoercedFunction(SpecificFunctionType requiredType) {
        this.requiredType = requiredType;
    }

    public void setTargetFunction(Function targetFunction) throws XPathException {
        if (targetFunction.getArity() != this.requiredType.getArity()) {
            throw new XPathException(CoercedFunction.wrongArityMessage(targetFunction, this.requiredType.getArity()), "XPTY0004");
        }
        this.targetFunction = targetFunction;
    }

    @Override
    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        if (this.targetFunction instanceof AbstractFunction) {
            ((AbstractFunction)this.targetFunction).typeCheck(visitor, contextItemType);
        }
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        return this.requiredType;
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.targetFunction.getFunctionName();
    }

    @Override
    public String getDescription() {
        return "coerced " + this.targetFunction.getDescription();
    }

    @Override
    public int getArity() {
        return this.targetFunction.getArity();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        RoleDiagnostic role;
        SpecificFunctionType req = this.requiredType;
        SequenceType[] argTypes = this.targetFunction.getFunctionItemType().getArgumentTypes();
        TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
        Sequence[] targetArgs = new Sequence[args.length];
        for (int i = 0; i < args.length; ++i) {
            args[i] = args[i].materialize();
            if (argTypes[i].matches(args[i], th)) {
                targetArgs[i] = args[i];
                continue;
            }
            role = new RoleDiagnostic(0, this.targetFunction.getDescription(), i);
            targetArgs[i] = th.applyFunctionConversionRules(args[i], argTypes[i], role, Loc.NONE);
        }
        Sequence rawResult = this.targetFunction.call(context, targetArgs);
        rawResult = rawResult.materialize();
        if (req.getResultType().matches(rawResult, th)) {
            return rawResult;
        }
        role = new RoleDiagnostic(5, this.targetFunction.getDescription(), 0);
        return th.applyFunctionConversionRules(rawResult, req.getResultType(), role, Loc.NONE);
    }

    public static CoercedFunction coerce(Function suppliedFunction, SpecificFunctionType requiredType, RoleDiagnostic role) throws XPathException {
        int arity = requiredType.getArity();
        if (suppliedFunction.getArity() != arity) {
            String msg = role.composeErrorMessage((ItemType)requiredType, suppliedFunction, null);
            msg = msg + ". " + CoercedFunction.wrongArityMessage(suppliedFunction, arity);
            throw new XPathException(msg, "XPTY0004");
        }
        return new CoercedFunction(suppliedFunction, requiredType);
    }

    private static String wrongArityMessage(Function supplied, int expected) {
        return "The supplied function (" + supplied.getDescription() + ") has " + FunctionCall.pluralArguments(supplied.getArity()) + " - expected " + expected;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("coercedFn");
        out.emitAttribute("type", this.requiredType.toExportString());
        new FunctionLiteral(this.targetFunction).export(out);
        out.endElement();
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.ListCastableFunction;
import net.sf.saxon.expr.ListConstructorFunction;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.UnionCastableFunction;
import net.sf.saxon.expr.UnionConstructorFunction;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.OriginalFunction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.LocalUnionType;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.UnionType;

public class StaticFunctionCall
extends FunctionCall
implements Callable {
    private Function target;

    public StaticFunctionCall(Function target, Expression[] arguments) {
        if (target.getArity() != arguments.length) {
            throw new IllegalArgumentException("Function call to " + target.getFunctionName() + " with wrong number of arguments (" + arguments.length + ")");
        }
        this.target = target;
        this.setOperanda(arguments, target.getOperandRoles());
    }

    public Function getTargetFunction() {
        return this.target;
    }

    @Override
    public Function getTargetFunction(XPathContext context) {
        return this.getTargetFunction();
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.target.getFunctionName();
    }

    @Override
    public boolean isCallOn(Class<? extends SystemFunction> function) {
        return function.isAssignableFrom(this.target.getClass());
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.checkFunctionCall(this.target, visitor);
        return super.typeCheck(visitor, contextInfo);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Expression[] args = new Expression[this.getArity()];
        for (int i = 0; i < args.length; ++i) {
            args[i] = this.getArg(i).copy(rebindings);
        }
        return new StaticFunctionCall(this.target, args);
    }

    @Override
    protected int computeCardinality() {
        return this.target.getFunctionItemType().getResultType().getCardinality();
    }

    @Override
    public ItemType getItemType() {
        return this.target.getFunctionItemType().getResultType().getPrimaryType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        UType result = this.getItemType().getUType();
        for (Operand o : this.operands()) {
            if (o.getUsage() != OperandUsage.TRANSMISSION) continue;
            result = result.intersection(o.getChildExpression().getStaticUType(contextItemType));
        }
        return result;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.target.call(context, arguments);
    }

    @Override
    public String getExpressionName() {
        return "staticFunctionCall";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        if (this.target instanceof OriginalFunction) {
            ExpressionPresenter.ExportOptions options = (ExpressionPresenter.ExportOptions)out.getOptions();
            OriginalFunction pf = (OriginalFunction)this.target;
            out.startElement("origFC", this);
            out.emitAttribute("name", pf.getFunctionName());
            out.emitAttribute("pack", options.packageMap.get(pf.getComponent().getContainingPackage()) + "");
            for (Operand o : this.operands()) {
                o.getChildExpression().export(out);
            }
            out.endElement();
        } else if (this.target instanceof UnionCastableFunction) {
            UnionType targetType = ((UnionConstructorFunction)this.target).getTargetType();
            out.startElement("castable", this);
            if (targetType instanceof LocalUnionType) {
                out.emitAttribute("to", AlphaCode.fromItemType(targetType));
            } else {
                out.emitAttribute("as", targetType.toExportString());
            }
            out.emitAttribute("flags", "u" + (((UnionConstructorFunction)this.target).isAllowEmpty() ? "e" : ""));
            for (Operand o : this.operands()) {
                o.getChildExpression().export(out);
            }
            out.endElement();
        } else if (this.target instanceof ListCastableFunction) {
            out.startElement("castable", this);
            out.emitAttribute("as", ((ListConstructorFunction)this.target).getTargetType().getStructuredQName());
            out.emitAttribute("flags", "l" + (((ListConstructorFunction)this.target).isAllowEmpty() ? "e" : ""));
            for (Operand o : this.operands()) {
                o.getChildExpression().export(out);
            }
            out.endElement();
        } else if (this.target instanceof UnionConstructorFunction) {
            UnionType targetType = ((UnionConstructorFunction)this.target).getTargetType();
            out.startElement("cast", this);
            if (targetType instanceof LocalUnionType) {
                out.emitAttribute("to", AlphaCode.fromItemType(targetType));
            } else {
                out.emitAttribute("as", targetType.toExportString());
            }
            out.emitAttribute("flags", "u" + (((UnionConstructorFunction)this.target).isAllowEmpty() ? "e" : ""));
            for (Operand o : this.operands()) {
                o.getChildExpression().export(out);
            }
            out.endElement();
        } else if (this.target instanceof ListConstructorFunction) {
            out.startElement("cast", this);
            out.emitAttribute("as", ((ListConstructorFunction)this.target).getTargetType().getStructuredQName());
            out.emitAttribute("flags", "l" + (((ListConstructorFunction)this.target).isAllowEmpty() ? "e" : ""));
            for (Operand o : this.operands()) {
                o.getChildExpression().export(out);
            }
            out.endElement();
        } else {
            super.export(out);
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import java.util.Arrays;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.SequenceType;

public class CurriedFunction
extends AbstractFunction {
    private Function targetFunction;
    private Sequence[] boundValues;
    private FunctionItemType functionType;

    public CurriedFunction(Function targetFunction, Sequence[] boundValues) {
        this.targetFunction = targetFunction;
        this.boundValues = boundValues;
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        if (this.functionType == null) {
            FunctionItemType baseItemType = this.targetFunction.getFunctionItemType();
            SequenceType resultType = SequenceType.ANY_SEQUENCE;
            if (baseItemType instanceof SpecificFunctionType) {
                resultType = baseItemType.getResultType();
            }
            int placeholders = 0;
            for (Sequence boundArgument : this.boundValues) {
                if (boundArgument != null) continue;
                ++placeholders;
            }
            Object[] argTypes = new SequenceType[placeholders];
            if (baseItemType instanceof SpecificFunctionType) {
                int j = 0;
                for (int i = 0; i < this.boundValues.length; ++i) {
                    if (this.boundValues[i] != null) continue;
                    argTypes[j++] = baseItemType.getArgumentTypes()[i];
                }
            } else {
                Arrays.fill(argTypes, SequenceType.ANY_SEQUENCE);
            }
            this.functionType = new SpecificFunctionType((SequenceType[])argTypes, resultType);
        }
        return this.functionType;
    }

    @Override
    public StructuredQName getFunctionName() {
        return null;
    }

    @Override
    public String getDescription() {
        return "partially-applied function " + this.targetFunction.getDescription();
    }

    @Override
    public int getArity() {
        int count = 0;
        for (Sequence v : this.boundValues) {
            if (v != null) continue;
            ++count;
        }
        return count;
    }

    @Override
    public AnnotationList getAnnotations() {
        return this.targetFunction.getAnnotations();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        Sequence[] newArgs = new Sequence[this.boundValues.length];
        int j = 0;
        for (int i = 0; i < newArgs.length; ++i) {
            newArgs[i] = this.boundValues[i] == null ? args[j++] : this.boundValues[i];
        }
        XPathContext c2 = this.targetFunction.makeNewContext(context, null);
        if (this.targetFunction instanceof UserFunction) {
            ((XPathContextMajor)c2).setCurrentComponent(((UserFunction)this.targetFunction).getDeclaringComponent());
        }
        return this.targetFunction.call(c2, newArgs);
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("curriedFunc");
        this.targetFunction.export(out);
        out.startElement("args");
        for (Sequence seq : this.boundValues) {
            if (seq == null) {
                out.startElement("x");
                out.endElement();
                continue;
            }
            Literal.exportValue(seq, out);
        }
        out.endElement();
        out.endElement();
    }
}


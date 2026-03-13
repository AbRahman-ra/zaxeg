/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.SequenceType;

public abstract class ExtensionFunctionDefinition {
    public abstract StructuredQName getFunctionQName();

    public int getMinimumNumberOfArguments() {
        return this.getArgumentTypes().length;
    }

    public int getMaximumNumberOfArguments() {
        return this.getMinimumNumberOfArguments();
    }

    public abstract SequenceType[] getArgumentTypes();

    public abstract SequenceType getResultType(SequenceType[] var1);

    public boolean trustResultType() {
        return false;
    }

    public boolean dependsOnFocus() {
        return false;
    }

    public boolean hasSideEffects() {
        return false;
    }

    public abstract ExtensionFunctionCall makeCallExpression();

    public final Function asFunction() {
        return new AbstractFunction(){

            @Override
            public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
                return ExtensionFunctionDefinition.this.makeCallExpression().call(context, args);
            }

            @Override
            public FunctionItemType getFunctionItemType() {
                return new SpecificFunctionType(ExtensionFunctionDefinition.this.getArgumentTypes(), ExtensionFunctionDefinition.this.getResultType(ExtensionFunctionDefinition.this.getArgumentTypes()));
            }

            @Override
            public StructuredQName getFunctionName() {
                return ExtensionFunctionDefinition.this.getFunctionQName();
            }

            @Override
            public int getArity() {
                return ExtensionFunctionDefinition.this.getArgumentTypes().length;
            }

            @Override
            public String getDescription() {
                return ExtensionFunctionDefinition.this.getFunctionQName().getDisplayName();
            }

            @Override
            public boolean isTrustedResultType() {
                return ExtensionFunctionDefinition.this.trustResultType();
            }
        };
    }
}


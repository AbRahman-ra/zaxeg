/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;

public class UnresolvedXQueryFunctionItem
extends AbstractFunction {
    private final XQueryFunction fd;
    private final SymbolicName.F functionName;
    private final UserFunctionReference ref;

    public UnresolvedXQueryFunctionItem(XQueryFunction fd, SymbolicName.F functionName, UserFunctionReference ref) {
        this.fd = fd;
        this.functionName = functionName;
        this.ref = ref;
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        return new SpecificFunctionType(this.fd.getArgumentTypes(), this.fd.getResultType());
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.functionName.getComponentName();
    }

    @Override
    public int getArity() {
        return this.fd.getNumberOfArguments();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        return this.ref.evaluateItem(context).call(context, args);
    }

    @Override
    public String getDescription() {
        return this.functionName.toString();
    }

    public UserFunctionReference getFunctionReference() {
        return this.ref;
    }
}


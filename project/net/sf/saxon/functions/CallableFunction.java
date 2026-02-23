/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.FunctionItemType;

public class CallableFunction
extends AbstractFunction {
    private Callable callable;
    private SymbolicName.F name;
    private FunctionItemType type;
    private AnnotationList annotations;

    public CallableFunction(SymbolicName.F name, Callable callable, FunctionItemType type) {
        this.name = name;
        this.callable = callable;
        this.type = type;
    }

    public CallableFunction(int arity, Callable callable, FunctionItemType type) {
        this.name = new SymbolicName.F(new StructuredQName("", "anon", "anon"), arity);
        this.callable = callable;
        this.type = type;
    }

    public Callable getCallable() {
        return this.callable;
    }

    public void setCallable(Callable callable) {
        this.callable = callable;
    }

    public void setType(FunctionItemType type) {
        this.type = type;
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        UserFunction uf;
        if (this.type == AnyFunctionType.getInstance() && this.callable instanceof XQueryFunctionLibrary.UnresolvedCallable && (uf = ((XQueryFunctionLibrary.UnresolvedCallable)this.callable).getFunction()) != null) {
            this.type = uf.getFunctionItemType();
        }
        return this.type;
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.name.getComponentName();
    }

    @Override
    public String getDescription() {
        return this.callable.toString();
    }

    @Override
    public int getArity() {
        return this.name.getArity();
    }

    public void setAnnotations(AnnotationList annotations) {
        this.annotations = annotations;
    }

    @Override
    public AnnotationList getAnnotations() {
        return this.annotations;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        return this.callable.call(context, args);
    }

    @Override
    public void export(ExpressionPresenter out) {
        throw new UnsupportedOperationException("A CallableFunction is a transient value that cannot be exported");
    }
}


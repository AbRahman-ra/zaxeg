/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.util.HashSet;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryFunctionBinder;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;

public class ImportedFunctionLibrary
implements FunctionLibrary,
XQueryFunctionBinder {
    private transient QueryModule importingModule;
    private XQueryFunctionLibrary baseLibrary;
    private HashSet<String> namespaces = new HashSet(5);

    public ImportedFunctionLibrary(QueryModule importingModule, XQueryFunctionLibrary baseLibrary) {
        this.importingModule = importingModule;
        this.baseLibrary = baseLibrary;
    }

    public void addImportedNamespace(String namespace) {
        this.namespaces.add(namespace);
    }

    @Override
    public Expression bind(SymbolicName.F symbolicName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        StructuredQName functionName = symbolicName.getComponentName();
        String uri = functionName.getURI();
        RetainedStaticContext rsc = new RetainedStaticContext(env);
        for (Expression arg : staticArgs) {
            if (arg.getLocalRetainedStaticContext() != null) continue;
            arg.setRetainedStaticContext(rsc);
        }
        if (this.namespaces.contains(uri)) {
            return this.baseLibrary.bind(symbolicName, staticArgs, env, reasons);
        }
        return null;
    }

    @Override
    public XQueryFunction getDeclaration(StructuredQName functionName, int staticArgs) {
        String uri = functionName.getURI();
        if (this.namespaces.contains(uri)) {
            return this.baseLibrary.getDeclaration(functionName, staticArgs);
        }
        return null;
    }

    @Override
    public FunctionLibrary copy() {
        ImportedFunctionLibrary lib = new ImportedFunctionLibrary(this.importingModule, this.baseLibrary);
        for (String ns : this.namespaces) {
            lib.addImportedNamespace(ns);
        }
        return lib;
    }

    public void setImportingModule(QueryModule importingModule) {
        this.importingModule = importingModule;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) throws XPathException {
        if (this.namespaces.contains(functionName.getComponentName().getURI())) {
            return this.baseLibrary.getFunctionItem(functionName, staticContext);
        }
        return null;
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        return this.namespaces.contains(functionName.getComponentName().getURI()) && this.baseLibrary.isAvailable(functionName);
    }
}


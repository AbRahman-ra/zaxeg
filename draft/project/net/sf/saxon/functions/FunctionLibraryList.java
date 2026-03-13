/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryFunctionBinder;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;

public class FunctionLibraryList
implements FunctionLibrary,
XQueryFunctionBinder {
    public List<FunctionLibrary> libraryList = new ArrayList<FunctionLibrary>(8);

    public int addFunctionLibrary(FunctionLibrary lib) {
        this.libraryList.add(lib);
        return this.libraryList.size() - 1;
    }

    public FunctionLibrary get(int n) {
        return this.libraryList.get(n);
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) throws XPathException {
        for (FunctionLibrary lib : this.libraryList) {
            Function fi = lib.getFunctionItem(functionName, staticContext);
            if (fi == null) continue;
            return fi;
        }
        return null;
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        for (FunctionLibrary lib : this.libraryList) {
            if (!lib.isAvailable(functionName)) continue;
            return true;
        }
        return false;
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        boolean debug = env.getConfiguration().getBooleanProperty(Feature.TRACE_EXTERNAL_FUNCTIONS);
        Logger err = env.getConfiguration().getLogger();
        if (debug) {
            err.info("Looking for function " + functionName.getComponentName().getEQName() + "#" + functionName.getArity());
        }
        for (FunctionLibrary lib : this.libraryList) {
            Expression func;
            if (debug) {
                err.info("Trying " + lib.getClass().getName());
            }
            if ((func = lib.bind(functionName, staticArgs, env, reasons)) == null) continue;
            return func;
        }
        if (debug) {
            err.info("Function " + functionName.getComponentName().getEQName() + " not found!");
        }
        return null;
    }

    @Override
    public XQueryFunction getDeclaration(StructuredQName functionName, int staticArgs) {
        for (FunctionLibrary lib : this.libraryList) {
            XQueryFunction func;
            if (!(lib instanceof XQueryFunctionBinder) || (func = ((XQueryFunctionBinder)lib).getDeclaration(functionName, staticArgs)) == null) continue;
            return func;
        }
        return null;
    }

    public List<FunctionLibrary> getLibraryList() {
        return this.libraryList;
    }

    @Override
    public FunctionLibrary copy() {
        FunctionLibraryList fll = new FunctionLibraryList();
        fll.libraryList = new ArrayList<FunctionLibrary>(this.libraryList.size());
        for (int i = 0; i < this.libraryList.size(); ++i) {
            fll.libraryList.add(this.libraryList.get(i).copy());
        }
        return fll;
    }
}


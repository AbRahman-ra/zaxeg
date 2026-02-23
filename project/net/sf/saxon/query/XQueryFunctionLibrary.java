/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.UserFunctionResolvable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.hof.UnresolvedXQueryFunctionItem;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryFunctionBinder;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SpecificFunctionType;

public class XQueryFunctionLibrary
implements FunctionLibrary,
XQueryFunctionBinder {
    private Configuration config;
    private HashMap<SymbolicName, XQueryFunction> functions = new HashMap(20);

    public XQueryFunctionLibrary(Configuration config) {
        this.config = config;
    }

    @Override
    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public void declareFunction(XQueryFunction function) throws XPathException {
        SymbolicName keyObj = function.getIdentificationKey();
        XQueryFunction existing = this.functions.get(keyObj);
        if (existing == function) {
            return;
        }
        if (existing != null) {
            XPathException err = new XPathException("Duplicate definition of function " + function.getDisplayName() + " (see line " + existing.getLineNumber() + " in " + existing.getSystemId() + ')');
            err.setErrorCode("XQST0034");
            err.setIsStaticError(true);
            err.setLocator(function);
            throw err;
        }
        this.functions.put(keyObj, function);
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) throws XPathException {
        XQueryFunction fd = this.functions.get(functionName);
        if (fd != null) {
            if (fd.isPrivate() && !fd.getSystemId().equals(staticContext.getStaticBaseURI())) {
                throw new XPathException("Cannot call the private function " + functionName.getComponentName().getDisplayName() + " from outside its module", "XPST0017");
            }
            UserFunction fn = fd.getUserFunction();
            SpecificFunctionType type = new SpecificFunctionType(fd.getArgumentTypes(), fd.getResultType(), fd.getAnnotations());
            if (fn == null) {
                UserFunction uf = new UserFunction();
                uf.setFunctionName(functionName.getComponentName());
                uf.setResultType(fd.getResultType());
                uf.setParameterDefinitions(fd.getParameterDefinitions());
                UserFunctionReference ref = new UserFunctionReference(uf);
                fd.registerReference(ref);
                return new UnresolvedXQueryFunctionItem(fd, functionName, ref);
            }
            return fn;
        }
        return null;
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        return this.functions.get(functionName) != null;
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] arguments, StaticContext env, List<String> reasons) {
        XQueryFunction fd = this.functions.get(functionName);
        if (fd != null) {
            if (fd.isPrivate() && fd.getStaticContext() != env) {
                reasons.add("Cannot call the private XQuery function " + functionName.getComponentName().getDisplayName() + " from outside its module");
                return null;
            }
            UserFunctionCall ufc = new UserFunctionCall();
            ufc.setFunctionName(fd.getFunctionName());
            ufc.setArguments(arguments);
            ufc.setStaticType(fd.getResultType());
            UserFunction fn = fd.getUserFunction();
            if (fn == null) {
                fd.registerReference(ufc);
            } else {
                ufc.setFunction(fn);
            }
            return ufc;
        }
        return null;
    }

    @Override
    public XQueryFunction getDeclaration(StructuredQName functionName, int staticArgs) {
        SymbolicName functionKey = XQueryFunction.getIdentificationKey(functionName, staticArgs);
        return this.functions.get(functionKey);
    }

    public XQueryFunction getDeclarationByKey(SymbolicName functionKey) {
        return this.functions.get(functionKey);
    }

    public Iterator<XQueryFunction> getFunctionDefinitions() {
        return this.functions.values().iterator();
    }

    protected void fixupGlobalFunctions(QueryModule env) throws XPathException {
        ExpressionVisitor visitor = ExpressionVisitor.make(env);
        for (XQueryFunction fn : this.functions.values()) {
            fn.compile();
        }
        for (XQueryFunction fn : this.functions.values()) {
            fn.checkReferences(visitor);
        }
    }

    protected void optimizeGlobalFunctions(QueryModule topModule) throws XPathException {
        for (XQueryFunction fn : this.functions.values()) {
            if (((QueryModule)fn.getStaticContext()).getTopLevelModule() != topModule) continue;
            fn.optimize();
        }
    }

    public void explainGlobalFunctions(ExpressionPresenter out) throws XPathException {
        for (XQueryFunction fn : this.functions.values()) {
            fn.explain(out);
        }
    }

    public UserFunction getUserDefinedFunction(String uri, String localName, int arity) {
        SymbolicName.F functionKey = new SymbolicName.F(new StructuredQName("", uri, localName), arity);
        XQueryFunction fd = this.functions.get(functionKey);
        if (fd == null) {
            return null;
        }
        return fd.getUserFunction();
    }

    @Override
    public FunctionLibrary copy() {
        XQueryFunctionLibrary qfl = new XQueryFunctionLibrary(this.config);
        qfl.functions = new HashMap<SymbolicName, XQueryFunction>(this.functions);
        return qfl;
    }

    public static class UnresolvedCallable
    implements UserFunctionResolvable,
    Callable {
        SymbolicName.F symbolicName;
        UserFunction function;

        public UnresolvedCallable(SymbolicName.F symbolicName) {
            this.symbolicName = symbolicName;
        }

        public StructuredQName getFunctionName() {
            return this.symbolicName.getComponentName();
        }

        public int getArity() {
            return this.symbolicName.getArity();
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            if (this.function == null) {
                throw new XPathException("Forwards reference to XQuery function has not been resolved");
            }
            Sequence[] args = new Sequence[arguments.length];
            for (int i = 0; i < arguments.length; ++i) {
                args[i] = arguments[i].iterate().materialize();
            }
            return this.function.call(context.newCleanContext(), args);
        }

        @Override
        public void setFunction(UserFunction function) {
            this.function = function;
        }

        public UserFunction getFunction() {
            return this.function;
        }
    }
}


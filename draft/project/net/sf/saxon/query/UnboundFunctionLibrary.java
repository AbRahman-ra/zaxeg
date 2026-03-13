/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.UserFunctionResolvable;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryFunctionBinder;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.FunctionItemType;

public class UnboundFunctionLibrary
implements FunctionLibrary {
    private List<UserFunctionResolvable> unboundFunctionReferences = new ArrayList<UserFunctionResolvable>(20);
    private List<StaticContext> correspondingStaticContext = new ArrayList<StaticContext>(20);
    private List<List<String>> correspondingReasons = new ArrayList<List<String>>();
    private boolean resolving = false;

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] arguments, StaticContext env, List<String> reasons) {
        if (this.resolving) {
            return null;
        }
        if (!reasons.isEmpty() && reasons.get(0).startsWith("Cannot call the private XQuery function")) {
            return null;
        }
        UserFunctionCall ufc = new UserFunctionCall();
        ufc.setFunctionName(functionName.getComponentName());
        ufc.setArguments(arguments);
        this.unboundFunctionReferences.add(ufc);
        this.correspondingStaticContext.add(env);
        this.correspondingReasons.add(reasons);
        return ufc;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) {
        if (this.resolving) {
            return null;
        }
        XQueryFunctionLibrary.UnresolvedCallable uc = new XQueryFunctionLibrary.UnresolvedCallable(functionName);
        this.unboundFunctionReferences.add(uc);
        this.correspondingStaticContext.add(null);
        this.correspondingReasons.add(new ArrayList());
        CallableFunction fi = new CallableFunction(functionName, (Callable)uc, (FunctionItemType)AnyFunctionType.getInstance());
        return fi;
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        return false;
    }

    public void bindUnboundFunctionReferences(XQueryFunctionBinder lib, Configuration config) throws XPathException {
        this.resolving = true;
        for (int i = 0; i < this.unboundFunctionReferences.size(); ++i) {
            int arity;
            UserFunctionResolvable ref = this.unboundFunctionReferences.get(i);
            if (ref instanceof UserFunctionCall) {
                String supplementary;
                UserFunctionCall ufc = (UserFunctionCall)ref;
                QueryModule importingModule = (QueryModule)this.correspondingStaticContext.get(i);
                if (importingModule == null) continue;
                this.correspondingStaticContext.set(i, null);
                StructuredQName q = ufc.getFunctionName();
                int arity2 = ufc.getArity();
                XQueryFunction fd = lib.getDeclaration(q, arity2);
                if (fd != null) {
                    fd.registerReference(ufc);
                    ufc.setStaticType(fd.getResultType());
                }
                if (fd != null) continue;
                StringBuilder sb = new StringBuilder("Cannot find a " + arity2 + "-argument function named " + q.getEQName() + "()");
                List<String> reasons = this.correspondingReasons.get(i);
                for (String reason : reasons) {
                    sb.append(". ").append(reason);
                }
                if (reasons.isEmpty() && (supplementary = XPathParser.getMissingFunctionExplanation(q, config)) != null) {
                    sb.append(". ").append(supplementary);
                }
                XPathException err = new XPathException(sb.toString(), "XPST0017", ufc.getLocation());
                err.setIsStaticError(true);
                throw err;
            }
            if (!(ref instanceof XQueryFunctionLibrary.UnresolvedCallable)) continue;
            XQueryFunctionLibrary.UnresolvedCallable uc = (XQueryFunctionLibrary.UnresolvedCallable)ref;
            StructuredQName q = uc.getFunctionName();
            XQueryFunction fd = lib.getDeclaration(q, arity = uc.getArity());
            if (fd != null) {
                fd.registerReference(uc);
                continue;
            }
            String msg = "Cannot find a " + arity + "-argument function named " + q.getEQName() + "()";
            if (!config.getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)) {
                msg = msg + ". Note: external function calls have been disabled";
            }
            XPathException err = new XPathException(msg);
            err.setErrorCode("XPST0017");
            err.setIsStaticError(true);
            throw err;
        }
    }

    @Override
    public FunctionLibrary copy() {
        UnboundFunctionLibrary qfl = new UnboundFunctionLibrary();
        qfl.unboundFunctionReferences = new ArrayList<UserFunctionResolvable>(this.unboundFunctionReferences);
        qfl.correspondingStaticContext = new ArrayList<StaticContext>(this.correspondingStaticContext);
        qfl.resolving = this.resolving;
        return qfl;
    }
}


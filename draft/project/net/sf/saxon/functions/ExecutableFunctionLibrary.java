/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.Function;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;

public class ExecutableFunctionLibrary
implements FunctionLibrary {
    private transient Configuration config;
    private HashMap<SymbolicName, UserFunction> functions = new HashMap(20);

    public ExecutableFunctionLibrary(Configuration config) {
        this.config = config;
    }

    public void addFunction(UserFunction fn) {
        this.functions.put(fn.getSymbolicName(), fn);
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        UserFunction fn = this.functions.get(functionName);
        if (fn == null) {
            return null;
        }
        UserFunctionCall fc = new UserFunctionCall();
        fc.setFunctionName(functionName.getComponentName());
        fc.setArguments(staticArgs);
        fc.setFunction(fn);
        fc.setStaticType(fn.getResultType());
        return fc;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) throws XPathException {
        UserFunction fn = this.functions.get(functionName);
        if (fn != null && fn.isUpdating()) {
            throw new XPathException("Cannot bind a function item to an updating function");
        }
        return fn;
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        return this.functions.get(functionName) != null;
    }

    @Override
    public FunctionLibrary copy() {
        ExecutableFunctionLibrary efl = new ExecutableFunctionLibrary(this.config);
        efl.functions = new HashMap<SymbolicName, UserFunction>(this.functions);
        return efl;
    }

    public Iterator<UserFunction> iterateFunctions() {
        return this.functions.values().iterator();
    }
}


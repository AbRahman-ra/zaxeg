/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.HashMap;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.IntegratedFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;

public class IntegratedFunctionLibrary
implements FunctionLibrary {
    private HashMap<StructuredQName, ExtensionFunctionDefinition> functions = new HashMap();

    public void registerFunction(ExtensionFunctionDefinition function) {
        this.functions.put(function.getFunctionQName(), function);
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        ExtensionFunctionDefinition defn = this.functions.get(functionName.getComponentName());
        if (defn == null) {
            return null;
        }
        return IntegratedFunctionLibrary.makeFunctionCall(defn, staticArgs);
    }

    public static Expression makeFunctionCall(ExtensionFunctionDefinition defn, Expression[] staticArgs) {
        ExtensionFunctionCall f = defn.makeCallExpression();
        f.setDefinition(defn);
        IntegratedFunctionCall fc = new IntegratedFunctionCall(defn.getFunctionQName(), f);
        fc.setArguments(staticArgs);
        return fc;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) throws XPathException {
        ExtensionFunctionDefinition defn = this.functions.get(functionName.getComponentName());
        if (defn == null) {
            return null;
        }
        try {
            return defn.asFunction();
        } catch (Exception err) {
            throw new XPathException("Failed to create call to extension function " + functionName.getComponentName().getDisplayName(), err);
        }
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        ExtensionFunctionDefinition defn = this.functions.get(functionName.getComponentName());
        int arity = functionName.getArity();
        return defn != null && defn.getMaximumNumberOfArguments() >= arity && defn.getMinimumNumberOfArguments() <= arity;
    }

    @Override
    public FunctionLibrary copy() {
        IntegratedFunctionLibrary lib = new IntegratedFunctionLibrary();
        lib.functions = new HashMap<StructuredQName, ExtensionFunctionDefinition>(this.functions);
        return lib;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.List;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.Function;
import net.sf.saxon.style.StylesheetFunctionLibrary;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;

public class PublicStylesheetFunctionLibrary
implements FunctionLibrary {
    private FunctionLibrary base;

    public PublicStylesheetFunctionLibrary(FunctionLibrary base) {
        this.base = base;
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        Expression baseCall = this.base.bind(functionName, staticArgs, env, reasons);
        if (baseCall instanceof UserFunctionCall) {
            Component target = ((UserFunctionCall)baseCall).getTarget();
            Visibility v = target.getVisibility();
            if (v == Visibility.PUBLIC || v == Visibility.FINAL) {
                return baseCall;
            }
            reasons.add("The function exists, but does not have public visibility");
        }
        return null;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) throws XPathException {
        Visibility v;
        Function baseFunction = this.base.getFunctionItem(functionName, staticContext);
        if (baseFunction instanceof UserFunction && ((v = ((UserFunction)baseFunction).getDeclaredVisibility()) == Visibility.PUBLIC || v == Visibility.FINAL)) {
            return baseFunction;
        }
        return null;
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        if (this.base instanceof StylesheetFunctionLibrary) {
            StylesheetPackage pack = ((StylesheetFunctionLibrary)this.base).getStylesheetPackage();
            UserFunction fn = pack.getFunction(functionName);
            if (fn != null) {
                Visibility v = fn.getDeclaredVisibility();
                return v == Visibility.PUBLIC || v == Visibility.FINAL;
            }
            return false;
        }
        return this.base.isAvailable(functionName);
    }

    @Override
    public FunctionLibrary copy() {
        return this;
    }
}


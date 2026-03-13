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
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.Function;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;

public class StylesheetFunctionLibrary
implements FunctionLibrary {
    private StylesheetPackage pack;
    private boolean overrideExtensionFunction;

    public StylesheetFunctionLibrary(StylesheetPackage sheet, boolean overrideExtensionFunction) {
        this.pack = sheet;
        this.overrideExtensionFunction = overrideExtensionFunction;
    }

    public boolean isOverrideExtensionFunction() {
        return this.overrideExtensionFunction;
    }

    public StylesheetPackage getStylesheetPackage() {
        return this.pack;
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        UserFunction fn = this.pack.getFunction(functionName);
        if (fn == null) {
            return null;
        }
        if (fn.isOverrideExtensionFunction() != this.overrideExtensionFunction) {
            return null;
        }
        UserFunctionCall fc = new UserFunctionCall();
        fc.setFunctionName(functionName.getComponentName());
        fc.setArguments(staticArgs);
        fc.setFunction(fn);
        if (env instanceof ExpressionContext) {
            PrincipalStylesheetModule psm = ((ExpressionContext)env).getStyleElement().getCompilation().getPrincipalStylesheetModule();
            ExpressionVisitor visitor = ExpressionVisitor.make(env);
            psm.addFixupAction(() -> {
                if (fc.getFunction() == null) {
                    Component target = psm.getComponent(fc.getSymbolicName());
                    UserFunction fn1 = (UserFunction)target.getActor();
                    if (fn1 != null) {
                        fc.allocateArgumentEvaluators();
                        fc.setStaticType(fn1.getResultType());
                    } else {
                        XPathException err = new XPathException("There is no available function named " + fc.getDisplayName() + " with " + fc.getArity() + " arguments", "XPST0017");
                        err.setLocator(fc.getLocation());
                        throw err;
                    }
                }
            });
        }
        return fc;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) throws XPathException {
        return this.pack.getFunction(functionName);
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        return this.pack.getFunction(functionName) != null;
    }

    @Override
    public FunctionLibrary copy() {
        return this;
    }
}


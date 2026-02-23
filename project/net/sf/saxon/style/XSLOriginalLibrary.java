/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.List;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StaticFunctionCall;
import net.sf.saxon.expr.instruct.OriginalFunction;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFunction;
import net.sf.saxon.style.XSLOverride;
import net.sf.saxon.style.XSLUsePackage;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;

public class XSLOriginalLibrary
implements FunctionLibrary {
    private static XSLOriginalLibrary THE_INSTANCE = new XSLOriginalLibrary();
    public static StructuredQName XSL_ORIGINAL = new StructuredQName("xsl", "http://www.w3.org/1999/XSL/Transform", "original");

    public static XSLOriginalLibrary getInstance() {
        return THE_INSTANCE;
    }

    private XSLOriginalLibrary() {
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        try {
            Function target = this.getFunctionItem(functionName, env);
            if (target == null) {
                return null;
            }
            return new StaticFunctionCall(target, staticArgs);
        } catch (XPathException e) {
            reasons.add(e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        return false;
    }

    @Override
    public FunctionLibrary copy() {
        return this;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext env) throws XPathException {
        if (functionName.getComponentKind() == 158 && functionName.getComponentName().hasURI("http://www.w3.org/1999/XSL/Transform") && functionName.getComponentName().getLocalPart().equals("original") && env instanceof ExpressionContext) {
            ExpressionContext expressionContext = (ExpressionContext)env;
            StyleElement overridingFunction = expressionContext.getStyleElement();
            while (!(overridingFunction instanceof XSLFunction)) {
                NodeImpl parent = overridingFunction.getParent();
                if (!(parent instanceof StyleElement)) {
                    return null;
                }
                overridingFunction = (StyleElement)parent;
            }
            SymbolicName.F originalName = ((XSLFunction)overridingFunction).getSymbolicName();
            XSLOverride override = (XSLOverride)overridingFunction.getParent();
            XSLUsePackage use = (XSLUsePackage)override.getParent();
            Component overridden = use.getUsedPackage().getComponent(originalName);
            if (overridden == null) {
                throw new XPathException("Function " + originalName + " does not exist in used package", "XTSE3058");
            }
            return new OriginalFunction(overridden);
        }
        return null;
    }
}


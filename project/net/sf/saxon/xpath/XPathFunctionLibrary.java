/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.xpath;

import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.xpath.XPathFunctionCall;

public class XPathFunctionLibrary
implements FunctionLibrary {
    private XPathFunctionResolver resolver;

    public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
        this.resolver = resolver;
    }

    public XPathFunctionResolver getXPathFunctionResolver() {
        return this.resolver;
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        if (this.resolver == null) {
            return null;
        }
        StructuredQName qn = functionName.getComponentName();
        QName name = new QName(qn.getURI(), qn.getLocalPart());
        XPathFunction function = this.resolver.resolveFunction(name, functionName.getArity());
        if (function == null) {
            return null;
        }
        XPathFunctionCall fc = new XPathFunctionCall(qn, function);
        fc.setArguments(staticArgs);
        return fc;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F symbolicName, StaticContext staticContext) throws XPathException {
        if (this.resolver == null) {
            return null;
        }
        StructuredQName functionName = symbolicName.getComponentName();
        int arity = symbolicName.getArity();
        QName name = new QName(functionName.getURI(), functionName.getLocalPart());
        XPathFunction function = this.resolver.resolveFunction(name, arity);
        if (function == null) {
            return null;
        }
        XPathFunctionCall functionCall = new XPathFunctionCall(functionName, function);
        Object[] argTypes = new SequenceType[arity];
        Arrays.fill(argTypes, SequenceType.ANY_SEQUENCE);
        SpecificFunctionType functionType = new SpecificFunctionType((SequenceType[])argTypes, SequenceType.ANY_SEQUENCE);
        return new CallableFunction(symbolicName, (Callable)functionCall, (FunctionItemType)functionType);
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        return this.resolver != null && this.resolver.resolveFunction(new QName(functionName.getComponentName().getURI(), functionName.getComponentName().getLocalPart()), functionName.getArity()) != null;
    }

    @Override
    public FunctionLibrary copy() {
        XPathFunctionLibrary xfl = new XPathFunctionLibrary();
        xfl.resolver = this.resolver;
        return xfl;
    }
}


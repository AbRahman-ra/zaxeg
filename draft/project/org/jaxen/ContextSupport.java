/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.io.Serializable;
import org.jaxen.Function;
import org.jaxen.FunctionContext;
import org.jaxen.NamespaceContext;
import org.jaxen.Navigator;
import org.jaxen.UnresolvableException;
import org.jaxen.VariableContext;

public class ContextSupport
implements Serializable {
    private static final long serialVersionUID = 4494082174713652559L;
    private transient FunctionContext functionContext;
    private NamespaceContext namespaceContext;
    private VariableContext variableContext;
    private Navigator navigator;

    public ContextSupport() {
    }

    public ContextSupport(NamespaceContext namespaceContext, FunctionContext functionContext, VariableContext variableContext, Navigator navigator) {
        this.setNamespaceContext(namespaceContext);
        this.setFunctionContext(functionContext);
        this.setVariableContext(variableContext);
        this.navigator = navigator;
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) {
        this.namespaceContext = namespaceContext;
    }

    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    public void setFunctionContext(FunctionContext functionContext) {
        this.functionContext = functionContext;
    }

    public FunctionContext getFunctionContext() {
        return this.functionContext;
    }

    public void setVariableContext(VariableContext variableContext) {
        this.variableContext = variableContext;
    }

    public VariableContext getVariableContext() {
        return this.variableContext;
    }

    public Navigator getNavigator() {
        return this.navigator;
    }

    public String translateNamespacePrefixToUri(String prefix) {
        if ("xml".equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        NamespaceContext context = this.getNamespaceContext();
        if (context != null) {
            return context.translateNamespacePrefixToUri(prefix);
        }
        return null;
    }

    public Object getVariableValue(String namespaceURI, String prefix, String localName) throws UnresolvableException {
        VariableContext context = this.getVariableContext();
        if (context != null) {
            return context.getVariableValue(namespaceURI, prefix, localName);
        }
        throw new UnresolvableException("No variable context installed");
    }

    public Function getFunction(String namespaceURI, String prefix, String localName) throws UnresolvableException {
        FunctionContext context = this.getFunctionContext();
        if (context != null) {
            return context.getFunction(namespaceURI, prefix, localName);
        }
        throw new UnresolvableException("No function context installed");
    }
}


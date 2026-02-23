/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.util.List;
import org.jaxen.FunctionContext;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.Navigator;
import org.jaxen.VariableContext;

public interface XPath {
    public Object evaluate(Object var1) throws JaxenException;

    public String valueOf(Object var1) throws JaxenException;

    public String stringValueOf(Object var1) throws JaxenException;

    public boolean booleanValueOf(Object var1) throws JaxenException;

    public Number numberValueOf(Object var1) throws JaxenException;

    public List selectNodes(Object var1) throws JaxenException;

    public Object selectSingleNode(Object var1) throws JaxenException;

    public void addNamespace(String var1, String var2) throws JaxenException;

    public void setNamespaceContext(NamespaceContext var1);

    public void setFunctionContext(FunctionContext var1);

    public void setVariableContext(VariableContext var1);

    public NamespaceContext getNamespaceContext();

    public FunctionContext getFunctionContext();

    public VariableContext getVariableContext();

    public Navigator getNavigator();
}


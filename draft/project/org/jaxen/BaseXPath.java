/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.io.Serializable;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.FunctionContext;
import org.jaxen.JaxenException;
import org.jaxen.JaxenHandler;
import org.jaxen.NamespaceContext;
import org.jaxen.Navigator;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.VariableContext;
import org.jaxen.XPath;
import org.jaxen.XPathFunctionContext;
import org.jaxen.expr.Expr;
import org.jaxen.expr.XPathExpr;
import org.jaxen.function.BooleanFunction;
import org.jaxen.function.NumberFunction;
import org.jaxen.function.StringFunction;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.XPathSyntaxException;
import org.jaxen.saxpath.helpers.XPathReaderFactory;
import org.jaxen.util.SingletonList;

public class BaseXPath
implements XPath,
Serializable {
    private static final long serialVersionUID = -1993731281300293168L;
    private final String exprText;
    private final XPathExpr xpath;
    private ContextSupport support;
    private Navigator navigator;

    protected BaseXPath(String xpathExpr) throws JaxenException {
        try {
            XPathReader reader = XPathReaderFactory.createReader();
            JaxenHandler handler = new JaxenHandler();
            reader.setXPathHandler(handler);
            reader.parse(xpathExpr);
            this.xpath = handler.getXPathExpr();
        } catch (XPathSyntaxException e) {
            throw new org.jaxen.XPathSyntaxException(e);
        } catch (SAXPathException e) {
            throw new JaxenException(e);
        }
        this.exprText = xpathExpr;
    }

    public BaseXPath(String xpathExpr, Navigator navigator) throws JaxenException {
        this(xpathExpr);
        this.navigator = navigator;
    }

    public Object evaluate(Object context) throws JaxenException {
        Object first;
        List answer = this.selectNodes(context);
        if (answer != null && answer.size() == 1 && ((first = answer.get(0)) instanceof String || first instanceof Number || first instanceof Boolean)) {
            return first;
        }
        return answer;
    }

    public List selectNodes(Object node) throws JaxenException {
        Context context = this.getContext(node);
        return this.selectNodesForContext(context);
    }

    public Object selectSingleNode(Object node) throws JaxenException {
        List results = this.selectNodes(node);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public String valueOf(Object node) throws JaxenException {
        return this.stringValueOf(node);
    }

    public String stringValueOf(Object node) throws JaxenException {
        Context context = this.getContext(node);
        Object result = this.selectSingleNodeForContext(context);
        if (result == null) {
            return "";
        }
        return StringFunction.evaluate(result, context.getNavigator());
    }

    public boolean booleanValueOf(Object node) throws JaxenException {
        Context context = this.getContext(node);
        List result = this.selectNodesForContext(context);
        if (result == null) {
            return false;
        }
        return BooleanFunction.evaluate(result, context.getNavigator());
    }

    public Number numberValueOf(Object node) throws JaxenException {
        Context context = this.getContext(node);
        Object result = this.selectSingleNodeForContext(context);
        return NumberFunction.evaluate(result, context.getNavigator());
    }

    public void addNamespace(String prefix, String uri) throws JaxenException {
        NamespaceContext nsContext = this.getNamespaceContext();
        if (nsContext instanceof SimpleNamespaceContext) {
            ((SimpleNamespaceContext)nsContext).addNamespace(prefix, uri);
            return;
        }
        throw new JaxenException("Operation not permitted while using a non-simple namespace context.");
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) {
        this.getContextSupport().setNamespaceContext(namespaceContext);
    }

    public void setFunctionContext(FunctionContext functionContext) {
        this.getContextSupport().setFunctionContext(functionContext);
    }

    public void setVariableContext(VariableContext variableContext) {
        this.getContextSupport().setVariableContext(variableContext);
    }

    public NamespaceContext getNamespaceContext() {
        return this.getContextSupport().getNamespaceContext();
    }

    public FunctionContext getFunctionContext() {
        return this.getContextSupport().getFunctionContext();
    }

    public VariableContext getVariableContext() {
        return this.getContextSupport().getVariableContext();
    }

    public Expr getRootExpr() {
        return this.xpath.getRootExpr();
    }

    public String toString() {
        return this.exprText;
    }

    public String debug() {
        return this.xpath.toString();
    }

    protected Context getContext(Object node) {
        if (node instanceof Context) {
            return (Context)node;
        }
        Context fullContext = new Context(this.getContextSupport());
        if (node instanceof List) {
            fullContext.setNodeSet((List)node);
        } else {
            SingletonList list = new SingletonList(node);
            fullContext.setNodeSet(list);
        }
        return fullContext;
    }

    protected ContextSupport getContextSupport() {
        if (this.support == null) {
            this.support = new ContextSupport(this.createNamespaceContext(), this.createFunctionContext(), this.createVariableContext(), this.getNavigator());
        }
        return this.support;
    }

    public Navigator getNavigator() {
        return this.navigator;
    }

    protected FunctionContext createFunctionContext() {
        return XPathFunctionContext.getInstance();
    }

    protected NamespaceContext createNamespaceContext() {
        return new SimpleNamespaceContext();
    }

    protected VariableContext createVariableContext() {
        return new SimpleVariableContext();
    }

    protected List selectNodesForContext(Context context) throws JaxenException {
        List list = this.xpath.asList(context);
        return list;
    }

    protected Object selectSingleNodeForContext(Context context) throws JaxenException {
        List results = this.selectNodesForContext(context);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
}


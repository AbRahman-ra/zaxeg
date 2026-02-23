/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.xpath;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.NodeFilter;
import org.dom4j.XPath;
import org.dom4j.XPathException;
import org.dom4j.xpath.DefaultNamespaceContext;
import org.jaxen.FunctionContext;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.VariableContext;
import org.jaxen.dom4j.Dom4jXPath;

public class DefaultXPath
implements XPath,
NodeFilter,
Serializable {
    private String text;
    private org.jaxen.XPath xpath;
    private NamespaceContext namespaceContext;

    public DefaultXPath(String text) throws InvalidXPathException {
        this.text = text;
        this.xpath = DefaultXPath.parse(text);
    }

    public String toString() {
        return "[XPath: " + this.xpath + "]";
    }

    public String getText() {
        return this.text;
    }

    public FunctionContext getFunctionContext() {
        return this.xpath.getFunctionContext();
    }

    public void setFunctionContext(FunctionContext functionContext) {
        this.xpath.setFunctionContext(functionContext);
    }

    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    public void setNamespaceURIs(Map map) {
        this.setNamespaceContext(new SimpleNamespaceContext(map));
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) {
        this.namespaceContext = namespaceContext;
        this.xpath.setNamespaceContext(namespaceContext);
    }

    public VariableContext getVariableContext() {
        return this.xpath.getVariableContext();
    }

    public void setVariableContext(VariableContext variableContext) {
        this.xpath.setVariableContext(variableContext);
    }

    public Object evaluate(Object context) {
        try {
            this.setNSContext(context);
            List answer = this.xpath.selectNodes(context);
            if (answer != null && answer.size() == 1) {
                return answer.get(0);
            }
            return answer;
        } catch (JaxenException e) {
            this.handleJaxenException(e);
            return null;
        }
    }

    public Object selectObject(Object context) {
        return this.evaluate(context);
    }

    public List selectNodes(Object context) {
        try {
            this.setNSContext(context);
            return this.xpath.selectNodes(context);
        } catch (JaxenException e) {
            this.handleJaxenException(e);
            return Collections.EMPTY_LIST;
        }
    }

    public List selectNodes(Object context, XPath sortXPath) {
        List answer = this.selectNodes(context);
        sortXPath.sort(answer);
        return answer;
    }

    public List selectNodes(Object context, XPath sortXPath, boolean distinct) {
        List answer = this.selectNodes(context);
        sortXPath.sort(answer, distinct);
        return answer;
    }

    public Node selectSingleNode(Object context) {
        try {
            this.setNSContext(context);
            Object answer = this.xpath.selectSingleNode(context);
            if (answer instanceof Node) {
                return (Node)answer;
            }
            if (answer == null) {
                return null;
            }
            throw new XPathException("The result of the XPath expression is not a Node. It was: " + answer + " of type: " + answer.getClass().getName());
        } catch (JaxenException e) {
            this.handleJaxenException(e);
            return null;
        }
    }

    public String valueOf(Object context) {
        try {
            this.setNSContext(context);
            return this.xpath.stringValueOf(context);
        } catch (JaxenException e) {
            this.handleJaxenException(e);
            return "";
        }
    }

    public Number numberValueOf(Object context) {
        try {
            this.setNSContext(context);
            return this.xpath.numberValueOf(context);
        } catch (JaxenException e) {
            this.handleJaxenException(e);
            return null;
        }
    }

    public boolean booleanValueOf(Object context) {
        try {
            this.setNSContext(context);
            return this.xpath.booleanValueOf(context);
        } catch (JaxenException e) {
            this.handleJaxenException(e);
            return false;
        }
    }

    public void sort(List list) {
        this.sort(list, false);
    }

    public void sort(List list, boolean distinct) {
        if (list != null && !list.isEmpty()) {
            int size = list.size();
            HashMap<Node, Object> sortValues = new HashMap<Node, Object>(size);
            for (int i = 0; i < size; ++i) {
                Object object = list.get(i);
                if (!(object instanceof Node)) continue;
                Node node = (Node)object;
                Object expression = this.getCompareValue(node);
                sortValues.put(node, expression);
            }
            this.sort(list, sortValues);
            if (distinct) {
                this.removeDuplicates(list, sortValues);
            }
        }
    }

    public boolean matches(Node node) {
        try {
            this.setNSContext(node);
            List answer = this.xpath.selectNodes(node);
            if (answer != null && answer.size() > 0) {
                Object item = answer.get(0);
                if (item instanceof Boolean) {
                    return (Boolean)item;
                }
                return answer.contains(node);
            }
            return false;
        } catch (JaxenException e) {
            this.handleJaxenException(e);
            return false;
        }
    }

    protected void sort(List list, final Map sortValues) {
        Collections.sort(list, new Comparator(){

            public int compare(Object o1, Object o2) {
                if ((o1 = sortValues.get(o1)) == (o2 = sortValues.get(o2))) {
                    return 0;
                }
                if (o1 instanceof Comparable) {
                    Comparable c1 = (Comparable)o1;
                    return c1.compareTo(o2);
                }
                if (o1 == null) {
                    return 1;
                }
                if (o2 == null) {
                    return -1;
                }
                return o1.equals(o2) ? 0 : -1;
            }
        });
    }

    protected void removeDuplicates(List list, Map sortValues) {
        HashSet distinctValues = new HashSet();
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Object node = iter.next();
            Object value = sortValues.get(node);
            if (distinctValues.contains(value)) {
                iter.remove();
                continue;
            }
            distinctValues.add(value);
        }
    }

    protected Object getCompareValue(Node node) {
        return this.valueOf(node);
    }

    protected static org.jaxen.XPath parse(String text) {
        try {
            return new Dom4jXPath(text);
        } catch (JaxenException e) {
            throw new InvalidXPathException(text, e.getMessage());
        } catch (Throwable t) {
            throw new InvalidXPathException(text, t);
        }
    }

    protected void setNSContext(Object context) {
        if (this.namespaceContext == null) {
            this.xpath.setNamespaceContext(DefaultNamespaceContext.create(context));
        }
    }

    protected void handleJaxenException(JaxenException exception) throws XPathException {
        throw new XPathException(this.text, exception);
    }
}


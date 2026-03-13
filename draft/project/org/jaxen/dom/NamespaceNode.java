/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.dom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public class NamespaceNode
implements Node {
    public static final short NAMESPACE_NODE = 13;
    private Node parent;
    private String name;
    private String value;
    private HashMap userData = new HashMap();

    public NamespaceNode(Node parent, String name, String value) {
        this.parent = parent;
        this.name = name == null ? "" : name;
        this.value = value;
    }

    NamespaceNode(Node parent, Node attribute) {
        String attributeName = attribute.getNodeName();
        this.name = attributeName.equals("xmlns") ? "" : (attributeName.startsWith("xmlns:") ? attributeName.substring(6) : attributeName);
        this.parent = parent;
        this.value = attribute.getNodeValue();
    }

    public String getNodeName() {
        return this.name;
    }

    public String getNodeValue() {
        return this.value;
    }

    public void setNodeValue(String value) throws DOMException {
        this.disallowModification();
    }

    public short getNodeType() {
        return 13;
    }

    public Node getParentNode() {
        return this.parent;
    }

    public NodeList getChildNodes() {
        return new EmptyNodeList();
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getLastChild() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public Document getOwnerDocument() {
        if (this.parent == null) {
            return null;
        }
        return this.parent.getOwnerDocument();
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        this.disallowModification();
        return null;
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        this.disallowModification();
        return null;
    }

    public Node removeChild(Node oldChild) throws DOMException {
        this.disallowModification();
        return null;
    }

    public Node appendChild(Node newChild) throws DOMException {
        this.disallowModification();
        return null;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public Node cloneNode(boolean deep) {
        return new NamespaceNode(this.parent, this.name, this.value);
    }

    public void normalize() {
    }

    public boolean isSupported(String feature, String version) {
        return false;
    }

    public String getNamespaceURI() {
        return null;
    }

    public String getPrefix() {
        return null;
    }

    public void setPrefix(String prefix) throws DOMException {
        this.disallowModification();
    }

    public String getLocalName() {
        return this.name;
    }

    public boolean hasAttributes() {
        return false;
    }

    private void disallowModification() throws DOMException {
        throw new DOMException(7, "Namespace node may not be modified");
    }

    public int hashCode() {
        return this.hashCode(this.parent) + this.hashCode(this.name) + this.hashCode(this.value);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof NamespaceNode) {
            NamespaceNode ns = (NamespaceNode)o;
            return this.equals(this.parent, ns.getParentNode()) && this.equals(this.name, ns.getNodeName()) && this.equals(this.value, ns.getNodeValue());
        }
        return false;
    }

    private int hashCode(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    private boolean equals(Object a, Object b) {
        return a == null && b == null || a != null && a.equals(b);
    }

    public String getBaseURI() {
        Class clazz = Node.class;
        try {
            Object[] args = new Class[]{};
            Method getBaseURI = clazz.getMethod("getBaseURI", (Class<?>[])args);
            String base = (String)getBaseURI.invoke(this.getParentNode(), args);
            return base;
        } catch (Exception ex) {
            return null;
        }
    }

    public short compareDocumentPosition(Node other) throws DOMException {
        DOMException ex = new DOMException(9, "DOM level 3 interfaces are not fully implemented in Jaxen's NamespaceNode class");
        throw ex;
    }

    public String getTextContent() {
        return this.value;
    }

    public void setTextContent(String textContent) throws DOMException {
        this.disallowModification();
    }

    public boolean isSameNode(Node other) {
        boolean b;
        boolean a = this.isEqualNode(other);
        Node thisParent = this.getParentNode();
        Node thatParent = other.getParentNode();
        try {
            Class clazz = Node.class;
            Class[] args = new Class[]{clazz};
            Method isEqual = clazz.getMethod("isEqual", args);
            Object[] args2 = new Object[]{thatParent};
            Boolean result = (Boolean)isEqual.invoke(thisParent, args2);
            b = result;
        } catch (NoSuchMethodException ex) {
            b = thisParent.equals(thatParent);
        } catch (InvocationTargetException ex) {
            b = thisParent.equals(thatParent);
        } catch (IllegalAccessException ex) {
            b = thisParent.equals(thatParent);
        }
        return a && b;
    }

    public String lookupPrefix(String namespaceURI) {
        try {
            Class clazz = Node.class;
            Class[] argTypes = new Class[]{String.class};
            Method lookupPrefix = clazz.getMethod("lookupPrefix", argTypes);
            Object[] args = new String[]{namespaceURI};
            String result = (String)lookupPrefix.invoke(this.parent, args);
            return result;
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException("Cannot lookup prefixes in DOM 2");
        } catch (InvocationTargetException ex) {
            throw new UnsupportedOperationException("Cannot lookup prefixes in DOM 2");
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Cannot lookup prefixes in DOM 2");
        }
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        return namespaceURI.equals(this.lookupNamespaceURI(null));
    }

    public String lookupNamespaceURI(String prefix) {
        try {
            Class clazz = Node.class;
            Class[] argTypes = new Class[]{String.class};
            Method lookupNamespaceURI = clazz.getMethod("lookupNamespaceURI", argTypes);
            Object[] args = new String[]{prefix};
            String result = (String)lookupNamespaceURI.invoke(this.parent, args);
            return result;
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException("Cannot lookup namespace URIs in DOM 2");
        } catch (InvocationTargetException ex) {
            throw new UnsupportedOperationException("Cannot lookup namespace URIs in DOM 2");
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Cannot lookup namespace URIs in DOM 2");
        }
    }

    public boolean isEqualNode(Node arg) {
        if (arg.getNodeType() == this.getNodeType()) {
            NamespaceNode other = (NamespaceNode)arg;
            if (other.name == null && this.name != null) {
                return false;
            }
            if (other.name != null && this.name == null) {
                return false;
            }
            if (other.value == null && this.value != null) {
                return false;
            }
            if (other.value != null && this.value == null) {
                return false;
            }
            if (other.name == null && this.name == null) {
                return other.value.equals(this.value);
            }
            return other.name.equals(this.name) && other.value.equals(this.value);
        }
        return false;
    }

    public Object getFeature(String feature, String version) {
        return null;
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        Object oldValue = this.getUserData(key);
        this.userData.put(key, data);
        return oldValue;
    }

    public Object getUserData(String key) {
        return this.userData.get(key);
    }

    private static class EmptyNodeList
    implements NodeList {
        private EmptyNodeList() {
        }

        public int getLength() {
            return 0;
        }

        public Node item(int index) {
            return null;
        }
    }
}


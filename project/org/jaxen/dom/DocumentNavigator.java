/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.dom;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenConstants;
import org.jaxen.Navigator;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.jaxen.dom.NamespaceNode;
import org.jaxen.saxpath.SAXPathException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

public class DocumentNavigator
extends DefaultNavigator {
    private static final long serialVersionUID = 8460943068889528115L;
    private static final DocumentNavigator SINGLETON = new DocumentNavigator();

    public static Navigator getInstance() {
        return SINGLETON;
    }

    public Iterator getChildAxisIterator(Object contextNode) {
        Node node = (Node)contextNode;
        if (node.getNodeType() == 1 || node.getNodeType() == 9) {
            return new NodeIterator((Node)contextNode){

                protected Node getFirstNode(Node node) {
                    return node.getFirstChild();
                }

                protected Node getNextNode(Node node) {
                    return node.getNextSibling();
                }
            };
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getParentAxisIterator(Object contextNode) {
        Node node = (Node)contextNode;
        if (node.getNodeType() == 2) {
            return new NodeIterator(node){

                protected Node getFirstNode(Node n) {
                    return ((Attr)n).getOwnerElement();
                }

                protected Node getNextNode(Node n) {
                    return null;
                }
            };
        }
        return new NodeIterator(node){

            protected Node getFirstNode(Node n) {
                return n.getParentNode();
            }

            protected Node getNextNode(Node n) {
                return null;
            }
        };
    }

    public Object getParentNode(Object child) {
        Node node = (Node)child;
        if (node.getNodeType() == 2) {
            return ((Attr)node).getOwnerElement();
        }
        return node.getParentNode();
    }

    public Iterator getFollowingSiblingAxisIterator(Object contextNode) {
        return new NodeIterator((Node)contextNode){

            protected Node getFirstNode(Node node) {
                return this.getNextNode(node);
            }

            protected Node getNextNode(Node node) {
                return node.getNextSibling();
            }
        };
    }

    public Iterator getPrecedingSiblingAxisIterator(Object contextNode) {
        return new NodeIterator((Node)contextNode){

            protected Node getFirstNode(Node node) {
                return this.getNextNode(node);
            }

            protected Node getNextNode(Node node) {
                return node.getPreviousSibling();
            }
        };
    }

    public Iterator getFollowingAxisIterator(Object contextNode) {
        return new NodeIterator((Node)contextNode){

            protected Node getFirstNode(Node node) {
                if (node == null) {
                    return null;
                }
                Node sibling = node.getNextSibling();
                if (sibling == null) {
                    return this.getFirstNode(node.getParentNode());
                }
                return sibling;
            }

            protected Node getNextNode(Node node) {
                if (node == null) {
                    return null;
                }
                Node n = node.getFirstChild();
                if (n == null) {
                    n = node.getNextSibling();
                }
                if (n == null) {
                    return this.getFirstNode(node.getParentNode());
                }
                return n;
            }
        };
    }

    public Iterator getAttributeAxisIterator(Object contextNode) {
        if (this.isElement(contextNode)) {
            return new AttributeIterator((Node)contextNode);
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getNamespaceAxisIterator(Object contextNode) {
        if (this.isElement(contextNode)) {
            HashMap<String, NamespaceNode> nsMap = new HashMap<String, NamespaceNode>();
            for (Node n = (Node)contextNode; n != null; n = n.getParentNode()) {
                String attributeNamespace;
                Attr att;
                int i;
                String myPrefix;
                String myNamespace = n.getNamespaceURI();
                if (myNamespace != null && !"".equals(myNamespace) && !nsMap.containsKey(myPrefix = n.getPrefix())) {
                    NamespaceNode ns = new NamespaceNode((Node)contextNode, myPrefix, myNamespace);
                    nsMap.put(myPrefix, ns);
                }
                if (!n.hasAttributes()) continue;
                NamedNodeMap atts = n.getAttributes();
                int length = atts.getLength();
                for (i = 0; i < length; ++i) {
                    att = (Attr)atts.item(i);
                    attributeNamespace = att.getNamespaceURI();
                    if ("http://www.w3.org/2000/xmlns/".equals(attributeNamespace) || attributeNamespace == null) continue;
                    String prefix = att.getPrefix();
                    NamespaceNode ns = new NamespaceNode((Node)contextNode, prefix, attributeNamespace);
                    if (nsMap.containsKey(prefix)) continue;
                    nsMap.put(prefix, ns);
                }
                for (i = 0; i < length; ++i) {
                    NamespaceNode ns;
                    String name;
                    att = (Attr)atts.item(i);
                    attributeNamespace = att.getNamespaceURI();
                    if (!"http://www.w3.org/2000/xmlns/".equals(attributeNamespace) || nsMap.containsKey(name = (ns = new NamespaceNode((Node)contextNode, att)).getNodeName())) continue;
                    nsMap.put(name, ns);
                }
            }
            nsMap.put("xml", new NamespaceNode((Node)contextNode, "xml", "http://www.w3.org/XML/1998/namespace"));
            NamespaceNode defaultNS = (NamespaceNode)nsMap.get("");
            if (defaultNS != null && defaultNS.getNodeValue().length() == 0) {
                nsMap.remove("");
            }
            return nsMap.values().iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public XPath parseXPath(String xpath) throws SAXPathException {
        return new DOMXPath(xpath);
    }

    public Object getDocumentNode(Object contextNode) {
        if (this.isDocument(contextNode)) {
            return contextNode;
        }
        return ((Node)contextNode).getOwnerDocument();
    }

    public String getElementNamespaceUri(Object element) {
        try {
            Node node = (Node)element;
            if (node.getNodeType() == 1) {
                return node.getNamespaceURI();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return null;
    }

    public String getElementName(Object element) {
        if (this.isElement(element)) {
            String name = ((Node)element).getLocalName();
            if (name == null) {
                name = ((Node)element).getNodeName();
            }
            return name;
        }
        return null;
    }

    public String getElementQName(Object element) {
        try {
            Node node = (Node)element;
            if (node.getNodeType() == 1) {
                return node.getNodeName();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return null;
    }

    public String getAttributeNamespaceUri(Object attribute) {
        try {
            Node node = (Node)attribute;
            if (node.getNodeType() == 2) {
                return node.getNamespaceURI();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return null;
    }

    public String getAttributeName(Object attribute) {
        if (this.isAttribute(attribute)) {
            String name = ((Node)attribute).getLocalName();
            if (name == null) {
                name = ((Node)attribute).getNodeName();
            }
            return name;
        }
        return null;
    }

    public String getAttributeQName(Object attribute) {
        try {
            Node node = (Node)attribute;
            if (node.getNodeType() == 2) {
                return node.getNodeName();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return null;
    }

    public boolean isDocument(Object object) {
        return object instanceof Node && ((Node)object).getNodeType() == 9;
    }

    public boolean isNamespace(Object object) {
        return object instanceof NamespaceNode;
    }

    public boolean isElement(Object object) {
        return object instanceof Node && ((Node)object).getNodeType() == 1;
    }

    public boolean isAttribute(Object object) {
        return object instanceof Node && ((Node)object).getNodeType() == 2 && !"http://www.w3.org/2000/xmlns/".equals(((Node)object).getNamespaceURI());
    }

    public boolean isComment(Object object) {
        return object instanceof Node && ((Node)object).getNodeType() == 8;
    }

    public boolean isText(Object object) {
        if (object instanceof Node) {
            switch (((Node)object).getNodeType()) {
                case 3: 
                case 4: {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean isProcessingInstruction(Object object) {
        return object instanceof Node && ((Node)object).getNodeType() == 7;
    }

    public String getElementStringValue(Object object) {
        if (this.isElement(object)) {
            return this.getStringValue((Node)object, new StringBuffer()).toString();
        }
        return null;
    }

    private StringBuffer getStringValue(Node node, StringBuffer buffer) {
        if (this.isText(node)) {
            buffer.append(node.getNodeValue());
        } else {
            NodeList children = node.getChildNodes();
            int length = children.getLength();
            for (int i = 0; i < length; ++i) {
                this.getStringValue(children.item(i), buffer);
            }
        }
        return buffer;
    }

    public String getAttributeStringValue(Object object) {
        if (this.isAttribute(object)) {
            return ((Node)object).getNodeValue();
        }
        return null;
    }

    public String getTextStringValue(Object object) {
        if (this.isText(object)) {
            return ((Node)object).getNodeValue();
        }
        return null;
    }

    public String getCommentStringValue(Object object) {
        if (this.isComment(object)) {
            return ((Node)object).getNodeValue();
        }
        return null;
    }

    public String getNamespaceStringValue(Object object) {
        if (this.isNamespace(object)) {
            return ((NamespaceNode)object).getNodeValue();
        }
        return null;
    }

    public String getNamespacePrefix(Object object) {
        if (this.isNamespace(object)) {
            return ((NamespaceNode)object).getLocalName();
        }
        return null;
    }

    public String translateNamespacePrefixToUri(String prefix, Object element) {
        Iterator it = this.getNamespaceAxisIterator(element);
        while (it.hasNext()) {
            NamespaceNode ns = (NamespaceNode)it.next();
            if (!prefix.equals(ns.getNodeName())) continue;
            return ns.getNodeValue();
        }
        return null;
    }

    public Object getDocument(String uri) throws FunctionCallException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(uri);
        } catch (ParserConfigurationException e) {
            throw new FunctionCallException("JAXP setup error in document() function: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new FunctionCallException("XML error in document() function: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new FunctionCallException("I/O error in document() function: " + e.getMessage(), e);
        }
    }

    public String getProcessingInstructionTarget(Object obj) {
        if (this.isProcessingInstruction(obj)) {
            ProcessingInstruction pi = (ProcessingInstruction)obj;
            return pi.getTarget();
        }
        throw new ClassCastException(obj + " is not a processing instruction");
    }

    public String getProcessingInstructionData(Object obj) {
        if (this.isProcessingInstruction(obj)) {
            ProcessingInstruction pi = (ProcessingInstruction)obj;
            return pi.getData();
        }
        throw new ClassCastException(obj + " is not a processing instruction");
    }

    public Object getElementById(Object object, String elementId) {
        Document doc = (Document)this.getDocumentNode(object);
        if (doc != null) {
            return doc.getElementById(elementId);
        }
        return null;
    }

    private static class AttributeIterator
    implements Iterator {
        private NamedNodeMap map;
        private int pos;
        private int lastAttribute = -1;

        AttributeIterator(Node parent) {
            this.map = parent.getAttributes();
            this.pos = 0;
            for (int i = this.map.getLength() - 1; i >= 0; --i) {
                Node node = this.map.item(i);
                if ("http://www.w3.org/2000/xmlns/".equals(node.getNamespaceURI())) continue;
                this.lastAttribute = i;
                break;
            }
        }

        public boolean hasNext() {
            return this.pos <= this.lastAttribute;
        }

        public Object next() {
            Node attr;
            if ((attr = this.map.item(this.pos++)) == null) {
                throw new NoSuchElementException();
            }
            if ("http://www.w3.org/2000/xmlns/".equals(attr.getNamespaceURI())) {
                return this.next();
            }
            return attr;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    abstract class NodeIterator
    implements Iterator {
        private Node node;

        public NodeIterator(Node contextNode) {
            this.node = this.getFirstNode(contextNode);
            while (!this.isXPathNode(this.node)) {
                this.node = this.getNextNode(this.node);
            }
        }

        public boolean hasNext() {
            return this.node != null;
        }

        public Object next() {
            if (this.node == null) {
                throw new NoSuchElementException();
            }
            Node ret = this.node;
            this.node = this.getNextNode(this.node);
            while (!this.isXPathNode(this.node)) {
                this.node = this.getNextNode(this.node);
            }
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected abstract Node getFirstNode(Node var1);

        protected abstract Node getNextNode(Node var1);

        private boolean isXPathNode(Node node) {
            if (node == null) {
                return true;
            }
            switch (node.getNodeType()) {
                case 5: 
                case 6: 
                case 10: 
                case 11: 
                case 12: {
                    return false;
                }
            }
            return true;
        }
    }
}


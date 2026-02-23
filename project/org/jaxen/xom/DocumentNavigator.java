/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  nu.xom.Attribute
 *  nu.xom.Builder
 *  nu.xom.Comment
 *  nu.xom.Document
 *  nu.xom.Element
 *  nu.xom.Node
 *  nu.xom.NodeFactory
 *  nu.xom.ParentNode
 *  nu.xom.ProcessingInstruction
 *  nu.xom.Text
 */
package org.jaxen.xom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Comment;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.NodeFactory;
import nu.xom.ParentNode;
import nu.xom.ProcessingInstruction;
import nu.xom.Text;
import org.jaxen.BaseXPath;
import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenConstants;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.util.SingleObjectIterator;

public class DocumentNavigator
extends DefaultNavigator {
    private static final long serialVersionUID = 3159311338575942877L;

    public boolean isAttribute(Object o) {
        return o instanceof Attribute;
    }

    public boolean isComment(Object o) {
        return o instanceof Comment;
    }

    public boolean isDocument(Object o) {
        return o instanceof Document;
    }

    public boolean isElement(Object o) {
        return o instanceof Element;
    }

    public boolean isNamespace(Object o) {
        return o instanceof XPathNamespace;
    }

    public boolean isProcessingInstruction(Object o) {
        return o instanceof ProcessingInstruction;
    }

    public boolean isText(Object o) {
        return o instanceof Text;
    }

    public String getAttributeName(Object o) {
        return this.isAttribute(o) ? ((Attribute)o).getLocalName() : null;
    }

    public String getAttributeNamespaceUri(Object o) {
        return this.isAttribute(o) ? ((Attribute)o).getNamespaceURI() : null;
    }

    public String getAttributeQName(Object o) {
        return this.isAttribute(o) ? ((Attribute)o).getQualifiedName() : null;
    }

    public String getAttributeStringValue(Object o) {
        return this.isAttribute(o) ? ((Attribute)o).getValue() : null;
    }

    public String getCommentStringValue(Object o) {
        return this.isComment(o) ? ((Comment)o).getValue() : null;
    }

    public String getElementName(Object o) {
        return this.isElement(o) ? ((Element)o).getLocalName() : null;
    }

    public String getElementNamespaceUri(Object o) {
        return this.isElement(o) ? ((Element)o).getNamespaceURI() : null;
    }

    public String getElementQName(Object o) {
        return this.isElement(o) ? ((Element)o).getQualifiedName() : null;
    }

    public String getElementStringValue(Object o) {
        return o instanceof Node ? ((Node)o).getValue() : null;
    }

    public String getNamespacePrefix(Object o) {
        if (this.isElement(o)) {
            return ((Element)o).getNamespacePrefix();
        }
        if (this.isAttribute(o)) {
            return ((Attribute)o).getNamespacePrefix();
        }
        if (o instanceof XPathNamespace) {
            return ((XPathNamespace)o).getNamespacePrefix();
        }
        return null;
    }

    public String getNamespaceStringValue(Object o) {
        if (this.isElement(o)) {
            return ((Element)o).getNamespaceURI();
        }
        if (this.isAttribute(o)) {
            return ((Attribute)o).getNamespaceURI();
        }
        if (o instanceof XPathNamespace) {
            return ((XPathNamespace)o).getNamespaceURI();
        }
        return null;
    }

    public String getTextStringValue(Object o) {
        return o instanceof Text ? ((Text)o).getValue() : null;
    }

    public Object getDocument(String s) throws FunctionCallException {
        try {
            return new Builder(new NodeFactory()).build(s);
        } catch (Exception pe) {
            throw new FunctionCallException(pe);
        }
    }

    public Object getDocumentNode(Object o) {
        ParentNode parent = null;
        if (o instanceof ParentNode) {
            parent = (ParentNode)o;
        } else if (o instanceof Node) {
            parent = ((Node)o).getParent();
        }
        return parent.getDocument();
    }

    public Iterator getAttributeAxisIterator(Object o) {
        if (this.isElement(o)) {
            return new IndexIterator(o, 0, ((Element)o).getAttributeCount()){

                public Object get(Object o, int i) {
                    return ((Element)o).getAttribute(i);
                }
            };
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getChildAxisIterator(Object o) {
        if (this.isElement(o) || o instanceof Document) {
            return new IndexIterator(o, 0, ((ParentNode)o).getChildCount()){

                public Object get(Object o, int i) {
                    return ((ParentNode)o).getChild(i);
                }
            };
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getParentAxisIterator(Object o) {
        ParentNode parent = null;
        if (o instanceof Node) {
            parent = ((Node)o).getParent();
        } else if (this.isNamespace(o)) {
            parent = ((XPathNamespace)o).getElement();
        }
        return parent != null ? new SingleObjectIterator(parent) : null;
    }

    public Object getParentNode(Object o) {
        return o instanceof Node ? ((Node)o).getParent() : null;
    }

    public Iterator getPrecedingAxisIterator(Object o) throws UnsupportedAxisException {
        return super.getPrecedingAxisIterator(o);
    }

    public Iterator getPrecedingSiblingAxisIterator(Object o) throws UnsupportedAxisException {
        return super.getPrecedingSiblingAxisIterator(o);
    }

    public String getProcessingInstructionData(Object o) {
        return o instanceof ProcessingInstruction ? ((ProcessingInstruction)o).getValue() : null;
    }

    public String getProcessingInstructionTarget(Object o) {
        return o instanceof ProcessingInstruction ? ((ProcessingInstruction)o).getTarget() : null;
    }

    public String translateNamespacePrefixToUri(String s, Object o) {
        Element element = null;
        if (o instanceof Element) {
            element = (Element)o;
        } else if (!(o instanceof ParentNode)) {
            if (o instanceof Node) {
                element = (Element)((Node)o).getParent();
            } else if (o instanceof XPathNamespace) {
                element = ((XPathNamespace)o).getElement();
            }
        }
        if (element != null) {
            return element.getNamespaceURI(s);
        }
        return null;
    }

    public XPath parseXPath(String s) throws SAXPathException {
        return new BaseXPath(s, this);
    }

    private boolean addNamespaceForElement(Element elt, String uri, String prefix, Map map) {
        if (uri != null && uri.length() > 0 && !map.containsKey(prefix)) {
            map.put(prefix, new XPathNamespace(elt, uri, prefix));
            return true;
        }
        return false;
    }

    public Iterator getNamespaceAxisIterator(Object o) {
        Element elt;
        if (!this.isElement(o)) {
            return JaxenConstants.EMPTY_ITERATOR;
        }
        HashMap nsMap = new HashMap();
        Element parent = elt = (Element)o;
        while (parent instanceof Element) {
            elt = parent;
            String uri = elt.getNamespaceURI();
            String prefix = elt.getNamespacePrefix();
            this.addNamespaceForElement(elt, uri, prefix, nsMap);
            int count = elt.getNamespaceDeclarationCount();
            for (int i = 0; i < count; ++i) {
                prefix = elt.getNamespacePrefix(i);
                uri = elt.getNamespaceURI(prefix);
                this.addNamespaceForElement(elt, uri, prefix, nsMap);
            }
            parent = elt.getParent();
        }
        this.addNamespaceForElement(elt, "http://www.w3.org/XML/1998/namespace", "xml", nsMap);
        return nsMap.values().iterator();
    }

    private static class XPathNamespace {
        private Element element;
        private String uri;
        private String prefix;

        public XPathNamespace(Element elt, String uri, String prefix) {
            this.element = elt;
            this.uri = uri;
            this.prefix = prefix;
        }

        public Element getElement() {
            return this.element;
        }

        public String getNamespaceURI() {
            return this.uri;
        }

        public String getNamespacePrefix() {
            return this.prefix;
        }

        public String toString() {
            return "[xmlns:" + this.prefix + "=\"" + this.uri + "\", element=" + this.element.getLocalName() + "]";
        }
    }

    private static abstract class IndexIterator
    implements Iterator {
        private Object o = null;
        private int pos = 0;
        private int end = -1;

        public IndexIterator(Object o, int pos, int end) {
            this.o = o;
            this.pos = pos;
            this.end = end;
        }

        public boolean hasNext() {
            return this.pos < this.end;
        }

        public abstract Object get(Object var1, int var2);

        public Object next() {
            return this.get(this.o, this.pos++);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}


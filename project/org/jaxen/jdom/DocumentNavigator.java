/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.jdom.Attribute
 *  org.jdom.CDATA
 *  org.jdom.Comment
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.Namespace
 *  org.jdom.Parent
 *  org.jdom.ProcessingInstruction
 *  org.jdom.Text
 *  org.jdom.input.SAXBuilder
 */
package org.jaxen.jdom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenConstants;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.Navigator;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.jdom.XPathNamespace;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.util.SingleObjectIterator;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Parent;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

public class DocumentNavigator
extends DefaultNavigator
implements NamedAccessNavigator {
    private static final long serialVersionUID = -1636727587303584165L;

    public static Navigator getInstance() {
        return Singleton.instance;
    }

    public boolean isElement(Object obj) {
        return obj instanceof Element;
    }

    public boolean isComment(Object obj) {
        return obj instanceof Comment;
    }

    public boolean isText(Object obj) {
        return obj instanceof Text || obj instanceof CDATA;
    }

    public boolean isAttribute(Object obj) {
        return obj instanceof Attribute;
    }

    public boolean isProcessingInstruction(Object obj) {
        return obj instanceof ProcessingInstruction;
    }

    public boolean isDocument(Object obj) {
        return obj instanceof Document;
    }

    public boolean isNamespace(Object obj) {
        return obj instanceof Namespace || obj instanceof XPathNamespace;
    }

    public String getElementName(Object obj) {
        Element elem = (Element)obj;
        return elem.getName();
    }

    public String getElementNamespaceUri(Object obj) {
        Element elem = (Element)obj;
        String uri = elem.getNamespaceURI();
        if (uri != null && uri.length() == 0) {
            return null;
        }
        return uri;
    }

    public String getAttributeName(Object obj) {
        Attribute attr = (Attribute)obj;
        return attr.getName();
    }

    public String getAttributeNamespaceUri(Object obj) {
        Attribute attr = (Attribute)obj;
        String uri = attr.getNamespaceURI();
        if (uri != null && uri.length() == 0) {
            return null;
        }
        return uri;
    }

    public Iterator getChildAxisIterator(Object contextNode) {
        if (contextNode instanceof Element) {
            return ((Element)contextNode).getContent().iterator();
        }
        if (contextNode instanceof Document) {
            return ((Document)contextNode).getContent().iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getChildAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) {
        if (contextNode instanceof Element) {
            Element node = (Element)contextNode;
            if (namespaceURI == null) {
                return node.getChildren(localName).iterator();
            }
            return node.getChildren(localName, Namespace.getNamespace((String)namespacePrefix, (String)namespaceURI)).iterator();
        }
        if (contextNode instanceof Document) {
            Document node = (Document)contextNode;
            Element el = node.getRootElement();
            if (!el.getName().equals(localName)) {
                return JaxenConstants.EMPTY_ITERATOR;
            }
            if (namespaceURI != null ? !Namespace.getNamespace((String)namespacePrefix, (String)namespaceURI).equals((Object)el.getNamespace()) : el.getNamespace() != Namespace.NO_NAMESPACE) {
                return JaxenConstants.EMPTY_ITERATOR;
            }
            return new SingleObjectIterator(el);
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getNamespaceAxisIterator(Object contextNode) {
        if (!(contextNode instanceof Element)) {
            return JaxenConstants.EMPTY_ITERATOR;
        }
        Element elem = (Element)contextNode;
        HashMap<String, XPathNamespace> nsMap = new HashMap<String, XPathNamespace>();
        Element current = elem;
        while (current != null) {
            Namespace ns = current.getNamespace();
            if (ns != Namespace.NO_NAMESPACE && !nsMap.containsKey(ns.getPrefix())) {
                nsMap.put(ns.getPrefix(), new XPathNamespace(elem, ns));
            }
            Iterator additional = current.getAdditionalNamespaces().iterator();
            while (additional.hasNext()) {
                ns = (Namespace)additional.next();
                if (nsMap.containsKey(ns.getPrefix())) continue;
                nsMap.put(ns.getPrefix(), new XPathNamespace(elem, ns));
            }
            Iterator attributes = current.getAttributes().iterator();
            while (attributes.hasNext()) {
                Attribute attribute = (Attribute)attributes.next();
                Namespace attrNS = attribute.getNamespace();
                if (attrNS == Namespace.NO_NAMESPACE || nsMap.containsKey(attrNS.getPrefix())) continue;
                nsMap.put(attrNS.getPrefix(), new XPathNamespace(elem, attrNS));
            }
            if (current.getParent() instanceof Element) {
                current = (Element)current.getParent();
                continue;
            }
            current = null;
        }
        nsMap.put("xml", new XPathNamespace(elem, Namespace.XML_NAMESPACE));
        return nsMap.values().iterator();
    }

    public Iterator getParentAxisIterator(Object contextNode) {
        Parent parent = null;
        if (contextNode instanceof Document) {
            return JaxenConstants.EMPTY_ITERATOR;
        }
        if (contextNode instanceof Element) {
            parent = ((Element)contextNode).getParent();
            if (parent == null && ((Element)contextNode).isRootElement()) {
                parent = ((Element)contextNode).getDocument();
            }
        } else if (contextNode instanceof Attribute) {
            parent = ((Attribute)contextNode).getParent();
        } else if (contextNode instanceof XPathNamespace) {
            parent = ((XPathNamespace)contextNode).getJDOMElement();
        } else if (contextNode instanceof ProcessingInstruction) {
            parent = ((ProcessingInstruction)contextNode).getParent();
        } else if (contextNode instanceof Comment) {
            parent = ((Comment)contextNode).getParent();
        } else if (contextNode instanceof Text) {
            parent = ((Text)contextNode).getParent();
        }
        if (parent != null) {
            return new SingleObjectIterator(parent);
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getAttributeAxisIterator(Object contextNode) {
        if (!(contextNode instanceof Element)) {
            return JaxenConstants.EMPTY_ITERATOR;
        }
        Element elem = (Element)contextNode;
        return elem.getAttributes().iterator();
    }

    public Iterator getAttributeAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) {
        Namespace namespace;
        Element node;
        Attribute attr;
        if (contextNode instanceof Element && (attr = (node = (Element)contextNode).getAttribute(localName, namespace = namespaceURI == null ? Namespace.NO_NAMESPACE : Namespace.getNamespace((String)namespacePrefix, (String)namespaceURI))) != null) {
            return new SingleObjectIterator(attr);
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public XPath parseXPath(String xpath) throws SAXPathException {
        return new JDOMXPath(xpath);
    }

    public Object getDocumentNode(Object contextNode) {
        if (contextNode instanceof Document) {
            return contextNode;
        }
        Element elem = (Element)contextNode;
        return elem.getDocument();
    }

    public String getElementQName(Object obj) {
        Element elem = (Element)obj;
        String prefix = elem.getNamespacePrefix();
        if (prefix == null || prefix.length() == 0) {
            return elem.getName();
        }
        return prefix + ":" + elem.getName();
    }

    public String getAttributeQName(Object obj) {
        Attribute attr = (Attribute)obj;
        String prefix = attr.getNamespacePrefix();
        if (prefix == null || "".equals(prefix)) {
            return attr.getName();
        }
        return prefix + ":" + attr.getName();
    }

    public String getNamespaceStringValue(Object obj) {
        if (obj instanceof Namespace) {
            Namespace ns = (Namespace)obj;
            return ns.getURI();
        }
        XPathNamespace ns = (XPathNamespace)obj;
        return ns.getJDOMNamespace().getURI();
    }

    public String getNamespacePrefix(Object obj) {
        if (obj instanceof Namespace) {
            Namespace ns = (Namespace)obj;
            return ns.getPrefix();
        }
        XPathNamespace ns = (XPathNamespace)obj;
        return ns.getJDOMNamespace().getPrefix();
    }

    public String getTextStringValue(Object obj) {
        if (obj instanceof Text) {
            return ((Text)obj).getText();
        }
        if (obj instanceof CDATA) {
            return ((CDATA)obj).getText();
        }
        return "";
    }

    public String getAttributeStringValue(Object obj) {
        Attribute attr = (Attribute)obj;
        return attr.getValue();
    }

    public String getElementStringValue(Object obj) {
        Element elem = (Element)obj;
        StringBuffer buf = new StringBuffer();
        List content = elem.getContent();
        Iterator contentIter = content.iterator();
        Object each = null;
        while (contentIter.hasNext()) {
            each = contentIter.next();
            if (each instanceof Text) {
                buf.append(((Text)each).getText());
                continue;
            }
            if (each instanceof CDATA) {
                buf.append(((CDATA)each).getText());
                continue;
            }
            if (!(each instanceof Element)) continue;
            buf.append(this.getElementStringValue(each));
        }
        return buf.toString();
    }

    public String getProcessingInstructionTarget(Object obj) {
        ProcessingInstruction pi = (ProcessingInstruction)obj;
        return pi.getTarget();
    }

    public String getProcessingInstructionData(Object obj) {
        ProcessingInstruction pi = (ProcessingInstruction)obj;
        return pi.getData();
    }

    public String getCommentStringValue(Object obj) {
        Comment cmt = (Comment)obj;
        return cmt.getText();
    }

    public String translateNamespacePrefixToUri(String prefix, Object context) {
        Namespace namespace;
        Element element = null;
        if (context instanceof Element) {
            element = (Element)context;
        } else if (context instanceof Text) {
            element = (Element)((Text)context).getParent();
        } else if (context instanceof Attribute) {
            element = ((Attribute)context).getParent();
        } else if (context instanceof XPathNamespace) {
            element = ((XPathNamespace)context).getJDOMElement();
        } else if (context instanceof Comment) {
            element = (Element)((Comment)context).getParent();
        } else if (context instanceof ProcessingInstruction) {
            element = (Element)((ProcessingInstruction)context).getParent();
        }
        if (element != null && (namespace = element.getNamespace(prefix)) != null) {
            return namespace.getURI();
        }
        return null;
    }

    public Object getDocument(String url) throws FunctionCallException {
        try {
            SAXBuilder builder = new SAXBuilder();
            return builder.build(url);
        } catch (Exception e) {
            throw new FunctionCallException(e.getMessage());
        }
    }

    private static class Singleton {
        private static DocumentNavigator instance = new DocumentNavigator();

        private Singleton() {
        }
    }
}


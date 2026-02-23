/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.dom4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;
import org.dom4j.Text;
import org.dom4j.io.SAXReader;
import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenConstants;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.Navigator;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.util.SingleObjectIterator;

public class DocumentNavigator
extends DefaultNavigator
implements NamedAccessNavigator {
    private static final long serialVersionUID = 5582300797286535936L;
    private transient SAXReader reader;

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
        return obj instanceof Namespace;
    }

    public String getElementName(Object obj) {
        Element elem = (Element)obj;
        return elem.getName();
    }

    public String getElementNamespaceUri(Object obj) {
        Element elem = (Element)obj;
        String uri = elem.getNamespaceURI();
        if (uri == null) {
            return "";
        }
        return uri;
    }

    public String getElementQName(Object obj) {
        Element elem = (Element)obj;
        return elem.getQualifiedName();
    }

    public String getAttributeName(Object obj) {
        Attribute attr = (Attribute)obj;
        return attr.getName();
    }

    public String getAttributeNamespaceUri(Object obj) {
        Attribute attr = (Attribute)obj;
        String uri = attr.getNamespaceURI();
        if (uri == null) {
            return "";
        }
        return uri;
    }

    public String getAttributeQName(Object obj) {
        Attribute attr = (Attribute)obj;
        return attr.getQualifiedName();
    }

    public Iterator getChildAxisIterator(Object contextNode) {
        Iterator result = null;
        if (contextNode instanceof Branch) {
            Branch node = (Branch)contextNode;
            result = node.nodeIterator();
        }
        if (result != null) {
            return result;
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getChildAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) {
        if (contextNode instanceof Element) {
            Element node = (Element)contextNode;
            return node.elementIterator(QName.get(localName, namespacePrefix, namespaceURI));
        }
        if (contextNode instanceof Document) {
            Document node = (Document)contextNode;
            Element el = node.getRootElement();
            if (el == null || !el.getName().equals(localName)) {
                return JaxenConstants.EMPTY_ITERATOR;
            }
            if (namespaceURI != null && !namespaceURI.equals(el.getNamespaceURI())) {
                return JaxenConstants.EMPTY_ITERATOR;
            }
            return new SingleObjectIterator(el);
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getParentAxisIterator(Object contextNode) {
        if (contextNode instanceof Document) {
            return JaxenConstants.EMPTY_ITERATOR;
        }
        Node node = (Node)contextNode;
        Branch parent = node.getParent();
        if (parent == null) {
            parent = node.getDocument();
        }
        return new SingleObjectIterator(parent);
    }

    public Iterator getAttributeAxisIterator(Object contextNode) {
        if (!(contextNode instanceof Element)) {
            return JaxenConstants.EMPTY_ITERATOR;
        }
        Element elem = (Element)contextNode;
        return elem.attributeIterator();
    }

    public Iterator getAttributeAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) {
        if (contextNode instanceof Element) {
            Element node = (Element)contextNode;
            Attribute attr = node.attribute(QName.get(localName, namespacePrefix, namespaceURI));
            if (attr == null) {
                return JaxenConstants.EMPTY_ITERATOR;
            }
            return new SingleObjectIterator(attr);
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getNamespaceAxisIterator(Object contextNode) {
        if (!(contextNode instanceof Element)) {
            return JaxenConstants.EMPTY_ITERATOR;
        }
        Element element = (Element)contextNode;
        ArrayList<Node> nsList = new ArrayList<Node>();
        HashSet<String> prefixes = new HashSet<String>();
        for (Element context = element; context != null; context = context.getParent()) {
            ArrayList<Namespace> declaredNS = new ArrayList<Namespace>(context.declaredNamespaces());
            declaredNS.add(context.getNamespace());
            Iterator iter = context.attributes().iterator();
            while (iter.hasNext()) {
                Attribute attr = (Attribute)iter.next();
                declaredNS.add(attr.getNamespace());
            }
            iter = declaredNS.iterator();
            while (iter.hasNext()) {
                String prefix;
                Namespace namespace = (Namespace)iter.next();
                if (namespace == Namespace.NO_NAMESPACE || prefixes.contains(prefix = namespace.getPrefix())) continue;
                prefixes.add(prefix);
                nsList.add(namespace.asXPathResult(element));
            }
        }
        nsList.add(Namespace.XML_NAMESPACE.asXPathResult(element));
        return nsList.iterator();
    }

    public Object getDocumentNode(Object contextNode) {
        if (contextNode instanceof Document) {
            return contextNode;
        }
        if (contextNode instanceof Node) {
            Node node = (Node)contextNode;
            return node.getDocument();
        }
        return null;
    }

    public XPath parseXPath(String xpath) throws SAXPathException {
        return new Dom4jXPath(xpath);
    }

    public Object getParentNode(Object contextNode) {
        if (contextNode instanceof Node) {
            Node node = (Node)contextNode;
            Branch answer = node.getParent();
            if (answer == null && (answer = node.getDocument()) == contextNode) {
                return null;
            }
            return answer;
        }
        return null;
    }

    public String getTextStringValue(Object obj) {
        return this.getNodeStringValue((Node)obj);
    }

    public String getElementStringValue(Object obj) {
        return this.getNodeStringValue((Node)obj);
    }

    public String getAttributeStringValue(Object obj) {
        return this.getNodeStringValue((Node)obj);
    }

    private String getNodeStringValue(Node node) {
        return node.getStringValue();
    }

    public String getNamespaceStringValue(Object obj) {
        Namespace ns = (Namespace)obj;
        return ns.getURI();
    }

    public String getNamespacePrefix(Object obj) {
        Namespace ns = (Namespace)obj;
        return ns.getPrefix();
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
        } else if (context instanceof Node) {
            Node node = (Node)context;
            element = node.getParent();
        }
        if (element != null && (namespace = element.getNamespaceForPrefix(prefix)) != null) {
            return namespace.getURI();
        }
        return null;
    }

    public short getNodeType(Object node) {
        if (node instanceof Node) {
            return ((Node)node).getNodeType();
        }
        return 0;
    }

    public Object getDocument(String uri) throws FunctionCallException {
        try {
            return this.getSAXReader().read(uri);
        } catch (DocumentException e) {
            throw new FunctionCallException("Failed to parse document for URI: " + uri, e);
        }
    }

    public String getProcessingInstructionTarget(Object obj) {
        ProcessingInstruction pi = (ProcessingInstruction)obj;
        return pi.getTarget();
    }

    public String getProcessingInstructionData(Object obj) {
        ProcessingInstruction pi = (ProcessingInstruction)obj;
        return pi.getText();
    }

    public SAXReader getSAXReader() {
        if (this.reader == null) {
            this.reader = new SAXReader();
            this.reader.setMergeAdjacentText(true);
        }
        return this.reader;
    }

    public void setSAXReader(SAXReader reader) {
        this.reader = reader;
    }

    private static class Singleton {
        private static DocumentNavigator instance = new DocumentNavigator();

        private Singleton() {
        }
    }
}


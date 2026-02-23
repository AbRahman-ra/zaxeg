/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.security.spec.AlgorithmParameterSpec;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;
import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class DOMUtils {
    private DOMUtils() {
    }

    public static Document getOwnerDocument(Node node) {
        if (node.getNodeType() == 9) {
            return (Document)node;
        }
        return node.getOwnerDocument();
    }

    public static Element createElement(Document doc, String tag, String nsURI, String prefix) {
        String qName = prefix == null || prefix.length() == 0 ? tag : prefix + ":" + tag;
        return doc.createElementNS(nsURI, qName);
    }

    public static void setAttribute(Element elem, String name, String value) {
        if (value == null) {
            return;
        }
        elem.setAttributeNS(null, name, value);
    }

    public static void setAttributeID(Element elem, String name, String value) {
        if (value == null) {
            return;
        }
        elem.setAttributeNS(null, name, value);
        elem.setIdAttributeNS(null, name, true);
    }

    public static Element getFirstChildElement(Node node) {
        Node child;
        for (child = node.getFirstChild(); child != null && child.getNodeType() != 1; child = child.getNextSibling()) {
        }
        return (Element)child;
    }

    @Deprecated
    public static Element getFirstChildElement(Node node, String localName) throws MarshalException {
        return DOMUtils.verifyElement(DOMUtils.getFirstChildElement(node), localName);
    }

    public static Element getFirstChildElement(Node node, String localName, String namespaceURI) throws MarshalException {
        return DOMUtils.verifyElement(DOMUtils.getFirstChildElement(node), localName, namespaceURI);
    }

    private static Element verifyElement(Element elem, String localName) throws MarshalException {
        if (elem == null) {
            throw new MarshalException("Missing " + localName + " element");
        }
        String name = elem.getLocalName();
        if (!name.equals(localName)) {
            throw new MarshalException("Invalid element name: " + name + ", expected " + localName);
        }
        return elem;
    }

    private static Element verifyElement(Element elem, String localName, String namespaceURI) throws MarshalException {
        if (elem == null) {
            throw new MarshalException("Missing " + localName + " element");
        }
        String name = elem.getLocalName();
        String namespace = elem.getNamespaceURI();
        if (!name.equals(localName) || namespace == null && namespaceURI != null || namespace != null && !namespace.equals(namespaceURI)) {
            throw new MarshalException("Invalid element name: " + namespace + ":" + name + ", expected " + namespaceURI + ":" + localName);
        }
        return elem;
    }

    public static Element getLastChildElement(Node node) {
        Node child;
        for (child = node.getLastChild(); child != null && child.getNodeType() != 1; child = child.getPreviousSibling()) {
        }
        return (Element)child;
    }

    public static Element getNextSiblingElement(Node node) {
        Node sibling;
        for (sibling = node.getNextSibling(); sibling != null && sibling.getNodeType() != 1; sibling = sibling.getNextSibling()) {
        }
        return (Element)sibling;
    }

    @Deprecated
    public static Element getNextSiblingElement(Node node, String localName) throws MarshalException {
        return DOMUtils.verifyElement(DOMUtils.getNextSiblingElement(node), localName);
    }

    public static Element getNextSiblingElement(Node node, String localName, String namespaceURI) throws MarshalException {
        return DOMUtils.verifyElement(DOMUtils.getNextSiblingElement(node), localName, namespaceURI);
    }

    public static String getAttributeValue(Element elem, String name) {
        Attr attr = elem.getAttributeNodeNS(null, name);
        return attr == null ? null : attr.getValue();
    }

    public static Set<Node> nodeSet(NodeList nl) {
        return new NodeSet(nl);
    }

    public static String getNSPrefix(XMLCryptoContext context, String nsURI) {
        if (context != null) {
            return context.getNamespacePrefix(nsURI, context.getDefaultNamespacePrefix());
        }
        return null;
    }

    public static String getSignaturePrefix(XMLCryptoContext context) {
        return DOMUtils.getNSPrefix(context, "http://www.w3.org/2000/09/xmldsig#");
    }

    public static void removeAllChildren(Node node) {
        for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            Node nodeToRemove = firstChild;
            node.removeChild(nodeToRemove);
        }
    }

    public static boolean nodesEqual(Node thisNode, Node otherNode) {
        if (thisNode == otherNode) {
            return true;
        }
        return thisNode.getNodeType() == otherNode.getNodeType();
    }

    public static void appendChild(Node parent, Node child) {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        if (child.getOwnerDocument() != ownerDoc) {
            parent.appendChild(ownerDoc.importNode(child, true));
        } else {
            parent.appendChild(child);
        }
    }

    public static boolean paramsEqual(AlgorithmParameterSpec spec1, AlgorithmParameterSpec spec2) {
        if (spec1 == spec2) {
            return true;
        }
        if (spec1 instanceof XPathFilter2ParameterSpec && spec2 instanceof XPathFilter2ParameterSpec) {
            return DOMUtils.paramsEqual((XPathFilter2ParameterSpec)spec1, (XPathFilter2ParameterSpec)spec2);
        }
        if (spec1 instanceof ExcC14NParameterSpec && spec2 instanceof ExcC14NParameterSpec) {
            return DOMUtils.paramsEqual((ExcC14NParameterSpec)spec1, (ExcC14NParameterSpec)spec2);
        }
        if (spec1 instanceof XPathFilterParameterSpec && spec2 instanceof XPathFilterParameterSpec) {
            return DOMUtils.paramsEqual((XPathFilterParameterSpec)spec1, (XPathFilterParameterSpec)spec2);
        }
        if (spec1 instanceof XSLTTransformParameterSpec && spec2 instanceof XSLTTransformParameterSpec) {
            return DOMUtils.paramsEqual((XSLTTransformParameterSpec)spec1, (XSLTTransformParameterSpec)spec2);
        }
        return false;
    }

    private static boolean paramsEqual(XPathFilter2ParameterSpec spec1, XPathFilter2ParameterSpec spec2) {
        List types = spec1.getXPathList();
        List otypes = spec2.getXPathList();
        int size = types.size();
        if (size != otypes.size()) {
            return false;
        }
        for (int i = 0; i < size; ++i) {
            XPathType type = (XPathType)types.get(i);
            XPathType otype = (XPathType)otypes.get(i);
            if (type.getExpression().equals(otype.getExpression()) && ((Object)type.getNamespaceMap()).equals(otype.getNamespaceMap()) && type.getFilter() == otype.getFilter()) continue;
            return false;
        }
        return true;
    }

    private static boolean paramsEqual(ExcC14NParameterSpec spec1, ExcC14NParameterSpec spec2) {
        return ((Object)spec1.getPrefixList()).equals(spec2.getPrefixList());
    }

    private static boolean paramsEqual(XPathFilterParameterSpec spec1, XPathFilterParameterSpec spec2) {
        return spec1.getXPath().equals(spec2.getXPath()) && ((Object)spec1.getNamespaceMap()).equals(spec2.getNamespaceMap());
    }

    private static boolean paramsEqual(XSLTTransformParameterSpec spec1, XSLTTransformParameterSpec spec2) {
        XMLStructure ostylesheet = spec2.getStylesheet();
        if (!(ostylesheet instanceof DOMStructure)) {
            return false;
        }
        Node ostylesheetElem = ((DOMStructure)ostylesheet).getNode();
        XMLStructure stylesheet = spec1.getStylesheet();
        Node stylesheetElem = ((DOMStructure)stylesheet).getNode();
        return DOMUtils.nodesEqual(stylesheetElem, ostylesheetElem);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class NodeSet
    extends AbstractSet<Node> {
        private NodeList nl;

        public NodeSet(NodeList nl) {
            this.nl = nl;
        }

        @Override
        public int size() {
            return this.nl.getLength();
        }

        @Override
        public Iterator<Node> iterator() {
            return new Iterator<Node>(){
                int index = 0;

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Node next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return NodeSet.this.nl.item(this.index++);
                }

                @Override
                public boolean hasNext() {
                    return this.index < NodeSet.this.nl.getLength();
                }
            };
        }
    }
}


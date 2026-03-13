/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.util.ArrayList;
import net.sf.saxon.dom.DOMImplementationImpl;
import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.tree.iter.AxisIterator;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DocumentOverNodeInfo
extends NodeOverNodeInfo
implements Document {
    @Override
    public DocumentType getDoctype() {
        return null;
    }

    @Override
    public DOMImplementation getImplementation() {
        return new DOMImplementationImpl();
    }

    @Override
    public Element createElement(String tagName) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public DocumentFragment createDocumentFragment() {
        return null;
    }

    @Override
    public Text createTextNode(String data) {
        return null;
    }

    @Override
    public Comment createComment(String data) {
        return null;
    }

    @Override
    public CDATASection createCDATASection(String data) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Attr createAttribute(String name) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public EntityReference createEntityReference(String name) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public NodeList getElementsByTagName(String tagname) {
        return DocumentOverNodeInfo.getElementsByTagName(this.node, tagname);
    }

    @Override
    public Element getDocumentElement() {
        NodeInfo root = this.node.getRoot();
        if (root == null) {
            return null;
        }
        AxisIterator children = root.iterateAxis(3, NodeKindTest.ELEMENT);
        return (Element)((Object)DocumentOverNodeInfo.wrap(children.next()));
    }

    protected static NodeList getElementsByTagName(NodeInfo node, String tagname) {
        NodeInfo next;
        AxisIterator allElements = node.iterateAxis(4);
        ArrayList<Node> nodes = new ArrayList<Node>(100);
        while ((next = allElements.next()) != null) {
            if (next.getNodeKind() != 1 || !tagname.equals("*") && !tagname.equals(next.getDisplayName())) continue;
            nodes.add(NodeOverNodeInfo.wrap(next));
        }
        return new DOMNodeList(nodes);
    }

    @Override
    public Node importNode(Node importedNode, boolean deep) throws UnsupportedOperationException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Element createElementNS(String namespaceURI, String qualifiedName) throws UnsupportedOperationException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws UnsupportedOperationException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return DocumentOverNodeInfo.getElementsByTagNameNS(this.node, namespaceURI, localName);
    }

    public static NodeList getElementsByTagNameNS(NodeInfo node, String namespaceURI, String localName) {
        NodeInfo next;
        String ns = namespaceURI == null ? "" : namespaceURI;
        AxisIterator allElements = node.iterateAxis(4);
        ArrayList<Node> nodes = new ArrayList<Node>(100);
        while ((next = allElements.next()) != null) {
            if (next.getNodeKind() != 1 || !ns.equals("*") && !ns.equals(next.getURI()) || !localName.equals("*") && !localName.equals(next.getLocalPart())) continue;
            nodes.add(NodeOverNodeInfo.wrap(next));
        }
        return new DOMNodeList(nodes);
    }

    @Override
    public Element getElementById(String elementId) {
        TreeInfo doc = this.node.getTreeInfo();
        if (doc == null) {
            return null;
        }
        return (Element)((Object)DocumentOverNodeInfo.wrap(doc.selectID(elementId, false)));
    }

    @Override
    public String getInputEncoding() {
        return null;
    }

    @Override
    public String getXmlEncoding() {
        return null;
    }

    @Override
    public boolean getXmlStandalone() {
        return false;
    }

    @Override
    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
    }

    @Override
    public String getXmlVersion() {
        return "1.0";
    }

    @Override
    public void setXmlVersion(String xmlVersion) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
    }

    @Override
    public boolean getStrictErrorChecking() {
        return false;
    }

    @Override
    public void setStrictErrorChecking(boolean strictErrorChecking) {
    }

    @Override
    public String getDocumentURI() {
        return this.node.getSystemId();
    }

    @Override
    public void setDocumentURI(String documentURI) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
    }

    @Override
    public Node adoptNode(Node source) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public DOMConfiguration getDomConfig() {
        return null;
    }

    @Override
    public void normalizeDocument() throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
    }

    @Override
    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
        DocumentOverNodeInfo.disallowUpdate();
        return null;
    }
}


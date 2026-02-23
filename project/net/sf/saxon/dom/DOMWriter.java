/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.util.Stack;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMWriter
extends Builder {
    private PipelineConfiguration pipe;
    private Node currentNode;
    private Document document;
    private Node nextSibling;
    private int level = 0;
    private boolean canNormalize = true;
    private String systemId;
    private final Stack<NamespaceMap> nsStack = new Stack();

    public DOMWriter() {
        this.nsStack.push(NamespaceMap.emptyMap());
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipe = pipe;
        this.config = pipe.getConfiguration();
    }

    @Override
    public PipelineConfiguration getPipelineConfiguration() {
        return this.pipe;
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        if (this.document == null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                this.document = factory.newDocumentBuilder().newDocument();
                this.currentNode = this.document;
            } catch (ParserConfigurationException err) {
                throw new XPathException(err);
            }
        }
    }

    @Override
    public void endDocument() throws XPathException {
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        String qname = elemName.getDisplayName();
        String uri = elemName.getURI();
        try {
            Element element = this.document.createElementNS("".equals(uri) ? null : uri, qname);
            if (this.nextSibling != null && this.level == 0) {
                this.currentNode.insertBefore(element, this.nextSibling);
            } else {
                this.currentNode.appendChild(element);
            }
            this.currentNode = element;
            NamespaceMap parentNamespaces = this.nsStack.peek();
            if (namespaces != parentNamespaces) {
                NamespaceBinding[] declarations;
                for (NamespaceBinding ns : declarations = namespaces.getDifferences(parentNamespaces, false)) {
                    String prefix = ns.getPrefix();
                    String nsuri = ns.getURI();
                    if (nsuri.equals("http://www.w3.org/XML/1998/namespace")) continue;
                    if (prefix.isEmpty()) {
                        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", nsuri);
                        continue;
                    }
                    element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, nsuri);
                }
            }
            this.nsStack.push(namespaces);
            for (AttributeInfo att : attributes) {
                NodeName attName = att.getNodeName();
                String atturi = attName.getURI();
                element.setAttributeNS("".equals(atturi) ? null : atturi, attName.getDisplayName(), att.getValue());
                if (!attName.equals(StandardNames.XML_ID_NAME) && !ReceiverOption.contains(properties, 2048) && (!attName.hasURI("http://www.w3.org/XML/1998/namespace") || !attName.getLocalPart().equals("id"))) continue;
                String localName = attName.getLocalPart();
                element.setIdAttributeNS("".equals(atturi) ? null : atturi, localName, true);
            }
        } catch (DOMException err) {
            throw new XPathException(err);
        }
        ++this.level;
    }

    @Override
    public void endElement() throws XPathException {
        this.nsStack.pop();
        if (this.canNormalize) {
            try {
                this.currentNode.normalize();
            } catch (Throwable err) {
                this.canNormalize = false;
            }
        }
        this.currentNode = this.currentNode.getParentNode();
        --this.level;
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.level == 0 && this.nextSibling == null && Whitespace.isWhite(chars)) {
            return;
        }
        try {
            Text text = this.document.createTextNode(chars.toString());
            if (this.nextSibling != null && this.level == 0) {
                this.currentNode.insertBefore(text, this.nextSibling);
            } else {
                this.currentNode.appendChild(text);
            }
        } catch (DOMException err) {
            throw new XPathException(err);
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        try {
            ProcessingInstruction pi = this.document.createProcessingInstruction(target, data.toString());
            if (this.nextSibling != null && this.level == 0) {
                this.currentNode.insertBefore(pi, this.nextSibling);
            } else {
                this.currentNode.appendChild(pi);
            }
        } catch (DOMException err) {
            throw new XPathException(err);
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        try {
            Comment comment = this.document.createComment(chars.toString());
            if (this.nextSibling != null && this.level == 0) {
                this.currentNode.insertBefore(comment, this.nextSibling);
            } else {
                this.currentNode.appendChild(comment);
            }
        } catch (DOMException err) {
            throw new XPathException(err);
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return false;
    }

    public void setNode(Node node) {
        if (node == null) {
            return;
        }
        this.currentNode = node;
        if (node.getNodeType() == 9) {
            this.document = (Document)node;
        } else {
            this.document = this.currentNode.getOwnerDocument();
            if (this.document == null) {
                this.document = new DocumentOverNodeInfo();
            }
        }
    }

    public void setNextSibling(Node nextSibling) {
        this.nextSibling = nextSibling;
    }

    @Override
    public NodeInfo getCurrentRoot() {
        return new DocumentWrapper(this.document, this.systemId, this.config).getRootNode();
    }

    protected Document getDOMDocumentNode() {
        return this.document;
    }
}


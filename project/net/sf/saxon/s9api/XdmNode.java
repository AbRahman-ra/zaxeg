/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.streams.Steps;
import net.sf.saxon.s9api.streams.XdmStream;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.wrapper.VirtualNode;

public class XdmNode
extends XdmItem {
    private XdmNode() {
    }

    public XdmNode(NodeInfo node) {
        this.setValue(node);
    }

    public XdmNodeKind getNodeKind() {
        switch (this.getUnderlyingNode().getNodeKind()) {
            case 9: {
                return XdmNodeKind.DOCUMENT;
            }
            case 1: {
                return XdmNodeKind.ELEMENT;
            }
            case 2: {
                return XdmNodeKind.ATTRIBUTE;
            }
            case 3: {
                return XdmNodeKind.TEXT;
            }
            case 8: {
                return XdmNodeKind.COMMENT;
            }
            case 7: {
                return XdmNodeKind.PROCESSING_INSTRUCTION;
            }
            case 13: {
                return XdmNodeKind.NAMESPACE;
            }
        }
        throw new IllegalStateException("nodeKind");
    }

    public Processor getProcessor() {
        Configuration config = this.getUnderlyingNode().getConfiguration();
        Configuration.ApiProvider originator = config.getProcessor();
        if (originator instanceof Processor) {
            return (Processor)originator;
        }
        return new Processor(config);
    }

    @Override
    public NodeInfo getUnderlyingValue() {
        return (NodeInfo)super.getUnderlyingValue();
    }

    public QName getNodeName() {
        NodeInfo n = this.getUnderlyingNode();
        switch (n.getNodeKind()) {
            case 3: 
            case 8: 
            case 9: {
                return null;
            }
            case 7: 
            case 13: {
                if (n.getLocalPart().isEmpty()) {
                    return null;
                }
                return new QName(new StructuredQName("", "", n.getLocalPart()));
            }
            case 1: 
            case 2: {
                return new QName(n.getPrefix(), n.getURI(), n.getLocalPart());
            }
        }
        return null;
    }

    public XdmValue getTypedValue() throws SaxonApiException {
        try {
            AtomicSequence v = this.getUnderlyingNode().atomize();
            return XdmValue.wrap(v);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public int getLineNumber() {
        return this.getUnderlyingNode().getLineNumber();
    }

    public int getColumnNumber() {
        return this.getUnderlyingNode().getColumnNumber();
    }

    public Source asSource() {
        return this.getUnderlyingNode();
    }

    public Iterable<XdmNode> children() {
        return this.select(Steps.child()).asListOfNodes();
    }

    public Iterable<XdmNode> children(String localName) {
        return this.select(Steps.child(localName)).asListOfNodes();
    }

    public Iterable<XdmNode> children(String uri, String localName) {
        return this.select(Steps.child(uri, localName)).asListOfNodes();
    }

    public Iterable<XdmNode> children(Predicate<? super XdmNode> filter) {
        return this.select(Steps.child(filter)).asListOfNodes();
    }

    public XdmSequenceIterator<XdmNode> axisIterator(Axis axis) {
        AxisIterator base = this.getUnderlyingNode().iterateAxis(axis.getAxisNumber());
        return XdmSequenceIterator.ofNodes(base);
    }

    public XdmSequenceIterator<XdmNode> axisIterator(Axis axis, QName name) {
        int kind;
        switch (axis) {
            case ATTRIBUTE: {
                kind = 2;
                break;
            }
            case NAMESPACE: {
                kind = 13;
                break;
            }
            default: {
                kind = 1;
            }
        }
        NodeInfo node = this.getUnderlyingNode();
        NameTest test = new NameTest(kind, name.getNamespaceURI(), name.getLocalName(), node.getConfiguration().getNamePool());
        AxisIterator base = node.iterateAxis(axis.getAxisNumber(), test);
        return XdmSequenceIterator.ofNodes(base);
    }

    public XdmNode getParent() {
        NodeInfo p = this.getUnderlyingNode().getParent();
        return p == null ? null : (XdmNode)XdmValue.wrap(p);
    }

    public XdmNode getRoot() {
        NodeInfo p = this.getUnderlyingNode().getRoot();
        return p == null ? null : (XdmNode)XdmValue.wrap(p);
    }

    public String getAttributeValue(QName name) {
        NodeInfo node = this.getUnderlyingNode();
        return node.getAttributeValue(name.getNamespaceURI(), name.getLocalName());
    }

    public String attribute(String name) {
        return this.getUnderlyingNode().getAttributeValue("", name);
    }

    public URI getBaseURI() {
        try {
            String uri = this.getUnderlyingNode().getBaseURI();
            if (uri == null) {
                return null;
            }
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("baseURI", e);
        }
    }

    public URI getDocumentURI() {
        try {
            String systemId = this.getUnderlyingNode().getSystemId();
            return systemId == null || systemId.isEmpty() ? null : new URI(systemId);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("documentURI", e);
        }
    }

    public int hashCode() {
        return this.getUnderlyingNode().hashCode();
    }

    public boolean equals(Object other) {
        return other instanceof XdmNode && this.getUnderlyingNode().equals(((XdmNode)other).getUnderlyingNode());
    }

    @Override
    public String toString() {
        NodeInfo node = this.getUnderlyingNode();
        if (node.getNodeKind() == 2) {
            String val = node.getStringValue().replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
            return node.getDisplayName() + "=\"" + val + '\"';
        }
        if (node.getNodeKind() == 13) {
            String val = node.getStringValue().replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
            String name = node.getDisplayName();
            name = name.equals("") ? "xmlns" : "xmlns:" + name;
            return name + "=\"" + val + '\"';
        }
        if (node.getNodeKind() == 3) {
            return node.getStringValue().replace("&", "&amp;").replace("<", "&lt;").replace("]]>", "]]&gt;");
        }
        try {
            return QueryResult.serialize(node).trim();
        } catch (XPathException err) {
            throw new IllegalStateException(err);
        }
    }

    public NodeInfo getUnderlyingNode() {
        return this.getUnderlyingValue();
    }

    public Object getExternalNode() {
        NodeInfo saxonNode = this.getUnderlyingNode();
        if (saxonNode instanceof VirtualNode) {
            Object externalNode = ((VirtualNode)saxonNode).getRealNode();
            return externalNode instanceof NodeInfo ? null : externalNode;
        }
        return null;
    }

    public XdmSequenceIterator<XdmNode> nodeIterator() {
        return XdmSequenceIterator.ofNode(this);
    }

    public XdmStream<XdmNode> stream() {
        return new XdmStream<XdmNode>(Stream.of(this));
    }
}


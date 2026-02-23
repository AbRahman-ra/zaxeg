/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Stack;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceDeltaMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

public class DOMSender {
    private final Receiver receiver;
    protected Node root;
    protected String systemId;
    private Stack<NamespaceMap> namespaces = new Stack();
    private Node currentNode;

    public DOMSender(Node startNode, Receiver receiver) {
        if (startNode == null) {
            throw new NullPointerException("startNode");
        }
        if (receiver == null) {
            throw new NullPointerException("receiver");
        }
        this.root = startNode;
        this.receiver = new NamespaceReducer(receiver);
        this.namespaces.push(NamespaceMap.emptyMap());
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public void send() throws XPathException {
        this.receiver.setSystemId(this.systemId);
        Loc loc = Loc.NONE;
        switch (this.root.getNodeType()) {
            case 9: 
            case 11: {
                this.receiver.startDocument(0);
                this.sendUnparsedEntities();
                this.walkNode(this.root);
                this.receiver.endDocument();
                break;
            }
            case 1: {
                this.sendElement((Element)this.root);
                break;
            }
            case 3: 
            case 4: {
                this.receiver.characters(((CharacterData)this.root).getData(), loc, 0);
                break;
            }
            case 8: {
                this.receiver.comment(((Comment)this.root).getData(), loc, 0);
                break;
            }
            case 7: {
                this.receiver.processingInstruction(((ProcessingInstruction)this.root).getTarget(), ((ProcessingInstruction)this.root).getData(), loc, 0);
                break;
            }
            default: {
                throw new IllegalStateException("DOMSender: unsupported kind of start node (" + this.root.getNodeType() + ")");
            }
        }
    }

    private void sendUnparsedEntities() throws XPathException {
        NamedNodeMap map;
        DocumentType docType;
        if (this.root instanceof Document && (docType = ((Document)this.root).getDoctype()) != null && (map = docType.getEntities()) != null) {
            for (int i = 0; i < map.getLength(); ++i) {
                Entity e = (Entity)map.item(i);
                if (e.getNotationName() == null) continue;
                String name = e.getNodeName();
                String systemId = e.getSystemId();
                try {
                    String base;
                    URI systemIdURI = new URI(systemId);
                    if (!systemIdURI.isAbsolute() && (base = this.root.getBaseURI()) != null) {
                        systemId = ResolveURI.makeAbsolute(systemId, base).toString();
                    }
                } catch (URISyntaxException systemIdURI) {
                    // empty catch block
                }
                String publicId = e.getPublicId();
                this.receiver.setUnparsedEntity(name, systemId, publicId);
            }
        }
    }

    private void sendElement(Element startNode) throws XPathException {
        ArrayList<Element> ancestors = new ArrayList<Element>();
        NamespaceMap inScopeNamespaces = NamespaceMap.emptyMap();
        for (Node parent = startNode; parent != null && parent.getNodeType() == 1; parent = parent.getParentNode()) {
            ancestors.add((Element)parent);
        }
        for (int i = ancestors.size() - 1; i >= 0; --i) {
            inScopeNamespaces = inScopeNamespaces.applyDifferences(this.gatherNamespaces((Element)ancestors.get(i)));
        }
        this.namespaces.push(inScopeNamespaces);
        this.outputElement(startNode, true);
        this.namespaces.pop();
    }

    private NodeName getNodeName(String name, boolean useDefaultNS) {
        int colon = name.indexOf(58);
        if (colon < 0) {
            String uri;
            if (useDefaultNS && !(uri = this.getUriForPrefix("")).isEmpty()) {
                return new FingerprintedQName("", uri, name);
            }
            return new NoNamespaceName(name);
        }
        String prefix = name.substring(0, colon);
        String uri = this.getUriForPrefix(prefix);
        if (uri == null) {
            throw new IllegalStateException("Prefix " + prefix + " is not bound to any namespace");
        }
        return new FingerprintedQName(prefix, uri, name.substring(colon + 1));
    }

    private void walkNode(Node node) throws XPathException {
        Loc loc = Loc.NONE;
        if (node.hasChildNodes()) {
            NodeList nit = node.getChildNodes();
            int len = nit.getLength();
            block9: for (int i = 0; i < len; ++i) {
                Node child;
                this.currentNode = child = nit.item(i);
                switch (child.getNodeType()) {
                    case 9: 
                    case 11: {
                        continue block9;
                    }
                    case 1: {
                        Element element = (Element)child;
                        NamespaceMap parentNamespaces = this.namespaces.peek();
                        NamespaceMap childNamespaces = parentNamespaces.applyDifferences(this.gatherNamespaces(element));
                        this.namespaces.push(childNamespaces);
                        this.outputElement(element, !childNamespaces.isEmpty());
                        this.namespaces.pop();
                        continue block9;
                    }
                    case 2: {
                        continue block9;
                    }
                    case 7: {
                        this.receiver.processingInstruction(((ProcessingInstruction)child).getTarget(), ((ProcessingInstruction)child).getData(), loc, 0);
                        continue block9;
                    }
                    case 8: {
                        String text = ((Comment)child).getData();
                        if (text == null) continue block9;
                        this.receiver.comment(text, loc, 0);
                        continue block9;
                    }
                    case 3: 
                    case 4: {
                        String text = ((CharacterData)child).getData();
                        if (text == null) continue block9;
                        this.receiver.characters(text, loc, 0);
                        continue block9;
                    }
                    case 5: {
                        this.walkNode(child);
                        continue block9;
                    }
                }
            }
        }
    }

    public Node getCurrentNode() {
        return this.currentNode;
    }

    private void outputElement(Element element, boolean hasNamespaceDeclarations) throws XPathException {
        NodeName name = this.getNodeName(element.getTagName(), true);
        Loc loc = new Loc(this.systemId, -1, -1);
        AttributeMap attributes = EmptyAttributeMap.getInstance();
        NamedNodeMap atts = element.getAttributes();
        if (atts != null) {
            int len = atts.getLength();
            for (int a2 = 0; a2 < len; ++a2) {
                Attr att = (Attr)atts.item(a2);
                int props = att.isId() ? 2048 : 0;
                String attname = att.getName();
                if (hasNamespaceDeclarations && attname.startsWith("xmlns") && (attname.length() == 5 || attname.charAt(5) == ':')) continue;
                NodeName attNodeName = this.getNodeName(attname, false);
                attributes = attributes.put(new AttributeInfo(attNodeName, BuiltInAtomicType.UNTYPED_ATOMIC, att.getValue(), loc, props));
            }
        }
        this.receiver.startElement(name, Untyped.getInstance(), attributes, this.namespaces.peek(), loc, 0);
        this.walkNode(element);
        this.receiver.endElement();
    }

    private String getUriForPrefix(String prefix) {
        if (prefix.equals("xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        String uri = this.namespaces.peek().getURI(prefix);
        return uri == null ? "" : uri;
    }

    private NamespaceDeltaMap gatherNamespaces(Element element) {
        NamespaceDeltaMap result = NamespaceDeltaMap.emptyMap();
        try {
            String prefix = element.getPrefix();
            String uri = element.getNamespaceURI();
            if (prefix == null) {
                prefix = "";
            }
            if (uri == null) {
                uri = "";
            }
            result = result.put(prefix, uri);
        } catch (Throwable prefix) {
            // empty catch block
        }
        NamedNodeMap atts = element.getAttributes();
        if (atts == null) {
            return result;
        }
        int alen = atts.getLength();
        for (int a1 = 0; a1 < alen; ++a1) {
            String uri;
            String prefix;
            Attr att = (Attr)atts.item(a1);
            String attname = att.getName();
            boolean possibleNamespace = attname.startsWith("xmlns");
            if (possibleNamespace && attname.length() == 5) {
                String uri2 = att.getValue();
                result = result.put("", uri2);
                continue;
            }
            if (possibleNamespace && attname.startsWith("xmlns:")) {
                prefix = attname.substring(6);
                uri = att.getValue();
                result = result.put(prefix, uri);
                continue;
            }
            if (attname.indexOf(58) < 0) continue;
            try {
                prefix = att.getPrefix();
                uri = att.getNamespaceURI();
                result = result.put(prefix, uri);
                continue;
            } catch (Throwable throwable) {
                // empty catch block
            }
        }
        return result;
    }
}


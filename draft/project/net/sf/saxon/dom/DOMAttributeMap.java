/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.NamespaceNode;
import net.sf.saxon.tree.iter.AxisIterator;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

class DOMAttributeMap
implements NamedNodeMap {
    private NodeInfo element;
    private NamespaceBinding[] namespaceDeltas;
    private boolean excludeNamespaceUndeclarations;

    public DOMAttributeMap(NodeInfo element) {
        this.element = element;
        if (element.getConfiguration().getXMLVersion() == 10) {
            this.excludeNamespaceUndeclarations = true;
        }
    }

    private NamespaceBinding[] getNamespaceDeltas() {
        NamespaceMap allNamespaces = this.element.getAllNamespaces();
        NodeInfo parent = this.element.getParent();
        NamespaceBinding[] bindings = parent != null && parent.getNodeKind() == 1 ? allNamespaces.getDifferences(parent.getAllNamespaces(), !this.excludeNamespaceUndeclarations) : allNamespaces.getNamespaceBindings();
        return bindings;
    }

    @Override
    public Node getNamedItem(String name) {
        if (name.equals("xmlns")) {
            NamespaceBinding[] nsarray = this.getNamespaceBindings();
            for (int i = 0; i < nsarray.length; ++i) {
                if (nsarray[i] == null) {
                    return null;
                }
                if (!nsarray[i].getPrefix().isEmpty()) continue;
                NamespaceNode nn = new NamespaceNode(this.element, nsarray[i], i + 1);
                return NodeOverNodeInfo.wrap(nn);
            }
            return null;
        }
        if (name.startsWith("xmlns:")) {
            String prefix = name.substring(6);
            NamespaceBinding[] nsarray = this.getNamespaceBindings();
            for (int i = 0; i < nsarray.length; ++i) {
                if (nsarray[i] == null) {
                    return null;
                }
                if (!prefix.equals(nsarray[i].getPrefix())) continue;
                NamespaceNode nn = new NamespaceNode(this.element, nsarray[i], i + 1);
                return NodeOverNodeInfo.wrap(nn);
            }
            return null;
        }
        AxisIterator atts = this.element.iterateAxis(2, att -> att.getDisplayName().equals(name));
        NodeInfo att2 = atts.next();
        return att2 == null ? null : NodeOverNodeInfo.wrap(att2);
    }

    @Override
    public Node item(int index) {
        NodeInfo att;
        if (index < 0) {
            return null;
        }
        NamespaceBinding[] namespaces = this.getNamespaceBindings();
        if (index < namespaces.length) {
            NamespaceBinding ns = namespaces[index];
            NamespaceNode nn = new NamespaceNode(this.element, ns, index);
            return NodeOverNodeInfo.wrap(nn);
        }
        int pos = 0;
        int attNr = index - namespaces.length;
        AxisIterator atts = this.element.iterateAxis(2);
        while ((att = atts.next()) != null) {
            if (pos == attNr) {
                return NodeOverNodeInfo.wrap(att);
            }
            ++pos;
        }
        return null;
    }

    private int getNumberOfNamespaces() {
        return this.getNamespaceBindings().length;
    }

    private NamespaceBinding[] getNamespaceBindings() {
        if (this.namespaceDeltas == null) {
            this.namespaceDeltas = this.getNamespaceDeltas();
        }
        return this.namespaceDeltas;
    }

    @Override
    public int getLength() {
        int length = 0;
        AxisIterator atts = this.element.iterateAxis(2);
        while (atts.next() != null) {
            ++length;
        }
        return this.getNumberOfNamespaces() + length;
    }

    @Override
    public Node getNamedItemNS(String uri, String localName) {
        NodeInfo att;
        if (uri == null) {
            uri = "";
        }
        if ("http://www.w3.org/2000/xmlns/".equals(uri)) {
            return this.getNamedItem("xmlns:" + localName);
        }
        if (uri.equals("") && localName.equals("xmlns")) {
            return this.getNamedItem("xmlns");
        }
        AxisIterator atts = this.element.iterateAxis(2);
        do {
            if ((att = atts.next()) != null) continue;
            return null;
        } while (!uri.equals(att.getURI()) || !localName.equals(att.getLocalPart()));
        return NodeOverNodeInfo.wrap(att);
    }

    @Override
    public Node setNamedItem(Node arg) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Node removeNamedItem(String name) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Node setNamedItemNS(Node arg) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Node removeNamedItemNS(String uri, String localName) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }
}


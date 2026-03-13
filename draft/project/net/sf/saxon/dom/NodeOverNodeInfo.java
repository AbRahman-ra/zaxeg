/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.util.ArrayList;
import net.sf.saxon.dom.AttrOverNodeInfo;
import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.dom.PIOverNodeInfo;
import net.sf.saxon.dom.TextOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.functions.DeepEqual;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public abstract class NodeOverNodeInfo
implements Node {
    protected NodeInfo node;

    public NodeInfo getUnderlyingNodeInfo() {
        return this.node;
    }

    public static NodeOverNodeInfo wrap(NodeInfo node) {
        NodeOverNodeInfo n;
        if (node == null) {
            return null;
        }
        switch (node.getNodeKind()) {
            case 9: {
                n = new DocumentOverNodeInfo();
                break;
            }
            case 1: {
                n = new ElementOverNodeInfo();
                break;
            }
            case 2: {
                n = new AttrOverNodeInfo();
                break;
            }
            case 3: 
            case 8: {
                n = new TextOverNodeInfo();
                break;
            }
            case 7: {
                n = new PIOverNodeInfo();
                break;
            }
            case 13: {
                n = new AttrOverNodeInfo();
                break;
            }
            default: {
                return null;
            }
        }
        n.node = node;
        return n;
    }

    @Override
    public final boolean isSameNode(Node other) {
        return other instanceof NodeOverNodeInfo && this.node.equals(((NodeOverNodeInfo)other).node);
    }

    public boolean equals(Object obj) {
        return obj instanceof Node && this.isSameNode((Node)obj);
    }

    public int hashCode() {
        return this.node.hashCode();
    }

    @Override
    public String getBaseURI() {
        return this.node.getBaseURI();
    }

    @Override
    public String getNodeName() {
        switch (this.node.getNodeKind()) {
            case 9: {
                return "#document";
            }
            case 1: {
                return this.node.getDisplayName();
            }
            case 2: {
                return this.node.getDisplayName();
            }
            case 3: {
                return "#text";
            }
            case 8: {
                return "#comment";
            }
            case 7: {
                return this.node.getLocalPart();
            }
            case 13: {
                if (this.node.getLocalPart().isEmpty()) {
                    return "xmlns";
                }
                return "xmlns:" + this.node.getLocalPart();
            }
        }
        return "#unknown";
    }

    @Override
    public String getLocalName() {
        switch (this.node.getNodeKind()) {
            case 1: 
            case 2: {
                return this.node.getLocalPart();
            }
            case 3: 
            case 7: 
            case 8: 
            case 9: {
                return null;
            }
            case 13: {
                if (this.node.getLocalPart().isEmpty()) {
                    return "xmlns";
                }
                return this.node.getLocalPart();
            }
        }
        return null;
    }

    @Override
    public boolean hasChildNodes() {
        return this.node.hasChildNodes();
    }

    @Override
    public boolean hasAttributes() {
        return true;
    }

    @Override
    public short getNodeType() {
        short kind = (short)this.node.getNodeKind();
        if (kind == 13) {
            return 2;
        }
        return kind;
    }

    @Override
    public Node getParentNode() {
        return NodeOverNodeInfo.wrap(this.node.getParent());
    }

    @Override
    public Node getPreviousSibling() {
        return NodeOverNodeInfo.wrap(this.node.iterateAxis(11).next());
    }

    @Override
    public Node getNextSibling() {
        return NodeOverNodeInfo.wrap(this.node.iterateAxis(7).next());
    }

    @Override
    public Node getFirstChild() {
        return NodeOverNodeInfo.wrap(this.node.iterateAxis(3).next());
    }

    @Override
    public Node getLastChild() {
        AxisIterator children = this.node.iterateAxis(3);
        NodeInfo last = null;
        NodeInfo next;
        while ((next = children.next()) != null) {
            last = next;
        }
        return NodeOverNodeInfo.wrap(last);
    }

    @Override
    public String getNodeValue() {
        switch (this.node.getNodeKind()) {
            case 1: 
            case 9: {
                return null;
            }
            case 2: 
            case 3: 
            case 7: 
            case 8: 
            case 13: {
                return this.node.getStringValue();
            }
        }
        return null;
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
    }

    @Override
    public NodeList getChildNodes() {
        ArrayList<Node> nodes = new ArrayList<Node>(10);
        for (NodeInfo nodeInfo : this.node.children()) {
            nodes.add(NodeOverNodeInfo.wrap(nodeInfo));
        }
        return new DOMNodeList(nodes);
    }

    @Override
    public NamedNodeMap getAttributes() {
        return null;
    }

    @Override
    public Document getOwnerDocument() {
        return (Document)((Object)NodeOverNodeInfo.wrap(this.node.getRoot()));
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Node cloneNode(boolean deep) {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public void normalize() {
    }

    @Override
    public boolean isSupported(String feature, String version) {
        return !(!feature.equalsIgnoreCase("XML") && !feature.equalsIgnoreCase("Core") || version != null && !version.isEmpty() && !version.equals("3.0") && !version.equals("2.0") && !version.equals("1.0"));
    }

    @Override
    public String getNamespaceURI() {
        if (this.node.getNodeKind() == 13) {
            return "http://www.w3.org/2000/xmlns/";
        }
        String uri = this.node.getURI();
        return "".equals(uri) ? null : uri;
    }

    @Override
    public String getPrefix() {
        if (this.node.getNodeKind() == 13) {
            if (this.node.getLocalPart().isEmpty()) {
                return null;
            }
            return "xmlns";
        }
        String p = this.node.getPrefix();
        return "".equals(p) ? null : p;
    }

    @Override
    public void setPrefix(String prefix) throws DOMException {
        NodeOverNodeInfo.disallowUpdate();
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        boolean DOCUMENT_POSITION_DISCONNECTED = true;
        int DOCUMENT_POSITION_PRECEDING = 2;
        int DOCUMENT_POSITION_FOLLOWING = 4;
        int DOCUMENT_POSITION_CONTAINS = 8;
        int DOCUMENT_POSITION_CONTAINED_BY = 16;
        if (!(other instanceof NodeOverNodeInfo)) {
            return 1;
        }
        int c = this.node.compareOrder(((NodeOverNodeInfo)other).node);
        if (c == 0) {
            return 0;
        }
        if (c == -1) {
            short result = 4;
            short d = this.compareDocumentPosition(other.getParentNode());
            if (d == 0 || (d & 0x10) != 0) {
                result = (short)(result | 0x10);
            }
            return result;
        }
        if (c == 1) {
            short result = 2;
            short d = this.getParentNode().compareDocumentPosition(other);
            if (d == 0 || (d & 8) != 0) {
                result = (short)(result | 8);
            }
            return result;
        }
        throw new AssertionError();
    }

    @Override
    public String getTextContent() throws DOMException {
        if (this.node.getNodeKind() == 9) {
            return null;
        }
        return this.node.getStringValue();
    }

    @Override
    public void setTextContent(String textContent) throws UnsupportedOperationException {
        NodeOverNodeInfo.disallowUpdate();
    }

    @Override
    public String lookupPrefix(String namespaceURI) {
        if (this.node.getNodeKind() == 9) {
            return null;
        }
        if (this.node.getNodeKind() == 1) {
            NodeInfo ns;
            AxisIterator iter = this.node.iterateAxis(8);
            while ((ns = iter.next()) != null) {
                if (!ns.getStringValue().equals(namespaceURI)) continue;
                return ns.getLocalPart();
            }
            return null;
        }
        return this.getParentNode().lookupPrefix(namespaceURI);
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        return namespaceURI.equals(this.lookupNamespaceURI(""));
    }

    @Override
    public String lookupNamespaceURI(String prefix) {
        if (this.node.getNodeKind() == 9) {
            return null;
        }
        if (this.node.getNodeKind() == 1) {
            NodeInfo ns;
            AxisIterator iter = this.node.iterateAxis(8);
            while ((ns = iter.next()) != null) {
                if (!ns.getLocalPart().equals(prefix)) continue;
                return ns.getStringValue();
            }
            return null;
        }
        return this.getParentNode().lookupNamespaceURI(prefix);
    }

    @Override
    public boolean isEqualNode(Node arg) {
        if (!(arg instanceof NodeOverNodeInfo)) {
            throw new IllegalArgumentException("Other Node must wrap a Saxon NodeInfo");
        }
        try {
            XPathContext context = this.node.getConfiguration().getConversionContext();
            return DeepEqual.deepEqual(SingletonIterator.makeIterator(this.node), SingletonIterator.makeIterator(((NodeOverNodeInfo)arg).node), new GenericAtomicComparer(CodepointCollator.getInstance(), context), context, 46);
        } catch (XPathException err) {
            return false;
        }
    }

    @Override
    public Object getFeature(String feature, String version) {
        return null;
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        NodeOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Object getUserData(String key) {
        return null;
    }

    protected static void disallowUpdate() throws DOMException {
        throw new DOMException(7, "The Saxon DOM implementation cannot be updated");
    }
}


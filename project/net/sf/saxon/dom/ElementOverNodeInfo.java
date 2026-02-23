/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import net.sf.saxon.dom.AttrOverNodeInfo;
import net.sf.saxon.dom.DOMAttributeMap;
import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.dom.TypeInfoImpl;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

public class ElementOverNodeInfo
extends NodeOverNodeInfo
implements Element {
    private DOMAttributeMap attributeMap = null;

    @Override
    public String getTagName() {
        return this.node.getDisplayName();
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        return DocumentOverNodeInfo.getElementsByTagName(this.node, name);
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
        return DocumentOverNodeInfo.getElementsByTagNameNS(this.node, namespaceURI, localName);
    }

    @Override
    public NamedNodeMap getAttributes() {
        if (this.attributeMap == null) {
            this.attributeMap = new DOMAttributeMap(this.node);
        }
        return this.attributeMap;
    }

    @Override
    public String getAttribute(String name) {
        NodeInfo att;
        if (name.startsWith("xmlns")) {
            Node node = this.getAttributes().getNamedItem(name);
            return node == null ? "" : node.getNodeValue();
        }
        AxisIterator atts = this.node.iterateAxis(2);
        do {
            if ((att = atts.next()) != null) continue;
            return "";
        } while (!att.getDisplayName().equals(name));
        String val = att.getStringValue();
        if (val == null) {
            return "";
        }
        return val;
    }

    @Override
    public Attr getAttributeNode(String name) {
        NodeInfo att;
        AxisIterator atts = this.node.iterateAxis(2);
        do {
            if ((att = atts.next()) != null) continue;
            return null;
        } while (!att.getDisplayName().equals(name));
        return (AttrOverNodeInfo)ElementOverNodeInfo.wrap(att);
    }

    @Override
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        ElementOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public void removeAttribute(String oldAttr) throws DOMException {
        ElementOverNodeInfo.disallowUpdate();
    }

    @Override
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        ElementOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public String getAttributeNS(String namespaceURI, String localName) {
        if ("http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
            Node node = this.getAttributes().getNamedItemNS(namespaceURI, localName);
            return node == null ? "" : node.getNodeValue();
        }
        String uri = namespaceURI == null ? "" : namespaceURI;
        String val = this.node.getAttributeValue(uri, localName);
        if (val == null) {
            return "";
        }
        return val;
    }

    @Override
    public void setAttribute(String name, String value) throws DOMException {
        ElementOverNodeInfo.disallowUpdate();
    }

    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        ElementOverNodeInfo.disallowUpdate();
    }

    @Override
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        ElementOverNodeInfo.disallowUpdate();
    }

    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        NamePool pool = this.node.getConfiguration().getNamePool();
        NameTest test = new NameTest(2, namespaceURI, localName, pool);
        AxisIterator atts = this.node.iterateAxis(2, test);
        return (Attr)((Object)ElementOverNodeInfo.wrap(atts.next()));
    }

    @Override
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        ElementOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public boolean hasAttribute(String name) {
        NodeInfo att;
        if (name.startsWith("xmlns")) {
            Node node = this.getAttributes().getNamedItem(name);
            return node != null;
        }
        AxisIterator atts = this.node.iterateAxis(2);
        do {
            if ((att = atts.next()) != null) continue;
            return false;
        } while (!att.getDisplayName().equals(name));
        return true;
    }

    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        if ("http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
            Node node = this.getAttributes().getNamedItemNS(namespaceURI, localName);
            return node != null;
        }
        String uri = namespaceURI == null ? "" : namespaceURI;
        return this.node.getAttributeValue(uri, localName) != null;
    }

    @Override
    public void setIdAttribute(String name, boolean isId) throws UnsupportedOperationException {
        ElementOverNodeInfo.disallowUpdate();
    }

    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws UnsupportedOperationException {
        ElementOverNodeInfo.disallowUpdate();
    }

    @Override
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws UnsupportedOperationException {
        ElementOverNodeInfo.disallowUpdate();
    }

    @Override
    public TypeInfo getSchemaTypeInfo() {
        SchemaType type = this.node.getSchemaType();
        if (type == null || Untyped.getInstance().equals(type) || BuiltInAtomicType.UNTYPED_ATOMIC.equals(type)) {
            return null;
        }
        return new TypeInfoImpl(this.node.getConfiguration(), type);
    }
}


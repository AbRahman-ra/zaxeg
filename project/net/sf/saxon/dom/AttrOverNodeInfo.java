/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.util.ArrayList;
import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.dom.TextOverAttrInfo;
import net.sf.saxon.dom.TypeInfoImpl;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

public class AttrOverNodeInfo
extends NodeOverNodeInfo
implements Attr {
    @Override
    public String getName() {
        if (this.node.getNodeKind() == 13) {
            String local = this.node.getLocalPart();
            if (local.equals("")) {
                return "xmlns";
            }
            return "xmlns:" + local;
        }
        return this.node.getDisplayName();
    }

    @Override
    public String getValue() {
        return this.node.getStringValue();
    }

    @Override
    public boolean hasChildNodes() {
        return true;
    }

    @Override
    public Node getFirstChild() {
        return new TextOverAttrInfo(this);
    }

    @Override
    public Node getLastChild() {
        return this.getFirstChild();
    }

    @Override
    public NodeList getChildNodes() {
        ArrayList<Node> list = new ArrayList<Node>(1);
        list.add(this.getFirstChild());
        return new DOMNodeList(list);
    }

    @Override
    public boolean getSpecified() {
        return true;
    }

    @Override
    public void setValue(String value) throws DOMException {
        AttrOverNodeInfo.disallowUpdate();
    }

    @Override
    public boolean isId() {
        return this.node.isId();
    }

    @Override
    public Element getOwnerElement() {
        if (this.node.getNodeKind() == 2 || this.node.getNodeKind() == 13) {
            return (Element)((Object)AttrOverNodeInfo.wrap(this.node.getParent()));
        }
        throw new UnsupportedOperationException("This method is defined only on attribute and namespace nodes");
    }

    @Override
    public TypeInfo getSchemaTypeInfo() {
        SchemaType type = this.node.getSchemaType();
        if (type == null || BuiltInAtomicType.UNTYPED_ATOMIC.equals(type)) {
            return null;
        }
        return new TypeInfoImpl(this.node.getConfiguration(), type);
    }
}


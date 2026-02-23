/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import net.sf.saxon.dom.DOMExceptionImpl;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Text;

public class TextOverNodeInfo
extends NodeOverNodeInfo
implements Text,
Comment {
    @Override
    public String getData() {
        return this.node.getStringValue();
    }

    @Override
    public void setData(String data) throws DOMException {
        TextOverNodeInfo.disallowUpdate();
    }

    @Override
    public int getLength() {
        return this.node.getStringValue().length();
    }

    @Override
    public String substringData(int offset, int count) throws DOMException {
        try {
            return this.node.getStringValue().substring(offset, offset + count);
        } catch (IndexOutOfBoundsException err2) {
            throw new DOMExceptionImpl(1, "substringData: index out of bounds");
        }
    }

    @Override
    public void appendData(String arg) throws DOMException {
        TextOverNodeInfo.disallowUpdate();
    }

    @Override
    public void insertData(int offset, String arg) throws DOMException {
        TextOverNodeInfo.disallowUpdate();
    }

    @Override
    public void deleteData(int offset, int count) throws DOMException {
        TextOverNodeInfo.disallowUpdate();
    }

    @Override
    public void replaceData(int offset, int count, String arg) throws DOMException {
        TextOverNodeInfo.disallowUpdate();
    }

    @Override
    public Text splitText(int offset) throws DOMException {
        TextOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public Text replaceWholeText(String content) throws DOMException {
        TextOverNodeInfo.disallowUpdate();
        return null;
    }

    @Override
    public boolean isElementContentWhitespace() {
        if (this.node.getNodeKind() != 3) {
            throw new UnsupportedOperationException("Method is defined only on text nodes");
        }
        if (!Whitespace.isWhite(this.node.getStringValue())) {
            return false;
        }
        NodeInfo parent = this.node.getParent();
        if (parent == null) {
            return false;
        }
        SchemaType type = parent.getSchemaType();
        return type.isComplexType() && !((ComplexType)type).isMixedContent();
    }

    @Override
    public String getWholeText() {
        if (this.node.getNodeKind() != 3) {
            throw new UnsupportedOperationException("Method is defined only on text nodes");
        }
        return this.node.getStringValue();
    }
}


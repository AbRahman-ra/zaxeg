/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import net.sf.saxon.dom.AttrOverNodeInfo;
import net.sf.saxon.dom.TextOverNodeInfo;
import org.w3c.dom.Node;

public class TextOverAttrInfo
extends TextOverNodeInfo {
    private final AttrOverNodeInfo attr;

    public TextOverAttrInfo(AttrOverNodeInfo attr) {
        this.attr = attr;
        this.node = attr.getUnderlyingNodeInfo();
    }

    @Override
    public boolean isElementContentWhitespace() {
        return false;
    }

    @Override
    public short getNodeType() {
        return 3;
    }

    @Override
    public short compareDocumentPosition(Node other) {
        int DOCUMENT_POSITION_FOLLOWING = 4;
        if (other instanceof TextOverAttrInfo) {
            if (this.node.equals(((TextOverAttrInfo)other).node)) {
                return 0;
            }
            return this.attr.compareDocumentPosition(((TextOverAttrInfo)other).attr);
        }
        if (other instanceof AttrOverNodeInfo && this.node.equals(((AttrOverNodeInfo)other).getUnderlyingNodeInfo())) {
            return 4;
        }
        return this.attr.compareDocumentPosition(other);
    }

    @Override
    public Node getParentNode() {
        return this.attr;
    }
}


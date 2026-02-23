/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DOMNodeList
implements NodeList {
    private final List<Node> sequence;

    public DOMNodeList(List<Node> extent) {
        this.sequence = extent;
    }

    @Override
    public int getLength() {
        return this.sequence.size();
    }

    @Override
    public Node item(int index) {
        if (index < 0 || index >= this.sequence.size()) {
            return null;
        }
        return this.sequence.get(index);
    }
}


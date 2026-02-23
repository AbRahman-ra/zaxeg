/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.function.IntPredicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTextualElement;
import net.sf.saxon.tree.tiny.TinyTree;

final class DescendantIterator
implements AxisIterator {
    private final TinyTree tree;
    private int nextNodeNr;
    private final int startDepth;
    private final IntPredicate matcher;
    private NodeInfo pending = null;

    DescendantIterator(TinyTree doc, TinyNodeImpl node, NodeTest nodeTest) {
        this.tree = doc;
        this.nextNodeNr = node.nodeNr;
        this.startDepth = doc.depth[this.nextNodeNr];
        this.matcher = nodeTest.getMatcher(doc);
    }

    @Override
    public NodeInfo next() {
        do {
            if (this.pending != null) {
                NodeInfo p = this.pending;
                this.pending = null;
                return p;
            }
            ++this.nextNodeNr;
            try {
                if (this.tree.depth[this.nextNodeNr] <= this.startDepth) {
                    this.nextNodeNr = -1;
                    return null;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                this.nextNodeNr = -1;
                return null;
            }
            if (this.tree.nodeKind[this.nextNodeNr] != 17) continue;
            this.pending = ((TinyTextualElement)this.tree.getNode(this.nextNodeNr)).getTextNode();
        } while (!this.matcher.test(this.nextNodeNr));
        return this.tree.getNode(this.nextNodeNr);
    }
}


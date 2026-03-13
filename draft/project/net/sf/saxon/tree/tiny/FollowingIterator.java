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

final class FollowingIterator
implements AxisIterator {
    private TinyTree tree;
    private TinyNodeImpl startNode;
    private NodeInfo current;
    private NodeTest test;
    private boolean includeDescendants;
    int position = 0;
    private final IntPredicate matcher;
    private NodeInfo pending;

    public FollowingIterator(TinyTree doc, TinyNodeImpl node, NodeTest nodeTest, boolean includeDescendants) {
        this.tree = doc;
        this.test = nodeTest;
        this.startNode = node;
        this.includeDescendants = includeDescendants;
        this.matcher = nodeTest.getMatcher(doc);
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public NodeInfo next() {
        int nodeNr;
        block16: {
            block15: {
                block14: {
                    if (this.pending != null) {
                        NodeInfo p = this.pending;
                        this.pending = null;
                        return p;
                    }
                    if (this.position > 0) break block14;
                    if (this.position < 0) {
                        return null;
                    }
                    nodeNr = this.startNode.nodeNr;
                    if (!this.includeDescendants) break block15;
                    ++nodeNr;
                    break block16;
                }
                assert (this.current != null);
                TinyNodeImpl here = this.current instanceof TinyTextualElement.TinyTextualElementText ? (TinyNodeImpl)this.current.getParent() : (TinyNodeImpl)this.current;
                nodeNr = here.nodeNr + 1;
                break block16;
            }
            while (true) {
                int nextSib;
                if ((nextSib = this.tree.next[nodeNr]) > nodeNr) {
                    nodeNr = nextSib;
                    break;
                }
                if (this.tree.depth[nextSib] == 0) {
                    this.current = null;
                    this.position = -1;
                    return null;
                }
                nodeNr = nextSib;
            }
        }
        while (true) {
            if (this.tree.depth[nodeNr] == 0) {
                this.current = null;
                this.position = -1;
                return null;
            }
            if (this.tree.nodeKind[nodeNr] == 17) {
                TinyTextualElement e = (TinyTextualElement)this.tree.getNode(nodeNr);
                TinyTextualElement.TinyTextualElementText t = e.getTextNode();
                if (this.matcher.test(nodeNr)) {
                    if (this.test.test(t)) {
                        this.pending = t;
                    }
                    ++this.position;
                    this.current = this.tree.getNode(nodeNr);
                    return this.current;
                }
                if (this.test.test(t)) {
                    ++this.position;
                    this.current = t;
                    return this.current;
                }
            } else if (this.matcher.test(nodeNr)) {
                ++this.position;
                this.current = this.tree.getNode(nodeNr);
                return this.current;
            }
            ++nodeNr;
        }
    }
}


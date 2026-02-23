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
import net.sf.saxon.type.UType;

final class PrecedingIterator
implements AxisIterator {
    private TinyTree tree;
    private NodeInfo current;
    private int nextAncestorDepth;
    private boolean includeAncestors;
    private final IntPredicate matcher;
    private NodeInfo pending = null;
    private NodeTest nodeTest;
    private boolean matchesTextNodes;

    public PrecedingIterator(TinyTree doc, TinyNodeImpl node, NodeTest nodeTest, boolean includeAncestors) {
        this.includeAncestors = includeAncestors;
        this.tree = doc;
        this.current = node;
        this.nextAncestorDepth = doc.depth[node.nodeNr] - 1;
        this.nodeTest = nodeTest;
        this.matcher = nodeTest.getMatcher(doc);
        this.matchesTextNodes = nodeTest.getUType().overlaps(UType.TEXT);
    }

    @Override
    public NodeInfo next() {
        if (this.pending != null) {
            this.current = this.pending;
            this.pending = null;
            return this.current;
        }
        if (this.current == null) {
            return null;
        }
        if (this.current instanceof TinyTextualElement.TinyTextualElementText) {
            this.current = this.current.getParent();
        }
        int nextNodeNr = ((TinyNodeImpl)this.current).nodeNr;
        while (true) {
            if (!this.includeAncestors) {
                --nextNodeNr;
                while (this.nextAncestorDepth >= 0 && this.tree.depth[nextNodeNr] == this.nextAncestorDepth) {
                    if (this.nextAncestorDepth-- <= 0) {
                        this.current = null;
                        return null;
                    }
                    --nextNodeNr;
                }
            } else {
                if (this.tree.depth[nextNodeNr] == 0) {
                    this.current = null;
                    return null;
                }
                --nextNodeNr;
            }
            if (this.matchesTextNodes && this.tree.nodeKind[nextNodeNr] == 17) {
                TinyTextualElement element = (TinyTextualElement)this.tree.getNode(nextNodeNr);
                TinyTextualElement.TinyTextualElementText text = element.getTextNode();
                if (this.nodeTest.test(text)) {
                    if (this.nodeTest.test(element)) {
                        this.pending = element;
                    }
                    this.current = text;
                    return this.current;
                }
                if (!this.nodeTest.test(element)) continue;
                this.current = element;
                return this.current;
            }
            if (this.matcher.test(nextNodeNr)) {
                this.current = this.tree.getNode(nextNodeNr);
                return this.current;
            }
            if (this.tree.depth[nextNodeNr] == 0) break;
        }
        this.current = null;
        return null;
    }
}


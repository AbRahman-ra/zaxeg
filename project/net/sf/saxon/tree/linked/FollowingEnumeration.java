/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.function.Predicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.TreeEnumeration;

final class FollowingEnumeration
extends TreeEnumeration {
    private NodeImpl root;

    public FollowingEnumeration(NodeImpl node, Predicate<? super NodeInfo> nodeTest) {
        super(node, nodeTest);
        this.root = (NodeImpl)node.getRoot();
        int type = node.getNodeKind();
        if (type == 2 || type == 13) {
            this.next = node.getParent().getNextInDocument(this.root);
        } else {
            do {
                this.next = node.getNextSibling();
                if (this.next != null) continue;
                node = node.getParent();
            } while (this.next == null && node != null);
        }
        while (!this.conforms(this.next)) {
            this.step();
        }
    }

    @Override
    protected void step() {
        this.next = this.next.getNextInDocument(this.root);
    }
}


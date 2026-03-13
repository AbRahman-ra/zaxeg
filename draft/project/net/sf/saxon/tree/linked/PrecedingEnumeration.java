/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.function.Predicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.TreeEnumeration;

final class PrecedingEnumeration
extends TreeEnumeration {
    NodeImpl nextAncestor;

    public PrecedingEnumeration(NodeImpl node, Predicate<? super NodeInfo> nodeTest) {
        super(node, nodeTest);
        this.nextAncestor = node.getParent();
        this.advance();
    }

    @Override
    protected boolean conforms(NodeImpl node) {
        if (node != null && node.equals(this.nextAncestor)) {
            this.nextAncestor = this.nextAncestor.getParent();
            return false;
        }
        return super.conforms(node);
    }

    @Override
    protected void step() {
        this.next = this.next.getPreviousInDocument();
    }
}


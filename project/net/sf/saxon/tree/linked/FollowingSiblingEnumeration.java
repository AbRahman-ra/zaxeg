/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.function.Predicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.TreeEnumeration;

final class FollowingSiblingEnumeration
extends TreeEnumeration {
    public FollowingSiblingEnumeration(NodeImpl node, Predicate<? super NodeInfo> nodeTest) {
        super(node, nodeTest);
        this.advance();
    }

    @Override
    protected void step() {
        this.next = this.next.getNextSibling();
    }
}


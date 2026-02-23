/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.function.Predicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.TreeEnumeration;

final class AncestorEnumeration
extends TreeEnumeration {
    private boolean includeSelf;

    public AncestorEnumeration(NodeImpl node, Predicate<? super NodeInfo> nodeTest, boolean includeSelf) {
        super(node, nodeTest);
        this.includeSelf = includeSelf;
        if (!includeSelf || !this.conforms(node)) {
            this.advance();
        }
    }

    @Override
    protected void step() {
        this.next = this.next.getParent();
    }
}


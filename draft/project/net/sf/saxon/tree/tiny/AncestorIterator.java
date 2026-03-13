/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.function.Predicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;

public final class AncestorIterator
implements AxisIterator {
    private NodeInfo startNode;
    private NodeInfo current;
    private Predicate<? super NodeInfo> test;

    public AncestorIterator(NodeInfo node, Predicate<? super NodeInfo> nodeTest) {
        this.test = nodeTest;
        this.current = this.startNode = node;
    }

    @Override
    public NodeInfo next() {
        NodeInfo node;
        if (this.current == null) {
            return null;
        }
        for (node = this.current.getParent(); node != null && !this.test.test(node); node = node.getParent()) {
        }
        this.current = node;
        return this.current;
    }
}


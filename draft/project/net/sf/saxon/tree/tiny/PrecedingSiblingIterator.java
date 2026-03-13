/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.function.IntPredicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTree;

final class PrecedingSiblingIterator
implements AxisIterator {
    private TinyTree document;
    private TinyNodeImpl startNode;
    private int nextNodeNr;
    private NodeTest test;
    private TinyNodeImpl parentNode;
    private final IntPredicate matcher;

    PrecedingSiblingIterator(TinyTree doc, TinyNodeImpl node, NodeTest nodeTest) {
        this.document = doc;
        this.document.ensurePriorIndex();
        this.test = nodeTest;
        this.startNode = node;
        this.nextNodeNr = node.nodeNr;
        this.parentNode = node.parent;
        this.matcher = nodeTest.getMatcher(doc);
    }

    @Override
    public NodeInfo next() {
        if (this.nextNodeNr < 0) {
            return null;
        }
        do {
            this.nextNodeNr = this.document.prior[this.nextNodeNr];
            if (this.nextNodeNr >= 0) continue;
            return null;
        } while (!this.matcher.test(this.nextNodeNr));
        TinyNodeImpl next = this.document.getNode(this.nextNodeNr);
        next.setParentNode(this.parentNode);
        return next;
    }
}


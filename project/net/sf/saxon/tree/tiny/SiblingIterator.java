/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.EnumSet;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AtomizedValueIterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTextImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.tiny.WhitespaceTextImpl;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.z.IntSetPredicate;

final class SiblingIterator
implements AxisIterator,
LookaheadIterator,
AtomizedValueIterator {
    private TinyTree tree;
    private int nextNodeNr;
    private Predicate<? super NodeInfo> test;
    private TinyNodeImpl startNode;
    private TinyNodeImpl parentNode;
    private boolean getChildren;
    private boolean needToAdvance = false;
    private final IntPredicate matcher;

    SiblingIterator(TinyTree tree, TinyNodeImpl node, Predicate<? super NodeInfo> nodeTest, boolean getChildren) {
        this.tree = tree;
        this.test = nodeTest;
        this.matcher = nodeTest instanceof NodeTest ? ((NodeTest)nodeTest).getMatcher(tree) : IntSetPredicate.ALWAYS_TRUE;
        this.startNode = node;
        this.getChildren = getChildren;
        if (getChildren) {
            this.parentNode = node;
            this.nextNodeNr = node.nodeNr + 1;
        } else {
            this.parentNode = node.getParent();
            if (this.parentNode == null) {
                this.nextNodeNr = -1;
            } else {
                this.nextNodeNr = tree.next[node.nodeNr];
                while (tree.nodeKind[this.nextNodeNr] == 12) {
                    this.nextNodeNr = tree.next[this.nextNodeNr];
                }
                if (this.nextNodeNr < node.nodeNr) {
                    this.nextNodeNr = -1;
                }
            }
        }
        if (this.nextNodeNr >= 0 && nodeTest != null && !this.matcher.test(this.nextNodeNr)) {
            this.needToAdvance = true;
        }
    }

    @Override
    public NodeInfo next() {
        if (this.needToAdvance) {
            int thisNode = this.nextNodeNr;
            int[] tNext = this.tree.next;
            Predicate<? super NodeInfo> nTest = this.test;
            if (nTest == null) {
                do {
                    this.nextNodeNr = tNext[this.nextNodeNr];
                } while (this.tree.nodeKind[this.nextNodeNr] == 12);
            } else {
                do {
                    this.nextNodeNr = tNext[this.nextNodeNr];
                } while (this.nextNodeNr >= thisNode && !this.matcher.test(this.nextNodeNr));
            }
            if (this.nextNodeNr < thisNode) {
                this.nextNodeNr = -1;
                this.needToAdvance = false;
                return null;
            }
        }
        if (this.nextNodeNr == -1) {
            return null;
        }
        this.needToAdvance = true;
        TinyNodeImpl nextNode = this.tree.getNode(this.nextNodeNr);
        nextNode.setParentNode(this.parentNode);
        return nextNode;
    }

    @Override
    public AtomicSequence nextAtomizedValue() throws XPathException {
        if (this.needToAdvance) {
            int thisNode = this.nextNodeNr;
            Predicate<? super NodeInfo> nTest = this.test;
            int[] tNext = this.tree.next;
            if (nTest == null) {
                do {
                    this.nextNodeNr = tNext[this.nextNodeNr];
                } while (this.tree.nodeKind[this.nextNodeNr] == 12);
            } else {
                do {
                    this.nextNodeNr = tNext[this.nextNodeNr];
                } while (this.nextNodeNr >= thisNode && !this.matcher.test(this.nextNodeNr));
            }
            if (this.nextNodeNr < thisNode) {
                this.nextNodeNr = -1;
                this.needToAdvance = false;
                return null;
            }
        }
        if (this.nextNodeNr == -1) {
            return null;
        }
        this.needToAdvance = true;
        byte kind = this.tree.nodeKind[this.nextNodeNr];
        switch (kind) {
            case 3: {
                return new UntypedAtomicValue(TinyTextImpl.getStringValue(this.tree, this.nextNodeNr));
            }
            case 4: {
                return new UntypedAtomicValue(WhitespaceTextImpl.getStringValueCS(this.tree, this.nextNodeNr));
            }
            case 1: 
            case 17: {
                return this.tree.getTypedValueOfElement(this.nextNodeNr);
            }
            case 7: 
            case 8: {
                return this.tree.getAtomizedValueOfUntypedNode(this.nextNodeNr);
            }
        }
        throw new AssertionError((Object)"Unknown node kind on child axis");
    }

    @Override
    public boolean hasNext() {
        int n = this.nextNodeNr;
        if (this.needToAdvance) {
            Predicate<? super NodeInfo> nTest = this.test;
            int[] tNext = this.tree.next;
            if (nTest == null) {
                while (this.tree.nodeKind[n = tNext[n]] == 12) {
                }
            } else {
                while ((n = tNext[n]) >= this.nextNodeNr && !this.matcher.test(n)) {
                }
            }
            if (n < this.nextNodeNr) {
                return false;
            }
        }
        return n != -1;
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.ATOMIZING);
    }
}


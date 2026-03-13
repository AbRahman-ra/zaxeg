/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.EnumSet;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AtomizedValueIterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTree;

final class NamedChildIterator
implements AxisIterator,
LookaheadIterator,
AtomizedValueIterator {
    private TinyTree tree;
    private int nextNodeNr;
    private int fingerprint;
    private TinyNodeImpl startNode;
    private boolean needToAdvance = false;

    NamedChildIterator(TinyTree tree, TinyNodeImpl node, int fingerprint) {
        this.tree = tree;
        this.fingerprint = fingerprint;
        this.startNode = node;
        this.startNode = node;
        this.nextNodeNr = node.nodeNr + 1;
        if ((tree.nodeKind[this.nextNodeNr] & 0xF) != 1 || (tree.nameCode[this.nextNodeNr] & 0xFFFFF) != fingerprint) {
            this.needToAdvance = true;
        }
    }

    @Override
    public NodeInfo next() {
        if (this.needToAdvance) {
            int thisNode = this.nextNodeNr;
            do {
                this.nextNodeNr = this.tree.next[this.nextNodeNr];
                if (this.nextNodeNr >= thisNode) continue;
                this.nextNodeNr = -1;
                this.needToAdvance = false;
                return null;
            } while ((this.tree.nameCode[this.nextNodeNr] & 0xFFFFF) != this.fingerprint || (this.tree.nodeKind[this.nextNodeNr] & 0xF) != 1);
        } else if (this.nextNodeNr == -1) {
            return null;
        }
        this.needToAdvance = true;
        TinyNodeImpl nextNode = this.tree.getNode(this.nextNodeNr);
        nextNode.setParentNode(this.startNode);
        return nextNode;
    }

    @Override
    public AtomicSequence nextAtomizedValue() throws XPathException {
        if (this.needToAdvance) {
            int thisNode = this.nextNodeNr;
            do {
                this.nextNodeNr = this.tree.next[this.nextNodeNr];
                if (this.nextNodeNr >= thisNode) continue;
                this.nextNodeNr = -1;
                this.needToAdvance = false;
                return null;
            } while ((this.tree.nameCode[this.nextNodeNr] & 0xFFFFF) != this.fingerprint || (this.tree.nodeKind[this.nextNodeNr] & 0xF) != 1);
        } else if (this.nextNodeNr == -1) {
            return null;
        }
        this.needToAdvance = true;
        return this.tree.getTypedValueOfElement(this.nextNodeNr);
    }

    @Override
    public boolean hasNext() {
        int n = this.nextNodeNr;
        if (this.needToAdvance) {
            int thisNode = n;
            do {
                if ((n = this.tree.next[n]) >= thisNode) continue;
                return false;
            } while ((this.tree.nodeKind[n] & 0xF) != 1 || (this.tree.nameCode[n] & 0xFFFFF) != this.fingerprint);
            return true;
        }
        return n != -1;
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.ATOMIZING);
    }
}


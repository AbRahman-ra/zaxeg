/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.TreeSet;
import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class UnionIterator
implements SequenceIterator,
LookaheadIterator {
    private TreeSet<Intake> intakes;

    public UnionIterator(List<SequenceIterator> inputs, ItemOrderComparer comparer) throws XPathException {
        Comparator comp = (a, b) -> comparer.compare(a.nextNode, b.nextNode);
        this.intakes = new TreeSet(comp);
        for (SequenceIterator seq : inputs) {
            boolean added;
            NodeInfo next = (NodeInfo)seq.next();
            while (next != null && !(added = this.intakes.add(new Intake(seq, next)))) {
                next = (NodeInfo)seq.next();
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !this.intakes.isEmpty();
    }

    @Override
    public NodeInfo next() throws XPathException {
        Intake nextIntake = this.intakes.pollFirst();
        if (nextIntake != null) {
            SequenceIterator iter = nextIntake.iter;
            NodeInfo nextNode = (NodeInfo)iter.next();
            while (nextNode != null) {
                boolean added = false;
                if (!nextNode.isSameNodeInfo(nextIntake.nextNode)) {
                    Intake replacement = new Intake(iter, nextNode);
                    added = this.intakes.add(replacement);
                }
                if (added) break;
                nextNode = (NodeInfo)iter.next();
            }
            return nextIntake.nextNode;
        }
        return null;
    }

    @Override
    public void close() {
        for (Intake intake : this.intakes) {
            intake.iter.close();
        }
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD);
    }

    private static class Intake {
        public SequenceIterator iter;
        public NodeInfo nextNode;

        public Intake(SequenceIterator iter, NodeInfo nextNode) {
            this.iter = iter;
            this.nextNode = nextNode;
        }
    }
}


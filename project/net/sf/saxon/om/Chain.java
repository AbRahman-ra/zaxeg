/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;

public class Chain
implements GroundedValue {
    private List<GroundedValue> children;
    private List<Item> extent = null;

    public Chain(List<GroundedValue> children) {
        this.children = children;
        int size = 0;
        boolean copy = false;
        for (GroundedValue gv : children) {
            if (gv instanceof Chain) {
                if (((Chain)gv).children.size() < 30) {
                    size += ((Chain)gv).children.size();
                    copy = true;
                    continue;
                }
                ++size;
                continue;
            }
            ++size;
        }
        if (copy) {
            this.children = new ArrayList<GroundedValue>(size);
            for (GroundedValue gv : children) {
                if (gv instanceof Chain) {
                    if (((Chain)gv).children.size() < 30) {
                        this.children.addAll(((Chain)gv).children);
                        continue;
                    }
                    this.children.add(gv);
                    continue;
                }
                this.children.add(gv);
            }
        }
    }

    @Override
    public Item head() {
        if (this.extent != null) {
            return this.extent.isEmpty() ? null : this.extent.get(0);
        }
        for (GroundedValue seq : this.children) {
            Item head = seq.head();
            if (head == null) continue;
            return head;
        }
        return null;
    }

    @Override
    public UnfailingIterator iterate() {
        if (this.extent != null) {
            return new ListIterator<Item>(this.extent);
        }
        return new ChainIterator(this);
    }

    public void append(Item item) {
        if (this.extent != null) {
            throw new IllegalStateException();
        }
        if (item != null) {
            this.children.add(item);
        }
    }

    private void consolidate() {
        if (this.extent == null) {
            this.extent = this.iterate().toList();
        }
    }

    @Override
    public Item itemAt(int n) {
        if (n == 0) {
            return this.head();
        }
        this.consolidate();
        if (n >= 0 && n < this.extent.size()) {
            return this.extent.get(n);
        }
        return null;
    }

    @Override
    public GroundedValue subsequence(int start, int length) {
        int newEnd;
        this.consolidate();
        if (start < 0) {
            start = 0;
        } else if (start >= this.extent.size()) {
            return EmptySequence.getInstance();
        }
        int newStart = start;
        if (length == Integer.MAX_VALUE) {
            newEnd = this.extent.size();
        } else {
            if (length < 0) {
                return EmptySequence.getInstance();
            }
            newEnd = newStart + length;
            if (newEnd > this.extent.size()) {
                newEnd = this.extent.size();
            }
        }
        return new SequenceExtent(this.extent.subList(newStart, newEnd));
    }

    @Override
    public int getLength() {
        if (this.extent != null) {
            return this.extent.size();
        }
        int n = 0;
        for (GroundedValue v : this.children) {
            n += v.getLength();
        }
        return n;
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        return ExpressionTool.effectiveBooleanValue(this.iterate());
    }

    @Override
    public String getStringValue() throws XPathException {
        return SequenceTool.getStringValue(this);
    }

    @Override
    public CharSequence getStringValueCS() throws XPathException {
        return SequenceTool.getStringValue(this);
    }

    @Override
    public GroundedValue reduce() {
        this.consolidate();
        return SequenceExtent.makeSequenceExtent(this.extent);
    }

    private static class ChainIterator
    implements UnfailingIterator,
    GroundedIterator {
        private final Queue<UnfailingIterator> queue = new LinkedList<UnfailingIterator>();
        private final Stack<ChainPosition> stack;
        private final Chain thisChain;

        public ChainIterator(Chain thisChain) {
            this.thisChain = thisChain;
            this.stack = new Stack();
            this.stack.push(new ChainPosition(thisChain, 0));
        }

        @Override
        public Item next() {
            while (!this.queue.isEmpty()) {
                UnfailingIterator ui = this.queue.peek();
                while (ui != null) {
                    Item current = ui.next();
                    if (current != null) {
                        return current;
                    }
                    this.queue.remove();
                    ui = this.queue.peek();
                }
            }
            while (!this.stack.isEmpty()) {
                ChainPosition cp = this.stack.peek();
                if (cp.offset >= cp.chain.children.size()) {
                    this.stack.pop();
                    continue;
                }
                GroundedValue gv = (GroundedValue)cp.chain.children.get(cp.offset++);
                if (gv instanceof Chain) {
                    this.stack.push(new ChainPosition((Chain)gv, 0));
                    continue;
                }
                if (gv instanceof Item) {
                    return (Item)gv;
                }
                this.queue.offer(gv.iterate());
                return this.next();
            }
            return null;
        }

        @Override
        public EnumSet<SequenceIterator.Property> getProperties() {
            return EnumSet.of(SequenceIterator.Property.GROUNDED);
        }

        @Override
        public GroundedValue materialize() {
            return this.thisChain;
        }

        @Override
        public GroundedValue getResidue() throws XPathException {
            return new SequenceExtent(this);
        }

        private static class ChainPosition {
            Chain chain;
            int offset;

            public ChainPosition(Chain chain, int offset) {
                this.chain = chain;
                this.offset = offset;
            }
        }
    }
}


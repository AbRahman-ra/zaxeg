/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.linked.AttributeImpl;
import net.sf.saxon.tree.linked.ElementImpl;

final class AttributeAxisIterator
implements AxisIterator,
LookaheadIterator {
    private final ElementImpl element;
    private final Predicate<? super NodeInfo> nodeTest;
    private NodeInfo next;
    private int index;
    private final int length;

    AttributeAxisIterator(ElementImpl node, Predicate<? super NodeInfo> nodeTest) {
        this.element = node;
        this.nodeTest = nodeTest;
        this.index = 0;
        this.length = node.attributes().size();
        this.advance();
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public NodeInfo next() {
        if (this.next == null) {
            return null;
        }
        NodeInfo current = this.next;
        this.advance();
        return current;
    }

    private void advance() {
        while (true) {
            if (this.index >= this.length) {
                this.next = null;
                return;
            }
            AttributeInfo info = this.element.attributes().itemAt(this.index);
            if (info instanceof AttributeInfo.Deleted) {
                ++this.index;
                continue;
            }
            this.next = new AttributeImpl(this.element, this.index);
            ++this.index;
            if (this.nodeTest.test(this.next)) break;
        }
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD);
    }
}


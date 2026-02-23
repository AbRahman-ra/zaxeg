/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.Iterator;
import java.util.function.Consumer;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.UnfailingIterator;

public interface AxisIterator
extends UnfailingIterator {
    @Override
    public NodeInfo next();

    default public Iterator<NodeInfo> asIterator() {
        return new Iterator<NodeInfo>(){
            NodeInfo next;
            {
                this.next = AxisIterator.this.next();
            }

            @Override
            public boolean hasNext() {
                return this.next != null;
            }

            @Override
            public NodeInfo next() {
                NodeInfo curr = this.next;
                this.next = AxisIterator.this.next();
                return curr;
            }
        };
    }

    default public void forEachNode(Consumer<? super NodeInfo> consumer) {
        NodeInfo item;
        while ((item = this.next()) != null) {
            consumer.accept(item);
        }
    }
}


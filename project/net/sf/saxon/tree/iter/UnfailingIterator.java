/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;

public interface UnfailingIterator
extends SequenceIterator {
    @Override
    public Item next();

    default public void forEach(Consumer<? super Item> consumer) {
        Item item;
        while ((item = this.next()) != null) {
            consumer.accept(item);
        }
    }

    default public List<Item> toList() {
        ArrayList<Item> list = new ArrayList<Item>();
        this.forEach(list::add);
        return list;
    }
}


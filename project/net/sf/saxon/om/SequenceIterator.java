/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.io.Closeable;
import java.util.EnumSet;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.ItemConsumer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;

public interface SequenceIterator
extends Closeable {
    public Item next() throws XPathException;

    @Override
    default public void close() {
    }

    default public EnumSet<Property> getProperties() {
        return EnumSet.noneOf(Property.class);
    }

    default public void forEachOrFail(ItemConsumer<? super Item> consumer) throws XPathException {
        Item item;
        while ((item = this.next()) != null) {
            consumer.accept(item);
        }
    }

    default public GroundedValue materialize() throws XPathException {
        return new SequenceExtent(this).reduce();
    }

    public static enum Property {
        GROUNDED,
        LAST_POSITION_FINDER,
        LOOKAHEAD,
        ATOMIZING;

    }
}


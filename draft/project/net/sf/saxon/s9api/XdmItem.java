/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.Map;
import java.util.stream.Stream;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.streams.XdmStream;
import net.sf.saxon.value.AtomicValue;

public abstract class XdmItem
extends XdmValue {
    protected XdmItem() {
    }

    public XdmItem(Item item) {
        this.setValue(item);
    }

    protected static XdmItem wrapItem(Item item) {
        return item == null ? null : (XdmItem)XdmValue.wrap(item);
    }

    protected static XdmNode wrapItem(NodeInfo item) {
        return item == null ? null : (XdmNode)XdmValue.wrap(item);
    }

    protected static XdmAtomicValue wrapItem(AtomicValue item) {
        return item == null ? null : (XdmAtomicValue)XdmValue.wrap(item);
    }

    @Override
    public Item getUnderlyingValue() {
        return (Item)super.getUnderlyingValue();
    }

    public String getStringValue() {
        return this.getUnderlyingValue().getStringValue();
    }

    public boolean isNode() {
        return this.getUnderlyingValue() instanceof NodeInfo;
    }

    public boolean isAtomicValue() {
        return this.getUnderlyingValue() instanceof AtomicValue;
    }

    @Override
    public int size() {
        return 1;
    }

    public Map<XdmAtomicValue, XdmValue> asMap() {
        return null;
    }

    @Override
    public XdmStream<? extends XdmItem> stream() {
        return new XdmStream<XdmItem>(Stream.of(this));
    }

    public boolean matches(ItemType type) {
        return type.matches(this);
    }
}


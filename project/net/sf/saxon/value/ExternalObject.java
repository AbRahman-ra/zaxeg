/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.om.Item;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;

public interface ExternalObject<T>
extends Item {
    public T getObject();

    public ItemType getItemType(TypeHierarchy var1);
}


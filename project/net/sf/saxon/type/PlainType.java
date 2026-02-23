/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.TypeHierarchy;

public interface PlainType
extends ItemType {
    public StructuredQName getTypeName();

    public boolean isNamespaceSensitive();

    public Iterable<? extends PlainType> getPlainMemberTypes() throws MissingComponentException;

    @Override
    public boolean matches(Item var1, TypeHierarchy var2);

    @Override
    public AtomicType getPrimitiveItemType();
}


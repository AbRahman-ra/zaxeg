/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.ObjectValue;

public class AnyExternalObjectType
implements ItemType {
    public static AnyExternalObjectType THE_INSTANCE = new AnyExternalObjectType();

    protected AnyExternalObjectType() {
    }

    @Override
    public boolean isAtomicType() {
        return false;
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) throws XPathException {
        return item instanceof ObjectValue;
    }

    @Override
    public boolean isPlainType() {
        return false;
    }

    @Override
    public int getPrimitiveType() {
        return -1;
    }

    @Override
    public String getBasicAlphaCode() {
        return "X";
    }

    @Override
    public ItemType getPrimitiveItemType() {
        return this;
    }

    @Override
    public UType getUType() {
        return UType.EXTENSION;
    }

    @Override
    public AtomicType getAtomizedItemType() {
        return BuiltInAtomicType.STRING;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return true;
    }

    @Override
    public Genre getGenre() {
        return Genre.EXTERNAL;
    }

    @Override
    public double getDefaultPriority() {
        return -1.0;
    }
}


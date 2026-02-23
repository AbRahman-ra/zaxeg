/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.List;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public class TypeAliasManager {
    public void registerTypeAlias(StructuredQName name, ItemType type) {
        throw new UnsupportedOperationException();
    }

    public void processDeclaration(ComponentDeclaration declaration) throws XPathException {
        throw new UnsupportedOperationException();
    }

    public void processAllDeclarations(List<ComponentDeclaration> topLevel) throws XPathException {
    }

    public ItemType getItemType(StructuredQName alias) {
        return null;
    }
}


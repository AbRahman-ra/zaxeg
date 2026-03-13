/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.NamespaceBindingSet;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NamespaceResolver;

public class NamespaceDeltaMap
extends NamespaceMap
implements NamespaceBindingSet,
NamespaceResolver {
    private static NamespaceDeltaMap EMPTY_MAP = new NamespaceDeltaMap();

    public static NamespaceDeltaMap emptyMap() {
        return EMPTY_MAP;
    }

    private NamespaceDeltaMap() {
        this.prefixes = new String[0];
        this.uris = new String[0];
    }

    @Override
    protected NamespaceMap newInstance() {
        return new NamespaceDeltaMap();
    }

    @Override
    public boolean allowsNamespaceUndeclarations() {
        return true;
    }

    @Override
    public NamespaceDeltaMap put(String prefix, String uri) {
        return (NamespaceDeltaMap)super.put(prefix, uri);
    }

    @Override
    public NamespaceDeltaMap remove(String prefix) {
        return (NamespaceDeltaMap)super.remove(prefix);
    }
}


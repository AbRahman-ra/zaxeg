/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.NamespaceBinding;

public interface NamespaceBindingSet
extends Iterable<NamespaceBinding> {
    public String getURI(String var1);
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Iterator;

public interface NamespaceResolver {
    public String getURIForPrefix(String var1, boolean var2);

    public Iterator<String> iteratePrefixes();
}


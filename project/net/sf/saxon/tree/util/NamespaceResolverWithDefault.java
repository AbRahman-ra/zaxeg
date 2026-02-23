/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

import java.util.ArrayList;
import java.util.Iterator;
import net.sf.saxon.om.NamespaceResolver;

public class NamespaceResolverWithDefault
implements NamespaceResolver {
    private NamespaceResolver baseResolver;
    private String defaultNamespace;

    public NamespaceResolverWithDefault(NamespaceResolver base, String defaultNamespace) {
        this.baseResolver = base;
        this.defaultNamespace = defaultNamespace;
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (useDefault && prefix.isEmpty()) {
            return this.defaultNamespace;
        }
        return this.baseResolver.getURIForPrefix(prefix, useDefault);
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        ArrayList<String> list = new ArrayList<String>(10);
        Iterator<String> it = this.baseResolver.iteratePrefixes();
        while (it.hasNext()) {
            String p = it.next();
            if (p.length() == 0) continue;
            list.add(p);
        }
        list.add("");
        return list.iterator();
    }
}


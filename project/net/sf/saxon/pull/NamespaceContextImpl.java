/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pull;

import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import net.sf.saxon.om.NamespaceResolver;

public class NamespaceContextImpl
implements NamespaceContext,
NamespaceResolver {
    NamespaceResolver resolver;

    public NamespaceContextImpl(NamespaceResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        return this.resolver.getURIForPrefix(prefix, useDefault);
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        return this.resolver.iteratePrefixes();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix.equals("xmlns")) {
            return "http://www.w3.org/2000/xmlns/";
        }
        return this.resolver.getURIForPrefix(prefix, true);
    }

    @Override
    public String getPrefix(String uri) {
        Iterator<String> prefixes = this.iteratePrefixes();
        while (prefixes.hasNext()) {
            String p = prefixes.next();
            String u = this.resolver.getURIForPrefix(p, true);
            if (!u.equals(uri)) continue;
            return p;
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String uri) {
        ArrayList list = new ArrayList(4);
        Iterator<String> prefixes = this.iteratePrefixes();
        prefixes.forEachRemaining(p -> {
            String u = this.resolver.getURIForPrefix((String)p, true);
            if (u.equals(uri)) {
                list.add(p);
            }
        });
        return list.iterator();
    }
}


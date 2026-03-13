/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Iterator;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.tree.jiter.PairIterator;

public final class DummyNamespaceResolver
implements NamespaceResolver {
    private static final DummyNamespaceResolver THE_INSTANCE = new DummyNamespaceResolver();

    public static DummyNamespaceResolver getInstance() {
        return THE_INSTANCE;
    }

    private DummyNamespaceResolver() {
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (prefix.isEmpty()) {
            return "";
        }
        if ("xml".equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        return "";
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        return new PairIterator<String>("", "xml");
    }
}


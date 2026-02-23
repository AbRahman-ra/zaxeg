/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jaxen.NamespaceContext;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;

public class SimpleNamespaceContext
implements NamespaceContext,
Serializable {
    private static final long serialVersionUID = -808928409643497762L;
    private Map namespaces;

    public SimpleNamespaceContext() {
        this.namespaces = new HashMap();
    }

    public SimpleNamespaceContext(Map namespaces) {
        Iterator entries = namespaces.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = entries.next();
            if (entry.getKey() instanceof String && entry.getValue() instanceof String) continue;
            throw new ClassCastException("Non-string namespace binding");
        }
        this.namespaces = new HashMap(namespaces);
    }

    public void addElementNamespaces(Navigator nav, Object element) throws UnsupportedAxisException {
        Iterator namespaceAxis = nav.getNamespaceAxisIterator(element);
        while (namespaceAxis.hasNext()) {
            Object namespace = namespaceAxis.next();
            String prefix = nav.getNamespacePrefix(namespace);
            String uri = nav.getNamespaceStringValue(namespace);
            if (this.translateNamespacePrefixToUri(prefix) != null) continue;
            this.addNamespace(prefix, uri);
        }
    }

    public void addNamespace(String prefix, String URI2) {
        this.namespaces.put(prefix, URI2);
    }

    public String translateNamespacePrefixToUri(String prefix) {
        if (this.namespaces.containsKey(prefix)) {
            return (String)this.namespaces.get(prefix);
        }
        return null;
    }
}


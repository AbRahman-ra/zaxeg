/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class DOMNamespaceContext
implements NamespaceContext {
    private Map<String, String> namespaceMap = new HashMap<String, String>();

    public DOMNamespaceContext(Node contextNode) {
        this.addNamespaces(contextNode);
    }

    @Override
    public String getNamespaceURI(String arg0) {
        return this.namespaceMap.get(arg0);
    }

    @Override
    public String getPrefix(String arg0) {
        for (String key : this.namespaceMap.keySet()) {
            String value = this.namespaceMap.get(key);
            if (!value.equals(arg0)) continue;
            return key;
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String arg0) {
        return this.namespaceMap.keySet().iterator();
    }

    private void addNamespaces(Node element) {
        if (element.getParentNode() != null) {
            this.addNamespaces(element.getParentNode());
        }
        if (element instanceof Element) {
            Element el = (Element)element;
            NamedNodeMap map = el.getAttributes();
            for (int x = 0; x < map.getLength(); ++x) {
                Attr attr = (Attr)map.item(x);
                if (!"xmlns".equals(attr.getPrefix())) continue;
                this.namespaceMap.put(attr.getLocalName(), attr.getValue());
            }
        }
    }
}


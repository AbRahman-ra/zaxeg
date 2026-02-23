/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms.params;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.transforms.TransformParam;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class InclusiveNamespaces
extends ElementProxy
implements TransformParam {
    public static final String _TAG_EC_INCLUSIVENAMESPACES = "InclusiveNamespaces";
    public static final String _ATT_EC_PREFIXLIST = "PrefixList";
    public static final String ExclusiveCanonicalizationNamespace = "http://www.w3.org/2001/10/xml-exc-c14n#";

    public InclusiveNamespaces(Document doc, String prefixList) {
        this(doc, InclusiveNamespaces.prefixStr2Set(prefixList));
    }

    public InclusiveNamespaces(Document doc, Set<String> prefixes) {
        super(doc);
        TreeSet<String> prefixList = null;
        prefixList = prefixes instanceof SortedSet ? (TreeSet<String>)prefixes : new TreeSet<String>(prefixes);
        StringBuilder sb = new StringBuilder();
        for (String prefix : prefixList) {
            if (prefix.equals("xmlns")) {
                sb.append("#default ");
                continue;
            }
            sb.append(prefix + " ");
        }
        this.constructionElement.setAttributeNS(null, _ATT_EC_PREFIXLIST, sb.toString().trim());
    }

    public InclusiveNamespaces(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public String getInclusiveNamespaces() {
        return this.constructionElement.getAttributeNS(null, _ATT_EC_PREFIXLIST);
    }

    public static SortedSet<String> prefixStr2Set(String inclusiveNamespaces) {
        String[] tokens;
        TreeSet<String> prefixes = new TreeSet<String>();
        if (inclusiveNamespaces == null || inclusiveNamespaces.length() == 0) {
            return prefixes;
        }
        for (String prefix : tokens = inclusiveNamespaces.split("\\s")) {
            if (prefix.equals("#default")) {
                prefixes.add("xmlns");
                continue;
            }
            prefixes.add(prefix);
        }
        return prefixes;
    }

    @Override
    public String getBaseNamespace() {
        return ExclusiveCanonicalizationNamespace;
    }

    @Override
    public String getBaseLocalName() {
        return _TAG_EC_INCLUSIVENAMESPACES;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IdResolver {
    private IdResolver() {
    }

    public static void registerElementById(Element element, Attr id) {
        element.setIdAttributeNode(id, true);
    }

    public static Element getElementById(Document doc, String id) {
        return doc.getElementById(id);
    }
}


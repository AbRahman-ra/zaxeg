/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class EncryptionElementProxy
extends ElementProxy {
    public EncryptionElementProxy(Document doc) {
        super(doc);
    }

    public EncryptionElementProxy(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public final String getBaseNamespace() {
        return "http://www.w3.org/2001/04/xmlenc#";
    }
}


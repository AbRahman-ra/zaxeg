/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class SignatureElementProxy
extends ElementProxy {
    protected SignatureElementProxy() {
    }

    public SignatureElementProxy(Document doc) {
        if (doc == null) {
            throw new RuntimeException("Document is null");
        }
        this.doc = doc;
        this.constructionElement = XMLUtils.createElementInSignatureSpace(this.doc, this.getBaseLocalName());
    }

    public SignatureElementProxy(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public String getBaseNamespace() {
        return "http://www.w3.org/2000/09/xmldsig#";
    }
}


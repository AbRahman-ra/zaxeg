/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class Signature11ElementProxy
extends ElementProxy {
    protected Signature11ElementProxy() {
    }

    public Signature11ElementProxy(Document doc) {
        if (doc == null) {
            throw new RuntimeException("Document is null");
        }
        this.doc = doc;
        this.constructionElement = XMLUtils.createElementInSignature11Space(this.doc, this.getBaseLocalName());
    }

    public Signature11ElementProxy(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public String getBaseNamespace() {
        return "http://www.w3.org/2009/xmldsig11#";
    }
}


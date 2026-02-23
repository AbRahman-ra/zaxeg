/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.algorithms;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class Algorithm
extends SignatureElementProxy {
    public Algorithm(Document doc, String algorithmURI) {
        super(doc);
        this.setAlgorithmURI(algorithmURI);
    }

    public Algorithm(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public String getAlgorithmURI() {
        return this.constructionElement.getAttributeNS(null, "Algorithm");
    }

    protected void setAlgorithmURI(String algorithmURI) {
        if (algorithmURI != null) {
            this.constructionElement.setAttributeNS(null, "Algorithm", algorithmURI);
        }
    }
}


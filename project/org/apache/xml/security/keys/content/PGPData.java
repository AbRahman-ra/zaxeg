/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.KeyInfoContent;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.w3c.dom.Element;

public class PGPData
extends SignatureElementProxy
implements KeyInfoContent {
    public PGPData(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public String getBaseLocalName() {
        return "PGPData";
    }
}


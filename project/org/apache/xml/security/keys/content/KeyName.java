/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.KeyInfoContent;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class KeyName
extends SignatureElementProxy
implements KeyInfoContent {
    public KeyName(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public KeyName(Document doc, String keyName) {
        super(doc);
        this.addText(keyName);
    }

    public String getKeyName() {
        return this.getTextFromTextChild();
    }

    public String getBaseLocalName() {
        return "KeyName";
    }
}


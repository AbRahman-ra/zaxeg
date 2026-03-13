/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.KeyInfoContent;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MgmtData
extends SignatureElementProxy
implements KeyInfoContent {
    public MgmtData(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public MgmtData(Document doc, String mgmtData) {
        super(doc);
        this.addText(mgmtData);
    }

    public String getMgmtData() {
        return this.getTextFromTextChild();
    }

    public String getBaseLocalName() {
        return "MgmtData";
    }
}


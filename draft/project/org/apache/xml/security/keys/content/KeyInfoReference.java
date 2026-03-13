/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.KeyInfoContent;
import org.apache.xml.security.utils.Signature11ElementProxy;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class KeyInfoReference
extends Signature11ElementProxy
implements KeyInfoContent {
    public KeyInfoReference(Element element, String baseURI) throws XMLSecurityException {
        super(element, baseURI);
    }

    public KeyInfoReference(Document doc, String URI2) {
        super(doc);
        this.constructionElement.setAttributeNS(null, "URI", URI2);
    }

    public Attr getURIAttr() {
        return this.constructionElement.getAttributeNodeNS(null, "URI");
    }

    public String getURI() {
        return this.getURIAttr().getNodeValue();
    }

    public void setId(String id) {
        if (id != null) {
            this.constructionElement.setAttributeNS(null, "Id", id);
            this.constructionElement.setIdAttributeNS(null, "Id", true);
        } else {
            this.constructionElement.removeAttributeNS(null, "Id");
        }
    }

    public String getId() {
        return this.constructionElement.getAttributeNS(null, "Id");
    }

    public String getBaseLocalName() {
        return "KeyInfoReference";
    }
}


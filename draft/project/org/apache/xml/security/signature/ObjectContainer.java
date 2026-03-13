/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ObjectContainer
extends SignatureElementProxy {
    public ObjectContainer(Document doc) {
        super(doc);
    }

    public ObjectContainer(Element element, String baseURI) throws XMLSecurityException {
        super(element, baseURI);
    }

    public void setId(String Id2) {
        if (Id2 != null) {
            this.constructionElement.setAttributeNS(null, "Id", Id2);
            this.constructionElement.setIdAttributeNS(null, "Id", true);
        }
    }

    public String getId() {
        return this.constructionElement.getAttributeNS(null, "Id");
    }

    public void setMimeType(String MimeType2) {
        if (MimeType2 != null) {
            this.constructionElement.setAttributeNS(null, "MimeType", MimeType2);
        }
    }

    public String getMimeType() {
        return this.constructionElement.getAttributeNS(null, "MimeType");
    }

    public void setEncoding(String Encoding) {
        if (Encoding != null) {
            this.constructionElement.setAttributeNS(null, "Encoding", Encoding);
        }
    }

    public String getEncoding() {
        return this.constructionElement.getAttributeNS(null, "Encoding");
    }

    public Node appendChild(Node node) {
        return this.constructionElement.appendChild(node);
    }

    public String getBaseLocalName() {
        return "Object";
    }
}


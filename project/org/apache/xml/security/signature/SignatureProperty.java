/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SignatureProperty
extends SignatureElementProxy {
    public SignatureProperty(Document doc, String target) {
        this(doc, target, null);
    }

    public SignatureProperty(Document doc, String target, String id) {
        super(doc);
        this.setTarget(target);
        this.setId(id);
    }

    public SignatureProperty(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public void setId(String id) {
        if (id != null) {
            this.constructionElement.setAttributeNS(null, "Id", id);
            this.constructionElement.setIdAttributeNS(null, "Id", true);
        }
    }

    public String getId() {
        return this.constructionElement.getAttributeNS(null, "Id");
    }

    public void setTarget(String target) {
        if (target != null) {
            this.constructionElement.setAttributeNS(null, "Target", target);
        }
    }

    public String getTarget() {
        return this.constructionElement.getAttributeNS(null, "Target");
    }

    public Node appendChild(Node node) {
        return this.constructionElement.appendChild(node);
    }

    public String getBaseLocalName() {
        return "SignatureProperty";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.SignatureProperty;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SignatureProperties
extends SignatureElementProxy {
    public SignatureProperties(Document doc) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public SignatureProperties(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
        Attr attr = element.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            element.setIdAttributeNode(attr, true);
        }
        int length = this.getLength();
        for (int i = 0; i < length; ++i) {
            Element propertyElem = XMLUtils.selectDsNode(this.constructionElement, "SignatureProperty", i);
            Attr propertyAttr = propertyElem.getAttributeNodeNS(null, "Id");
            if (propertyAttr == null) continue;
            propertyElem.setIdAttributeNode(propertyAttr, true);
        }
    }

    public int getLength() {
        Element[] propertyElems = XMLUtils.selectDsNodes(this.constructionElement, "SignatureProperty");
        return propertyElems.length;
    }

    public SignatureProperty item(int i) throws XMLSignatureException {
        try {
            Element propertyElem = XMLUtils.selectDsNode(this.constructionElement, "SignatureProperty", i);
            if (propertyElem == null) {
                return null;
            }
            return new SignatureProperty(propertyElem, this.baseURI);
        } catch (XMLSecurityException ex) {
            throw new XMLSignatureException("empty", ex);
        }
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

    public void addSignatureProperty(SignatureProperty sp) {
        this.constructionElement.appendChild(sp.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public String getBaseLocalName() {
        return "SignatureProperties";
    }
}


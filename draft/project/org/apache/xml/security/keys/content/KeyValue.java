/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content;

import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.KeyInfoContent;
import org.apache.xml.security.keys.content.keyvalues.DSAKeyValue;
import org.apache.xml.security.keys.content.keyvalues.RSAKeyValue;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class KeyValue
extends SignatureElementProxy
implements KeyInfoContent {
    public KeyValue(Document doc, DSAKeyValue dsaKeyValue) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.constructionElement.appendChild(dsaKeyValue.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public KeyValue(Document doc, RSAKeyValue rsaKeyValue) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.constructionElement.appendChild(rsaKeyValue.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public KeyValue(Document doc, Element unknownKeyValue) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.constructionElement.appendChild(unknownKeyValue);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public KeyValue(Document doc, PublicKey pk) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        if (pk instanceof DSAPublicKey) {
            DSAKeyValue dsa = new DSAKeyValue(this.doc, pk);
            this.constructionElement.appendChild(dsa.getElement());
            XMLUtils.addReturnToElement(this.constructionElement);
        } else if (pk instanceof RSAPublicKey) {
            RSAKeyValue rsa = new RSAKeyValue(this.doc, pk);
            this.constructionElement.appendChild(rsa.getElement());
            XMLUtils.addReturnToElement(this.constructionElement);
        }
    }

    public KeyValue(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public PublicKey getPublicKey() throws XMLSecurityException {
        Element rsa = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "RSAKeyValue", 0);
        if (rsa != null) {
            RSAKeyValue kv = new RSAKeyValue(rsa, this.baseURI);
            return kv.getPublicKey();
        }
        Element dsa = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "DSAKeyValue", 0);
        if (dsa != null) {
            DSAKeyValue kv = new DSAKeyValue(dsa, this.baseURI);
            return kv.getPublicKey();
        }
        return null;
    }

    public String getBaseLocalName() {
        return "KeyValue";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.KeyInfoContent;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RetrievalMethod
extends SignatureElementProxy
implements KeyInfoContent {
    public static final String TYPE_DSA = "http://www.w3.org/2000/09/xmldsig#DSAKeyValue";
    public static final String TYPE_RSA = "http://www.w3.org/2000/09/xmldsig#RSAKeyValue";
    public static final String TYPE_PGP = "http://www.w3.org/2000/09/xmldsig#PGPData";
    public static final String TYPE_SPKI = "http://www.w3.org/2000/09/xmldsig#SPKIData";
    public static final String TYPE_MGMT = "http://www.w3.org/2000/09/xmldsig#MgmtData";
    public static final String TYPE_X509 = "http://www.w3.org/2000/09/xmldsig#X509Data";
    public static final String TYPE_RAWX509 = "http://www.w3.org/2000/09/xmldsig#rawX509Certificate";

    public RetrievalMethod(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public RetrievalMethod(Document doc, String URI2, Transforms transforms, String Type2) {
        super(doc);
        this.constructionElement.setAttributeNS(null, "URI", URI2);
        if (Type2 != null) {
            this.constructionElement.setAttributeNS(null, "Type", Type2);
        }
        if (transforms != null) {
            this.constructionElement.appendChild(transforms.getElement());
            XMLUtils.addReturnToElement(this.constructionElement);
        }
    }

    public Attr getURIAttr() {
        return this.constructionElement.getAttributeNodeNS(null, "URI");
    }

    public String getURI() {
        return this.getURIAttr().getNodeValue();
    }

    public String getType() {
        return this.constructionElement.getAttributeNS(null, "Type");
    }

    public Transforms getTransforms() throws XMLSecurityException {
        try {
            Element transformsElem = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "Transforms", 0);
            if (transformsElem != null) {
                return new Transforms(transformsElem, this.baseURI);
            }
            return null;
        } catch (XMLSignatureException ex) {
            throw new XMLSecurityException("empty", ex);
        }
    }

    public String getBaseLocalName() {
        return "RetrievalMethod";
    }
}


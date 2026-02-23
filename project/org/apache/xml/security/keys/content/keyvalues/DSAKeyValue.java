/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content.keyvalues;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.keyvalues.KeyValueContent;
import org.apache.xml.security.utils.I18n;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DSAKeyValue
extends SignatureElementProxy
implements KeyValueContent {
    public DSAKeyValue(Element element, String baseURI) throws XMLSecurityException {
        super(element, baseURI);
    }

    public DSAKeyValue(Document doc, BigInteger P, BigInteger Q, BigInteger G, BigInteger Y) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.addBigIntegerElement(P, "P");
        this.addBigIntegerElement(Q, "Q");
        this.addBigIntegerElement(G, "G");
        this.addBigIntegerElement(Y, "Y");
    }

    public DSAKeyValue(Document doc, Key key) throws IllegalArgumentException {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        if (!(key instanceof DSAPublicKey)) {
            Object[] exArgs = new Object[]{"DSAKeyValue", key.getClass().getName()};
            throw new IllegalArgumentException(I18n.translate("KeyValue.IllegalArgument", exArgs));
        }
        this.addBigIntegerElement(((DSAPublicKey)key).getParams().getP(), "P");
        this.addBigIntegerElement(((DSAPublicKey)key).getParams().getQ(), "Q");
        this.addBigIntegerElement(((DSAPublicKey)key).getParams().getG(), "G");
        this.addBigIntegerElement(((DSAPublicKey)key).getY(), "Y");
    }

    public PublicKey getPublicKey() throws XMLSecurityException {
        try {
            DSAPublicKeySpec pkspec = new DSAPublicKeySpec(this.getBigIntegerFromChildElement("Y", "http://www.w3.org/2000/09/xmldsig#"), this.getBigIntegerFromChildElement("P", "http://www.w3.org/2000/09/xmldsig#"), this.getBigIntegerFromChildElement("Q", "http://www.w3.org/2000/09/xmldsig#"), this.getBigIntegerFromChildElement("G", "http://www.w3.org/2000/09/xmldsig#"));
            KeyFactory dsaFactory = KeyFactory.getInstance("DSA");
            PublicKey pk = dsaFactory.generatePublic(pkspec);
            return pk;
        } catch (NoSuchAlgorithmException ex) {
            throw new XMLSecurityException("empty", ex);
        } catch (InvalidKeySpecException ex) {
            throw new XMLSecurityException("empty", ex);
        }
    }

    public String getBaseLocalName() {
        return "DSAKeyValue";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content.keyvalues;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.keyvalues.KeyValueContent;
import org.apache.xml.security.utils.I18n;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RSAKeyValue
extends SignatureElementProxy
implements KeyValueContent {
    public RSAKeyValue(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public RSAKeyValue(Document doc, BigInteger modulus, BigInteger exponent) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.addBigIntegerElement(modulus, "Modulus");
        this.addBigIntegerElement(exponent, "Exponent");
    }

    public RSAKeyValue(Document doc, Key key) throws IllegalArgumentException {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        if (!(key instanceof RSAPublicKey)) {
            Object[] exArgs = new Object[]{"RSAKeyValue", key.getClass().getName()};
            throw new IllegalArgumentException(I18n.translate("KeyValue.IllegalArgument", exArgs));
        }
        this.addBigIntegerElement(((RSAPublicKey)key).getModulus(), "Modulus");
        this.addBigIntegerElement(((RSAPublicKey)key).getPublicExponent(), "Exponent");
    }

    public PublicKey getPublicKey() throws XMLSecurityException {
        try {
            KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaKeyspec = new RSAPublicKeySpec(this.getBigIntegerFromChildElement("Modulus", "http://www.w3.org/2000/09/xmldsig#"), this.getBigIntegerFromChildElement("Exponent", "http://www.w3.org/2000/09/xmldsig#"));
            PublicKey pk = rsaFactory.generatePublic(rsaKeyspec);
            return pk;
        } catch (NoSuchAlgorithmException ex) {
            throw new XMLSecurityException("empty", ex);
        } catch (InvalidKeySpecException ex) {
            throw new XMLSecurityException("empty", ex);
        }
    }

    public String getBaseLocalName() {
        return "RSAKeyValue";
    }
}


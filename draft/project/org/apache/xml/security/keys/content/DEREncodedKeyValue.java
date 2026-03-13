/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.KeyInfoContent;
import org.apache.xml.security.utils.Signature11ElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DEREncodedKeyValue
extends Signature11ElementProxy
implements KeyInfoContent {
    public static final String[] supportedKeyTypes = new String[]{"RSA", "DSA", "EC"};

    public DEREncodedKeyValue(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public DEREncodedKeyValue(Document doc, PublicKey publicKey) throws XMLSecurityException {
        super(doc);
        this.addBase64Text(this.getEncodedDER(publicKey));
    }

    public DEREncodedKeyValue(Document doc, byte[] encodedKey) {
        super(doc);
        this.addBase64Text(encodedKey);
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
        return "DEREncodedKeyValue";
    }

    public PublicKey getPublicKey() throws XMLSecurityException {
        byte[] encodedKey = this.getBytesFromTextChild();
        for (String keyType : supportedKeyTypes) {
            try {
                KeyFactory keyFactory = KeyFactory.getInstance(keyType);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
                PublicKey publicKey = keyFactory.generatePublic(keySpec);
                if (publicKey == null) continue;
                return publicKey;
            } catch (NoSuchAlgorithmException e) {
            } catch (InvalidKeySpecException e) {
                // empty catch block
            }
        }
        throw new XMLSecurityException("DEREncodedKeyValue.UnsupportedEncodedKey");
    }

    protected byte[] getEncodedDER(PublicKey publicKey) throws XMLSecurityException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(publicKey.getAlgorithm());
            X509EncodedKeySpec keySpec = keyFactory.getKeySpec(publicKey, X509EncodedKeySpec.class);
            return keySpec.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            Object[] exArgs = new Object[]{publicKey.getAlgorithm(), publicKey.getFormat(), publicKey.getClass().getName()};
            throw new XMLSecurityException("DEREncodedKeyValue.UnsupportedPublicKey", exArgs, e);
        } catch (InvalidKeySpecException e) {
            Object[] exArgs = new Object[]{publicKey.getAlgorithm(), publicKey.getFormat(), publicKey.getClass().getName()};
            throw new XMLSecurityException("DEREncodedKeyValue.UnsupportedPublicKey", exArgs, e);
        }
    }
}


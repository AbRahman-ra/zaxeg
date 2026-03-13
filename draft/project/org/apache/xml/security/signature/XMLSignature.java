/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import javax.crypto.SecretKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.signature.Manifest;
import org.apache.xml.security.signature.ObjectContainer;
import org.apache.xml.security.signature.SignatureProperties;
import org.apache.xml.security.signature.SignedInfo;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.I18n;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.SignerOutputStream;
import org.apache.xml.security.utils.UnsyncBufferedOutputStream;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public final class XMLSignature
extends SignatureElementProxy {
    public static final String ALGO_ID_MAC_HMAC_SHA1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";
    public static final String ALGO_ID_SIGNATURE_DSA = "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
    public static final String ALGO_ID_SIGNATURE_DSA_SHA256 = "http://www.w3.org/2009/xmldsig11#dsa-sha256";
    public static final String ALGO_ID_SIGNATURE_RSA = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    public static final String ALGO_ID_SIGNATURE_RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    public static final String ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5 = "http://www.w3.org/2001/04/xmldsig-more#rsa-md5";
    public static final String ALGO_ID_SIGNATURE_RSA_RIPEMD160 = "http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160";
    public static final String ALGO_ID_SIGNATURE_RSA_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha224";
    public static final String ALGO_ID_SIGNATURE_RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    public static final String ALGO_ID_SIGNATURE_RSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384";
    public static final String ALGO_ID_SIGNATURE_RSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
    public static final String ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5 = "http://www.w3.org/2001/04/xmldsig-more#hmac-md5";
    public static final String ALGO_ID_MAC_HMAC_RIPEMD160 = "http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160";
    public static final String ALGO_ID_MAC_HMAC_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha224";
    public static final String ALGO_ID_MAC_HMAC_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha256";
    public static final String ALGO_ID_MAC_HMAC_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha384";
    public static final String ALGO_ID_MAC_HMAC_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha512";
    public static final String ALGO_ID_SIGNATURE_ECDSA_SHA1 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1";
    public static final String ALGO_ID_SIGNATURE_ECDSA_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224";
    public static final String ALGO_ID_SIGNATURE_ECDSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";
    public static final String ALGO_ID_SIGNATURE_ECDSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384";
    public static final String ALGO_ID_SIGNATURE_ECDSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512";
    private static Log log = LogFactory.getLog(XMLSignature.class);
    private SignedInfo signedInfo;
    private KeyInfo keyInfo;
    private boolean followManifestsDuringValidation = false;
    private Element signatureValueElement;
    private static final int MODE_SIGN = 0;
    private static final int MODE_VERIFY = 1;
    private int state = 0;

    public XMLSignature(Document doc, String baseURI, String signatureMethodURI) throws XMLSecurityException {
        this(doc, baseURI, signatureMethodURI, 0, "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
    }

    public XMLSignature(Document doc, String baseURI, String signatureMethodURI, int hmacOutputLength) throws XMLSecurityException {
        this(doc, baseURI, signatureMethodURI, hmacOutputLength, "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
    }

    public XMLSignature(Document doc, String baseURI, String signatureMethodURI, String canonicalizationMethodURI) throws XMLSecurityException {
        this(doc, baseURI, signatureMethodURI, 0, canonicalizationMethodURI);
    }

    public XMLSignature(Document doc, String baseURI, String signatureMethodURI, int hmacOutputLength, String canonicalizationMethodURI) throws XMLSecurityException {
        super(doc);
        String xmlnsDsPrefix = XMLSignature.getDefaultPrefix("http://www.w3.org/2000/09/xmldsig#");
        if (xmlnsDsPrefix == null || xmlnsDsPrefix.length() == 0) {
            this.constructionElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.w3.org/2000/09/xmldsig#");
        } else {
            this.constructionElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + xmlnsDsPrefix, "http://www.w3.org/2000/09/xmldsig#");
        }
        XMLUtils.addReturnToElement(this.constructionElement);
        this.baseURI = baseURI;
        this.signedInfo = new SignedInfo(this.doc, signatureMethodURI, hmacOutputLength, canonicalizationMethodURI);
        this.constructionElement.appendChild(this.signedInfo.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
        this.signatureValueElement = XMLUtils.createElementInSignatureSpace(this.doc, "SignatureValue");
        this.constructionElement.appendChild(this.signatureValueElement);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public XMLSignature(Document doc, String baseURI, Element SignatureMethodElem, Element CanonicalizationMethodElem) throws XMLSecurityException {
        super(doc);
        String xmlnsDsPrefix = XMLSignature.getDefaultPrefix("http://www.w3.org/2000/09/xmldsig#");
        if (xmlnsDsPrefix == null || xmlnsDsPrefix.length() == 0) {
            this.constructionElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.w3.org/2000/09/xmldsig#");
        } else {
            this.constructionElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + xmlnsDsPrefix, "http://www.w3.org/2000/09/xmldsig#");
        }
        XMLUtils.addReturnToElement(this.constructionElement);
        this.baseURI = baseURI;
        this.signedInfo = new SignedInfo(this.doc, SignatureMethodElem, CanonicalizationMethodElem);
        this.constructionElement.appendChild(this.signedInfo.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
        this.signatureValueElement = XMLUtils.createElementInSignatureSpace(this.doc, "SignatureValue");
        this.constructionElement.appendChild(this.signatureValueElement);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public XMLSignature(Element element, String baseURI) throws XMLSignatureException, XMLSecurityException {
        this(element, baseURI, false);
    }

    public XMLSignature(Element element, String baseURI, boolean secureValidation) throws XMLSignatureException, XMLSecurityException {
        super(element, baseURI);
        Element keyInfoElem;
        Element signedInfoElem = XMLUtils.getNextElement(element.getFirstChild());
        if (signedInfoElem == null) {
            Object[] exArgs = new Object[]{"SignedInfo", "Signature"};
            throw new XMLSignatureException("xml.WrongContent", exArgs);
        }
        this.signedInfo = new SignedInfo(signedInfoElem, baseURI, secureValidation);
        signedInfoElem = XMLUtils.getNextElement(element.getFirstChild());
        this.signatureValueElement = XMLUtils.getNextElement(signedInfoElem.getNextSibling());
        if (this.signatureValueElement == null) {
            Object[] exArgs = new Object[]{"SignatureValue", "Signature"};
            throw new XMLSignatureException("xml.WrongContent", exArgs);
        }
        Attr signatureValueAttr = this.signatureValueElement.getAttributeNodeNS(null, "Id");
        if (signatureValueAttr != null) {
            this.signatureValueElement.setIdAttributeNode(signatureValueAttr, true);
        }
        if ((keyInfoElem = XMLUtils.getNextElement(this.signatureValueElement.getNextSibling())) != null && "http://www.w3.org/2000/09/xmldsig#".equals(keyInfoElem.getNamespaceURI()) && "KeyInfo".equals(keyInfoElem.getLocalName())) {
            this.keyInfo = new KeyInfo(keyInfoElem, baseURI);
            this.keyInfo.setSecureValidation(secureValidation);
        }
        Element objectElem = XMLUtils.getNextElement(this.signatureValueElement.getNextSibling());
        while (objectElem != null) {
            Attr objectAttr = objectElem.getAttributeNodeNS(null, "Id");
            if (objectAttr != null) {
                objectElem.setIdAttributeNode(objectAttr, true);
            }
            for (Node firstChild = objectElem.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                if (firstChild.getNodeType() != 1) continue;
                Element childElem = (Element)firstChild;
                String tag = childElem.getLocalName();
                if (tag.equals("Manifest")) {
                    new Manifest(childElem, baseURI);
                    continue;
                }
                if (!tag.equals("SignatureProperties")) continue;
                new SignatureProperties(childElem, baseURI);
            }
            objectElem = XMLUtils.getNextElement(objectElem.getNextSibling());
        }
        this.state = 1;
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

    public SignedInfo getSignedInfo() {
        return this.signedInfo;
    }

    public byte[] getSignatureValue() throws XMLSignatureException {
        try {
            return Base64.decode(this.signatureValueElement);
        } catch (Base64DecodingException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    private void setSignatureValueElement(byte[] bytes) {
        while (this.signatureValueElement.hasChildNodes()) {
            this.signatureValueElement.removeChild(this.signatureValueElement.getFirstChild());
        }
        String base64codedValue = Base64.encode(bytes);
        if (base64codedValue.length() > 76 && !XMLUtils.ignoreLineBreaks()) {
            base64codedValue = "\n" + base64codedValue + "\n";
        }
        Text t = this.doc.createTextNode(base64codedValue);
        this.signatureValueElement.appendChild(t);
    }

    public KeyInfo getKeyInfo() {
        if (this.state == 0 && this.keyInfo == null) {
            this.keyInfo = new KeyInfo(this.doc);
            Element keyInfoElement = this.keyInfo.getElement();
            Element firstObject = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "Object", 0);
            if (firstObject != null) {
                this.constructionElement.insertBefore(keyInfoElement, firstObject);
                XMLUtils.addReturnBeforeChild(this.constructionElement, firstObject);
            } else {
                this.constructionElement.appendChild(keyInfoElement);
                XMLUtils.addReturnToElement(this.constructionElement);
            }
        }
        return this.keyInfo;
    }

    public void appendObject(ObjectContainer object) throws XMLSignatureException {
        this.constructionElement.appendChild(object.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public ObjectContainer getObjectItem(int i) {
        Element objElem = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "Object", i);
        try {
            return new ObjectContainer(objElem, this.baseURI);
        } catch (XMLSecurityException ex) {
            return null;
        }
    }

    public int getObjectLength() {
        return this.length("http://www.w3.org/2000/09/xmldsig#", "Object");
    }

    public void sign(Key signingKey) throws XMLSignatureException {
        if (signingKey instanceof PublicKey) {
            throw new IllegalArgumentException(I18n.translate("algorithms.operationOnlyVerification"));
        }
        try {
            SignedInfo si = this.getSignedInfo();
            SignatureAlgorithm sa = si.getSignatureAlgorithm();
            OutputStream so = null;
            try {
                sa.initSign(signingKey);
                si.generateDigestValues();
                so = new UnsyncBufferedOutputStream(new SignerOutputStream(sa));
                si.signInOctetStream(so);
            } catch (XMLSecurityException ex) {
                throw ex;
            } finally {
                block17: {
                    if (so != null) {
                        try {
                            so.close();
                        } catch (IOException ex) {
                            if (!log.isDebugEnabled()) break block17;
                            log.debug(ex);
                        }
                    }
                }
            }
            this.setSignatureValueElement(sa.sign());
        } catch (XMLSignatureException ex) {
            throw ex;
        } catch (CanonicalizationException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (InvalidCanonicalizerException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (XMLSecurityException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    public void addResourceResolver(ResourceResolver resolver) {
        this.getSignedInfo().addResourceResolver(resolver);
    }

    public void addResourceResolver(ResourceResolverSpi resolver) {
        this.getSignedInfo().addResourceResolver(resolver);
    }

    public boolean checkSignatureValue(X509Certificate cert) throws XMLSignatureException {
        if (cert != null) {
            return this.checkSignatureValue(cert.getPublicKey());
        }
        Object[] exArgs = new Object[]{"Didn't get a certificate"};
        throw new XMLSignatureException("empty", exArgs);
    }

    public boolean checkSignatureValue(Key pk) throws XMLSignatureException {
        if (pk == null) {
            Object[] exArgs = new Object[]{"Didn't get a key"};
            throw new XMLSignatureException("empty", exArgs);
        }
        try {
            SignedInfo si = this.getSignedInfo();
            SignatureAlgorithm sa = si.getSignatureAlgorithm();
            if (log.isDebugEnabled()) {
                log.debug("signatureMethodURI = " + sa.getAlgorithmURI());
                log.debug("jceSigAlgorithm    = " + sa.getJCEAlgorithmString());
                log.debug("jceSigProvider     = " + sa.getJCEProviderName());
                log.debug("PublicKey = " + pk);
            }
            byte[] sigBytes = null;
            try {
                sa.initVerify(pk);
                SignerOutputStream so = new SignerOutputStream(sa);
                UnsyncBufferedOutputStream bos = new UnsyncBufferedOutputStream(so);
                si.signInOctetStream(bos);
                ((OutputStream)bos).close();
                sigBytes = this.getSignatureValue();
            } catch (IOException ex) {
                if (log.isDebugEnabled()) {
                    log.debug(ex);
                }
            } catch (XMLSecurityException ex) {
                throw ex;
            }
            if (!sa.verify(sigBytes)) {
                log.warn("Signature verification failed.");
                return false;
            }
            return si.verify(this.followManifestsDuringValidation);
        } catch (XMLSignatureException ex) {
            throw ex;
        } catch (XMLSecurityException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    public void addDocument(String referenceURI, Transforms trans, String digestURI, String referenceId, String referenceType) throws XMLSignatureException {
        this.signedInfo.addDocument(this.baseURI, referenceURI, trans, digestURI, referenceId, referenceType);
    }

    public void addDocument(String referenceURI, Transforms trans, String digestURI) throws XMLSignatureException {
        this.signedInfo.addDocument(this.baseURI, referenceURI, trans, digestURI, null, null);
    }

    public void addDocument(String referenceURI, Transforms trans) throws XMLSignatureException {
        this.signedInfo.addDocument(this.baseURI, referenceURI, trans, "http://www.w3.org/2000/09/xmldsig#sha1", null, null);
    }

    public void addDocument(String referenceURI) throws XMLSignatureException {
        this.signedInfo.addDocument(this.baseURI, referenceURI, null, "http://www.w3.org/2000/09/xmldsig#sha1", null, null);
    }

    public void addKeyInfo(X509Certificate cert) throws XMLSecurityException {
        X509Data x509data = new X509Data(this.doc);
        x509data.addCertificate(cert);
        this.getKeyInfo().add(x509data);
    }

    public void addKeyInfo(PublicKey pk) {
        this.getKeyInfo().add(pk);
    }

    public SecretKey createSecretKey(byte[] secretKeyBytes) {
        return this.getSignedInfo().createSecretKey(secretKeyBytes);
    }

    public void setFollowNestedManifests(boolean followManifests) {
        this.followManifestsDuringValidation = followManifests;
    }

    public String getBaseLocalName() {
        return "Signature";
    }
}


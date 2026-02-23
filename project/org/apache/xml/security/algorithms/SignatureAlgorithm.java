/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.algorithms;

import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.algorithms.Algorithm;
import org.apache.xml.security.algorithms.SignatureAlgorithmSpi;
import org.apache.xml.security.algorithms.implementations.IntegrityHmac;
import org.apache.xml.security.algorithms.implementations.SignatureBaseRSA;
import org.apache.xml.security.algorithms.implementations.SignatureDSA;
import org.apache.xml.security.algorithms.implementations.SignatureECDSA;
import org.apache.xml.security.exceptions.AlgorithmAlreadyRegisteredException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.ClassLoaderUtils;
import org.apache.xml.security.utils.JavaUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class SignatureAlgorithm
extends Algorithm {
    private static Log log = LogFactory.getLog(SignatureAlgorithm.class);
    private static Map<String, Class<? extends SignatureAlgorithmSpi>> algorithmHash = new ConcurrentHashMap<String, Class<? extends SignatureAlgorithmSpi>>();
    private final SignatureAlgorithmSpi signatureAlgorithm;
    private final String algorithmURI;

    public SignatureAlgorithm(Document doc, String algorithmURI) throws XMLSecurityException {
        super(doc, algorithmURI);
        this.algorithmURI = algorithmURI;
        this.signatureAlgorithm = SignatureAlgorithm.getSignatureAlgorithmSpi(algorithmURI);
        this.signatureAlgorithm.engineGetContextFromElement(this.constructionElement);
    }

    public SignatureAlgorithm(Document doc, String algorithmURI, int hmacOutputLength) throws XMLSecurityException {
        super(doc, algorithmURI);
        this.algorithmURI = algorithmURI;
        this.signatureAlgorithm = SignatureAlgorithm.getSignatureAlgorithmSpi(algorithmURI);
        this.signatureAlgorithm.engineGetContextFromElement(this.constructionElement);
        this.signatureAlgorithm.engineSetHMACOutputLength(hmacOutputLength);
        ((IntegrityHmac)this.signatureAlgorithm).engineAddContextToElement(this.constructionElement);
    }

    public SignatureAlgorithm(Element element, String baseURI) throws XMLSecurityException {
        this(element, baseURI, false);
    }

    public SignatureAlgorithm(Element element, String baseURI, boolean secureValidation) throws XMLSecurityException {
        super(element, baseURI);
        this.algorithmURI = this.getURI();
        Attr attr = element.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            element.setIdAttributeNode(attr, true);
        }
        if (secureValidation && ("http://www.w3.org/2001/04/xmldsig-more#hmac-md5".equals(this.algorithmURI) || "http://www.w3.org/2001/04/xmldsig-more#rsa-md5".equals(this.algorithmURI))) {
            Object[] exArgs = new Object[]{this.algorithmURI};
            throw new XMLSecurityException("signature.signatureAlgorithm", exArgs);
        }
        this.signatureAlgorithm = SignatureAlgorithm.getSignatureAlgorithmSpi(this.algorithmURI);
        this.signatureAlgorithm.engineGetContextFromElement(this.constructionElement);
    }

    private static SignatureAlgorithmSpi getSignatureAlgorithmSpi(String algorithmURI) throws XMLSignatureException {
        try {
            Class<? extends SignatureAlgorithmSpi> implementingClass = algorithmHash.get(algorithmURI);
            if (log.isDebugEnabled()) {
                log.debug("Create URI \"" + algorithmURI + "\" class \"" + implementingClass + "\"");
            }
            return implementingClass.newInstance();
        } catch (IllegalAccessException ex) {
            Object[] exArgs = new Object[]{algorithmURI, ex.getMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs, ex);
        } catch (InstantiationException ex) {
            Object[] exArgs = new Object[]{algorithmURI, ex.getMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs, ex);
        } catch (NullPointerException ex) {
            Object[] exArgs = new Object[]{algorithmURI, ex.getMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs, ex);
        }
    }

    public byte[] sign() throws XMLSignatureException {
        return this.signatureAlgorithm.engineSign();
    }

    public String getJCEAlgorithmString() {
        return this.signatureAlgorithm.engineGetJCEAlgorithmString();
    }

    public String getJCEProviderName() {
        return this.signatureAlgorithm.engineGetJCEProviderName();
    }

    public void update(byte[] input) throws XMLSignatureException {
        this.signatureAlgorithm.engineUpdate(input);
    }

    public void update(byte input) throws XMLSignatureException {
        this.signatureAlgorithm.engineUpdate(input);
    }

    public void update(byte[] buf, int offset, int len) throws XMLSignatureException {
        this.signatureAlgorithm.engineUpdate(buf, offset, len);
    }

    public void initSign(Key signingKey) throws XMLSignatureException {
        this.signatureAlgorithm.engineInitSign(signingKey);
    }

    public void initSign(Key signingKey, SecureRandom secureRandom) throws XMLSignatureException {
        this.signatureAlgorithm.engineInitSign(signingKey, secureRandom);
    }

    public void initSign(Key signingKey, AlgorithmParameterSpec algorithmParameterSpec) throws XMLSignatureException {
        this.signatureAlgorithm.engineInitSign(signingKey, algorithmParameterSpec);
    }

    public void setParameter(AlgorithmParameterSpec params) throws XMLSignatureException {
        this.signatureAlgorithm.engineSetParameter(params);
    }

    public void initVerify(Key verificationKey) throws XMLSignatureException {
        this.signatureAlgorithm.engineInitVerify(verificationKey);
    }

    public boolean verify(byte[] signature) throws XMLSignatureException {
        return this.signatureAlgorithm.engineVerify(signature);
    }

    public final String getURI() {
        return this.constructionElement.getAttributeNS(null, "Algorithm");
    }

    public static void register(String algorithmURI, String implementingClass) throws AlgorithmAlreadyRegisteredException, ClassNotFoundException, XMLSignatureException {
        Class<? extends SignatureAlgorithmSpi> registeredClass;
        JavaUtils.checkRegisterPermission();
        if (log.isDebugEnabled()) {
            log.debug("Try to register " + algorithmURI + " " + implementingClass);
        }
        if ((registeredClass = algorithmHash.get(algorithmURI)) != null) {
            Object[] exArgs = new Object[]{algorithmURI, registeredClass};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered", exArgs);
        }
        try {
            Class<?> clazz = ClassLoaderUtils.loadClass(implementingClass, SignatureAlgorithm.class);
            algorithmHash.put(algorithmURI, clazz);
        } catch (NullPointerException ex) {
            Object[] exArgs = new Object[]{algorithmURI, ex.getMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs, ex);
        }
    }

    public static void register(String algorithmURI, Class<? extends SignatureAlgorithmSpi> implementingClass) throws AlgorithmAlreadyRegisteredException, ClassNotFoundException, XMLSignatureException {
        Class<? extends SignatureAlgorithmSpi> registeredClass;
        JavaUtils.checkRegisterPermission();
        if (log.isDebugEnabled()) {
            log.debug("Try to register " + algorithmURI + " " + implementingClass);
        }
        if ((registeredClass = algorithmHash.get(algorithmURI)) != null) {
            Object[] exArgs = new Object[]{algorithmURI, registeredClass};
            throw new AlgorithmAlreadyRegisteredException("algorithm.alreadyRegistered", exArgs);
        }
        algorithmHash.put(algorithmURI, implementingClass);
    }

    public static void registerDefaultAlgorithms() {
        algorithmHash.put("http://www.w3.org/2000/09/xmldsig#dsa-sha1", SignatureDSA.class);
        algorithmHash.put("http://www.w3.org/2009/xmldsig11#dsa-sha256", SignatureDSA.SHA256.class);
        algorithmHash.put("http://www.w3.org/2000/09/xmldsig#rsa-sha1", SignatureBaseRSA.SignatureRSASHA1.class);
        algorithmHash.put("http://www.w3.org/2000/09/xmldsig#hmac-sha1", IntegrityHmac.IntegrityHmacSHA1.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#rsa-md5", SignatureBaseRSA.SignatureRSAMD5.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160", SignatureBaseRSA.SignatureRSARIPEMD160.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha224", SignatureBaseRSA.SignatureRSASHA224.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", SignatureBaseRSA.SignatureRSASHA256.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", SignatureBaseRSA.SignatureRSASHA384.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", SignatureBaseRSA.SignatureRSASHA512.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1", SignatureECDSA.SignatureECDSASHA1.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224", SignatureECDSA.SignatureECDSASHA224.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", SignatureECDSA.SignatureECDSASHA256.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384", SignatureECDSA.SignatureECDSASHA384.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512", SignatureECDSA.SignatureECDSASHA512.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#hmac-md5", IntegrityHmac.IntegrityHmacMD5.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160", IntegrityHmac.IntegrityHmacRIPEMD160.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#hmac-sha224", IntegrityHmac.IntegrityHmacSHA224.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256", IntegrityHmac.IntegrityHmacSHA256.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#hmac-sha384", IntegrityHmac.IntegrityHmacSHA384.class);
        algorithmHash.put("http://www.w3.org/2001/04/xmldsig-more#hmac-sha512", IntegrityHmac.IntegrityHmacSHA512.class);
    }

    @Override
    public String getBaseNamespace() {
        return "http://www.w3.org/2000/09/xmldsig#";
    }

    @Override
    public String getBaseLocalName() {
        return "SignatureMethod";
    }
}


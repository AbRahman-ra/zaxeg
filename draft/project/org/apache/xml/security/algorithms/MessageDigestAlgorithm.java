/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.algorithms;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import org.apache.xml.security.algorithms.Algorithm;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.signature.XMLSignatureException;
import org.w3c.dom.Document;

public class MessageDigestAlgorithm
extends Algorithm {
    public static final String ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5 = "http://www.w3.org/2001/04/xmldsig-more#md5";
    public static final String ALGO_ID_DIGEST_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
    public static final String ALGO_ID_DIGEST_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#sha224";
    public static final String ALGO_ID_DIGEST_SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    public static final String ALGO_ID_DIGEST_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#sha384";
    public static final String ALGO_ID_DIGEST_SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
    public static final String ALGO_ID_DIGEST_RIPEMD160 = "http://www.w3.org/2001/04/xmlenc#ripemd160";
    private final MessageDigest algorithm;

    private MessageDigestAlgorithm(Document doc, String algorithmURI) throws XMLSignatureException {
        super(doc, algorithmURI);
        this.algorithm = MessageDigestAlgorithm.getDigestInstance(algorithmURI);
    }

    public static MessageDigestAlgorithm getInstance(Document doc, String algorithmURI) throws XMLSignatureException {
        return new MessageDigestAlgorithm(doc, algorithmURI);
    }

    private static MessageDigest getDigestInstance(String algorithmURI) throws XMLSignatureException {
        MessageDigest md;
        String algorithmID = JCEMapper.translateURItoJCEID(algorithmURI);
        if (algorithmID == null) {
            Object[] exArgs = new Object[]{algorithmURI};
            throw new XMLSignatureException("algorithms.NoSuchMap", exArgs);
        }
        String provider = JCEMapper.getProviderId();
        try {
            md = provider == null ? MessageDigest.getInstance(algorithmID) : MessageDigest.getInstance(algorithmID, provider);
        } catch (NoSuchAlgorithmException ex) {
            Object[] exArgs = new Object[]{algorithmID, ex.getLocalizedMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        } catch (NoSuchProviderException ex) {
            Object[] exArgs = new Object[]{algorithmID, ex.getLocalizedMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        }
        return md;
    }

    public MessageDigest getAlgorithm() {
        return this.algorithm;
    }

    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        return MessageDigest.isEqual(digesta, digestb);
    }

    public byte[] digest() {
        return this.algorithm.digest();
    }

    public byte[] digest(byte[] input) {
        return this.algorithm.digest(input);
    }

    public int digest(byte[] buf, int offset, int len) throws DigestException {
        return this.algorithm.digest(buf, offset, len);
    }

    public String getJCEAlgorithmString() {
        return this.algorithm.getAlgorithm();
    }

    public Provider getJCEProvider() {
        return this.algorithm.getProvider();
    }

    public int getDigestLength() {
        return this.algorithm.getDigestLength();
    }

    public void reset() {
        this.algorithm.reset();
    }

    public void update(byte[] input) {
        this.algorithm.update(input);
    }

    public void update(byte input) {
        this.algorithm.update(input);
    }

    public void update(byte[] buf, int offset, int len) {
        this.algorithm.update(buf, offset, len);
    }

    public String getBaseNamespace() {
        return "http://www.w3.org/2000/09/xmldsig#";
    }

    public String getBaseLocalName() {
        return "DigestMethod";
    }
}


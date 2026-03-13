/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.algorithms.implementations;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.algorithms.SignatureAlgorithmSpi;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Base64;

public abstract class SignatureECDSA
extends SignatureAlgorithmSpi {
    private static Log log = LogFactory.getLog(SignatureECDSA.class);
    private Signature signatureAlgorithm = null;

    public abstract String engineGetURI();

    public static byte[] convertASN1toXMLDSIG(byte[] asn1Bytes) throws IOException {
        int sLength;
        int j;
        int rLength;
        int i;
        int offset;
        if (asn1Bytes.length < 8 || asn1Bytes[0] != 48) {
            throw new IOException("Invalid ASN.1 format of ECDSA signature");
        }
        if (asn1Bytes[1] > 0) {
            offset = 2;
        } else if (asn1Bytes[1] == -127) {
            offset = 3;
        } else {
            throw new IOException("Invalid ASN.1 format of ECDSA signature");
        }
        for (i = rLength = asn1Bytes[offset + 1]; i > 0 && asn1Bytes[offset + 2 + rLength - i] == 0; --i) {
        }
        for (j = sLength = asn1Bytes[offset + 2 + rLength + 1]; j > 0 && asn1Bytes[offset + 2 + rLength + 2 + sLength - j] == 0; --j) {
        }
        int rawLen = Math.max(i, j);
        if ((asn1Bytes[offset - 1] & 0xFF) != asn1Bytes.length - offset || (asn1Bytes[offset - 1] & 0xFF) != 2 + rLength + 2 + sLength || asn1Bytes[offset] != 2 || asn1Bytes[offset + 2 + rLength] != 2) {
            throw new IOException("Invalid ASN.1 format of ECDSA signature");
        }
        byte[] xmldsigBytes = new byte[2 * rawLen];
        System.arraycopy(asn1Bytes, offset + 2 + rLength - i, xmldsigBytes, rawLen - i, i);
        System.arraycopy(asn1Bytes, offset + 2 + rLength + 2 + sLength - j, xmldsigBytes, 2 * rawLen - j, j);
        return xmldsigBytes;
    }

    public static byte[] convertXMLDSIGtoASN1(byte[] xmldsigBytes) throws IOException {
        int offset;
        byte[] asn1Bytes;
        int len;
        int k;
        int rawLen;
        int i;
        for (i = rawLen = xmldsigBytes.length / 2; i > 0 && xmldsigBytes[rawLen - i] == 0; --i) {
        }
        int j = i;
        if (xmldsigBytes[rawLen - i] < 0) {
            ++j;
        }
        for (k = rawLen; k > 0 && xmldsigBytes[2 * rawLen - k] == 0; --k) {
        }
        int l = k;
        if (xmldsigBytes[2 * rawLen - k] < 0) {
            ++l;
        }
        if ((len = 2 + j + 2 + l) > 255) {
            throw new IOException("Invalid XMLDSIG format of ECDSA signature");
        }
        if (len < 128) {
            asn1Bytes = new byte[4 + j + 2 + l];
            offset = 1;
        } else {
            asn1Bytes = new byte[5 + j + 2 + l];
            asn1Bytes[1] = -127;
            offset = 2;
        }
        asn1Bytes[0] = 48;
        asn1Bytes[offset++] = (byte)len;
        asn1Bytes[offset++] = 2;
        asn1Bytes[offset++] = (byte)j;
        System.arraycopy(xmldsigBytes, rawLen - i, asn1Bytes, offset + j - i, i);
        offset += j;
        asn1Bytes[offset++] = 2;
        asn1Bytes[offset++] = (byte)l;
        System.arraycopy(xmldsigBytes, 2 * rawLen - k, asn1Bytes, offset + l - k, k);
        return asn1Bytes;
    }

    public SignatureECDSA() throws XMLSignatureException {
        String algorithmID = JCEMapper.translateURItoJCEID(this.engineGetURI());
        if (log.isDebugEnabled()) {
            log.debug("Created SignatureECDSA using " + algorithmID);
        }
        String provider = JCEMapper.getProviderId();
        try {
            this.signatureAlgorithm = provider == null ? Signature.getInstance(algorithmID) : Signature.getInstance(algorithmID, provider);
        } catch (NoSuchAlgorithmException ex) {
            Object[] exArgs = new Object[]{algorithmID, ex.getLocalizedMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        } catch (NoSuchProviderException ex) {
            Object[] exArgs = new Object[]{algorithmID, ex.getLocalizedMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        }
    }

    protected void engineSetParameter(AlgorithmParameterSpec params) throws XMLSignatureException {
        try {
            this.signatureAlgorithm.setParameter(params);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected boolean engineVerify(byte[] signature) throws XMLSignatureException {
        try {
            byte[] jcebytes = SignatureECDSA.convertXMLDSIGtoASN1(signature);
            if (log.isDebugEnabled()) {
                log.debug("Called ECDSA.verify() on " + Base64.encode(signature));
            }
            return this.signatureAlgorithm.verify(jcebytes);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (IOException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineInitVerify(Key publicKey) throws XMLSignatureException {
        if (!(publicKey instanceof PublicKey)) {
            String supplied = null;
            if (publicKey != null) {
                supplied = publicKey.getClass().getName();
            }
            String needed = PublicKey.class.getName();
            Object[] exArgs = new Object[]{supplied, needed};
            throw new XMLSignatureException("algorithms.WrongKeyForThisOperation", exArgs);
        }
        try {
            this.signatureAlgorithm.initVerify((PublicKey)publicKey);
        } catch (InvalidKeyException ex) {
            Signature sig = this.signatureAlgorithm;
            try {
                this.signatureAlgorithm = Signature.getInstance(this.signatureAlgorithm.getAlgorithm());
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception when reinstantiating Signature:" + e);
                }
                this.signatureAlgorithm = sig;
            }
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected byte[] engineSign() throws XMLSignatureException {
        try {
            byte[] jcebytes = this.signatureAlgorithm.sign();
            return SignatureECDSA.convertASN1toXMLDSIG(jcebytes);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (IOException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineInitSign(Key privateKey, SecureRandom secureRandom) throws XMLSignatureException {
        if (!(privateKey instanceof PrivateKey)) {
            String supplied = null;
            if (privateKey != null) {
                supplied = privateKey.getClass().getName();
            }
            String needed = PrivateKey.class.getName();
            Object[] exArgs = new Object[]{supplied, needed};
            throw new XMLSignatureException("algorithms.WrongKeyForThisOperation", exArgs);
        }
        try {
            if (secureRandom == null) {
                this.signatureAlgorithm.initSign((PrivateKey)privateKey);
            } else {
                this.signatureAlgorithm.initSign((PrivateKey)privateKey, secureRandom);
            }
        } catch (InvalidKeyException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineInitSign(Key privateKey) throws XMLSignatureException {
        this.engineInitSign(privateKey, (SecureRandom)null);
    }

    protected void engineUpdate(byte[] input) throws XMLSignatureException {
        try {
            this.signatureAlgorithm.update(input);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineUpdate(byte input) throws XMLSignatureException {
        try {
            this.signatureAlgorithm.update(input);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineUpdate(byte[] buf, int offset, int len) throws XMLSignatureException {
        try {
            this.signatureAlgorithm.update(buf, offset, len);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected String engineGetJCEAlgorithmString() {
        return this.signatureAlgorithm.getAlgorithm();
    }

    protected String engineGetJCEProviderName() {
        return this.signatureAlgorithm.getProvider().getName();
    }

    protected void engineSetHMACOutputLength(int HMACOutputLength) throws XMLSignatureException {
        throw new XMLSignatureException("algorithms.HMACOutputLengthOnlyForHMAC");
    }

    protected void engineInitSign(Key signingKey, AlgorithmParameterSpec algorithmParameterSpec) throws XMLSignatureException {
        throw new XMLSignatureException("algorithms.CannotUseAlgorithmParameterSpecOnRSA");
    }

    public static class SignatureECDSASHA512
    extends SignatureECDSA {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512";
        }
    }

    public static class SignatureECDSASHA384
    extends SignatureECDSA {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384";
        }
    }

    public static class SignatureECDSASHA256
    extends SignatureECDSA {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";
        }
    }

    public static class SignatureECDSASHA224
    extends SignatureECDSA {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224";
        }
    }

    public static class SignatureECDSASHA1
    extends SignatureECDSA {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1";
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.algorithms.implementations;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.algorithms.SignatureAlgorithmSpi;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public abstract class IntegrityHmac
extends SignatureAlgorithmSpi {
    private static Log log = LogFactory.getLog(IntegrityHmac.class);
    private Mac macAlgorithm = null;
    private int HMACOutputLength = 0;
    private boolean HMACOutputLengthSet = false;

    public abstract String engineGetURI();

    abstract int getDigestLength();

    public IntegrityHmac() throws XMLSignatureException {
        String algorithmID = JCEMapper.translateURItoJCEID(this.engineGetURI());
        if (log.isDebugEnabled()) {
            log.debug("Created IntegrityHmacSHA1 using " + algorithmID);
        }
        try {
            this.macAlgorithm = Mac.getInstance(algorithmID);
        } catch (NoSuchAlgorithmException ex) {
            Object[] exArgs = new Object[]{algorithmID, ex.getLocalizedMessage()};
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        }
    }

    protected void engineSetParameter(AlgorithmParameterSpec params) throws XMLSignatureException {
        throw new XMLSignatureException("empty");
    }

    public void reset() {
        this.HMACOutputLength = 0;
        this.HMACOutputLengthSet = false;
        this.macAlgorithm.reset();
    }

    protected boolean engineVerify(byte[] signature) throws XMLSignatureException {
        try {
            if (this.HMACOutputLengthSet && this.HMACOutputLength < this.getDigestLength()) {
                if (log.isDebugEnabled()) {
                    log.debug("HMACOutputLength must not be less than " + this.getDigestLength());
                }
                Object[] exArgs = new Object[]{String.valueOf(this.getDigestLength())};
                throw new XMLSignatureException("algorithms.HMACOutputLengthMin", exArgs);
            }
            byte[] completeResult = this.macAlgorithm.doFinal();
            return MessageDigestAlgorithm.isEqual(completeResult, signature);
        } catch (IllegalStateException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineInitVerify(Key secretKey) throws XMLSignatureException {
        if (!(secretKey instanceof SecretKey)) {
            String supplied = null;
            if (secretKey != null) {
                supplied = secretKey.getClass().getName();
            }
            String needed = SecretKey.class.getName();
            Object[] exArgs = new Object[]{supplied, needed};
            throw new XMLSignatureException("algorithms.WrongKeyForThisOperation", exArgs);
        }
        try {
            this.macAlgorithm.init(secretKey);
        } catch (InvalidKeyException ex) {
            Mac mac = this.macAlgorithm;
            try {
                this.macAlgorithm = Mac.getInstance(this.macAlgorithm.getAlgorithm());
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception when reinstantiating Mac:" + e);
                }
                this.macAlgorithm = mac;
            }
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected byte[] engineSign() throws XMLSignatureException {
        try {
            if (this.HMACOutputLengthSet && this.HMACOutputLength < this.getDigestLength()) {
                if (log.isDebugEnabled()) {
                    log.debug("HMACOutputLength must not be less than " + this.getDigestLength());
                }
                Object[] exArgs = new Object[]{String.valueOf(this.getDigestLength())};
                throw new XMLSignatureException("algorithms.HMACOutputLengthMin", exArgs);
            }
            return this.macAlgorithm.doFinal();
        } catch (IllegalStateException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineInitSign(Key secretKey) throws XMLSignatureException {
        this.engineInitSign(secretKey, (AlgorithmParameterSpec)null);
    }

    protected void engineInitSign(Key secretKey, AlgorithmParameterSpec algorithmParameterSpec) throws XMLSignatureException {
        if (!(secretKey instanceof SecretKey)) {
            String supplied = null;
            if (secretKey != null) {
                supplied = secretKey.getClass().getName();
            }
            String needed = SecretKey.class.getName();
            Object[] exArgs = new Object[]{supplied, needed};
            throw new XMLSignatureException("algorithms.WrongKeyForThisOperation", exArgs);
        }
        try {
            if (algorithmParameterSpec == null) {
                this.macAlgorithm.init(secretKey);
            } else {
                this.macAlgorithm.init(secretKey, algorithmParameterSpec);
            }
        } catch (InvalidKeyException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineInitSign(Key secretKey, SecureRandom secureRandom) throws XMLSignatureException {
        throw new XMLSignatureException("algorithms.CannotUseSecureRandomOnMAC");
    }

    protected void engineUpdate(byte[] input) throws XMLSignatureException {
        try {
            this.macAlgorithm.update(input);
        } catch (IllegalStateException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineUpdate(byte input) throws XMLSignatureException {
        try {
            this.macAlgorithm.update(input);
        } catch (IllegalStateException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineUpdate(byte[] buf, int offset, int len) throws XMLSignatureException {
        try {
            this.macAlgorithm.update(buf, offset, len);
        } catch (IllegalStateException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected String engineGetJCEAlgorithmString() {
        return this.macAlgorithm.getAlgorithm();
    }

    protected String engineGetJCEProviderName() {
        return this.macAlgorithm.getProvider().getName();
    }

    protected void engineSetHMACOutputLength(int HMACOutputLength) {
        this.HMACOutputLength = HMACOutputLength;
        this.HMACOutputLengthSet = true;
    }

    protected void engineGetContextFromElement(Element element) {
        super.engineGetContextFromElement(element);
        if (element == null) {
            throw new IllegalArgumentException("element null");
        }
        Text hmaclength = XMLUtils.selectDsNodeText(element.getFirstChild(), "HMACOutputLength", 0);
        if (hmaclength != null) {
            this.HMACOutputLength = Integer.parseInt(hmaclength.getData());
            this.HMACOutputLengthSet = true;
        }
    }

    public void engineAddContextToElement(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("null element");
        }
        if (this.HMACOutputLengthSet) {
            Document doc = element.getOwnerDocument();
            Element HMElem = XMLUtils.createElementInSignatureSpace(doc, "HMACOutputLength");
            Text HMText = doc.createTextNode(Integer.valueOf(this.HMACOutputLength).toString());
            HMElem.appendChild(HMText);
            XMLUtils.addReturnToElement(element);
            element.appendChild(HMElem);
            XMLUtils.addReturnToElement(element);
        }
    }

    public static class IntegrityHmacMD5
    extends IntegrityHmac {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#hmac-md5";
        }

        int getDigestLength() {
            return 128;
        }
    }

    public static class IntegrityHmacRIPEMD160
    extends IntegrityHmac {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160";
        }

        int getDigestLength() {
            return 160;
        }
    }

    public static class IntegrityHmacSHA512
    extends IntegrityHmac {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#hmac-sha512";
        }

        int getDigestLength() {
            return 512;
        }
    }

    public static class IntegrityHmacSHA384
    extends IntegrityHmac {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#hmac-sha384";
        }

        int getDigestLength() {
            return 384;
        }
    }

    public static class IntegrityHmacSHA256
    extends IntegrityHmac {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#hmac-sha256";
        }

        int getDigestLength() {
            return 256;
        }
    }

    public static class IntegrityHmacSHA224
    extends IntegrityHmac {
        public String engineGetURI() {
            return "http://www.w3.org/2001/04/xmldsig-more#hmac-sha224";
        }

        int getDigestLength() {
            return 224;
        }
    }

    public static class IntegrityHmacSHA1
    extends IntegrityHmac {
        public String engineGetURI() {
            return "http://www.w3.org/2000/09/xmldsig#hmac-sha1";
        }

        int getDigestLength() {
            return 160;
        }
    }
}


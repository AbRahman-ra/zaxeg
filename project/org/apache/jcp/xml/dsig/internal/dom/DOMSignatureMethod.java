/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAKey;
import java.security.spec.AlgorithmParameterSpec;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcp.xml.dsig.internal.SignerOutputStream;
import org.apache.jcp.xml.dsig.internal.dom.AbstractDOMSignatureMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMHMACSignatureMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.xml.security.algorithms.implementations.SignatureECDSA;
import org.apache.xml.security.utils.JavaUtils;
import org.w3c.dom.Element;

public abstract class DOMSignatureMethod
extends AbstractDOMSignatureMethod {
    private static Log log = LogFactory.getLog(DOMSignatureMethod.class);
    private SignatureMethodParameterSpec params;
    private Signature signature;
    static final String RSA_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha224";
    static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    static final String RSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384";
    static final String RSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
    static final String RSA_RIPEMD160 = "http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160";
    static final String ECDSA_SHA1 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1";
    static final String ECDSA_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224";
    static final String ECDSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";
    static final String ECDSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384";
    static final String ECDSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512";
    static final String DSA_SHA256 = "http://www.w3.org/2009/xmldsig11#dsa-sha256";

    DOMSignatureMethod(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params != null && !(params instanceof SignatureMethodParameterSpec)) {
            throw new InvalidAlgorithmParameterException("params must be of type SignatureMethodParameterSpec");
        }
        this.checkParams((SignatureMethodParameterSpec)params);
        this.params = (SignatureMethodParameterSpec)params;
    }

    DOMSignatureMethod(Element smElem) throws MarshalException {
        Element paramsElem = DOMUtils.getFirstChildElement(smElem);
        if (paramsElem != null) {
            this.params = this.unmarshalParams(paramsElem);
        }
        try {
            this.checkParams(this.params);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new MarshalException(iape);
        }
    }

    static SignatureMethod unmarshal(Element smElem) throws MarshalException {
        String alg = DOMUtils.getAttributeValue(smElem, "Algorithm");
        if (alg.equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1")) {
            return new SHA1withRSA(smElem);
        }
        if (alg.equals(RSA_SHA224)) {
            return new SHA224withRSA(smElem);
        }
        if (alg.equals(RSA_SHA256)) {
            return new SHA256withRSA(smElem);
        }
        if (alg.equals(RSA_SHA384)) {
            return new SHA384withRSA(smElem);
        }
        if (alg.equals(RSA_SHA512)) {
            return new SHA512withRSA(smElem);
        }
        if (alg.equals(RSA_RIPEMD160)) {
            return new RIPEMD160withRSA(smElem);
        }
        if (alg.equals("http://www.w3.org/2000/09/xmldsig#dsa-sha1")) {
            return new SHA1withDSA(smElem);
        }
        if (alg.equals(DSA_SHA256)) {
            return new SHA256withDSA(smElem);
        }
        if (alg.equals(ECDSA_SHA1)) {
            return new SHA1withECDSA(smElem);
        }
        if (alg.equals(ECDSA_SHA224)) {
            return new SHA224withECDSA(smElem);
        }
        if (alg.equals(ECDSA_SHA256)) {
            return new SHA256withECDSA(smElem);
        }
        if (alg.equals(ECDSA_SHA384)) {
            return new SHA384withECDSA(smElem);
        }
        if (alg.equals(ECDSA_SHA512)) {
            return new SHA512withECDSA(smElem);
        }
        if (alg.equals("http://www.w3.org/2000/09/xmldsig#hmac-sha1")) {
            return new DOMHMACSignatureMethod.SHA1(smElem);
        }
        if (alg.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha224")) {
            return new DOMHMACSignatureMethod.SHA224(smElem);
        }
        if (alg.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256")) {
            return new DOMHMACSignatureMethod.SHA256(smElem);
        }
        if (alg.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha384")) {
            return new DOMHMACSignatureMethod.SHA384(smElem);
        }
        if (alg.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha512")) {
            return new DOMHMACSignatureMethod.SHA512(smElem);
        }
        if (alg.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160")) {
            return new DOMHMACSignatureMethod.RIPEMD160(smElem);
        }
        throw new MarshalException("unsupported SignatureMethod algorithm: " + alg);
    }

    public final AlgorithmParameterSpec getParameterSpec() {
        return this.params;
    }

    boolean verify(Key key, SignedInfo si, byte[] sig, XMLValidateContext context) throws InvalidKeyException, SignatureException, XMLSignatureException {
        if (key == null || si == null || sig == null) {
            throw new NullPointerException();
        }
        if (!(key instanceof PublicKey)) {
            throw new InvalidKeyException("key must be PublicKey");
        }
        if (this.signature == null) {
            try {
                Provider p = (Provider)context.getProperty("org.jcp.xml.dsig.internal.dom.SignatureProvider");
                this.signature = p == null ? Signature.getInstance(this.getJCAAlgorithm()) : Signature.getInstance(this.getJCAAlgorithm(), p);
            } catch (NoSuchAlgorithmException nsae) {
                throw new XMLSignatureException(nsae);
            }
        }
        this.signature.initVerify((PublicKey)key);
        if (log.isDebugEnabled()) {
            log.debug("Signature provider:" + this.signature.getProvider());
            log.debug("Verifying with key: " + key);
            log.debug("JCA Algorithm: " + this.getJCAAlgorithm());
            log.debug("Signature Bytes length: " + sig.length);
        }
        ((DOMSignedInfo)si).canonicalize(context, new SignerOutputStream(this.signature));
        try {
            AbstractDOMSignatureMethod.Type type = this.getAlgorithmType();
            if (type == AbstractDOMSignatureMethod.Type.DSA) {
                int size = ((DSAKey)((Object)key)).getParams().getQ().bitLength();
                return this.signature.verify(JavaUtils.convertDsaXMLDSIGtoASN1(sig, size / 8));
            }
            if (type == AbstractDOMSignatureMethod.Type.ECDSA) {
                return this.signature.verify(SignatureECDSA.convertXMLDSIGtoASN1(sig));
            }
            return this.signature.verify(sig);
        } catch (IOException ioe) {
            throw new XMLSignatureException(ioe);
        }
    }

    byte[] sign(Key key, SignedInfo si, XMLSignContext context) throws InvalidKeyException, XMLSignatureException {
        if (key == null || si == null) {
            throw new NullPointerException();
        }
        if (!(key instanceof PrivateKey)) {
            throw new InvalidKeyException("key must be PrivateKey");
        }
        if (this.signature == null) {
            try {
                Provider p = (Provider)context.getProperty("org.jcp.xml.dsig.internal.dom.SignatureProvider");
                this.signature = p == null ? Signature.getInstance(this.getJCAAlgorithm()) : Signature.getInstance(this.getJCAAlgorithm(), p);
            } catch (NoSuchAlgorithmException nsae) {
                throw new XMLSignatureException(nsae);
            }
        }
        this.signature.initSign((PrivateKey)key);
        if (log.isDebugEnabled()) {
            log.debug("Signature provider:" + this.signature.getProvider());
            log.debug("Signing with key: " + key);
            log.debug("JCA Algorithm: " + this.getJCAAlgorithm());
        }
        ((DOMSignedInfo)si).canonicalize(context, new SignerOutputStream(this.signature));
        try {
            AbstractDOMSignatureMethod.Type type = this.getAlgorithmType();
            if (type == AbstractDOMSignatureMethod.Type.DSA) {
                int size = ((DSAKey)((Object)key)).getParams().getQ().bitLength();
                return JavaUtils.convertDsaASN1toXMLDSIG(this.signature.sign(), size / 8);
            }
            if (type == AbstractDOMSignatureMethod.Type.ECDSA) {
                return SignatureECDSA.convertASN1toXMLDSIG(this.signature.sign());
            }
            return this.signature.sign();
        } catch (SignatureException se) {
            throw new XMLSignatureException(se);
        } catch (IOException ioe) {
            throw new XMLSignatureException(ioe);
        }
    }

    static final class SHA512withECDSA
    extends DOMSignatureMethod {
        SHA512withECDSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA512withECDSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.ECDSA_SHA512;
        }

        String getJCAAlgorithm() {
            return "SHA512withECDSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.ECDSA;
        }
    }

    static final class SHA384withECDSA
    extends DOMSignatureMethod {
        SHA384withECDSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA384withECDSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.ECDSA_SHA384;
        }

        String getJCAAlgorithm() {
            return "SHA384withECDSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.ECDSA;
        }
    }

    static final class SHA256withECDSA
    extends DOMSignatureMethod {
        SHA256withECDSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA256withECDSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.ECDSA_SHA256;
        }

        String getJCAAlgorithm() {
            return "SHA256withECDSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.ECDSA;
        }
    }

    static final class SHA224withECDSA
    extends DOMSignatureMethod {
        SHA224withECDSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA224withECDSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.ECDSA_SHA224;
        }

        String getJCAAlgorithm() {
            return "SHA224withECDSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.ECDSA;
        }
    }

    static final class SHA1withECDSA
    extends DOMSignatureMethod {
        SHA1withECDSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA1withECDSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.ECDSA_SHA1;
        }

        String getJCAAlgorithm() {
            return "SHA1withECDSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.ECDSA;
        }
    }

    static final class SHA256withDSA
    extends DOMSignatureMethod {
        SHA256withDSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA256withDSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.DSA_SHA256;
        }

        String getJCAAlgorithm() {
            return "SHA256withDSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.DSA;
        }
    }

    static final class SHA1withDSA
    extends DOMSignatureMethod {
        SHA1withDSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA1withDSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
        }

        String getJCAAlgorithm() {
            return "SHA1withDSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.DSA;
        }
    }

    static final class RIPEMD160withRSA
    extends DOMSignatureMethod {
        RIPEMD160withRSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        RIPEMD160withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.RSA_RIPEMD160;
        }

        String getJCAAlgorithm() {
            return "RIPEMD160withRSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.RSA;
        }
    }

    static final class SHA512withRSA
    extends DOMSignatureMethod {
        SHA512withRSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA512withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.RSA_SHA512;
        }

        String getJCAAlgorithm() {
            return "SHA512withRSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.RSA;
        }
    }

    static final class SHA384withRSA
    extends DOMSignatureMethod {
        SHA384withRSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA384withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.RSA_SHA384;
        }

        String getJCAAlgorithm() {
            return "SHA384withRSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.RSA;
        }
    }

    static final class SHA256withRSA
    extends DOMSignatureMethod {
        SHA256withRSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA256withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.RSA_SHA256;
        }

        String getJCAAlgorithm() {
            return "SHA256withRSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.RSA;
        }
    }

    static final class SHA224withRSA
    extends DOMSignatureMethod {
        SHA224withRSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA224withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return DOMSignatureMethod.RSA_SHA224;
        }

        String getJCAAlgorithm() {
            return "SHA224withRSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.RSA;
        }
    }

    static final class SHA1withRSA
    extends DOMSignatureMethod {
        SHA1withRSA(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            super(params);
        }

        SHA1withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        public String getAlgorithm() {
            return "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
        }

        String getJCAAlgorithm() {
            return "SHA1withRSA";
        }

        AbstractDOMSignatureMethod.Type getAlgorithmType() {
            return AbstractDOMSignatureMethod.Type.RSA;
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import org.apache.jcp.xml.dsig.internal.dom.DOMCryptoBinary;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.ClassLoaderUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DOMKeyValue
extends DOMStructure
implements KeyValue {
    private static final String XMLDSIG_11_XMLNS = "http://www.w3.org/2009/xmldsig11#";
    private final PublicKey publicKey;

    public DOMKeyValue(PublicKey key) throws KeyException {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        }
        this.publicKey = key;
    }

    public DOMKeyValue(Element kvtElem) throws MarshalException {
        this.publicKey = this.unmarshalKeyValue(kvtElem);
    }

    static KeyValue unmarshal(Element kvElem) throws MarshalException {
        Element kvtElem = DOMUtils.getFirstChildElement(kvElem);
        if (kvtElem == null) {
            throw new MarshalException("KeyValue must contain at least one type");
        }
        String namespace = kvtElem.getNamespaceURI();
        if (kvtElem.getLocalName().equals("DSAKeyValue") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
            return new DSA(kvtElem);
        }
        if (kvtElem.getLocalName().equals("RSAKeyValue") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
            return new RSA(kvtElem);
        }
        if (kvtElem.getLocalName().equals("ECKeyValue") && XMLDSIG_11_XMLNS.equals(namespace)) {
            return new EC(kvtElem);
        }
        return new Unknown(kvtElem);
    }

    public PublicKey getPublicKey() throws KeyException {
        if (this.publicKey == null) {
            throw new KeyException("can't convert KeyValue to PublicKey");
        }
        return this.publicKey;
    }

    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element kvElem = DOMUtils.createElement(ownerDoc, "KeyValue", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        this.marshalPublicKey(kvElem, ownerDoc, dsPrefix, context);
        parent.appendChild(kvElem);
    }

    abstract void marshalPublicKey(Node var1, Document var2, String var3, DOMCryptoContext var4) throws MarshalException;

    abstract PublicKey unmarshalKeyValue(Element var1) throws MarshalException;

    private static PublicKey generatePublicKey(KeyFactory kf, KeySpec keyspec) {
        try {
            return kf.generatePublic(keyspec);
        } catch (InvalidKeySpecException e) {
            return null;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyValue)) {
            return false;
        }
        try {
            KeyValue kv = (KeyValue)obj;
            if (this.publicKey == null ? kv.getPublicKey() != null : !this.publicKey.equals(kv.getPublicKey())) {
                return false;
            }
        } catch (KeyException ke) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = 17;
        if (this.publicKey != null) {
            result = 31 * result + this.publicKey.hashCode();
        }
        return result;
    }

    static final class Unknown
    extends DOMKeyValue {
        private javax.xml.crypto.dom.DOMStructure externalPublicKey;

        Unknown(Element elem) throws MarshalException {
            super(elem);
        }

        PublicKey unmarshalKeyValue(Element kvElem) throws MarshalException {
            this.externalPublicKey = new javax.xml.crypto.dom.DOMStructure(kvElem);
            return null;
        }

        void marshalPublicKey(Node parent, Document doc, String dsPrefix, DOMCryptoContext context) throws MarshalException {
            parent.appendChild(this.externalPublicKey.getNode());
        }
    }

    static final class EC
    extends DOMKeyValue {
        private static final String ver = System.getProperty("java.version");
        private static final boolean atLeast18 = !ver.startsWith("1.5") && !ver.startsWith("1.6") && !ver.startsWith("1.7");
        private byte[] ecPublicKey;
        private KeyFactory eckf;
        private ECParameterSpec ecParams;
        private Method encodePoint;
        private Method decodePoint;

        EC(PublicKey key) throws KeyException {
            super(key);
            ECPublicKey ecKey = (ECPublicKey)key;
            ECPoint ecPoint = ecKey.getW();
            this.ecParams = ecKey.getParams();
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>(){

                    @Override
                    public Void run() throws ClassNotFoundException, NoSuchMethodException {
                        EC.this.getMethods();
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                throw new KeyException("ECKeyValue not supported", pae.getException());
            }
            Object[] args = new Object[]{ecPoint, this.ecParams.getCurve()};
            try {
                this.ecPublicKey = (byte[])this.encodePoint.invoke(null, args);
            } catch (IllegalAccessException iae) {
                throw new KeyException(iae);
            } catch (InvocationTargetException ite) {
                throw new KeyException(ite);
            }
        }

        EC(Element dmElem) throws MarshalException {
            super(dmElem);
        }

        void getMethods() throws ClassNotFoundException, NoSuchMethodException {
            String className = atLeast18 ? "sun.security.util.ECUtil" : "sun.security.ec.ECParameters";
            Class<?> c = ClassLoaderUtils.loadClass(className, DOMKeyValue.class);
            Class[] params = new Class[]{ECPoint.class, EllipticCurve.class};
            this.encodePoint = c.getMethod("encodePoint", params);
            params = new Class[]{ECParameterSpec.class};
            params = new Class[]{byte[].class, EllipticCurve.class};
            this.decodePoint = c.getMethod("decodePoint", params);
        }

        void marshalPublicKey(Node parent, Document doc, String dsPrefix, DOMCryptoContext context) throws MarshalException {
            String prefix = DOMUtils.getNSPrefix(context, DOMKeyValue.XMLDSIG_11_XMLNS);
            Element ecKeyValueElem = DOMUtils.createElement(doc, "ECKeyValue", DOMKeyValue.XMLDSIG_11_XMLNS, prefix);
            Element namedCurveElem = DOMUtils.createElement(doc, "NamedCurve", DOMKeyValue.XMLDSIG_11_XMLNS, prefix);
            Element publicKeyElem = DOMUtils.createElement(doc, "PublicKey", DOMKeyValue.XMLDSIG_11_XMLNS, prefix);
            try {
                String oid = EC.getCurveName(this.ecParams);
                DOMUtils.setAttribute(namedCurveElem, "URI", "urn:oid:" + oid);
            } catch (GeneralSecurityException gse) {
                throw new MarshalException(gse);
            }
            String qname = prefix == null || prefix.length() == 0 ? "xmlns" : "xmlns:" + prefix;
            namedCurveElem.setAttributeNS("http://www.w3.org/2000/xmlns/", qname, DOMKeyValue.XMLDSIG_11_XMLNS);
            ecKeyValueElem.appendChild(namedCurveElem);
            String encoded = Base64.encode(this.ecPublicKey);
            publicKeyElem.appendChild(DOMUtils.getOwnerDocument(publicKeyElem).createTextNode(encoded));
            ecKeyValueElem.appendChild(publicKeyElem);
            parent.appendChild(ecKeyValueElem);
        }

        private static String getCurveName(ECParameterSpec spec) throws GeneralSecurityException {
            AlgorithmParameters ap = AlgorithmParameters.getInstance("EC");
            ap.init(spec);
            ECGenParameterSpec nameSpec = ap.getParameterSpec(ECGenParameterSpec.class);
            if (nameSpec == null) {
                return null;
            }
            return nameSpec.getName();
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        PublicKey unmarshalKeyValue(Element kvtElem) throws MarshalException {
            if (this.eckf == null) {
                try {
                    this.eckf = KeyFactory.getInstance("EC");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("unable to create EC KeyFactory: " + e.getMessage());
                }
            }
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>(){

                    @Override
                    public Void run() throws ClassNotFoundException, NoSuchMethodException {
                        EC.this.getMethods();
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                throw new MarshalException("ECKeyValue not supported", pae.getException());
            }
            ECParameterSpec ecParams = null;
            Element curElem = DOMUtils.getFirstChildElement(kvtElem);
            if (curElem == null) {
                throw new MarshalException("KeyValue must contain at least one type");
            }
            if (curElem.getLocalName().equals("ECParameters") && DOMKeyValue.XMLDSIG_11_XMLNS.equals(curElem.getNamespaceURI())) {
                throw new UnsupportedOperationException("ECParameters not supported");
            }
            if (!curElem.getLocalName().equals("NamedCurve") || !DOMKeyValue.XMLDSIG_11_XMLNS.equals(curElem.getNamespaceURI())) throw new MarshalException("Invalid ECKeyValue");
            String uri = DOMUtils.getAttributeValue(curElem, "URI");
            if (!uri.startsWith("urn:oid:")) throw new MarshalException("Invalid NamedCurve URI");
            String oid = uri.substring(8);
            try {
                ecParams = EC.getECParameterSpec(oid);
            } catch (GeneralSecurityException gse) {
                throw new MarshalException(gse);
            }
            curElem = DOMUtils.getNextSiblingElement(curElem, "PublicKey", DOMKeyValue.XMLDSIG_11_XMLNS);
            ECPoint ecPoint = null;
            try {
                Object[] args = new Object[]{Base64.decode(curElem), ecParams.getCurve()};
                ecPoint = (ECPoint)this.decodePoint.invoke(null, args);
            } catch (Base64DecodingException bde) {
                throw new MarshalException("Invalid EC PublicKey", bde);
            } catch (IllegalAccessException iae) {
                throw new MarshalException(iae);
            } catch (InvocationTargetException ite) {
                throw new MarshalException(ite);
            }
            ECPublicKeySpec spec = new ECPublicKeySpec(ecPoint, ecParams);
            return DOMKeyValue.generatePublicKey(this.eckf, spec);
        }

        private static ECParameterSpec getECParameterSpec(String name) throws GeneralSecurityException {
            AlgorithmParameters ap = AlgorithmParameters.getInstance("EC");
            ap.init(new ECGenParameterSpec(name));
            return ap.getParameterSpec(ECParameterSpec.class);
        }
    }

    static final class DSA
    extends DOMKeyValue {
        private DOMCryptoBinary p;
        private DOMCryptoBinary q;
        private DOMCryptoBinary g;
        private DOMCryptoBinary y;
        private DOMCryptoBinary j;
        private KeyFactory dsakf;

        DSA(PublicKey key) throws KeyException {
            super(key);
            DSAPublicKey dkey = (DSAPublicKey)key;
            DSAParams params = dkey.getParams();
            this.p = new DOMCryptoBinary(params.getP());
            this.q = new DOMCryptoBinary(params.getQ());
            this.g = new DOMCryptoBinary(params.getG());
            this.y = new DOMCryptoBinary(dkey.getY());
        }

        DSA(Element elem) throws MarshalException {
            super(elem);
        }

        void marshalPublicKey(Node parent, Document doc, String dsPrefix, DOMCryptoContext context) throws MarshalException {
            Element dsaElem = DOMUtils.createElement(doc, "DSAKeyValue", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            Element pElem = DOMUtils.createElement(doc, "P", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            Element qElem = DOMUtils.createElement(doc, "Q", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            Element gElem = DOMUtils.createElement(doc, "G", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            Element yElem = DOMUtils.createElement(doc, "Y", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            this.p.marshal(pElem, dsPrefix, context);
            this.q.marshal(qElem, dsPrefix, context);
            this.g.marshal(gElem, dsPrefix, context);
            this.y.marshal(yElem, dsPrefix, context);
            dsaElem.appendChild(pElem);
            dsaElem.appendChild(qElem);
            dsaElem.appendChild(gElem);
            dsaElem.appendChild(yElem);
            parent.appendChild(dsaElem);
        }

        PublicKey unmarshalKeyValue(Element kvtElem) throws MarshalException {
            Element curElem;
            if (this.dsakf == null) {
                try {
                    this.dsakf = KeyFactory.getInstance("DSA");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("unable to create DSA KeyFactory: " + e.getMessage());
                }
            }
            if ((curElem = DOMUtils.getFirstChildElement(kvtElem)) == null) {
                throw new MarshalException("KeyValue must contain at least one type");
            }
            if (curElem.getLocalName().equals("P") && "http://www.w3.org/2000/09/xmldsig#".equals(curElem.getNamespaceURI())) {
                this.p = new DOMCryptoBinary(curElem.getFirstChild());
                curElem = DOMUtils.getNextSiblingElement(curElem, "Q", "http://www.w3.org/2000/09/xmldsig#");
                this.q = new DOMCryptoBinary(curElem.getFirstChild());
                curElem = DOMUtils.getNextSiblingElement(curElem);
            }
            if (curElem != null && curElem.getLocalName().equals("G") && "http://www.w3.org/2000/09/xmldsig#".equals(curElem.getNamespaceURI())) {
                this.g = new DOMCryptoBinary(curElem.getFirstChild());
                curElem = DOMUtils.getNextSiblingElement(curElem, "Y");
            }
            if (curElem != null) {
                this.y = new DOMCryptoBinary(curElem.getFirstChild());
                curElem = DOMUtils.getNextSiblingElement(curElem);
            }
            if (curElem != null && curElem.getLocalName().equals("J")) {
                this.j = new DOMCryptoBinary(curElem.getFirstChild());
            }
            DSAPublicKeySpec spec = new DSAPublicKeySpec(this.y.getBigNum(), this.p.getBigNum(), this.q.getBigNum(), this.g.getBigNum());
            return DOMKeyValue.generatePublicKey(this.dsakf, spec);
        }
    }

    static final class RSA
    extends DOMKeyValue {
        private DOMCryptoBinary modulus;
        private DOMCryptoBinary exponent;
        private KeyFactory rsakf;

        RSA(PublicKey key) throws KeyException {
            super(key);
            RSAPublicKey rkey = (RSAPublicKey)key;
            this.exponent = new DOMCryptoBinary(rkey.getPublicExponent());
            this.modulus = new DOMCryptoBinary(rkey.getModulus());
        }

        RSA(Element elem) throws MarshalException {
            super(elem);
        }

        void marshalPublicKey(Node parent, Document doc, String dsPrefix, DOMCryptoContext context) throws MarshalException {
            Element rsaElem = DOMUtils.createElement(doc, "RSAKeyValue", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            Element modulusElem = DOMUtils.createElement(doc, "Modulus", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            Element exponentElem = DOMUtils.createElement(doc, "Exponent", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            this.modulus.marshal(modulusElem, dsPrefix, context);
            this.exponent.marshal(exponentElem, dsPrefix, context);
            rsaElem.appendChild(modulusElem);
            rsaElem.appendChild(exponentElem);
            parent.appendChild(rsaElem);
        }

        PublicKey unmarshalKeyValue(Element kvtElem) throws MarshalException {
            if (this.rsakf == null) {
                try {
                    this.rsakf = KeyFactory.getInstance("RSA");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("unable to create RSA KeyFactory: " + e.getMessage());
                }
            }
            Element modulusElem = DOMUtils.getFirstChildElement(kvtElem, "Modulus", "http://www.w3.org/2000/09/xmldsig#");
            this.modulus = new DOMCryptoBinary(modulusElem.getFirstChild());
            Element exponentElem = DOMUtils.getNextSiblingElement(modulusElem, "Exponent", "http://www.w3.org/2000/09/xmldsig#");
            this.exponent = new DOMCryptoBinary(exponentElem.getFirstChild());
            RSAPublicKeySpec spec = new RSAPublicKeySpec(this.modulus.getBigNum(), this.exponent.getBigNum());
            return DOMKeyValue.generatePublicKey(this.rsakf, spec);
        }
    }
}


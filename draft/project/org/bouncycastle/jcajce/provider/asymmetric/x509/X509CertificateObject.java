/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.util.Date;
import java.util.Enumeration;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.jcajce.provider.asymmetric.util.PKCS12BagAttributeCarrierImpl;
import org.bouncycastle.jcajce.provider.asymmetric.x509.X509CertificateImpl;
import org.bouncycastle.jcajce.provider.asymmetric.x509.X509CertificateInternal;
import org.bouncycastle.jcajce.provider.asymmetric.x509.X509SignatureUtil;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.util.Arrays;

class X509CertificateObject
extends X509CertificateImpl
implements PKCS12BagAttributeCarrier {
    private final Object cacheLock = new Object();
    private X509CertificateInternal internalCertificateValue;
    private X500Principal issuerValue;
    private PublicKey publicKeyValue;
    private X500Principal subjectValue;
    private long[] validityValues;
    private volatile boolean hashValueSet;
    private volatile int hashValue;
    private PKCS12BagAttributeCarrier attrCarrier = new PKCS12BagAttributeCarrierImpl();

    X509CertificateObject(JcaJceHelper jcaJceHelper, Certificate certificate) throws CertificateParsingException {
        super(jcaJceHelper, certificate, X509CertificateObject.createBasicConstraints(certificate), X509CertificateObject.createKeyUsage(certificate), X509CertificateObject.createSigAlgName(certificate), X509CertificateObject.createSigAlgParams(certificate));
    }

    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        long[] lArray;
        long l = date.getTime();
        if (l > (lArray = this.getValidityValues())[1]) {
            throw new CertificateExpiredException("certificate expired on " + this.c.getEndDate().getTime());
        }
        if (l < lArray[0]) {
            throw new CertificateNotYetValidException("certificate not valid till " + this.c.getStartDate().getTime());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public X500Principal getIssuerX500Principal() {
        Object object = this.cacheLock;
        synchronized (object) {
            if (null != this.issuerValue) {
                return this.issuerValue;
            }
        }
        object = super.getIssuerX500Principal();
        Object object2 = this.cacheLock;
        synchronized (object2) {
            if (null == this.issuerValue) {
                this.issuerValue = object;
            }
            return this.issuerValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PublicKey getPublicKey() {
        Object object = this.cacheLock;
        synchronized (object) {
            if (null != this.publicKeyValue) {
                return this.publicKeyValue;
            }
        }
        object = super.getPublicKey();
        if (null == object) {
            return null;
        }
        Object object2 = this.cacheLock;
        synchronized (object2) {
            if (null == this.publicKeyValue) {
                this.publicKeyValue = object;
            }
            return this.publicKeyValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public X500Principal getSubjectX500Principal() {
        Object object = this.cacheLock;
        synchronized (object) {
            if (null != this.subjectValue) {
                return this.subjectValue;
            }
        }
        object = super.getSubjectX500Principal();
        Object object2 = this.cacheLock;
        synchronized (object2) {
            if (null == this.subjectValue) {
                this.subjectValue = object;
            }
            return this.subjectValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long[] getValidityValues() {
        Object object = this.cacheLock;
        synchronized (object) {
            if (null != this.validityValues) {
                return this.validityValues;
            }
        }
        object = new long[]{super.getNotBefore().getTime(), super.getNotAfter().getTime()};
        Object object2 = this.cacheLock;
        synchronized (object2) {
            if (null == this.validityValues) {
                this.validityValues = (long[])object;
            }
            return this.validityValues;
        }
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        return Arrays.clone(this.getInternalCertificate().getEncoded());
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof X509CertificateObject) {
            DERBitString dERBitString;
            X509CertificateObject x509CertificateObject = (X509CertificateObject)object;
            if (this.hashValueSet && x509CertificateObject.hashValueSet ? this.hashValue != x509CertificateObject.hashValue : (null == this.internalCertificateValue || null == x509CertificateObject.internalCertificateValue) && null != (dERBitString = this.c.getSignature()) && !dERBitString.equals(x509CertificateObject.c.getSignature())) {
                return false;
            }
            return this.getInternalCertificate().equals(x509CertificateObject.getInternalCertificate());
        }
        return this.getInternalCertificate().equals(object);
    }

    public int hashCode() {
        if (!this.hashValueSet) {
            this.hashValue = this.getInternalCertificate().hashCode();
            this.hashValueSet = true;
        }
        return this.hashValue;
    }

    public int originalHashCode() {
        try {
            int n = 0;
            byte[] byArray = this.getInternalCertificate().getEncoded();
            for (int i = 1; i < byArray.length; ++i) {
                n += byArray[i] * i;
            }
            return n;
        } catch (CertificateEncodingException certificateEncodingException) {
            return 0;
        }
    }

    public void setBagAttribute(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.attrCarrier.setBagAttribute(aSN1ObjectIdentifier, aSN1Encodable);
    }

    public ASN1Encodable getBagAttribute(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return this.attrCarrier.getBagAttribute(aSN1ObjectIdentifier);
    }

    public Enumeration getBagAttributeKeys() {
        return this.attrCarrier.getBagAttributeKeys();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private X509CertificateInternal getInternalCertificate() {
        Object object = this.cacheLock;
        synchronized (object) {
            if (null != this.internalCertificateValue) {
                return this.internalCertificateValue;
            }
        }
        object = null;
        X509CertificateEncodingException x509CertificateEncodingException = null;
        try {
            object = this.c.getEncoded("DER");
        } catch (IOException iOException) {
            x509CertificateEncodingException = new X509CertificateEncodingException(iOException);
        }
        X509CertificateInternal x509CertificateInternal = new X509CertificateInternal(this.bcHelper, this.c, this.basicConstraints, this.keyUsage, this.sigAlgName, this.sigAlgParams, (byte[])object, x509CertificateEncodingException);
        Object object2 = this.cacheLock;
        synchronized (object2) {
            if (null == this.internalCertificateValue) {
                this.internalCertificateValue = x509CertificateInternal;
            }
            return this.internalCertificateValue;
        }
    }

    private static BasicConstraints createBasicConstraints(Certificate certificate) throws CertificateParsingException {
        try {
            byte[] byArray = X509CertificateObject.getExtensionOctets(certificate, "2.5.29.19");
            if (null == byArray) {
                return null;
            }
            return BasicConstraints.getInstance(ASN1Primitive.fromByteArray(byArray));
        } catch (Exception exception) {
            throw new CertificateParsingException("cannot construct BasicConstraints: " + exception);
        }
    }

    private static boolean[] createKeyUsage(Certificate certificate) throws CertificateParsingException {
        try {
            byte[] byArray = X509CertificateObject.getExtensionOctets(certificate, "2.5.29.15");
            if (null == byArray) {
                return null;
            }
            DERBitString dERBitString = DERBitString.getInstance(ASN1Primitive.fromByteArray(byArray));
            byte[] byArray2 = dERBitString.getBytes();
            int n = byArray2.length * 8 - dERBitString.getPadBits();
            boolean[] blArray = new boolean[n < 9 ? 9 : n];
            for (int i = 0; i != n; ++i) {
                blArray[i] = (byArray2[i / 8] & 128 >>> i % 8) != 0;
            }
            return blArray;
        } catch (Exception exception) {
            throw new CertificateParsingException("cannot construct KeyUsage: " + exception);
        }
    }

    private static String createSigAlgName(Certificate certificate) throws CertificateParsingException {
        try {
            return X509SignatureUtil.getSignatureName(certificate.getSignatureAlgorithm());
        } catch (Exception exception) {
            throw new CertificateParsingException("cannot construct SigAlgName: " + exception);
        }
    }

    private static byte[] createSigAlgParams(Certificate certificate) throws CertificateParsingException {
        try {
            ASN1Encodable aSN1Encodable = certificate.getSignatureAlgorithm().getParameters();
            if (null == aSN1Encodable) {
                return null;
            }
            return aSN1Encodable.toASN1Primitive().getEncoded("DER");
        } catch (Exception exception) {
            throw new CertificateParsingException("cannot construct SigAlgParams: " + exception);
        }
    }

    private static class X509CertificateEncodingException
    extends CertificateEncodingException {
        private final Throwable cause;

        X509CertificateEncodingException(Throwable throwable) {
            this.cause = throwable;
        }

        public Throwable getCause() {
            return this.cause;
        }
    }
}


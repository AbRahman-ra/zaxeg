/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.io.IOException;
import java.security.cert.CRLException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.bouncycastle.jcajce.provider.asymmetric.x509.ExtCRLException;
import org.bouncycastle.jcajce.provider.asymmetric.x509.X509CRLImpl;
import org.bouncycastle.jcajce.provider.asymmetric.x509.X509CRLInternal;
import org.bouncycastle.jcajce.provider.asymmetric.x509.X509SignatureUtil;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.util.Arrays;

class X509CRLObject
extends X509CRLImpl {
    private final Object cacheLock = new Object();
    private X509CRLInternal internalCRLValue;
    private volatile boolean hashValueSet;
    private volatile int hashValue;

    X509CRLObject(JcaJceHelper jcaJceHelper, CertificateList certificateList) throws CRLException {
        super(jcaJceHelper, certificateList, X509CRLObject.createSigAlgName(certificateList), X509CRLObject.createSigAlgParams(certificateList), X509CRLObject.isIndirectCRL(certificateList));
    }

    public byte[] getEncoded() throws CRLException {
        return Arrays.clone(this.getInternalCRL().getEncoded());
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof X509CRLObject) {
            DERBitString dERBitString;
            X509CRLObject x509CRLObject = (X509CRLObject)object;
            if (this.hashValueSet && x509CRLObject.hashValueSet ? this.hashValue != x509CRLObject.hashValue : (null == this.internalCRLValue || null == x509CRLObject.internalCRLValue) && null != (dERBitString = this.c.getSignature()) && !dERBitString.equals(x509CRLObject.c.getSignature())) {
                return false;
            }
            return this.getInternalCRL().equals(x509CRLObject.getInternalCRL());
        }
        return this.getInternalCRL().equals(object);
    }

    public int hashCode() {
        if (!this.hashValueSet) {
            this.hashValue = this.getInternalCRL().hashCode();
            this.hashValueSet = true;
        }
        return this.hashValue;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private X509CRLInternal getInternalCRL() {
        Object object = this.cacheLock;
        synchronized (object) {
            if (null != this.internalCRLValue) {
                return this.internalCRLValue;
            }
        }
        object = null;
        X509CRLException x509CRLException = null;
        try {
            object = this.c.getEncoded("DER");
        } catch (IOException iOException) {
            x509CRLException = new X509CRLException(iOException);
        }
        X509CRLInternal x509CRLInternal = new X509CRLInternal(this.bcHelper, this.c, this.sigAlgName, this.sigAlgParams, this.isIndirect, (byte[])object, x509CRLException);
        Object object2 = this.cacheLock;
        synchronized (object2) {
            if (null == this.internalCRLValue) {
                this.internalCRLValue = x509CRLInternal;
            }
            return this.internalCRLValue;
        }
    }

    private static String createSigAlgName(CertificateList certificateList) throws CRLException {
        try {
            return X509SignatureUtil.getSignatureName(certificateList.getSignatureAlgorithm());
        } catch (Exception exception) {
            throw new X509CRLException("CRL contents invalid: " + exception.getMessage(), exception);
        }
    }

    private static byte[] createSigAlgParams(CertificateList certificateList) throws CRLException {
        try {
            ASN1Encodable aSN1Encodable = certificateList.getSignatureAlgorithm().getParameters();
            if (null == aSN1Encodable) {
                return null;
            }
            return aSN1Encodable.toASN1Primitive().getEncoded("DER");
        } catch (Exception exception) {
            throw new CRLException("CRL contents invalid: " + exception);
        }
    }

    private static boolean isIndirectCRL(CertificateList certificateList) throws CRLException {
        try {
            byte[] byArray = X509CRLObject.getExtensionOctets(certificateList, Extension.issuingDistributionPoint.getId());
            if (null == byArray) {
                return false;
            }
            return IssuingDistributionPoint.getInstance(byArray).isIndirectCRL();
        } catch (Exception exception) {
            throw new ExtCRLException("Exception reading IssuingDistributionPoint", exception);
        }
    }

    private static class X509CRLException
    extends CRLException {
        private final Throwable cause;

        X509CRLException(String string, Throwable throwable) {
            super(string);
            this.cause = throwable;
        }

        X509CRLException(Throwable throwable) {
            this.cause = throwable;
        }

        public Throwable getCause() {
            return this.cause;
        }
    }
}


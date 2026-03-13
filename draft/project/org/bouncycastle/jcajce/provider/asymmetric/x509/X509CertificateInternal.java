/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.security.cert.CertificateEncodingException;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.jcajce.provider.asymmetric.x509.X509CertificateImpl;
import org.bouncycastle.jcajce.util.JcaJceHelper;

class X509CertificateInternal
extends X509CertificateImpl {
    private final byte[] encoding;
    private final CertificateEncodingException exception;

    X509CertificateInternal(JcaJceHelper jcaJceHelper, Certificate certificate, BasicConstraints basicConstraints, boolean[] blArray, String string, byte[] byArray, byte[] byArray2, CertificateEncodingException certificateEncodingException) {
        super(jcaJceHelper, certificate, basicConstraints, blArray, string, byArray);
        this.encoding = byArray2;
        this.exception = certificateEncodingException;
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        if (null != this.exception) {
            throw this.exception;
        }
        if (null == this.encoding) {
            throw new CertificateEncodingException();
        }
        return this.encoding;
    }
}


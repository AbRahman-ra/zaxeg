/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.security.cert.CRLException;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.jcajce.provider.asymmetric.x509.X509CRLImpl;
import org.bouncycastle.jcajce.util.JcaJceHelper;

class X509CRLInternal
extends X509CRLImpl {
    private final byte[] encoding;
    private final CRLException exception;

    X509CRLInternal(JcaJceHelper jcaJceHelper, CertificateList certificateList, String string, byte[] byArray, boolean bl, byte[] byArray2, CRLException cRLException) {
        super(jcaJceHelper, certificateList, string, byArray, bl);
        this.encoding = byArray2;
        this.exception = cRLException;
    }

    public byte[] getEncoded() throws CRLException {
        if (null != this.exception) {
            throw this.exception;
        }
        if (null == this.encoding) {
            throw new CRLException();
        }
        return this.encoding;
    }
}


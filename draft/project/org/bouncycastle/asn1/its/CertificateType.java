/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Primitive;

public class CertificateType {
    public static final CertificateType Explicit = new CertificateType(0);
    public static final CertificateType Implicit = new CertificateType(1);
    private final ASN1Enumerated enumerated;

    protected CertificateType(int n) {
        this.enumerated = new ASN1Enumerated(n);
    }

    private CertificateType(ASN1Enumerated aSN1Enumerated) {
        this.enumerated = aSN1Enumerated;
    }

    public CertificateType getInstance(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof CertificateType) {
            return (CertificateType)object;
        }
        return new CertificateType(ASN1Enumerated.getInstance(object));
    }

    public ASN1Primitive toASN1Primitive() {
        return this.enumerated;
    }
}


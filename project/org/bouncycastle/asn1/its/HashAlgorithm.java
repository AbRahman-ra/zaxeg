/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Primitive;

public class HashAlgorithm {
    public static final HashAlgorithm sha256 = new HashAlgorithm(0);
    public static final HashAlgorithm sha384 = new HashAlgorithm(1);
    private final ASN1Enumerated enumerated;

    protected HashAlgorithm(int n) {
        this.enumerated = new ASN1Enumerated(n);
    }

    private HashAlgorithm(ASN1Enumerated aSN1Enumerated) {
        this.enumerated = aSN1Enumerated;
    }

    public HashAlgorithm getInstance(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof HashAlgorithm) {
            return (HashAlgorithm)object;
        }
        return new HashAlgorithm(ASN1Enumerated.getInstance(object));
    }

    public ASN1Primitive toASN1Primitive() {
        return this.enumerated;
    }
}


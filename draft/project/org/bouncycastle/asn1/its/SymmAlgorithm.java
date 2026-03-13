/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

public class SymmAlgorithm
extends ASN1Object {
    public static SymmAlgorithm aes128Ccm = new SymmAlgorithm(new ASN1Enumerated(0));
    private ASN1Enumerated symmAlgorithm;

    private SymmAlgorithm(ASN1Enumerated aSN1Enumerated) {
        this.symmAlgorithm = aSN1Enumerated;
    }

    public SymmAlgorithm(int n) {
        this.symmAlgorithm = new ASN1Enumerated(n);
    }

    public SymmAlgorithm getInstance(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof SymmAlgorithm) {
            return (SymmAlgorithm)object;
        }
        return new SymmAlgorithm(ASN1Enumerated.getInstance(object));
    }

    public ASN1Enumerated getSymmAlgorithm() {
        return this.symmAlgorithm;
    }

    public void setSymmAlgorithm(ASN1Enumerated aSN1Enumerated) {
        this.symmAlgorithm = aSN1Enumerated;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.symmAlgorithm.toASN1Primitive();
    }
}


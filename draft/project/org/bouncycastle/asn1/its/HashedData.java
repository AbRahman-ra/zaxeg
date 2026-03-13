/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;

public class HashedData
extends ASN1Object
implements ASN1Choice {
    private ASN1OctetString hashData;

    public HashedData(byte[] byArray) {
        this.hashData = new DEROctetString(byArray);
    }

    private HashedData(ASN1OctetString aSN1OctetString) {
        this.hashData = aSN1OctetString;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.hashData;
    }

    public ASN1OctetString getHashData() {
        return this.hashData;
    }

    public void setHashData(ASN1OctetString aSN1OctetString) {
        this.hashData = aSN1OctetString;
    }
}


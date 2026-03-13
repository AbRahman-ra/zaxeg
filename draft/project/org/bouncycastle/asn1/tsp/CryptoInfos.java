/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.tsp;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.Attribute;

public class CryptoInfos
extends ASN1Object {
    private ASN1Sequence attributes;

    public static CryptoInfos getInstance(Object object) {
        if (object instanceof CryptoInfos) {
            return (CryptoInfos)object;
        }
        if (object != null) {
            return new CryptoInfos(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public static CryptoInfos getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        return CryptoInfos.getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, bl));
    }

    private CryptoInfos(ASN1Sequence aSN1Sequence) {
        this.attributes = aSN1Sequence;
    }

    public CryptoInfos(Attribute[] attributeArray) {
        this.attributes = new DERSequence(attributeArray);
    }

    public Attribute[] getAttributes() {
        Attribute[] attributeArray = new Attribute[this.attributes.size()];
        for (int i = 0; i != attributeArray.length; ++i) {
            attributeArray[i] = Attribute.getInstance(this.attributes.getObjectAt(i));
        }
        return attributeArray;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.attributes;
    }
}


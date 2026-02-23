/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.esf;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.esf.SigPolicyQualifierInfo;

public class SigPolicyQualifiers
extends ASN1Object {
    ASN1Sequence qualifiers;

    public static SigPolicyQualifiers getInstance(Object object) {
        if (object instanceof SigPolicyQualifiers) {
            return (SigPolicyQualifiers)object;
        }
        if (object instanceof ASN1Sequence) {
            return new SigPolicyQualifiers(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    private SigPolicyQualifiers(ASN1Sequence aSN1Sequence) {
        this.qualifiers = aSN1Sequence;
    }

    public SigPolicyQualifiers(SigPolicyQualifierInfo[] sigPolicyQualifierInfoArray) {
        this.qualifiers = new DERSequence(sigPolicyQualifierInfoArray);
    }

    public int size() {
        return this.qualifiers.size();
    }

    public SigPolicyQualifierInfo getInfoAt(int n) {
        return SigPolicyQualifierInfo.getInstance(this.qualifiers.getObjectAt(n));
    }

    public ASN1Primitive toASN1Primitive() {
        return this.qualifiers;
    }
}


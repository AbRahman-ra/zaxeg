/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.its.SequenceOfOctetString;
import org.bouncycastle.asn1.its.Utils;

public class AesCcmCiphertext
extends ASN1Object {
    private final byte[] nonce;
    private final SequenceOfOctetString opaque;

    private AesCcmCiphertext(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() != 2) {
            throw new IllegalArgumentException("sequence not length 2");
        }
        this.nonce = Utils.octetStringFixed(ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(0)).getOctets(), 12);
        this.opaque = SequenceOfOctetString.getInstance(aSN1Sequence.getObjectAt(1));
    }

    public static AesCcmCiphertext getInstance(Object object) {
        if (object instanceof AesCcmCiphertext) {
            return (AesCcmCiphertext)object;
        }
        if (object != null) {
            return new AesCcmCiphertext(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(new DEROctetString(this.nonce));
        aSN1EncodableVector.add(this.opaque);
        return new DERSequence(aSN1EncodableVector);
    }
}


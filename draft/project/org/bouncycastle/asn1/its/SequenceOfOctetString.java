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
import org.bouncycastle.util.Arrays;

public class SequenceOfOctetString
extends ASN1Object {
    private byte[][] octetStrings;

    private SequenceOfOctetString(ASN1Sequence aSN1Sequence) {
        this.octetStrings = SequenceOfOctetString.toByteArrays(aSN1Sequence);
    }

    public static SequenceOfOctetString getInstance(Object object) {
        if (object instanceof SequenceOfOctetString) {
            return (SequenceOfOctetString)object;
        }
        if (object != null) {
            return new SequenceOfOctetString(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public int size() {
        return this.octetStrings.length;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (int i = 0; i != this.octetStrings.length; ++i) {
            aSN1EncodableVector.add(new DEROctetString(Arrays.clone(this.octetStrings[i])));
        }
        return new DERSequence(aSN1EncodableVector);
    }

    static byte[][] toByteArrays(ASN1Sequence aSN1Sequence) {
        byte[][] byArrayArray = new byte[aSN1Sequence.size()][];
        for (int i = 0; i != aSN1Sequence.size(); ++i) {
            byArrayArray[i] = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(i)).getOctets();
        }
        return byArrayArray;
    }
}


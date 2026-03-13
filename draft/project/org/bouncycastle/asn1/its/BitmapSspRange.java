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
import org.bouncycastle.asn1.its.Utils;
import org.bouncycastle.util.Arrays;

public class BitmapSspRange
extends ASN1Object {
    private final byte[] sspValue;
    private final byte[] sspBitmask;

    private BitmapSspRange(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() != 2) {
            throw new IllegalArgumentException("expected sequence with sspValue and sspBitmask");
        }
        this.sspValue = Utils.octetStringFixed(ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(0)).getOctets());
        this.sspBitmask = Utils.octetStringFixed(ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(1)).getOctets());
    }

    public static BitmapSspRange getInstance(Object object) {
        if (object instanceof BitmapSspRange) {
            return (BitmapSspRange)object;
        }
        if (object != null) {
            return new BitmapSspRange(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public byte[] getSspValue() {
        return Arrays.clone(this.sspValue);
    }

    public byte[] getSspBitmask() {
        return Arrays.clone(this.sspBitmask);
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(new DEROctetString(this.sspValue));
        aSN1EncodableVector.add(new DEROctetString(this.sspBitmask));
        return new DERSequence(aSN1EncodableVector);
    }
}


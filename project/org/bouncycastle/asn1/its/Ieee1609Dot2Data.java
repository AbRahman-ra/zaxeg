/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.its.Ieee1609Dot2Content;

public class Ieee1609Dot2Data
extends ASN1Object {
    private final BigInteger protcolVersion;
    private final Ieee1609Dot2Content content;

    private Ieee1609Dot2Data(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() != 2) {
            throw new IllegalArgumentException("sequence not length 2");
        }
        this.protcolVersion = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0)).getValue();
        this.content = Ieee1609Dot2Content.getInstance(aSN1Sequence.getObjectAt(1));
    }

    public static Ieee1609Dot2Data getInstance(Object object) {
        if (object instanceof Ieee1609Dot2Data) {
            return (Ieee1609Dot2Data)object;
        }
        if (object != null) {
            return new Ieee1609Dot2Data(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        return new DERSequence(aSN1EncodableVector);
    }
}


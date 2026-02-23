/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.its.RectangularRegion;

public class SequenceOfRectangularRegion
extends ASN1Object {
    private final RectangularRegion[] sequence;

    private SequenceOfRectangularRegion(ASN1Sequence aSN1Sequence) {
        this.sequence = new RectangularRegion[aSN1Sequence.size()];
        for (int i = 0; i != aSN1Sequence.size(); ++i) {
            this.sequence[i] = RectangularRegion.getInstance(aSN1Sequence.getObjectAt(i));
        }
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence(this.sequence);
    }
}


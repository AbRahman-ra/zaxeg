/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

public class CircularRegion
extends ASN1Object {
    private CircularRegion(ASN1Sequence aSN1Sequence) {
    }

    public static CircularRegion getInstance(Object object) {
        if (object instanceof CircularRegion) {
            return (CircularRegion)object;
        }
        if (object != null) {
            return new CircularRegion(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        return null;
    }
}


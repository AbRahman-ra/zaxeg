/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class HeaderInfo
extends ASN1Object {
    private HeaderInfo(ASN1Sequence aSN1Sequence) {
    }

    public static HeaderInfo getInstance(Object object) {
        if (object instanceof HeaderInfo) {
            return (HeaderInfo)object;
        }
        if (object != null) {
            return new HeaderInfo(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        return new DERSequence(aSN1EncodableVector);
    }
}


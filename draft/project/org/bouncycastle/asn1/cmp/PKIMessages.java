/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cmp.PKIMessage;

public class PKIMessages
extends ASN1Object {
    private ASN1Sequence content;

    private PKIMessages(ASN1Sequence aSN1Sequence) {
        this.content = aSN1Sequence;
    }

    public static PKIMessages getInstance(Object object) {
        if (object instanceof PKIMessages) {
            return (PKIMessages)object;
        }
        if (object != null) {
            return new PKIMessages(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public PKIMessages(PKIMessage pKIMessage) {
        this.content = new DERSequence(pKIMessage);
    }

    public PKIMessages(PKIMessage[] pKIMessageArray) {
        this.content = new DERSequence(pKIMessageArray);
    }

    public PKIMessage[] toPKIMessageArray() {
        PKIMessage[] pKIMessageArray = new PKIMessage[this.content.size()];
        for (int i = 0; i != pKIMessageArray.length; ++i) {
            pKIMessageArray[i] = PKIMessage.getInstance(this.content.getObjectAt(i));
        }
        return pKIMessageArray;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.content;
    }
}


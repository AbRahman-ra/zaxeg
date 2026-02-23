/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cmc.OtherMsg;
import org.bouncycastle.asn1.cmc.TaggedAttribute;
import org.bouncycastle.asn1.cmc.TaggedContentInfo;
import org.bouncycastle.asn1.cmc.TaggedRequest;

public class PKIData
extends ASN1Object {
    private final TaggedAttribute[] controlSequence;
    private final TaggedRequest[] reqSequence;
    private final TaggedContentInfo[] cmsSequence;
    private final OtherMsg[] otherMsgSequence;

    public PKIData(TaggedAttribute[] taggedAttributeArray, TaggedRequest[] taggedRequestArray, TaggedContentInfo[] taggedContentInfoArray, OtherMsg[] otherMsgArray) {
        this.controlSequence = this.copy(taggedAttributeArray);
        this.reqSequence = this.copy(taggedRequestArray);
        this.cmsSequence = this.copy(taggedContentInfoArray);
        this.otherMsgSequence = this.copy(otherMsgArray);
    }

    private PKIData(ASN1Sequence aSN1Sequence) {
        int n;
        if (aSN1Sequence.size() != 4) {
            throw new IllegalArgumentException("Sequence not 4 elements.");
        }
        ASN1Sequence aSN1Sequence2 = (ASN1Sequence)aSN1Sequence.getObjectAt(0);
        this.controlSequence = new TaggedAttribute[aSN1Sequence2.size()];
        for (n = 0; n < this.controlSequence.length; ++n) {
            this.controlSequence[n] = TaggedAttribute.getInstance(aSN1Sequence2.getObjectAt(n));
        }
        aSN1Sequence2 = (ASN1Sequence)aSN1Sequence.getObjectAt(1);
        this.reqSequence = new TaggedRequest[aSN1Sequence2.size()];
        for (n = 0; n < this.reqSequence.length; ++n) {
            this.reqSequence[n] = TaggedRequest.getInstance(aSN1Sequence2.getObjectAt(n));
        }
        aSN1Sequence2 = (ASN1Sequence)aSN1Sequence.getObjectAt(2);
        this.cmsSequence = new TaggedContentInfo[aSN1Sequence2.size()];
        for (n = 0; n < this.cmsSequence.length; ++n) {
            this.cmsSequence[n] = TaggedContentInfo.getInstance(aSN1Sequence2.getObjectAt(n));
        }
        aSN1Sequence2 = (ASN1Sequence)aSN1Sequence.getObjectAt(3);
        this.otherMsgSequence = new OtherMsg[aSN1Sequence2.size()];
        for (n = 0; n < this.otherMsgSequence.length; ++n) {
            this.otherMsgSequence[n] = OtherMsg.getInstance(aSN1Sequence2.getObjectAt(n));
        }
    }

    public static PKIData getInstance(Object object) {
        if (object instanceof PKIData) {
            return (PKIData)object;
        }
        if (object != null) {
            return new PKIData(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence(new ASN1Encodable[]{new DERSequence(this.controlSequence), new DERSequence(this.reqSequence), new DERSequence(this.cmsSequence), new DERSequence(this.otherMsgSequence)});
    }

    public TaggedAttribute[] getControlSequence() {
        return this.copy(this.controlSequence);
    }

    private TaggedAttribute[] copy(TaggedAttribute[] taggedAttributeArray) {
        TaggedAttribute[] taggedAttributeArray2 = new TaggedAttribute[taggedAttributeArray.length];
        System.arraycopy(taggedAttributeArray, 0, taggedAttributeArray2, 0, taggedAttributeArray2.length);
        return taggedAttributeArray2;
    }

    public TaggedRequest[] getReqSequence() {
        return this.copy(this.reqSequence);
    }

    private TaggedRequest[] copy(TaggedRequest[] taggedRequestArray) {
        TaggedRequest[] taggedRequestArray2 = new TaggedRequest[taggedRequestArray.length];
        System.arraycopy(taggedRequestArray, 0, taggedRequestArray2, 0, taggedRequestArray2.length);
        return taggedRequestArray2;
    }

    public TaggedContentInfo[] getCmsSequence() {
        return this.copy(this.cmsSequence);
    }

    private TaggedContentInfo[] copy(TaggedContentInfo[] taggedContentInfoArray) {
        TaggedContentInfo[] taggedContentInfoArray2 = new TaggedContentInfo[taggedContentInfoArray.length];
        System.arraycopy(taggedContentInfoArray, 0, taggedContentInfoArray2, 0, taggedContentInfoArray2.length);
        return taggedContentInfoArray2;
    }

    public OtherMsg[] getOtherMsgSequence() {
        return this.copy(this.otherMsgSequence);
    }

    private OtherMsg[] copy(OtherMsg[] otherMsgArray) {
        OtherMsg[] otherMsgArray2 = new OtherMsg[otherMsgArray.length];
        System.arraycopy(otherMsgArray, 0, otherMsgArray2, 0, otherMsgArray2.length);
        return otherMsgArray2;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.crmf;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.crmf.SinglePubInfo;

public class PKIPublicationInfo
extends ASN1Object {
    public static final ASN1Integer dontPublish = new ASN1Integer(0L);
    public static final ASN1Integer pleasePublish = new ASN1Integer(1L);
    private ASN1Integer action;
    private ASN1Sequence pubInfos;

    private PKIPublicationInfo(ASN1Sequence aSN1Sequence) {
        this.action = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() > 1) {
            this.pubInfos = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(1));
        }
    }

    public static PKIPublicationInfo getInstance(Object object) {
        if (object instanceof PKIPublicationInfo) {
            return (PKIPublicationInfo)object;
        }
        if (object != null) {
            return new PKIPublicationInfo(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public PKIPublicationInfo(BigInteger bigInteger) {
        this(new ASN1Integer(bigInteger));
    }

    public PKIPublicationInfo(ASN1Integer aSN1Integer) {
        this.action = aSN1Integer;
    }

    public PKIPublicationInfo(SinglePubInfo singlePubInfo) {
        SinglePubInfo[] singlePubInfoArray;
        if (singlePubInfo != null) {
            SinglePubInfo[] singlePubInfoArray2 = new SinglePubInfo[1];
            singlePubInfoArray = singlePubInfoArray2;
            singlePubInfoArray2[0] = singlePubInfo;
        } else {
            singlePubInfoArray = null;
        }
        this(singlePubInfoArray);
    }

    public PKIPublicationInfo(SinglePubInfo[] singlePubInfoArray) {
        this.action = pleasePublish;
        this.pubInfos = singlePubInfoArray != null ? new DERSequence(singlePubInfoArray) : null;
    }

    public ASN1Integer getAction() {
        return this.action;
    }

    public SinglePubInfo[] getPubInfos() {
        if (this.pubInfos == null) {
            return null;
        }
        SinglePubInfo[] singlePubInfoArray = new SinglePubInfo[this.pubInfos.size()];
        for (int i = 0; i != singlePubInfoArray.length; ++i) {
            singlePubInfoArray[i] = SinglePubInfo.getInstance(this.pubInfos.getObjectAt(i));
        }
        return singlePubInfoArray;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        aSN1EncodableVector.add(this.action);
        if (this.pubInfos != null) {
            aSN1EncodableVector.add(this.pubInfos);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}


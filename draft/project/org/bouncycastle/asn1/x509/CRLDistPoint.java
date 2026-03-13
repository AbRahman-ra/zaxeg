/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.util.Strings;

public class CRLDistPoint
extends ASN1Object {
    ASN1Sequence seq = null;

    public static CRLDistPoint getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        return CRLDistPoint.getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, bl));
    }

    public static CRLDistPoint getInstance(Object object) {
        if (object instanceof CRLDistPoint) {
            return (CRLDistPoint)object;
        }
        if (object != null) {
            return new CRLDistPoint(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public static CRLDistPoint fromExtensions(Extensions extensions) {
        return CRLDistPoint.getInstance(Extensions.getExtensionParsedValue(extensions, Extension.cRLDistributionPoints));
    }

    private CRLDistPoint(ASN1Sequence aSN1Sequence) {
        this.seq = aSN1Sequence;
    }

    public CRLDistPoint(DistributionPoint[] distributionPointArray) {
        this.seq = new DERSequence(distributionPointArray);
    }

    public DistributionPoint[] getDistributionPoints() {
        DistributionPoint[] distributionPointArray = new DistributionPoint[this.seq.size()];
        for (int i = 0; i != this.seq.size(); ++i) {
            distributionPointArray[i] = DistributionPoint.getInstance(this.seq.getObjectAt(i));
        }
        return distributionPointArray;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.seq;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        String string = Strings.lineSeparator();
        stringBuffer.append("CRLDistPoint:");
        stringBuffer.append(string);
        DistributionPoint[] distributionPointArray = this.getDistributionPoints();
        for (int i = 0; i != distributionPointArray.length; ++i) {
            stringBuffer.append("    ");
            stringBuffer.append(distributionPointArray[i]);
            stringBuffer.append(string);
        }
        return stringBuffer.toString();
    }
}


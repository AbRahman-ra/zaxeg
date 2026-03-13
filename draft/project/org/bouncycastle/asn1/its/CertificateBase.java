/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.its.CertificateType;
import org.bouncycastle.asn1.its.ExplicitCertificate;
import org.bouncycastle.asn1.its.ImplicitCertificate;

public class CertificateBase
extends ASN1Object {
    private CertificateType type;
    private byte[] version;

    protected CertificateBase(ASN1Sequence aSN1Sequence) {
    }

    public static CertificateBase getInstance(Object object) {
        if (object instanceof ImplicitCertificate) {
            return (ImplicitCertificate)object;
        }
        if (object instanceof ExplicitCertificate) {
            return (ExplicitCertificate)object;
        }
        if (object != null) {
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(object);
            if (aSN1Sequence.getObjectAt(1).equals(CertificateType.Implicit)) {
                return ImplicitCertificate.getInstance(aSN1Sequence);
            }
            if (aSN1Sequence.getObjectAt(1).equals(CertificateType.Explicit)) {
                return ExplicitCertificate.getInstance(aSN1Sequence);
            }
            throw new IllegalArgumentException("unknown certificate type");
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        return new DERSequence(aSN1EncodableVector);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.its.EndEntityType;
import org.bouncycastle.asn1.its.SubjectPermissions;

public class PsidGroupPermissions
extends ASN1Object {
    private final SubjectPermissions subjectPermissions;
    private final BigInteger minChainLength;
    private final BigInteger chainLengthRange;
    private final Object eeType;

    private PsidGroupPermissions(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() != 2) {
            throw new IllegalArgumentException("sequence not length 2");
        }
        this.subjectPermissions = SubjectPermissions.getInstance(aSN1Sequence.getObjectAt(0));
        this.minChainLength = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(1)).getValue();
        this.chainLengthRange = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(2)).getValue();
        this.eeType = EndEntityType.getInstance(aSN1Sequence.getObjectAt(3));
    }

    public static PsidGroupPermissions getInstance(Object object) {
        if (object instanceof PsidGroupPermissions) {
            return (PsidGroupPermissions)object;
        }
        if (object != null) {
            return new PsidGroupPermissions(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        return null;
    }
}


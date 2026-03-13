/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cms.TimeStampTokenEvidence;
import org.bouncycastle.asn1.tsp.EvidenceRecord;

public class Evidence
extends ASN1Object
implements ASN1Choice {
    private TimeStampTokenEvidence tstEvidence;
    private EvidenceRecord ersEvidence;
    private ASN1Sequence otherEvidence;

    public Evidence(TimeStampTokenEvidence timeStampTokenEvidence) {
        this.tstEvidence = timeStampTokenEvidence;
    }

    public Evidence(EvidenceRecord evidenceRecord) {
        this.ersEvidence = evidenceRecord;
    }

    private Evidence(ASN1TaggedObject aSN1TaggedObject) {
        if (aSN1TaggedObject.getTagNo() == 0) {
            this.tstEvidence = TimeStampTokenEvidence.getInstance(aSN1TaggedObject, false);
        } else if (aSN1TaggedObject.getTagNo() == 1) {
            this.ersEvidence = EvidenceRecord.getInstance(aSN1TaggedObject, false);
        } else if (aSN1TaggedObject.getTagNo() == 2) {
            this.otherEvidence = ASN1Sequence.getInstance(aSN1TaggedObject, false);
        } else {
            throw new IllegalArgumentException("unknown tag in Evidence");
        }
    }

    public static Evidence getInstance(Object object) {
        if (object == null || object instanceof Evidence) {
            return (Evidence)object;
        }
        if (object instanceof ASN1TaggedObject) {
            return new Evidence(ASN1TaggedObject.getInstance(object));
        }
        throw new IllegalArgumentException("unknown object in getInstance");
    }

    public static Evidence getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        return Evidence.getInstance(aSN1TaggedObject.getObject());
    }

    public TimeStampTokenEvidence getTstEvidence() {
        return this.tstEvidence;
    }

    public EvidenceRecord getErsEvidence() {
        return this.ersEvidence;
    }

    public ASN1Primitive toASN1Primitive() {
        if (this.tstEvidence != null) {
            return new DERTaggedObject(false, 0, this.tstEvidence);
        }
        if (this.ersEvidence != null) {
            return new DERTaggedObject(false, 1, this.ersEvidence);
        }
        return new DERTaggedObject(false, 2, this.otherEvidence);
    }
}


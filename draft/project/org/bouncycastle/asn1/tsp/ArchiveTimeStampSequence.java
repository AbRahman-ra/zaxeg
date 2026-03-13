/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.tsp;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.tsp.ArchiveTimeStampChain;

public class ArchiveTimeStampSequence
extends ASN1Object {
    private ASN1Sequence archiveTimeStampChains;

    public static ArchiveTimeStampSequence getInstance(Object object) {
        if (object instanceof ArchiveTimeStampChain) {
            return (ArchiveTimeStampSequence)object;
        }
        if (object != null) {
            return new ArchiveTimeStampSequence(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    private ArchiveTimeStampSequence(ASN1Sequence aSN1Sequence) throws IllegalArgumentException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(aSN1Sequence.size());
        Enumeration enumeration = aSN1Sequence.getObjects();
        while (enumeration.hasMoreElements()) {
            aSN1EncodableVector.add(ArchiveTimeStampChain.getInstance(enumeration.nextElement()));
        }
        this.archiveTimeStampChains = new DERSequence(aSN1EncodableVector);
    }

    public ArchiveTimeStampSequence(ArchiveTimeStampChain archiveTimeStampChain) {
        this.archiveTimeStampChains = new DERSequence(archiveTimeStampChain);
    }

    public ArchiveTimeStampSequence(ArchiveTimeStampChain[] archiveTimeStampChainArray) {
        this.archiveTimeStampChains = new DERSequence(archiveTimeStampChainArray);
    }

    public ArchiveTimeStampChain[] getArchiveTimeStampChains() {
        ArchiveTimeStampChain[] archiveTimeStampChainArray = new ArchiveTimeStampChain[this.archiveTimeStampChains.size()];
        for (int i = 0; i != archiveTimeStampChainArray.length; ++i) {
            archiveTimeStampChainArray[i] = ArchiveTimeStampChain.getInstance(this.archiveTimeStampChains.getObjectAt(i));
        }
        return archiveTimeStampChainArray;
    }

    public int size() {
        return this.archiveTimeStampChains.size();
    }

    public ArchiveTimeStampSequence append(ArchiveTimeStampChain archiveTimeStampChain) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(this.archiveTimeStampChains.size() + 1);
        for (int i = 0; i != this.archiveTimeStampChains.size(); ++i) {
            aSN1EncodableVector.add(this.archiveTimeStampChains.getObjectAt(i));
        }
        aSN1EncodableVector.add(archiveTimeStampChain);
        return new ArchiveTimeStampSequence(new DERSequence(aSN1EncodableVector));
    }

    public ASN1Primitive toASN1Primitive() {
        return this.archiveTimeStampChains;
    }
}


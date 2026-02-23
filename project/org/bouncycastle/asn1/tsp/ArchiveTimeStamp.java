/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.tsp;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cms.Attributes;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.tsp.PartialHashtree;
import org.bouncycastle.asn1.tsp.TSTInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class ArchiveTimeStamp
extends ASN1Object {
    private final AlgorithmIdentifier digestAlgorithm;
    private final Attributes attributes;
    private final ASN1Sequence reducedHashTree;
    private final ContentInfo timeStamp;

    public static ArchiveTimeStamp getInstance(Object object) {
        if (object instanceof ArchiveTimeStamp) {
            return (ArchiveTimeStamp)object;
        }
        if (object != null) {
            return new ArchiveTimeStamp(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public ArchiveTimeStamp(AlgorithmIdentifier algorithmIdentifier, PartialHashtree[] partialHashtreeArray, ContentInfo contentInfo) {
        this(algorithmIdentifier, null, partialHashtreeArray, contentInfo);
    }

    public ArchiveTimeStamp(ContentInfo contentInfo) {
        this(null, null, null, contentInfo);
    }

    public ArchiveTimeStamp(AlgorithmIdentifier algorithmIdentifier, Attributes attributes, PartialHashtree[] partialHashtreeArray, ContentInfo contentInfo) {
        this.digestAlgorithm = algorithmIdentifier;
        this.attributes = attributes;
        this.reducedHashTree = partialHashtreeArray != null ? new DERSequence(partialHashtreeArray) : null;
        this.timeStamp = contentInfo;
    }

    private ArchiveTimeStamp(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() < 1 || aSN1Sequence.size() > 4) {
            throw new IllegalArgumentException("wrong sequence size in constructor: " + aSN1Sequence.size());
        }
        AlgorithmIdentifier algorithmIdentifier = null;
        Attributes attributes = null;
        ASN1Sequence aSN1Sequence2 = null;
        block5: for (int i = 0; i < aSN1Sequence.size() - 1; ++i) {
            ASN1Encodable aSN1Encodable = aSN1Sequence.getObjectAt(i);
            if (!(aSN1Encodable instanceof ASN1TaggedObject)) continue;
            ASN1TaggedObject aSN1TaggedObject = ASN1TaggedObject.getInstance(aSN1Encodable);
            switch (aSN1TaggedObject.getTagNo()) {
                case 0: {
                    algorithmIdentifier = AlgorithmIdentifier.getInstance(aSN1TaggedObject, false);
                    continue block5;
                }
                case 1: {
                    attributes = Attributes.getInstance(aSN1TaggedObject, false);
                    continue block5;
                }
                case 2: {
                    aSN1Sequence2 = ASN1Sequence.getInstance(aSN1TaggedObject, false);
                    continue block5;
                }
                default: {
                    throw new IllegalArgumentException("invalid tag no in constructor: " + aSN1TaggedObject.getTagNo());
                }
            }
        }
        this.digestAlgorithm = algorithmIdentifier;
        this.attributes = attributes;
        this.reducedHashTree = aSN1Sequence2;
        this.timeStamp = ContentInfo.getInstance(aSN1Sequence.getObjectAt(aSN1Sequence.size() - 1));
    }

    public AlgorithmIdentifier getDigestAlgorithmIdentifier() {
        if (this.digestAlgorithm != null) {
            return this.digestAlgorithm;
        }
        if (this.timeStamp.getContentType().equals(CMSObjectIdentifiers.signedData)) {
            SignedData signedData = SignedData.getInstance(this.timeStamp.getContent());
            if (signedData.getEncapContentInfo().getContentType().equals(PKCSObjectIdentifiers.id_ct_TSTInfo)) {
                TSTInfo tSTInfo = TSTInfo.getInstance(signedData.getEncapContentInfo());
                return tSTInfo.getMessageImprint().getHashAlgorithm();
            }
            throw new IllegalStateException("cannot parse time stamp");
        }
        throw new IllegalStateException("cannot identify algorithm identifier for digest");
    }

    public AlgorithmIdentifier getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    public PartialHashtree[] getReducedHashTree() {
        if (this.reducedHashTree == null) {
            return null;
        }
        PartialHashtree[] partialHashtreeArray = new PartialHashtree[this.reducedHashTree.size()];
        for (int i = 0; i != partialHashtreeArray.length; ++i) {
            partialHashtreeArray[i] = PartialHashtree.getInstance(this.reducedHashTree.getObjectAt(i));
        }
        return partialHashtreeArray;
    }

    public ContentInfo getTimeStamp() {
        return this.timeStamp;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(4);
        if (this.digestAlgorithm != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.digestAlgorithm));
        }
        if (this.attributes != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.attributes));
        }
        if (this.reducedHashTree != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 2, this.reducedHashTree));
        }
        aSN1EncodableVector.add(this.timeStamp);
        return new DERSequence(aSN1EncodableVector);
    }
}


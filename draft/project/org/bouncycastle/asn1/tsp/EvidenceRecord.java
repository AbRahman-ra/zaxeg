/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.tsp;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.tsp.ArchiveTimeStamp;
import org.bouncycastle.asn1.tsp.ArchiveTimeStampChain;
import org.bouncycastle.asn1.tsp.ArchiveTimeStampSequence;
import org.bouncycastle.asn1.tsp.CryptoInfos;
import org.bouncycastle.asn1.tsp.EncryptionInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class EvidenceRecord
extends ASN1Object {
    private static final ASN1ObjectIdentifier OID = new ASN1ObjectIdentifier("1.3.6.1.5.5.11.0.2.1");
    private ASN1Integer version = new ASN1Integer(1L);
    private ASN1Sequence digestAlgorithms;
    private CryptoInfos cryptoInfos;
    private EncryptionInfo encryptionInfo;
    private ArchiveTimeStampSequence archiveTimeStampSequence;

    public static EvidenceRecord getInstance(Object object) {
        if (object instanceof EvidenceRecord) {
            return (EvidenceRecord)object;
        }
        if (object != null) {
            return new EvidenceRecord(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public static EvidenceRecord getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        return EvidenceRecord.getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, bl));
    }

    private EvidenceRecord(EvidenceRecord evidenceRecord, ArchiveTimeStampSequence archiveTimeStampSequence, ArchiveTimeStamp archiveTimeStamp) {
        this.version = evidenceRecord.version;
        if (archiveTimeStamp != null) {
            AlgorithmIdentifier algorithmIdentifier = archiveTimeStamp.getDigestAlgorithmIdentifier();
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            Enumeration enumeration = evidenceRecord.digestAlgorithms.getObjects();
            boolean bl = false;
            while (enumeration.hasMoreElements()) {
                AlgorithmIdentifier algorithmIdentifier2 = AlgorithmIdentifier.getInstance(enumeration.nextElement());
                aSN1EncodableVector.add(algorithmIdentifier2);
                if (!algorithmIdentifier2.equals(algorithmIdentifier)) continue;
                bl = true;
                break;
            }
            if (!bl) {
                aSN1EncodableVector.add(algorithmIdentifier);
                this.digestAlgorithms = new DERSequence(aSN1EncodableVector);
            } else {
                this.digestAlgorithms = evidenceRecord.digestAlgorithms;
            }
        } else {
            this.digestAlgorithms = evidenceRecord.digestAlgorithms;
        }
        this.cryptoInfos = evidenceRecord.cryptoInfos;
        this.encryptionInfo = evidenceRecord.encryptionInfo;
        this.archiveTimeStampSequence = archiveTimeStampSequence;
    }

    public EvidenceRecord(CryptoInfos cryptoInfos, EncryptionInfo encryptionInfo, ArchiveTimeStamp archiveTimeStamp) {
        this.digestAlgorithms = new DERSequence(archiveTimeStamp.getDigestAlgorithmIdentifier());
        this.cryptoInfos = cryptoInfos;
        this.encryptionInfo = encryptionInfo;
        this.archiveTimeStampSequence = new ArchiveTimeStampSequence(new ArchiveTimeStampChain(archiveTimeStamp));
    }

    public EvidenceRecord(AlgorithmIdentifier[] algorithmIdentifierArray, CryptoInfos cryptoInfos, EncryptionInfo encryptionInfo, ArchiveTimeStampSequence archiveTimeStampSequence) {
        this.digestAlgorithms = new DERSequence(algorithmIdentifierArray);
        this.cryptoInfos = cryptoInfos;
        this.encryptionInfo = encryptionInfo;
        this.archiveTimeStampSequence = archiveTimeStampSequence;
    }

    private EvidenceRecord(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() < 3 && aSN1Sequence.size() > 5) {
            throw new IllegalArgumentException("wrong sequence size in constructor: " + aSN1Sequence.size());
        }
        ASN1Integer aSN1Integer = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0));
        if (!aSN1Integer.hasValue(1)) {
            throw new IllegalArgumentException("incompatible version");
        }
        this.version = aSN1Integer;
        this.digestAlgorithms = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(1));
        for (int i = 2; i != aSN1Sequence.size() - 1; ++i) {
            ASN1Encodable aSN1Encodable = aSN1Sequence.getObjectAt(i);
            if (aSN1Encodable instanceof ASN1TaggedObject) {
                ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject)aSN1Encodable;
                switch (aSN1TaggedObject.getTagNo()) {
                    case 0: {
                        this.cryptoInfos = CryptoInfos.getInstance(aSN1TaggedObject, false);
                        break;
                    }
                    case 1: {
                        this.encryptionInfo = EncryptionInfo.getInstance(aSN1TaggedObject, false);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("unknown tag in getInstance: " + aSN1TaggedObject.getTagNo());
                    }
                }
                continue;
            }
            throw new IllegalArgumentException("unknown object in getInstance: " + aSN1Encodable.getClass().getName());
        }
        this.archiveTimeStampSequence = ArchiveTimeStampSequence.getInstance(aSN1Sequence.getObjectAt(aSN1Sequence.size() - 1));
    }

    public AlgorithmIdentifier[] getDigestAlgorithms() {
        AlgorithmIdentifier[] algorithmIdentifierArray = new AlgorithmIdentifier[this.digestAlgorithms.size()];
        for (int i = 0; i != algorithmIdentifierArray.length; ++i) {
            algorithmIdentifierArray[i] = AlgorithmIdentifier.getInstance(this.digestAlgorithms.getObjectAt(i));
        }
        return algorithmIdentifierArray;
    }

    public ArchiveTimeStampSequence getArchiveTimeStampSequence() {
        return this.archiveTimeStampSequence;
    }

    public EvidenceRecord addArchiveTimeStamp(ArchiveTimeStamp archiveTimeStamp, boolean bl) {
        if (bl) {
            ArchiveTimeStampChain archiveTimeStampChain = new ArchiveTimeStampChain(archiveTimeStamp);
            return new EvidenceRecord(this, this.archiveTimeStampSequence.append(archiveTimeStampChain), archiveTimeStamp);
        }
        ArchiveTimeStampChain[] archiveTimeStampChainArray = this.archiveTimeStampSequence.getArchiveTimeStampChains();
        archiveTimeStampChainArray[archiveTimeStampChainArray.length - 1] = archiveTimeStampChainArray[archiveTimeStampChainArray.length - 1].append(archiveTimeStamp);
        return new EvidenceRecord(this, new ArchiveTimeStampSequence(archiveTimeStampChainArray), null);
    }

    public String toString() {
        return "EvidenceRecord: Oid(" + OID + ")";
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(5);
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.digestAlgorithms);
        if (null != this.cryptoInfos) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.cryptoInfos));
        }
        if (null != this.encryptionInfo) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.encryptionInfo));
        }
        aSN1EncodableVector.add(this.archiveTimeStampSequence);
        return new DERSequence(aSN1EncodableVector);
    }
}


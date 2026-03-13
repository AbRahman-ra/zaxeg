/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.io.IOException;
import java.util.Date;
import org.bouncycastle.asn1.tsp.ArchiveTimeStamp;
import org.bouncycastle.asn1.tsp.ArchiveTimeStampChain;
import org.bouncycastle.asn1.tsp.ArchiveTimeStampSequence;
import org.bouncycastle.asn1.tsp.EvidenceRecord;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.ers.ERSArchiveTimeStamp;
import org.bouncycastle.tsp.ers.ERSData;
import org.bouncycastle.tsp.ers.ERSException;

public class ERSEvidenceRecord {
    private final EvidenceRecord evidenceRecord;
    private final DigestCalculatorProvider digestCalculatorProvider;
    private final ERSArchiveTimeStamp lastArchiveTimeStamp;

    public ERSEvidenceRecord(byte[] byArray, DigestCalculatorProvider digestCalculatorProvider) throws TSPException, ERSException {
        this(EvidenceRecord.getInstance(byArray), digestCalculatorProvider);
    }

    public ERSEvidenceRecord(EvidenceRecord evidenceRecord, DigestCalculatorProvider digestCalculatorProvider) throws TSPException, ERSException {
        this.evidenceRecord = evidenceRecord;
        this.digestCalculatorProvider = digestCalculatorProvider;
        ArchiveTimeStampSequence archiveTimeStampSequence = evidenceRecord.getArchiveTimeStampSequence();
        ArchiveTimeStampChain[] archiveTimeStampChainArray = archiveTimeStampSequence.getArchiveTimeStampChains();
        ArchiveTimeStampChain archiveTimeStampChain = archiveTimeStampChainArray[archiveTimeStampChainArray.length - 1];
        ArchiveTimeStamp[] archiveTimeStampArray = archiveTimeStampChain.getArchiveTimestamps();
        this.lastArchiveTimeStamp = new ERSArchiveTimeStamp(archiveTimeStampArray[archiveTimeStampArray.length - 1], digestCalculatorProvider);
    }

    public ERSArchiveTimeStamp getLastArchiveTimeStamp() {
        return this.lastArchiveTimeStamp;
    }

    public void validatePresent(ERSData eRSData, Date date) throws ERSException, OperatorCreationException {
        this.lastArchiveTimeStamp.validatePresent(eRSData, date);
    }

    public void validatePresent(byte[] byArray, Date date) throws ERSException, OperatorCreationException {
        this.lastArchiveTimeStamp.validatePresent(byArray, date);
    }

    public X509CertificateHolder getSigningCertificate() {
        return this.lastArchiveTimeStamp.getSigningCertificate();
    }

    public void validate(SignerInformationVerifier signerInformationVerifier) throws TSPException {
        this.lastArchiveTimeStamp.validate(signerInformationVerifier);
    }

    public byte[] getEncoded() throws IOException {
        return this.evidenceRecord.getEncoded();
    }
}


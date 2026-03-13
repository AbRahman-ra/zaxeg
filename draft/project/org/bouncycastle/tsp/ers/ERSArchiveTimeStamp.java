/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import org.bouncycastle.asn1.tsp.ArchiveTimeStamp;
import org.bouncycastle.asn1.tsp.PartialHashtree;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.ers.ArchiveTimeStampValidationException;
import org.bouncycastle.tsp.ers.BinaryTreeRootCalculator;
import org.bouncycastle.tsp.ers.ERSData;
import org.bouncycastle.tsp.ers.ERSException;
import org.bouncycastle.tsp.ers.ERSRootNodeCalculator;
import org.bouncycastle.tsp.ers.ERSUtil;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Store;

public class ERSArchiveTimeStamp {
    private final ArchiveTimeStamp archiveTimeStamp;
    private final DigestCalculator digCalc;
    private final TimeStampToken timeStampToken;
    private ERSRootNodeCalculator rootNodeCalculator = new BinaryTreeRootCalculator();

    public ERSArchiveTimeStamp(byte[] byArray, DigestCalculatorProvider digestCalculatorProvider) throws TSPException, ERSException {
        this(ArchiveTimeStamp.getInstance(byArray), digestCalculatorProvider);
    }

    public ERSArchiveTimeStamp(ArchiveTimeStamp archiveTimeStamp, DigestCalculatorProvider digestCalculatorProvider) throws TSPException, ERSException {
        try {
            this.archiveTimeStamp = archiveTimeStamp;
            this.timeStampToken = new TimeStampToken(archiveTimeStamp.getTimeStamp());
            this.digCalc = digestCalculatorProvider.get(archiveTimeStamp.getDigestAlgorithmIdentifier());
        } catch (IOException iOException) {
            throw new ERSException(iOException.getMessage(), iOException);
        } catch (OperatorCreationException operatorCreationException) {
            throw new ERSException(operatorCreationException.getMessage(), operatorCreationException);
        }
    }

    ERSArchiveTimeStamp(ArchiveTimeStamp archiveTimeStamp, DigestCalculator digestCalculator, ERSRootNodeCalculator eRSRootNodeCalculator) throws TSPException, ERSException {
        try {
            this.archiveTimeStamp = archiveTimeStamp;
            this.timeStampToken = new TimeStampToken(archiveTimeStamp.getTimeStamp());
            this.digCalc = digestCalculator;
            this.rootNodeCalculator = eRSRootNodeCalculator;
        } catch (IOException iOException) {
            throw new ERSException(iOException.getMessage(), iOException);
        }
    }

    public AlgorithmIdentifier getDigestAlgorithmIdentifier() {
        return this.archiveTimeStamp.getDigestAlgorithmIdentifier();
    }

    public void validatePresent(ERSData eRSData, Date date) throws ERSException, OperatorCreationException {
        this.validatePresent(eRSData.getHash(this.digCalc), date);
    }

    public void validatePresent(byte[] byArray, Date date) throws ERSException, OperatorCreationException {
        if (this.timeStampToken.getTimeStampInfo().getGenTime().after(date)) {
            throw new ArchiveTimeStampValidationException("timestamp generation time is in the future");
        }
        this.checkContainsHashValue(byArray, this.digCalc);
        PartialHashtree[] partialHashtreeArray = this.archiveTimeStamp.getReducedHashTree();
        byte[] byArray2 = partialHashtreeArray != null ? this.rootNodeCalculator.computeRootHash(this.digCalc, this.archiveTimeStamp.getReducedHashTree()) : byArray;
        this.checkTimeStampValid(this.timeStampToken, byArray2);
    }

    public TimeStampToken getTimeStampToken() {
        return this.timeStampToken;
    }

    public X509CertificateHolder getSigningCertificate() {
        Collection<X509CertificateHolder> collection;
        Store<X509CertificateHolder> store = this.timeStampToken.getCertificates();
        if (store != null && !(collection = store.getMatches(this.timeStampToken.getSID())).isEmpty()) {
            return collection.iterator().next();
        }
        return null;
    }

    public void validate(SignerInformationVerifier signerInformationVerifier) throws TSPException {
        this.timeStampToken.validate(signerInformationVerifier);
    }

    void checkContainsHashValue(byte[] byArray, DigestCalculator digestCalculator) throws ArchiveTimeStampValidationException {
        PartialHashtree[] partialHashtreeArray = this.archiveTimeStamp.getReducedHashTree();
        if (partialHashtreeArray != null) {
            for (int i = 0; i != partialHashtreeArray.length; ++i) {
                PartialHashtree partialHashtree = partialHashtreeArray[i];
                if (partialHashtree.containsHash(byArray)) {
                    return;
                }
                if (partialHashtree.getValueCount() <= 1 || !Arrays.areEqual(byArray, ERSUtil.calculateBranchHash(digestCalculator, partialHashtree.getValues()))) continue;
                return;
            }
            throw new ArchiveTimeStampValidationException("object hash not found");
        }
        if (!Arrays.areEqual(byArray, this.timeStampToken.getTimeStampInfo().getMessageImprintDigest())) {
            throw new ArchiveTimeStampValidationException("object hash not found in wrapped timestamp");
        }
    }

    void checkTimeStampValid(TimeStampToken timeStampToken, byte[] byArray) throws ArchiveTimeStampValidationException {
        if (byArray != null && !Arrays.areEqual(byArray, timeStampToken.getTimeStampInfo().getMessageImprintDigest())) {
            throw new ArchiveTimeStampValidationException("timestamp hash does not match root");
        }
    }

    public Date getGenTime() {
        return this.timeStampToken.getTimeStampInfo().getGenTime();
    }

    public Date getExpiryTime() {
        X509CertificateHolder x509CertificateHolder = this.getSigningCertificate();
        if (x509CertificateHolder != null) {
            return x509CertificateHolder.getNotAfter();
        }
        return null;
    }

    public ArchiveTimeStamp toASN1Structure() {
        return this.archiveTimeStamp;
    }

    public byte[] getEncoded() throws IOException {
        return this.archiveTimeStamp.getEncoded();
    }
}


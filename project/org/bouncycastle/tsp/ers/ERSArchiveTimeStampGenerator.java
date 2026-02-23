/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.bouncycastle.asn1.tsp.ArchiveTimeStamp;
import org.bouncycastle.asn1.tsp.PartialHashtree;
import org.bouncycastle.asn1.tsp.TSTInfo;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.ers.BinaryTreeRootCalculator;
import org.bouncycastle.tsp.ers.ERSArchiveTimeStamp;
import org.bouncycastle.tsp.ers.ERSData;
import org.bouncycastle.tsp.ers.ERSDataGroup;
import org.bouncycastle.tsp.ers.ERSException;
import org.bouncycastle.tsp.ers.ERSRootNodeCalculator;
import org.bouncycastle.tsp.ers.ERSUtil;
import org.bouncycastle.util.Arrays;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ERSArchiveTimeStampGenerator {
    private final DigestCalculator digCalc;
    private List<ERSData> dataObjects = new ArrayList<ERSData>();
    private ERSRootNodeCalculator rootNodeCalculator = new BinaryTreeRootCalculator();

    public ERSArchiveTimeStampGenerator(DigestCalculator digestCalculator) {
        this.digCalc = digestCalculator;
    }

    public void addData(ERSData eRSData) {
        this.dataObjects.add(eRSData);
    }

    public void addAllData(List<ERSData> list) {
        this.dataObjects.addAll(list);
    }

    public TimeStampRequest generateTimeStampRequest(TimeStampRequestGenerator timeStampRequestGenerator) throws TSPException, IOException {
        PartialHashtree[] partialHashtreeArray = this.getPartialHashtrees();
        byte[] byArray = this.rootNodeCalculator.computeRootHash(this.digCalc, partialHashtreeArray);
        return timeStampRequestGenerator.generate(this.digCalc.getAlgorithmIdentifier(), byArray);
    }

    public TimeStampRequest generateTimeStampRequest(TimeStampRequestGenerator timeStampRequestGenerator, BigInteger bigInteger) throws TSPException, IOException {
        PartialHashtree[] partialHashtreeArray = this.getPartialHashtrees();
        byte[] byArray = this.rootNodeCalculator.computeRootHash(this.digCalc, partialHashtreeArray);
        return timeStampRequestGenerator.generate(this.digCalc.getAlgorithmIdentifier(), byArray, bigInteger);
    }

    public ERSArchiveTimeStamp generateArchiveTimeStamp(TimeStampResponse timeStampResponse) throws TSPException, ERSException {
        PartialHashtree[] partialHashtreeArray = this.getPartialHashtrees();
        byte[] byArray = this.rootNodeCalculator.computeRootHash(this.digCalc, partialHashtreeArray);
        TSTInfo tSTInfo = timeStampResponse.getTimeStampToken().getTimeStampInfo().toASN1Structure();
        if (!tSTInfo.getMessageImprint().getHashAlgorithm().equals(this.digCalc.getAlgorithmIdentifier())) {
            throw new ERSException("time stamp imprint for wrong algorithm");
        }
        if (!Arrays.areEqual(tSTInfo.getMessageImprint().getHashedMessage(), byArray)) {
            throw new ERSException("time stamp imprint for wrong root hash");
        }
        ArchiveTimeStamp archiveTimeStamp = partialHashtreeArray.length == 1 ? new ArchiveTimeStamp(null, null, timeStampResponse.getTimeStampToken().toCMSSignedData().toASN1Structure()) : new ArchiveTimeStamp(this.digCalc.getAlgorithmIdentifier(), partialHashtreeArray, timeStampResponse.getTimeStampToken().toCMSSignedData().toASN1Structure());
        return new ERSArchiveTimeStamp(archiveTimeStamp, this.digCalc, this.rootNodeCalculator);
    }

    private PartialHashtree[] getPartialHashtrees() {
        int n;
        List<byte[]> list = ERSUtil.buildHashList(this.digCalc, this.dataObjects);
        PartialHashtree[] partialHashtreeArray = new PartialHashtree[list.size()];
        HashSet<ERSDataGroup> hashSet = new HashSet<ERSDataGroup>();
        for (n = 0; n != this.dataObjects.size(); ++n) {
            if (!(this.dataObjects.get(n) instanceof ERSDataGroup)) continue;
            hashSet.add((ERSDataGroup)this.dataObjects.get(n));
        }
        for (n = 0; n != list.size(); ++n) {
            byte[] byArray = list.get(n);
            ERSDataGroup eRSDataGroup = null;
            for (ERSDataGroup eRSDataGroup2 : hashSet) {
                byte[] byArray2 = eRSDataGroup2.getHash(this.digCalc);
                if (!Arrays.areEqual(byArray2, byArray)) continue;
                List<byte[]> list2 = eRSDataGroup2.getHashes(this.digCalc);
                partialHashtreeArray[n] = new PartialHashtree((byte[][])list2.toArray((T[])new byte[list2.size()][]));
                eRSDataGroup = eRSDataGroup2;
                break;
            }
            if (eRSDataGroup == null) {
                partialHashtreeArray[n] = new PartialHashtree(byArray);
                continue;
            }
            hashSet.remove(eRSDataGroup);
        }
        return partialHashtreeArray;
    }
}


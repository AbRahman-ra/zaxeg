/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.tsp.ers.ERSCachingData;
import org.bouncycastle.tsp.ers.ERSData;
import org.bouncycastle.tsp.ers.ERSUtil;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ERSDataGroup
extends ERSCachingData {
    protected List<ERSData> dataObjects;

    public ERSDataGroup(ERSData ... eRSDataArray) {
        this.dataObjects = new ArrayList<ERSData>(eRSDataArray.length);
        this.dataObjects.addAll(Arrays.asList(eRSDataArray));
    }

    public ERSDataGroup(List<ERSData> list) {
        this.dataObjects = new ArrayList<ERSData>(list.size());
        this.dataObjects.addAll(list);
    }

    public ERSDataGroup(ERSData eRSData) {
        this.dataObjects = Collections.singletonList(eRSData);
    }

    public List<byte[]> getHashes(DigestCalculator digestCalculator) {
        return ERSUtil.buildHashList(digestCalculator, this.dataObjects);
    }

    @Override
    protected byte[] calculateHash(DigestCalculator digestCalculator) {
        List<byte[]> list = this.getHashes(digestCalculator);
        if (list.size() > 1) {
            return ERSUtil.calculateDigest(digestCalculator, list.iterator());
        }
        return list.get(0);
    }

    public int size() {
        return this.dataObjects.size();
    }
}


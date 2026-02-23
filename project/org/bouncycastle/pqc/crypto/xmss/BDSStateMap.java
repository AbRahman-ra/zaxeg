/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.pqc.crypto.xmss.BDS;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSUtil;
import org.bouncycastle.util.Integers;

public class BDSStateMap
implements Serializable {
    private static final long serialVersionUID = -3464451825208522308L;
    private final Map<Integer, BDS> bdsState = new TreeMap<Integer, BDS>();
    private transient long maxIndex;

    BDSStateMap(long l) {
        this.maxIndex = l;
    }

    BDSStateMap(BDSStateMap bDSStateMap, long l) {
        for (Integer n : bDSStateMap.bdsState.keySet()) {
            this.bdsState.put(n, new BDS(bDSStateMap.bdsState.get(n)));
        }
        this.maxIndex = l;
    }

    BDSStateMap(XMSSMTParameters xMSSMTParameters, long l, byte[] byArray, byte[] byArray2) {
        this.maxIndex = (1L << xMSSMTParameters.getHeight()) - 1L;
        for (long i = 0L; i < l; ++i) {
            this.updateState(xMSSMTParameters, i, byArray, byArray2);
        }
    }

    public long getMaxIndex() {
        return this.maxIndex;
    }

    void updateState(XMSSMTParameters xMSSMTParameters, long l, byte[] byArray, byte[] byArray2) {
        XMSSParameters xMSSParameters = xMSSMTParameters.getXMSSParameters();
        int n = xMSSParameters.getHeight();
        long l2 = XMSSUtil.getTreeIndex(l, n);
        int n2 = XMSSUtil.getLeafIndex(l, n);
        OTSHashAddress oTSHashAddress = (OTSHashAddress)((OTSHashAddress.Builder)new OTSHashAddress.Builder().withTreeAddress(l2)).withOTSAddress(n2).build();
        if (n2 < (1 << n) - 1) {
            if (this.get(0) == null || n2 == 0) {
                this.put(0, new BDS(xMSSParameters, byArray, byArray2, oTSHashAddress));
            }
            this.update(0, byArray, byArray2, oTSHashAddress);
        }
        for (int i = 1; i < xMSSMTParameters.getLayers(); ++i) {
            n2 = XMSSUtil.getLeafIndex(l2, n);
            l2 = XMSSUtil.getTreeIndex(l2, n);
            oTSHashAddress = (OTSHashAddress)((OTSHashAddress.Builder)((OTSHashAddress.Builder)new OTSHashAddress.Builder().withLayerAddress(i)).withTreeAddress(l2)).withOTSAddress(n2).build();
            if (this.bdsState.get(i) == null || XMSSUtil.isNewBDSInitNeeded(l, n, i)) {
                this.bdsState.put(i, new BDS(xMSSParameters, byArray, byArray2, oTSHashAddress));
            }
            if (n2 >= (1 << n) - 1 || !XMSSUtil.isNewAuthenticationPathNeeded(l, n, i)) continue;
            this.update(i, byArray, byArray2, oTSHashAddress);
        }
    }

    public boolean isEmpty() {
        return this.bdsState.isEmpty();
    }

    BDS get(int n) {
        return this.bdsState.get(Integers.valueOf(n));
    }

    BDS update(int n, byte[] byArray, byte[] byArray2, OTSHashAddress oTSHashAddress) {
        return this.bdsState.put(Integers.valueOf(n), this.bdsState.get(Integers.valueOf(n)).getNextState(byArray, byArray2, oTSHashAddress));
    }

    void put(int n, BDS bDS) {
        this.bdsState.put(Integers.valueOf(n), bDS);
    }

    public BDSStateMap withWOTSDigest(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        BDSStateMap bDSStateMap = new BDSStateMap(this.maxIndex);
        for (Integer n : this.bdsState.keySet()) {
            bDSStateMap.bdsState.put(n, this.bdsState.get(n).withWOTSDigest(aSN1ObjectIdentifier));
        }
        return bDSStateMap;
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.maxIndex = objectInputStream.available() != 0 ? objectInputStream.readLong() : 0L;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeLong(this.maxIndex);
    }
}


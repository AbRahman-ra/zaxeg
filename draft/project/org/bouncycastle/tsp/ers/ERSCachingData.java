/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.tsp.ers.ERSData;

public abstract class ERSCachingData
implements ERSData {
    private Map<AlgorithmIdentifier, byte[]> preCalcs = new HashMap<AlgorithmIdentifier, byte[]>();

    public byte[] getHash(DigestCalculator digestCalculator) {
        AlgorithmIdentifier algorithmIdentifier = digestCalculator.getAlgorithmIdentifier();
        if (this.preCalcs.containsKey(algorithmIdentifier)) {
            return this.preCalcs.get(algorithmIdentifier);
        }
        byte[] byArray = this.calculateHash(digestCalculator);
        this.preCalcs.put(algorithmIdentifier, byArray);
        return byArray;
    }

    protected abstract byte[] calculateHash(DigestCalculator var1);
}


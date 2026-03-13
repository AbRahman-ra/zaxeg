/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import org.bouncycastle.asn1.tsp.PartialHashtree;
import org.bouncycastle.operator.DigestCalculator;

public interface ERSRootNodeCalculator {
    public byte[] computeRootHash(DigestCalculator var1, PartialHashtree[] var2);
}


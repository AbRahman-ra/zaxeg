/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import org.bouncycastle.operator.DigestCalculator;

public interface ERSData {
    public byte[] getHash(DigestCalculator var1);
}


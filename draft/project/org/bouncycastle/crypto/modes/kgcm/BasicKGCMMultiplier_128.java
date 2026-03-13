/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.kgcm;

import org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier;
import org.bouncycastle.crypto.modes.kgcm.KGCMUtil_128;

public class BasicKGCMMultiplier_128
implements KGCMMultiplier {
    private final long[] H = new long[2];

    public void init(long[] lArray) {
        KGCMUtil_128.copy(lArray, this.H);
    }

    public void multiplyH(long[] lArray) {
        KGCMUtil_128.multiply(lArray, this.H, lArray);
    }
}


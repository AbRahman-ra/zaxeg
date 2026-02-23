/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.kgcm;

import org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier;
import org.bouncycastle.crypto.modes.kgcm.KGCMUtil_512;

public class BasicKGCMMultiplier_512
implements KGCMMultiplier {
    private final long[] H = new long[8];

    public void init(long[] lArray) {
        KGCMUtil_512.copy(lArray, this.H);
    }

    public void multiplyH(long[] lArray) {
        KGCMUtil_512.multiply(lArray, this.H, lArray);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.kgcm;

import org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier;
import org.bouncycastle.crypto.modes.kgcm.KGCMUtil_128;

public class Tables4kKGCMMultiplier_128
implements KGCMMultiplier {
    private long[][] T;

    public void init(long[] lArray) {
        if (this.T == null) {
            this.T = new long[256][2];
        } else if (KGCMUtil_128.equal(lArray, this.T[1])) {
            return;
        }
        KGCMUtil_128.copy(lArray, this.T[1]);
        for (int i = 2; i < 256; i += 2) {
            KGCMUtil_128.multiplyX(this.T[i >> 1], this.T[i]);
            KGCMUtil_128.add(this.T[i], this.T[1], this.T[i + 1]);
        }
    }

    public void multiplyH(long[] lArray) {
        long[] lArray2 = new long[2];
        KGCMUtil_128.copy(this.T[(int)(lArray[1] >>> 56) & 0xFF], lArray2);
        for (int i = 14; i >= 0; --i) {
            KGCMUtil_128.multiplyX8(lArray2, lArray2);
            KGCMUtil_128.add(this.T[(int)(lArray[i >>> 3] >>> ((i & 7) << 3)) & 0xFF], lArray2, lArray2);
        }
        KGCMUtil_128.copy(lArray2, lArray);
    }
}


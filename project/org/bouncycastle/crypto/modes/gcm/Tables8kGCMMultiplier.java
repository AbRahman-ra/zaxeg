/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.gcm;

import org.bouncycastle.crypto.modes.gcm.GCMMultiplier;
import org.bouncycastle.crypto.modes.gcm.GCMUtil;
import org.bouncycastle.util.Pack;

public class Tables8kGCMMultiplier
implements GCMMultiplier {
    private byte[] H;
    private long[][][] T;

    public void init(byte[] byArray) {
        if (this.T == null) {
            this.T = new long[32][16][2];
        } else if (0 != GCMUtil.areEqual(this.H, byArray)) {
            return;
        }
        this.H = new byte[16];
        GCMUtil.copy(byArray, this.H);
        for (int i = 0; i < 32; ++i) {
            long[][] lArray = this.T[i];
            if (i == 0) {
                GCMUtil.asLongs(this.H, lArray[1]);
                GCMUtil.multiplyP3(lArray[1], lArray[1]);
            } else {
                GCMUtil.multiplyP4(this.T[i - 1][1], lArray[1]);
            }
            for (int j = 2; j < 16; j += 2) {
                GCMUtil.divideP(lArray[j >> 1], lArray[j]);
                GCMUtil.xor(lArray[j], lArray[1], lArray[j + 1]);
            }
        }
    }

    public void multiplyH(byte[] byArray) {
        long l = 0L;
        long l2 = 0L;
        for (int i = 15; i >= 0; --i) {
            long[] lArray = this.T[i + i + 1][byArray[i] & 0xF];
            long[] lArray2 = this.T[i + i][(byArray[i] & 0xF0) >>> 4];
            l ^= lArray[0] ^ lArray2[0];
            l2 ^= lArray[1] ^ lArray2[1];
        }
        Pack.longToBigEndian(l, byArray, 0);
        Pack.longToBigEndian(l2, byArray, 8);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.gcm;

import org.bouncycastle.crypto.modes.gcm.GCMMultiplier;
import org.bouncycastle.crypto.modes.gcm.GCMUtil;
import org.bouncycastle.util.Pack;

public class Tables64kGCMMultiplier
implements GCMMultiplier {
    private byte[] H;
    private long[][][] T;

    public void init(byte[] byArray) {
        if (this.T == null) {
            this.T = new long[16][256][2];
        } else if (0 != GCMUtil.areEqual(this.H, byArray)) {
            return;
        }
        this.H = new byte[16];
        GCMUtil.copy(byArray, this.H);
        for (int i = 0; i < 16; ++i) {
            long[][] lArray = this.T[i];
            if (i == 0) {
                GCMUtil.asLongs(this.H, lArray[1]);
                GCMUtil.multiplyP7(lArray[1], lArray[1]);
            } else {
                GCMUtil.multiplyP8(this.T[i - 1][1], lArray[1]);
            }
            for (int j = 2; j < 256; j += 2) {
                GCMUtil.divideP(lArray[j >> 1], lArray[j]);
                GCMUtil.xor(lArray[j], lArray[1], lArray[j + 1]);
            }
        }
    }

    public void multiplyH(byte[] byArray) {
        long[] lArray = this.T[15][byArray[15] & 0xFF];
        long l = lArray[0];
        long l2 = lArray[1];
        for (int i = 14; i >= 0; --i) {
            lArray = this.T[i][byArray[i] & 0xFF];
            l ^= lArray[0];
            l2 ^= lArray[1];
        }
        Pack.longToBigEndian(l, byArray, 0);
        Pack.longToBigEndian(l2, byArray, 8);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.gcm;

import org.bouncycastle.crypto.modes.gcm.GCMMultiplier;
import org.bouncycastle.crypto.modes.gcm.GCMUtil;
import org.bouncycastle.util.Pack;

public class Tables4kGCMMultiplier
implements GCMMultiplier {
    private byte[] H;
    private long[][] T;

    public void init(byte[] byArray) {
        if (this.T == null) {
            this.T = new long[256][2];
        } else if (0 != GCMUtil.areEqual(this.H, byArray)) {
            return;
        }
        this.H = new byte[16];
        GCMUtil.copy(byArray, this.H);
        GCMUtil.asLongs(this.H, this.T[1]);
        GCMUtil.multiplyP7(this.T[1], this.T[1]);
        for (int i = 2; i < 256; i += 2) {
            GCMUtil.divideP(this.T[i >> 1], this.T[i]);
            GCMUtil.xor(this.T[i], this.T[1], this.T[i + 1]);
        }
    }

    public void multiplyH(byte[] byArray) {
        long[] lArray = this.T[byArray[15] & 0xFF];
        long l = lArray[0];
        long l2 = lArray[1];
        for (int i = 14; i >= 0; --i) {
            lArray = this.T[byArray[i] & 0xFF];
            long l3 = l2 << 56;
            l2 = lArray[1] ^ (l2 >>> 8 | l << 56);
            l = lArray[0] ^ l >>> 8 ^ l3 ^ l3 >>> 1 ^ l3 >>> 2 ^ l3 >>> 7;
        }
        Pack.longToBigEndian(l, byArray, 0);
        Pack.longToBigEndian(l2, byArray, 8);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.gcm;

import org.bouncycastle.crypto.modes.gcm.GCMExponentiator;
import org.bouncycastle.crypto.modes.gcm.GCMUtil;

public class BasicGCMExponentiator
implements GCMExponentiator {
    private long[] x;

    public void init(byte[] byArray) {
        this.x = GCMUtil.asLongs(byArray);
    }

    public void exponentiateX(long l, byte[] byArray) {
        long[] lArray = GCMUtil.oneAsLongs();
        if (l > 0L) {
            long[] lArray2 = new long[2];
            GCMUtil.copy(this.x, lArray2);
            do {
                if ((l & 1L) != 0L) {
                    GCMUtil.multiply(lArray, lArray2);
                }
                GCMUtil.square(lArray2, lArray2);
            } while ((l >>>= 1) > 0L);
        }
        GCMUtil.asBytes(lArray, byArray);
    }
}


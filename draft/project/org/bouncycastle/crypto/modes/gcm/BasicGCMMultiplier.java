/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.gcm;

import org.bouncycastle.crypto.modes.gcm.GCMMultiplier;
import org.bouncycastle.crypto.modes.gcm.GCMUtil;

public class BasicGCMMultiplier
implements GCMMultiplier {
    private long[] H;

    public void init(byte[] byArray) {
        this.H = GCMUtil.asLongs(byArray);
    }

    public void multiplyH(byte[] byArray) {
        long[] lArray = GCMUtil.asLongs(byArray);
        GCMUtil.multiply(lArray, this.H);
        GCMUtil.asBytes(lArray, byArray);
    }
}


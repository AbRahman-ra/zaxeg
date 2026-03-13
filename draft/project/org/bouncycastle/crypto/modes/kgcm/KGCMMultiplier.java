/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.kgcm;

public interface KGCMMultiplier {
    public void init(long[] var1);

    public void multiplyH(long[] var1);
}


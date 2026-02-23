/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement.kdf;

import org.bouncycastle.crypto.DerivationParameters;

public class GSKKDFParameters
implements DerivationParameters {
    private final byte[] z;
    private final int startCounter;
    private final byte[] nonce;

    public GSKKDFParameters(byte[] byArray, int n) {
        this(byArray, n, null);
    }

    public GSKKDFParameters(byte[] byArray, int n, byte[] byArray2) {
        this.z = byArray;
        this.startCounter = n;
        this.nonce = byArray2;
    }

    public byte[] getZ() {
        return this.z;
    }

    public int getStartCounter() {
        return this.startCounter;
    }

    public byte[] getNonce() {
        return this.nonce;
    }
}


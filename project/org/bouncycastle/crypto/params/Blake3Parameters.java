/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.util.Arrays;

public class Blake3Parameters
implements CipherParameters {
    private static final int KEYLEN = 32;
    private byte[] theKey;
    private byte[] theContext;

    public static Blake3Parameters context(byte[] byArray) {
        if (byArray == null) {
            throw new IllegalArgumentException("Invalid context");
        }
        Blake3Parameters blake3Parameters = new Blake3Parameters();
        blake3Parameters.theContext = Arrays.clone(byArray);
        return blake3Parameters;
    }

    public static Blake3Parameters key(byte[] byArray) {
        if (byArray == null || byArray.length != 32) {
            throw new IllegalArgumentException("Invalid keyLength");
        }
        Blake3Parameters blake3Parameters = new Blake3Parameters();
        blake3Parameters.theKey = Arrays.clone(byArray);
        return blake3Parameters;
    }

    public byte[] getKey() {
        return Arrays.clone(this.theKey);
    }

    public void clearKey() {
        Arrays.fill(this.theKey, (byte)0);
    }

    public byte[] getContext() {
        return Arrays.clone(this.theContext);
    }
}


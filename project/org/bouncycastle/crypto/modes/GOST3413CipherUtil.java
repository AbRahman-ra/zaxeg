/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes;

import org.bouncycastle.util.Arrays;

class GOST3413CipherUtil {
    GOST3413CipherUtil() {
    }

    public static byte[] MSB(byte[] byArray, int n) {
        return Arrays.copyOf(byArray, n);
    }

    public static byte[] LSB(byte[] byArray, int n) {
        byte[] byArray2 = new byte[n];
        System.arraycopy(byArray, byArray.length - n, byArray2, 0, n);
        return byArray2;
    }

    public static byte[] sum(byte[] byArray, byte[] byArray2) {
        byte[] byArray3 = new byte[byArray.length];
        for (int i = 0; i < byArray.length; ++i) {
            byArray3[i] = (byte)(byArray[i] ^ byArray2[i]);
        }
        return byArray3;
    }

    public static byte[] copyFromInput(byte[] byArray, int n, int n2) {
        if (byArray.length < n + n2) {
            n = byArray.length - n2;
        }
        byte[] byArray2 = new byte[n];
        System.arraycopy(byArray, n2, byArray2, 0, n);
        return byArray2;
    }
}


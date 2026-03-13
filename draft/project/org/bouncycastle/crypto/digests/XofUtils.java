/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.util.Arrays;

public class XofUtils {
    public static byte[] leftEncode(long l) {
        int n = 1;
        long l2 = l;
        while ((l2 >>= 8) != 0L) {
            n = (byte)(n + 1);
        }
        byte[] byArray = new byte[n + 1];
        byArray[0] = n;
        for (int i = 1; i <= n; ++i) {
            byArray[i] = (byte)(l >> 8 * (n - i));
        }
        return byArray;
    }

    public static byte[] rightEncode(long l) {
        int n = 1;
        long l2 = l;
        while ((l2 >>= 8) != 0L) {
            n = (byte)(n + 1);
        }
        byte[] byArray = new byte[n + 1];
        byArray[n] = n;
        for (int i = 0; i < n; ++i) {
            byArray[i] = (byte)(l >> 8 * (n - i - 1));
        }
        return byArray;
    }

    static byte[] encode(byte by) {
        return Arrays.concatenate(XofUtils.leftEncode(8L), new byte[]{by});
    }

    static byte[] encode(byte[] byArray, int n, int n2) {
        if (byArray.length == n2) {
            return Arrays.concatenate(XofUtils.leftEncode(n2 * 8), byArray);
        }
        return Arrays.concatenate(XofUtils.leftEncode(n2 * 8), Arrays.copyOfRange(byArray, n, n + n2));
    }
}


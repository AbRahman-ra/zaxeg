/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.kgcm;

import org.bouncycastle.math.raw.Interleave;

public class KGCMUtil_128 {
    public static final int SIZE = 2;

    public static void add(long[] lArray, long[] lArray2, long[] lArray3) {
        lArray3[0] = lArray[0] ^ lArray2[0];
        lArray3[1] = lArray[1] ^ lArray2[1];
    }

    public static void copy(long[] lArray, long[] lArray2) {
        lArray2[0] = lArray[0];
        lArray2[1] = lArray[1];
    }

    public static boolean equal(long[] lArray, long[] lArray2) {
        long l = 0L;
        l |= lArray[0] ^ lArray2[0];
        return (l |= lArray[1] ^ lArray2[1]) == 0L;
    }

    public static void multiply(long[] lArray, long[] lArray2, long[] lArray3) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = lArray2[0];
        long l4 = lArray2[1];
        long l5 = 0L;
        long l6 = 0L;
        long l7 = 0L;
        for (int i = 0; i < 64; ++i) {
            long l8 = -(l & 1L);
            l >>>= 1;
            l5 ^= l3 & l8;
            l6 ^= l4 & l8;
            long l9 = -(l2 & 1L);
            l2 >>>= 1;
            l6 ^= l3 & l9;
            l7 ^= l4 & l9;
            long l10 = l4 >> 63;
            l4 = l4 << 1 | l3 >>> 63;
            l3 = l3 << 1 ^ l10 & 0x87L;
        }
        lArray3[0] = l5 ^= l7 ^ l7 << 1 ^ l7 << 2 ^ l7 << 7;
        lArray3[1] = l6 ^= l7 >>> 63 ^ l7 >>> 62 ^ l7 >>> 57;
    }

    public static void multiplyX(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l2 >> 63;
        lArray2[0] = l << 1 ^ l3 & 0x87L;
        lArray2[1] = l2 << 1 | l >>> 63;
    }

    public static void multiplyX8(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l2 >>> 56;
        lArray2[0] = l << 8 ^ l3 ^ l3 << 1 ^ l3 << 2 ^ l3 << 7;
        lArray2[1] = l2 << 8 | l >>> 56;
    }

    public static void one(long[] lArray) {
        lArray[0] = 1L;
        lArray[1] = 0L;
    }

    public static void square(long[] lArray, long[] lArray2) {
        long[] lArray3 = new long[4];
        Interleave.expand64To128(lArray[0], lArray3, 0);
        Interleave.expand64To128(lArray[1], lArray3, 2);
        long l = lArray3[0];
        long l2 = lArray3[1];
        long l3 = lArray3[2];
        long l4 = lArray3[3];
        l2 ^= l4 ^ l4 << 1 ^ l4 << 2 ^ l4 << 7;
        lArray2[0] = l ^= (l3 ^= l4 >>> 63 ^ l4 >>> 62 ^ l4 >>> 57) ^ l3 << 1 ^ l3 << 2 ^ l3 << 7;
        lArray2[1] = l2 ^= l3 >>> 63 ^ l3 >>> 62 ^ l3 >>> 57;
    }

    public static void x(long[] lArray) {
        lArray[0] = 2L;
        lArray[1] = 0L;
    }

    public static void zero(long[] lArray) {
        lArray[0] = 0L;
        lArray[1] = 0L;
    }
}


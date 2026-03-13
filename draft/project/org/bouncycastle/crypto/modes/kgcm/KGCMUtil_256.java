/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.kgcm;

import org.bouncycastle.math.raw.Interleave;

public class KGCMUtil_256 {
    public static final int SIZE = 4;

    public static void add(long[] lArray, long[] lArray2, long[] lArray3) {
        lArray3[0] = lArray[0] ^ lArray2[0];
        lArray3[1] = lArray[1] ^ lArray2[1];
        lArray3[2] = lArray[2] ^ lArray2[2];
        lArray3[3] = lArray[3] ^ lArray2[3];
    }

    public static void copy(long[] lArray, long[] lArray2) {
        lArray2[0] = lArray[0];
        lArray2[1] = lArray[1];
        lArray2[2] = lArray[2];
        lArray2[3] = lArray[3];
    }

    public static boolean equal(long[] lArray, long[] lArray2) {
        long l = 0L;
        l |= lArray[0] ^ lArray2[0];
        l |= lArray[1] ^ lArray2[1];
        l |= lArray[2] ^ lArray2[2];
        return (l |= lArray[3] ^ lArray2[3]) == 0L;
    }

    public static void multiply(long[] lArray, long[] lArray2, long[] lArray3) {
        long l;
        long l2;
        long l3 = lArray[0];
        long l4 = lArray[1];
        long l5 = lArray[2];
        long l6 = lArray[3];
        long l7 = lArray2[0];
        long l8 = lArray2[1];
        long l9 = lArray2[2];
        long l10 = lArray2[3];
        long l11 = 0L;
        long l12 = 0L;
        long l13 = 0L;
        long l14 = 0L;
        long l15 = 0L;
        for (int i = 0; i < 64; ++i) {
            long l16 = -(l3 & 1L);
            l3 >>>= 1;
            l11 ^= l7 & l16;
            l12 ^= l8 & l16;
            l13 ^= l9 & l16;
            l14 ^= l10 & l16;
            l2 = -(l4 & 1L);
            l4 >>>= 1;
            l12 ^= l7 & l2;
            l13 ^= l8 & l2;
            l14 ^= l9 & l2;
            l15 ^= l10 & l2;
            l = l10 >> 63;
            l10 = l10 << 1 | l9 >>> 63;
            l9 = l9 << 1 | l8 >>> 63;
            l8 = l8 << 1 | l7 >>> 63;
            l7 = l7 << 1 ^ l & 0x425L;
        }
        long l17 = l10;
        l10 = l9;
        l9 = l8;
        l8 = l7 ^ l17 >>> 62 ^ l17 >>> 59 ^ l17 >>> 54;
        l7 = l17 ^ l17 << 2 ^ l17 << 5 ^ l17 << 10;
        for (int i = 0; i < 64; ++i) {
            l2 = -(l5 & 1L);
            l5 >>>= 1;
            l11 ^= l7 & l2;
            l12 ^= l8 & l2;
            l13 ^= l9 & l2;
            l14 ^= l10 & l2;
            l = -(l6 & 1L);
            l6 >>>= 1;
            l12 ^= l7 & l;
            l13 ^= l8 & l;
            l14 ^= l9 & l;
            l15 ^= l10 & l;
            long l18 = l10 >> 63;
            l10 = l10 << 1 | l9 >>> 63;
            l9 = l9 << 1 | l8 >>> 63;
            l8 = l8 << 1 | l7 >>> 63;
            l7 = l7 << 1 ^ l18 & 0x425L;
        }
        lArray3[0] = l11 ^= l15 ^ l15 << 2 ^ l15 << 5 ^ l15 << 10;
        lArray3[1] = l12 ^= l15 >>> 62 ^ l15 >>> 59 ^ l15 >>> 54;
        lArray3[2] = l13;
        lArray3[3] = l14;
    }

    public static void multiplyX(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = lArray[2];
        long l4 = lArray[3];
        long l5 = l4 >> 63;
        lArray2[0] = l << 1 ^ l5 & 0x425L;
        lArray2[1] = l2 << 1 | l >>> 63;
        lArray2[2] = l3 << 1 | l2 >>> 63;
        lArray2[3] = l4 << 1 | l3 >>> 63;
    }

    public static void multiplyX8(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = lArray[2];
        long l4 = lArray[3];
        long l5 = l4 >>> 56;
        lArray2[0] = l << 8 ^ l5 ^ l5 << 2 ^ l5 << 5 ^ l5 << 10;
        lArray2[1] = l2 << 8 | l >>> 56;
        lArray2[2] = l3 << 8 | l2 >>> 56;
        lArray2[3] = l4 << 8 | l3 >>> 56;
    }

    public static void one(long[] lArray) {
        lArray[0] = 1L;
        lArray[1] = 0L;
        lArray[2] = 0L;
        lArray[3] = 0L;
    }

    public static void square(long[] lArray, long[] lArray2) {
        int n;
        long[] lArray3 = new long[8];
        for (n = 0; n < 4; ++n) {
            Interleave.expand64To128(lArray[n], lArray3, n << 1);
        }
        n = 8;
        while (--n >= 4) {
            long l = lArray3[n];
            int n2 = n - 4;
            lArray3[n2] = lArray3[n2] ^ (l ^ l << 2 ^ l << 5 ^ l << 10);
            int n3 = n - 4 + 1;
            lArray3[n3] = lArray3[n3] ^ (l >>> 62 ^ l >>> 59 ^ l >>> 54);
        }
        KGCMUtil_256.copy(lArray3, lArray2);
    }

    public static void x(long[] lArray) {
        lArray[0] = 2L;
        lArray[1] = 0L;
        lArray[2] = 0L;
        lArray[3] = 0L;
    }

    public static void zero(long[] lArray) {
        lArray[0] = 0L;
        lArray[1] = 0L;
        lArray[2] = 0L;
        lArray[3] = 0L;
    }
}


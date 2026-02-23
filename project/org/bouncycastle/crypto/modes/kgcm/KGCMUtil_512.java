/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.kgcm;

import org.bouncycastle.math.raw.Interleave;

public class KGCMUtil_512 {
    public static final int SIZE = 8;

    public static void add(long[] lArray, long[] lArray2, long[] lArray3) {
        lArray3[0] = lArray[0] ^ lArray2[0];
        lArray3[1] = lArray[1] ^ lArray2[1];
        lArray3[2] = lArray[2] ^ lArray2[2];
        lArray3[3] = lArray[3] ^ lArray2[3];
        lArray3[4] = lArray[4] ^ lArray2[4];
        lArray3[5] = lArray[5] ^ lArray2[5];
        lArray3[6] = lArray[6] ^ lArray2[6];
        lArray3[7] = lArray[7] ^ lArray2[7];
    }

    public static void copy(long[] lArray, long[] lArray2) {
        lArray2[0] = lArray[0];
        lArray2[1] = lArray[1];
        lArray2[2] = lArray[2];
        lArray2[3] = lArray[3];
        lArray2[4] = lArray[4];
        lArray2[5] = lArray[5];
        lArray2[6] = lArray[6];
        lArray2[7] = lArray[7];
    }

    public static boolean equal(long[] lArray, long[] lArray2) {
        long l = 0L;
        l |= lArray[0] ^ lArray2[0];
        l |= lArray[1] ^ lArray2[1];
        l |= lArray[2] ^ lArray2[2];
        l |= lArray[3] ^ lArray2[3];
        l |= lArray[4] ^ lArray2[4];
        l |= lArray[5] ^ lArray2[5];
        l |= lArray[6] ^ lArray2[6];
        return (l |= lArray[7] ^ lArray2[7]) == 0L;
    }

    public static void multiply(long[] lArray, long[] lArray2, long[] lArray3) {
        long l = lArray2[0];
        long l2 = lArray2[1];
        long l3 = lArray2[2];
        long l4 = lArray2[3];
        long l5 = lArray2[4];
        long l6 = lArray2[5];
        long l7 = lArray2[6];
        long l8 = lArray2[7];
        long l9 = 0L;
        long l10 = 0L;
        long l11 = 0L;
        long l12 = 0L;
        long l13 = 0L;
        long l14 = 0L;
        long l15 = 0L;
        long l16 = 0L;
        long l17 = 0L;
        for (int i = 0; i < 8; i += 2) {
            long l18 = lArray[i];
            long l19 = lArray[i + 1];
            for (int j = 0; j < 64; ++j) {
                long l20 = -(l18 & 1L);
                l18 >>>= 1;
                l9 ^= l & l20;
                l10 ^= l2 & l20;
                l11 ^= l3 & l20;
                l12 ^= l4 & l20;
                l13 ^= l5 & l20;
                l14 ^= l6 & l20;
                l15 ^= l7 & l20;
                l16 ^= l8 & l20;
                long l21 = -(l19 & 1L);
                l19 >>>= 1;
                l10 ^= l & l21;
                l11 ^= l2 & l21;
                l12 ^= l3 & l21;
                l13 ^= l4 & l21;
                l14 ^= l5 & l21;
                l15 ^= l6 & l21;
                l16 ^= l7 & l21;
                l17 ^= l8 & l21;
                long l22 = l8 >> 63;
                l8 = l8 << 1 | l7 >>> 63;
                l7 = l7 << 1 | l6 >>> 63;
                l6 = l6 << 1 | l5 >>> 63;
                l5 = l5 << 1 | l4 >>> 63;
                l4 = l4 << 1 | l3 >>> 63;
                l3 = l3 << 1 | l2 >>> 63;
                l2 = l2 << 1 | l >>> 63;
                l = l << 1 ^ l22 & 0x125L;
            }
            long l23 = l8;
            l8 = l7;
            l7 = l6;
            l6 = l5;
            l5 = l4;
            l4 = l3;
            l3 = l2;
            l2 = l ^ l23 >>> 62 ^ l23 >>> 59 ^ l23 >>> 56;
            l = l23 ^ l23 << 2 ^ l23 << 5 ^ l23 << 8;
        }
        lArray3[0] = l9 ^= l17 ^ l17 << 2 ^ l17 << 5 ^ l17 << 8;
        lArray3[1] = l10 ^= l17 >>> 62 ^ l17 >>> 59 ^ l17 >>> 56;
        lArray3[2] = l11;
        lArray3[3] = l12;
        lArray3[4] = l13;
        lArray3[5] = l14;
        lArray3[6] = l15;
        lArray3[7] = l16;
    }

    public static void multiplyX(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = lArray[2];
        long l4 = lArray[3];
        long l5 = lArray[4];
        long l6 = lArray[5];
        long l7 = lArray[6];
        long l8 = lArray[7];
        long l9 = l8 >> 63;
        lArray2[0] = l << 1 ^ l9 & 0x125L;
        lArray2[1] = l2 << 1 | l >>> 63;
        lArray2[2] = l3 << 1 | l2 >>> 63;
        lArray2[3] = l4 << 1 | l3 >>> 63;
        lArray2[4] = l5 << 1 | l4 >>> 63;
        lArray2[5] = l6 << 1 | l5 >>> 63;
        lArray2[6] = l7 << 1 | l6 >>> 63;
        lArray2[7] = l8 << 1 | l7 >>> 63;
    }

    public static void multiplyX8(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = lArray[2];
        long l4 = lArray[3];
        long l5 = lArray[4];
        long l6 = lArray[5];
        long l7 = lArray[6];
        long l8 = lArray[7];
        long l9 = l8 >>> 56;
        lArray2[0] = l << 8 ^ l9 ^ l9 << 2 ^ l9 << 5 ^ l9 << 8;
        lArray2[1] = l2 << 8 | l >>> 56;
        lArray2[2] = l3 << 8 | l2 >>> 56;
        lArray2[3] = l4 << 8 | l3 >>> 56;
        lArray2[4] = l5 << 8 | l4 >>> 56;
        lArray2[5] = l6 << 8 | l5 >>> 56;
        lArray2[6] = l7 << 8 | l6 >>> 56;
        lArray2[7] = l8 << 8 | l7 >>> 56;
    }

    public static void one(long[] lArray) {
        lArray[0] = 1L;
        lArray[1] = 0L;
        lArray[2] = 0L;
        lArray[3] = 0L;
        lArray[4] = 0L;
        lArray[5] = 0L;
        lArray[6] = 0L;
        lArray[7] = 0L;
    }

    public static void square(long[] lArray, long[] lArray2) {
        int n;
        long[] lArray3 = new long[16];
        for (n = 0; n < 8; ++n) {
            Interleave.expand64To128(lArray[n], lArray3, n << 1);
        }
        n = 16;
        while (--n >= 8) {
            long l = lArray3[n];
            int n2 = n - 8;
            lArray3[n2] = lArray3[n2] ^ (l ^ l << 2 ^ l << 5 ^ l << 8);
            int n3 = n - 8 + 1;
            lArray3[n3] = lArray3[n3] ^ (l >>> 62 ^ l >>> 59 ^ l >>> 56);
        }
        KGCMUtil_512.copy(lArray3, lArray2);
    }

    public static void x(long[] lArray) {
        lArray[0] = 2L;
        lArray[1] = 0L;
        lArray[2] = 0L;
        lArray[3] = 0L;
        lArray[4] = 0L;
        lArray[5] = 0L;
        lArray[6] = 0L;
        lArray[7] = 0L;
    }

    public static void zero(long[] lArray) {
        lArray[0] = 0L;
        lArray[1] = 0L;
        lArray[2] = 0L;
        lArray[3] = 0L;
        lArray[4] = 0L;
        lArray[5] = 0L;
        lArray[6] = 0L;
        lArray[7] = 0L;
    }
}


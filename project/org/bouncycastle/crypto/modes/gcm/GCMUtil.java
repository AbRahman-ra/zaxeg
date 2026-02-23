/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.gcm;

import org.bouncycastle.math.raw.Interleave;
import org.bouncycastle.util.Longs;
import org.bouncycastle.util.Pack;

public abstract class GCMUtil {
    public static final int SIZE_BYTES = 16;
    public static final int SIZE_INTS = 4;
    public static final int SIZE_LONGS = 2;
    private static final int E1 = -520093696;
    private static final long E1L = -2233785415175766016L;

    public static byte[] oneAsBytes() {
        byte[] byArray = new byte[16];
        byArray[0] = -128;
        return byArray;
    }

    public static int[] oneAsInts() {
        int[] nArray = new int[4];
        nArray[0] = Integer.MIN_VALUE;
        return nArray;
    }

    public static long[] oneAsLongs() {
        long[] lArray = new long[2];
        lArray[0] = Long.MIN_VALUE;
        return lArray;
    }

    public static byte areEqual(byte[] byArray, byte[] byArray2) {
        int n = 0;
        for (int i = 0; i < 16; ++i) {
            n |= byArray[i] ^ byArray2[i];
        }
        n = n >>> 1 | n & 1;
        return (byte)(n - 1 >> 31);
    }

    public static int areEqual(int[] nArray, int[] nArray2) {
        int n = 0;
        n |= nArray[0] ^ nArray2[0];
        n |= nArray[1] ^ nArray2[1];
        n |= nArray[2] ^ nArray2[2];
        n |= nArray[3] ^ nArray2[3];
        n = n >>> 1 | n & 1;
        return n - 1 >> 31;
    }

    public static long areEqual(long[] lArray, long[] lArray2) {
        long l = 0L;
        l |= lArray[0] ^ lArray2[0];
        l |= lArray[1] ^ lArray2[1];
        l = l >>> 1 | l & 1L;
        return l - 1L >> 63;
    }

    public static byte[] asBytes(int[] nArray) {
        byte[] byArray = new byte[16];
        Pack.intToBigEndian(nArray, 0, 4, byArray, 0);
        return byArray;
    }

    public static void asBytes(int[] nArray, byte[] byArray) {
        Pack.intToBigEndian(nArray, 0, 4, byArray, 0);
    }

    public static byte[] asBytes(long[] lArray) {
        byte[] byArray = new byte[16];
        Pack.longToBigEndian(lArray, 0, 2, byArray, 0);
        return byArray;
    }

    public static void asBytes(long[] lArray, byte[] byArray) {
        Pack.longToBigEndian(lArray, 0, 2, byArray, 0);
    }

    public static int[] asInts(byte[] byArray) {
        int[] nArray = new int[4];
        Pack.bigEndianToInt(byArray, 0, nArray, 0, 4);
        return nArray;
    }

    public static void asInts(byte[] byArray, int[] nArray) {
        Pack.bigEndianToInt(byArray, 0, nArray, 0, 4);
    }

    public static long[] asLongs(byte[] byArray) {
        long[] lArray = new long[2];
        Pack.bigEndianToLong(byArray, 0, lArray, 0, 2);
        return lArray;
    }

    public static void asLongs(byte[] byArray, long[] lArray) {
        Pack.bigEndianToLong(byArray, 0, lArray, 0, 2);
    }

    public static void copy(byte[] byArray, byte[] byArray2) {
        for (int i = 0; i < 16; ++i) {
            byArray2[i] = byArray[i];
        }
    }

    public static void copy(int[] nArray, int[] nArray2) {
        nArray2[0] = nArray[0];
        nArray2[1] = nArray[1];
        nArray2[2] = nArray[2];
        nArray2[3] = nArray[3];
    }

    public static void copy(long[] lArray, long[] lArray2) {
        lArray2[0] = lArray[0];
        lArray2[1] = lArray[1];
    }

    public static void divideP(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l >> 63;
        lArray2[0] = (l ^= l3 & 0xE100000000000000L) << 1 | l2 >>> 63;
        lArray2[1] = l2 << 1 | -l3;
    }

    public static void multiply(byte[] byArray, byte[] byArray2) {
        long[] lArray = GCMUtil.asLongs(byArray);
        long[] lArray2 = GCMUtil.asLongs(byArray2);
        GCMUtil.multiply(lArray, lArray2);
        GCMUtil.asBytes(lArray, byArray);
    }

    public static void multiply(int[] nArray, int[] nArray2) {
        int n = nArray2[0];
        int n2 = nArray2[1];
        int n3 = nArray2[2];
        int n4 = nArray2[3];
        int n5 = 0;
        int n6 = 0;
        int n7 = 0;
        int n8 = 0;
        for (int i = 0; i < 4; ++i) {
            int n9 = nArray[i];
            for (int j = 0; j < 32; ++j) {
                int n10 = n9 >> 31;
                n9 <<= 1;
                n5 ^= n & n10;
                n6 ^= n2 & n10;
                n7 ^= n3 & n10;
                n8 ^= n4 & n10;
                int n11 = n4 << 31 >> 8;
                n4 = n4 >>> 1 | n3 << 31;
                n3 = n3 >>> 1 | n2 << 31;
                n2 = n2 >>> 1 | n << 31;
                n = n >>> 1 ^ n11 & 0xE1000000;
            }
        }
        nArray[0] = n5;
        nArray[1] = n6;
        nArray[2] = n7;
        nArray[3] = n8;
    }

    public static void multiply(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = lArray2[0];
        long l4 = lArray2[1];
        long l5 = Longs.reverse(l);
        long l6 = Longs.reverse(l2);
        long l7 = Longs.reverse(l3);
        long l8 = Longs.reverse(l4);
        long l9 = Longs.reverse(GCMUtil.implMul64(l5, l7));
        long l10 = GCMUtil.implMul64(l, l3) << 1;
        long l11 = Longs.reverse(GCMUtil.implMul64(l6, l8));
        long l12 = GCMUtil.implMul64(l2, l4) << 1;
        long l13 = Longs.reverse(GCMUtil.implMul64(l5 ^ l6, l7 ^ l8));
        long l14 = GCMUtil.implMul64(l ^ l2, l3 ^ l4) << 1;
        long l15 = l9;
        long l16 = l10 ^ l9 ^ l11 ^ l13;
        long l17 = l11 ^ l10 ^ l12 ^ l14;
        long l18 = l12;
        l16 ^= l18 ^ l18 >>> 1 ^ l18 >>> 2 ^ l18 >>> 7;
        lArray[0] = l15 ^= (l17 ^= l18 << 62 ^ l18 << 57) ^ l17 >>> 1 ^ l17 >>> 2 ^ l17 >>> 7;
        lArray[1] = l16 ^= l17 << 63 ^ l17 << 62 ^ l17 << 57;
    }

    public static void multiplyP(int[] nArray) {
        int n = nArray[0];
        int n2 = nArray[1];
        int n3 = nArray[2];
        int n4 = nArray[3];
        int n5 = n4 << 31 >> 31;
        nArray[0] = n >>> 1 ^ n5 & 0xE1000000;
        nArray[1] = n2 >>> 1 | n << 31;
        nArray[2] = n3 >>> 1 | n2 << 31;
        nArray[3] = n4 >>> 1 | n3 << 31;
    }

    public static void multiplyP(int[] nArray, int[] nArray2) {
        int n = nArray[0];
        int n2 = nArray[1];
        int n3 = nArray[2];
        int n4 = nArray[3];
        int n5 = n4 << 31 >> 31;
        nArray2[0] = n >>> 1 ^ n5 & 0xE1000000;
        nArray2[1] = n2 >>> 1 | n << 31;
        nArray2[2] = n3 >>> 1 | n2 << 31;
        nArray2[3] = n4 >>> 1 | n3 << 31;
    }

    public static void multiplyP(long[] lArray) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l2 << 63 >> 63;
        lArray[0] = l >>> 1 ^ l3 & 0xE100000000000000L;
        lArray[1] = l2 >>> 1 | l << 63;
    }

    public static void multiplyP(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l2 << 63 >> 63;
        lArray2[0] = l >>> 1 ^ l3 & 0xE100000000000000L;
        lArray2[1] = l2 >>> 1 | l << 63;
    }

    public static void multiplyP3(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l2 << 61;
        lArray2[0] = l >>> 3 ^ l3 ^ l3 >>> 1 ^ l3 >>> 2 ^ l3 >>> 7;
        lArray2[1] = l2 >>> 3 | l << 61;
    }

    public static void multiplyP4(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l2 << 60;
        lArray2[0] = l >>> 4 ^ l3 ^ l3 >>> 1 ^ l3 >>> 2 ^ l3 >>> 7;
        lArray2[1] = l2 >>> 4 | l << 60;
    }

    public static void multiplyP7(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l2 << 57;
        lArray2[0] = l >>> 7 ^ l3 ^ l3 >>> 1 ^ l3 >>> 2 ^ l3 >>> 7;
        lArray2[1] = l2 >>> 7 | l << 57;
    }

    public static void multiplyP8(int[] nArray) {
        int n = nArray[0];
        int n2 = nArray[1];
        int n3 = nArray[2];
        int n4 = nArray[3];
        int n5 = n4 << 24;
        nArray[0] = n >>> 8 ^ n5 ^ n5 >>> 1 ^ n5 >>> 2 ^ n5 >>> 7;
        nArray[1] = n2 >>> 8 | n << 24;
        nArray[2] = n3 >>> 8 | n2 << 24;
        nArray[3] = n4 >>> 8 | n3 << 24;
    }

    public static void multiplyP8(int[] nArray, int[] nArray2) {
        int n = nArray[0];
        int n2 = nArray[1];
        int n3 = nArray[2];
        int n4 = nArray[3];
        int n5 = n4 << 24;
        nArray2[0] = n >>> 8 ^ n5 ^ n5 >>> 1 ^ n5 >>> 2 ^ n5 >>> 7;
        nArray2[1] = n2 >>> 8 | n << 24;
        nArray2[2] = n3 >>> 8 | n2 << 24;
        nArray2[3] = n4 >>> 8 | n3 << 24;
    }

    public static void multiplyP8(long[] lArray) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l2 << 56;
        lArray[0] = l >>> 8 ^ l3 ^ l3 >>> 1 ^ l3 >>> 2 ^ l3 >>> 7;
        lArray[1] = l2 >>> 8 | l << 56;
    }

    public static void multiplyP8(long[] lArray, long[] lArray2) {
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = l2 << 56;
        lArray2[0] = l >>> 8 ^ l3 ^ l3 >>> 1 ^ l3 >>> 2 ^ l3 >>> 7;
        lArray2[1] = l2 >>> 8 | l << 56;
    }

    public static long[] pAsLongs() {
        long[] lArray = new long[2];
        lArray[0] = 0x4000000000000000L;
        return lArray;
    }

    public static void square(long[] lArray, long[] lArray2) {
        long[] lArray3 = new long[4];
        Interleave.expand64To128Rev(lArray[0], lArray3, 0);
        Interleave.expand64To128Rev(lArray[1], lArray3, 2);
        long l = lArray3[0];
        long l2 = lArray3[1];
        long l3 = lArray3[2];
        long l4 = lArray3[3];
        l2 ^= l4 ^ l4 >>> 1 ^ l4 >>> 2 ^ l4 >>> 7;
        lArray2[0] = l ^= (l3 ^= l4 << 63 ^ l4 << 62 ^ l4 << 57) ^ l3 >>> 1 ^ l3 >>> 2 ^ l3 >>> 7;
        lArray2[1] = l2 ^= l3 << 63 ^ l3 << 62 ^ l3 << 57;
    }

    public static void xor(byte[] byArray, byte[] byArray2) {
        int n = 0;
        do {
            int n2 = n;
            byArray[n2] = (byte)(byArray[n2] ^ byArray2[n]);
            int n3 = ++n;
            byArray[n3] = (byte)(byArray[n3] ^ byArray2[n]);
            int n4 = ++n;
            byArray[n4] = (byte)(byArray[n4] ^ byArray2[n]);
            int n5 = ++n;
            byArray[n5] = (byte)(byArray[n5] ^ byArray2[n]);
        } while (++n < 16);
    }

    public static void xor(byte[] byArray, byte[] byArray2, int n) {
        int n2 = 0;
        do {
            int n3 = n2;
            byArray[n3] = (byte)(byArray[n3] ^ byArray2[n + n2]);
            int n4 = ++n2;
            byArray[n4] = (byte)(byArray[n4] ^ byArray2[n + n2]);
            int n5 = ++n2;
            byArray[n5] = (byte)(byArray[n5] ^ byArray2[n + n2]);
            int n6 = ++n2;
            byArray[n6] = (byte)(byArray[n6] ^ byArray2[n + n2]);
        } while (++n2 < 16);
    }

    public static void xor(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, int n3) {
        int n4 = 0;
        do {
            byArray3[n3 + n4] = (byte)(byArray[n + n4] ^ byArray2[n2 + n4]);
            byArray3[n3 + ++n4] = (byte)(byArray[n + n4] ^ byArray2[n2 + n4]);
            byArray3[n3 + ++n4] = (byte)(byArray[n + n4] ^ byArray2[n2 + n4]);
            byArray3[n3 + ++n4] = (byte)(byArray[n + n4] ^ byArray2[n2 + n4]);
        } while (++n4 < 16);
    }

    public static void xor(byte[] byArray, byte[] byArray2, int n, int n2) {
        while (--n2 >= 0) {
            int n3 = n2;
            byArray[n3] = (byte)(byArray[n3] ^ byArray2[n + n2]);
        }
    }

    public static void xor(byte[] byArray, int n, byte[] byArray2, int n2, int n3) {
        while (--n3 >= 0) {
            int n4 = n + n3;
            byArray[n4] = (byte)(byArray[n4] ^ byArray2[n2 + n3]);
        }
    }

    public static void xor(byte[] byArray, byte[] byArray2, byte[] byArray3) {
        int n = 0;
        do {
            byArray3[n] = (byte)(byArray[n] ^ byArray2[n]);
            byArray3[++n] = (byte)(byArray[n] ^ byArray2[n]);
            byArray3[++n] = (byte)(byArray[n] ^ byArray2[n]);
            byArray3[++n] = (byte)(byArray[n] ^ byArray2[n]);
        } while (++n < 16);
    }

    public static void xor(int[] nArray, int[] nArray2) {
        nArray[0] = nArray[0] ^ nArray2[0];
        nArray[1] = nArray[1] ^ nArray2[1];
        nArray[2] = nArray[2] ^ nArray2[2];
        nArray[3] = nArray[3] ^ nArray2[3];
    }

    public static void xor(int[] nArray, int[] nArray2, int[] nArray3) {
        nArray3[0] = nArray[0] ^ nArray2[0];
        nArray3[1] = nArray[1] ^ nArray2[1];
        nArray3[2] = nArray[2] ^ nArray2[2];
        nArray3[3] = nArray[3] ^ nArray2[3];
    }

    public static void xor(long[] lArray, long[] lArray2) {
        lArray[0] = lArray[0] ^ lArray2[0];
        lArray[1] = lArray[1] ^ lArray2[1];
    }

    public static void xor(long[] lArray, long[] lArray2, long[] lArray3) {
        lArray3[0] = lArray[0] ^ lArray2[0];
        lArray3[1] = lArray[1] ^ lArray2[1];
    }

    private static long implMul64(long l, long l2) {
        long l3 = l & 0x1111111111111111L;
        long l4 = l & 0x2222222222222222L;
        long l5 = l & 0x4444444444444444L;
        long l6 = l & 0x8888888888888888L;
        long l7 = l2 & 0x1111111111111111L;
        long l8 = l2 & 0x2222222222222222L;
        long l9 = l2 & 0x4444444444444444L;
        long l10 = l2 & 0x8888888888888888L;
        long l11 = l3 * l7 ^ l4 * l10 ^ l5 * l9 ^ l6 * l8;
        long l12 = l3 * l8 ^ l4 * l7 ^ l5 * l10 ^ l6 * l9;
        long l13 = l3 * l9 ^ l4 * l8 ^ l5 * l7 ^ l6 * l10;
        long l14 = l3 * l10 ^ l4 * l9 ^ l5 * l8 ^ l6 * l7;
        return (l11 &= 0x1111111111111111L) | (l12 &= 0x2222222222222222L) | (l13 &= 0x4444444444444444L) | (l14 &= 0x8888888888888888L);
    }
}


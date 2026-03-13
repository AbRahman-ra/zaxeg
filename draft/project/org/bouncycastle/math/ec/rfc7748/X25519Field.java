/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec.rfc7748;

import org.bouncycastle.math.raw.Mod;

public abstract class X25519Field {
    public static final int SIZE = 10;
    private static final int M24 = 0xFFFFFF;
    private static final int M25 = 0x1FFFFFF;
    private static final int M26 = 0x3FFFFFF;
    private static final int[] P32 = new int[]{-19, -1, -1, -1, -1, -1, -1, Integer.MAX_VALUE};
    private static final int[] ROOT_NEG_ONE = new int[]{34513072, 59165138, 4688974, 3500415, 6194736, 33281959, 54535759, 32551604, 163342, 5703241};

    protected X25519Field() {
    }

    public static void add(int[] nArray, int[] nArray2, int[] nArray3) {
        for (int i = 0; i < 10; ++i) {
            nArray3[i] = nArray[i] + nArray2[i];
        }
    }

    public static void addOne(int[] nArray) {
        nArray[0] = nArray[0] + 1;
    }

    public static void addOne(int[] nArray, int n) {
        int n2 = n;
        nArray[n2] = nArray[n2] + 1;
    }

    public static void apm(int[] nArray, int[] nArray2, int[] nArray3, int[] nArray4) {
        for (int i = 0; i < 10; ++i) {
            int n = nArray[i];
            int n2 = nArray2[i];
            nArray3[i] = n + n2;
            nArray4[i] = n - n2;
        }
    }

    public static int areEqual(int[] nArray, int[] nArray2) {
        int n = 0;
        for (int i = 0; i < 10; ++i) {
            n |= nArray[i] ^ nArray2[i];
        }
        n = n >>> 1 | n & 1;
        return n - 1 >> 31;
    }

    public static boolean areEqualVar(int[] nArray, int[] nArray2) {
        return 0 != X25519Field.areEqual(nArray, nArray2);
    }

    public static void carry(int[] nArray) {
        int n = nArray[0];
        int n2 = nArray[1];
        int n3 = nArray[2];
        int n4 = nArray[3];
        int n5 = nArray[4];
        int n6 = nArray[5];
        int n7 = nArray[6];
        int n8 = nArray[7];
        int n9 = nArray[8];
        int n10 = nArray[9];
        n3 += n2 >> 26;
        n2 &= 0x3FFFFFF;
        n5 += n4 >> 26;
        n4 &= 0x3FFFFFF;
        n8 += n7 >> 26;
        n7 &= 0x3FFFFFF;
        n10 += n9 >> 26;
        n9 &= 0x3FFFFFF;
        n4 += n3 >> 25;
        n3 &= 0x1FFFFFF;
        n6 += n5 >> 25;
        n5 &= 0x1FFFFFF;
        n9 += n8 >> 25;
        n8 &= 0x1FFFFFF;
        n += (n10 >> 25) * 38;
        n10 &= 0x1FFFFFF;
        n2 += n >> 26;
        n &= 0x3FFFFFF;
        n7 += n6 >> 26;
        n6 &= 0x3FFFFFF;
        n3 += n2 >> 26;
        n2 &= 0x3FFFFFF;
        n5 += n4 >> 26;
        n4 &= 0x3FFFFFF;
        n8 += n7 >> 26;
        n7 &= 0x3FFFFFF;
        n10 += n9 >> 26;
        n9 &= 0x3FFFFFF;
        nArray[0] = n;
        nArray[1] = n2;
        nArray[2] = n3;
        nArray[3] = n4;
        nArray[4] = n5;
        nArray[5] = n6;
        nArray[6] = n7;
        nArray[7] = n8;
        nArray[8] = n9;
        nArray[9] = n10;
    }

    public static void cmov(int n, int[] nArray, int n2, int[] nArray2, int n3) {
        for (int i = 0; i < 10; ++i) {
            int n4 = nArray2[n3 + i];
            int n5 = n4 ^ nArray[n2 + i];
            nArray2[n3 + i] = n4 ^= n5 & n;
        }
    }

    public static void cnegate(int n, int[] nArray) {
        int n2 = 0 - n;
        for (int i = 0; i < 10; ++i) {
            nArray[i] = (nArray[i] ^ n2) - n2;
        }
    }

    public static void copy(int[] nArray, int n, int[] nArray2, int n2) {
        for (int i = 0; i < 10; ++i) {
            nArray2[n2 + i] = nArray[n + i];
        }
    }

    public static int[] create() {
        return new int[10];
    }

    public static int[] createTable(int n) {
        return new int[10 * n];
    }

    public static void cswap(int n, int[] nArray, int[] nArray2) {
        int n2 = 0 - n;
        for (int i = 0; i < 10; ++i) {
            int n3 = nArray[i];
            int n4 = nArray2[i];
            int n5 = n2 & (n3 ^ n4);
            nArray[i] = n3 ^ n5;
            nArray2[i] = n4 ^ n5;
        }
    }

    public static void decode(int[] nArray, int n, int[] nArray2) {
        X25519Field.decode128(nArray, n, nArray2, 0);
        X25519Field.decode128(nArray, n + 4, nArray2, 5);
        nArray2[9] = nArray2[9] & 0xFFFFFF;
    }

    public static void decode(byte[] byArray, int n, int[] nArray) {
        X25519Field.decode128(byArray, n, nArray, 0);
        X25519Field.decode128(byArray, n + 16, nArray, 5);
        nArray[9] = nArray[9] & 0xFFFFFF;
    }

    private static void decode128(int[] nArray, int n, int[] nArray2, int n2) {
        int n3 = nArray[n + 0];
        int n4 = nArray[n + 1];
        int n5 = nArray[n + 2];
        int n6 = nArray[n + 3];
        nArray2[n2 + 0] = n3 & 0x3FFFFFF;
        nArray2[n2 + 1] = (n4 << 6 | n3 >>> 26) & 0x3FFFFFF;
        nArray2[n2 + 2] = (n5 << 12 | n4 >>> 20) & 0x1FFFFFF;
        nArray2[n2 + 3] = (n6 << 19 | n5 >>> 13) & 0x3FFFFFF;
        nArray2[n2 + 4] = n6 >>> 7;
    }

    private static void decode128(byte[] byArray, int n, int[] nArray, int n2) {
        int n3 = X25519Field.decode32(byArray, n + 0);
        int n4 = X25519Field.decode32(byArray, n + 4);
        int n5 = X25519Field.decode32(byArray, n + 8);
        int n6 = X25519Field.decode32(byArray, n + 12);
        nArray[n2 + 0] = n3 & 0x3FFFFFF;
        nArray[n2 + 1] = (n4 << 6 | n3 >>> 26) & 0x3FFFFFF;
        nArray[n2 + 2] = (n5 << 12 | n4 >>> 20) & 0x1FFFFFF;
        nArray[n2 + 3] = (n6 << 19 | n5 >>> 13) & 0x3FFFFFF;
        nArray[n2 + 4] = n6 >>> 7;
    }

    private static int decode32(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        n2 |= (byArray[++n] & 0xFF) << 16;
        return n2 |= byArray[++n] << 24;
    }

    public static void encode(int[] nArray, int[] nArray2, int n) {
        X25519Field.encode128(nArray, 0, nArray2, n);
        X25519Field.encode128(nArray, 5, nArray2, n + 4);
    }

    public static void encode(int[] nArray, byte[] byArray, int n) {
        X25519Field.encode128(nArray, 0, byArray, n);
        X25519Field.encode128(nArray, 5, byArray, n + 16);
    }

    private static void encode128(int[] nArray, int n, int[] nArray2, int n2) {
        int n3 = nArray[n + 0];
        int n4 = nArray[n + 1];
        int n5 = nArray[n + 2];
        int n6 = nArray[n + 3];
        int n7 = nArray[n + 4];
        nArray2[n2 + 0] = n3 | n4 << 26;
        nArray2[n2 + 1] = n4 >>> 6 | n5 << 20;
        nArray2[n2 + 2] = n5 >>> 12 | n6 << 13;
        nArray2[n2 + 3] = n6 >>> 19 | n7 << 7;
    }

    private static void encode128(int[] nArray, int n, byte[] byArray, int n2) {
        int n3 = nArray[n + 0];
        int n4 = nArray[n + 1];
        int n5 = nArray[n + 2];
        int n6 = nArray[n + 3];
        int n7 = nArray[n + 4];
        int n8 = n3 | n4 << 26;
        X25519Field.encode32(n8, byArray, n2 + 0);
        int n9 = n4 >>> 6 | n5 << 20;
        X25519Field.encode32(n9, byArray, n2 + 4);
        int n10 = n5 >>> 12 | n6 << 13;
        X25519Field.encode32(n10, byArray, n2 + 8);
        int n11 = n6 >>> 19 | n7 << 7;
        X25519Field.encode32(n11, byArray, n2 + 12);
    }

    private static void encode32(int n, byte[] byArray, int n2) {
        byArray[n2] = (byte)n;
        byArray[++n2] = (byte)(n >>> 8);
        byArray[++n2] = (byte)(n >>> 16);
        byArray[++n2] = (byte)(n >>> 24);
    }

    public static void inv(int[] nArray, int[] nArray2) {
        int[] nArray3 = X25519Field.create();
        int[] nArray4 = new int[8];
        X25519Field.copy(nArray, 0, nArray3, 0);
        X25519Field.normalize(nArray3);
        X25519Field.encode(nArray3, nArray4, 0);
        Mod.modOddInverse(P32, nArray4, nArray4);
        X25519Field.decode(nArray4, 0, nArray2);
    }

    public static void invVar(int[] nArray, int[] nArray2) {
        int[] nArray3 = X25519Field.create();
        int[] nArray4 = new int[8];
        X25519Field.copy(nArray, 0, nArray3, 0);
        X25519Field.normalize(nArray3);
        X25519Field.encode(nArray3, nArray4, 0);
        Mod.modOddInverseVar(P32, nArray4, nArray4);
        X25519Field.decode(nArray4, 0, nArray2);
    }

    public static int isOne(int[] nArray) {
        int n = nArray[0] ^ 1;
        for (int i = 1; i < 10; ++i) {
            n |= nArray[i];
        }
        n = n >>> 1 | n & 1;
        return n - 1 >> 31;
    }

    public static boolean isOneVar(int[] nArray) {
        return 0 != X25519Field.isOne(nArray);
    }

    public static int isZero(int[] nArray) {
        int n = 0;
        for (int i = 0; i < 10; ++i) {
            n |= nArray[i];
        }
        n = n >>> 1 | n & 1;
        return n - 1 >> 31;
    }

    public static boolean isZeroVar(int[] nArray) {
        return 0 != X25519Field.isZero(nArray);
    }

    public static void mul(int[] nArray, int n, int[] nArray2) {
        int n2 = nArray[0];
        int n3 = nArray[1];
        int n4 = nArray[2];
        int n5 = nArray[3];
        int n6 = nArray[4];
        int n7 = nArray[5];
        int n8 = nArray[6];
        int n9 = nArray[7];
        int n10 = nArray[8];
        int n11 = nArray[9];
        long l = (long)n4 * (long)n;
        n4 = (int)l & 0x1FFFFFF;
        l >>= 25;
        long l2 = (long)n6 * (long)n;
        n6 = (int)l2 & 0x1FFFFFF;
        l2 >>= 25;
        long l3 = (long)n9 * (long)n;
        n9 = (int)l3 & 0x1FFFFFF;
        l3 >>= 25;
        long l4 = (long)n11 * (long)n;
        n11 = (int)l4 & 0x1FFFFFF;
        l4 >>= 25;
        l4 *= 38L;
        nArray2[0] = (int)(l4 += (long)n2 * (long)n) & 0x3FFFFFF;
        l4 >>= 26;
        nArray2[5] = (int)(l2 += (long)n7 * (long)n) & 0x3FFFFFF;
        l2 >>= 26;
        nArray2[1] = (int)(l4 += (long)n3 * (long)n) & 0x3FFFFFF;
        nArray2[3] = (int)(l += (long)n5 * (long)n) & 0x3FFFFFF;
        nArray2[6] = (int)(l2 += (long)n8 * (long)n) & 0x3FFFFFF;
        nArray2[8] = (int)(l3 += (long)n10 * (long)n) & 0x3FFFFFF;
        nArray2[2] = n4 + (int)(l4 >>= 26);
        nArray2[4] = n6 + (int)(l >>= 26);
        nArray2[7] = n9 + (int)(l2 >>= 26);
        nArray2[9] = n11 + (int)(l3 >>= 26);
    }

    public static void mul(int[] nArray, int[] nArray2, int[] nArray3) {
        int n = nArray[0];
        int n2 = nArray2[0];
        int n3 = nArray[1];
        int n4 = nArray2[1];
        int n5 = nArray[2];
        int n6 = nArray2[2];
        int n7 = nArray[3];
        int n8 = nArray2[3];
        int n9 = nArray[4];
        int n10 = nArray2[4];
        int n11 = nArray[5];
        int n12 = nArray2[5];
        int n13 = nArray[6];
        int n14 = nArray2[6];
        int n15 = nArray[7];
        int n16 = nArray2[7];
        int n17 = nArray[8];
        int n18 = nArray2[8];
        int n19 = nArray[9];
        int n20 = nArray2[9];
        long l = (long)n * (long)n2;
        long l2 = (long)n * (long)n4 + (long)n3 * (long)n2;
        long l3 = (long)n * (long)n6 + (long)n3 * (long)n4 + (long)n5 * (long)n2;
        long l4 = (long)n3 * (long)n6 + (long)n5 * (long)n4;
        l4 <<= 1;
        l4 += (long)n * (long)n8 + (long)n7 * (long)n2;
        long l5 = (long)n5 * (long)n6;
        l5 <<= 1;
        l5 += (long)n * (long)n10 + (long)n3 * (long)n8 + (long)n7 * (long)n4 + (long)n9 * (long)n2;
        long l6 = (long)n3 * (long)n10 + (long)n5 * (long)n8 + (long)n7 * (long)n6 + (long)n9 * (long)n4;
        l6 <<= 1;
        long l7 = (long)n5 * (long)n10 + (long)n9 * (long)n6;
        l7 <<= 1;
        l7 += (long)n7 * (long)n8;
        long l8 = (long)n7 * (long)n10 + (long)n9 * (long)n8;
        long l9 = (long)n9 * (long)n10;
        l9 <<= 1;
        long l10 = (long)n11 * (long)n12;
        long l11 = (long)n11 * (long)n14 + (long)n13 * (long)n12;
        long l12 = (long)n11 * (long)n16 + (long)n13 * (long)n14 + (long)n15 * (long)n12;
        long l13 = (long)n13 * (long)n16 + (long)n15 * (long)n14;
        l13 <<= 1;
        l13 += (long)n11 * (long)n18 + (long)n17 * (long)n12;
        long l14 = (long)n15 * (long)n16;
        l14 <<= 1;
        l14 += (long)n11 * (long)n20 + (long)n13 * (long)n18 + (long)n17 * (long)n14 + (long)n19 * (long)n12;
        long l15 = (long)n13 * (long)n20 + (long)n15 * (long)n18 + (long)n17 * (long)n16 + (long)n19 * (long)n14;
        long l16 = (long)n15 * (long)n20 + (long)n19 * (long)n16;
        l16 <<= 1;
        long l17 = (long)n17 * (long)n20 + (long)n19 * (long)n18;
        long l18 = (long)n19 * (long)n20;
        l -= l15 * 76L;
        l2 -= (l16 += (long)n17 * (long)n18) * 38L;
        l3 -= l17 * 38L;
        l4 -= l18 * 76L;
        l6 -= l10;
        l7 -= l11;
        l8 -= l12;
        l9 -= l13;
        n += n11;
        n2 += n12;
        n3 += n13;
        n4 += n14;
        n5 += n15;
        n6 += n16;
        n7 += n17;
        n8 += n18;
        n9 += n19;
        n10 += n20;
        long l19 = (long)n * (long)n2;
        long l20 = (long)n * (long)n4 + (long)n3 * (long)n2;
        long l21 = (long)n * (long)n6 + (long)n3 * (long)n4 + (long)n5 * (long)n2;
        long l22 = (long)n3 * (long)n6 + (long)n5 * (long)n4;
        l22 <<= 1;
        l22 += (long)n * (long)n8 + (long)n7 * (long)n2;
        long l23 = (long)n5 * (long)n6;
        l23 <<= 1;
        l23 += (long)n * (long)n10 + (long)n3 * (long)n8 + (long)n7 * (long)n4 + (long)n9 * (long)n2;
        long l24 = (long)n3 * (long)n10 + (long)n5 * (long)n8 + (long)n7 * (long)n6 + (long)n9 * (long)n4;
        l24 <<= 1;
        long l25 = (long)n5 * (long)n10 + (long)n9 * (long)n6;
        l25 <<= 1;
        l25 += (long)n7 * (long)n8;
        long l26 = (long)n7 * (long)n10 + (long)n9 * (long)n8;
        long l27 = (long)n9 * (long)n10;
        l27 <<= 1;
        long l28 = l9 + (l22 - l4);
        int n21 = (int)l28 & 0x3FFFFFF;
        l28 >>= 26;
        int n22 = (int)(l28 += l23 - l5 - l14) & 0x1FFFFFF;
        l28 >>= 25;
        l28 = l + (l28 + l24 - l6) * 38L;
        nArray3[0] = (int)l28 & 0x3FFFFFF;
        l28 >>= 26;
        nArray3[1] = (int)(l28 += l2 + (l25 - l7) * 38L) & 0x3FFFFFF;
        l28 >>= 26;
        nArray3[2] = (int)(l28 += l3 + (l26 - l8) * 38L) & 0x1FFFFFF;
        l28 >>= 25;
        nArray3[3] = (int)(l28 += l4 + (l27 - l9) * 38L) & 0x3FFFFFF;
        l28 >>= 26;
        nArray3[4] = (int)(l28 += l5 + l14 * 38L) & 0x1FFFFFF;
        l28 >>= 25;
        nArray3[5] = (int)(l28 += l6 + (l19 - l)) & 0x3FFFFFF;
        l28 >>= 26;
        nArray3[6] = (int)(l28 += l7 + (l20 - l2)) & 0x3FFFFFF;
        l28 >>= 26;
        nArray3[7] = (int)(l28 += l8 + (l21 - l3)) & 0x1FFFFFF;
        l28 >>= 25;
        nArray3[8] = (int)(l28 += (long)n21) & 0x3FFFFFF;
        nArray3[9] = n22 + (int)(l28 >>= 26);
    }

    public static void negate(int[] nArray, int[] nArray2) {
        for (int i = 0; i < 10; ++i) {
            nArray2[i] = -nArray[i];
        }
    }

    public static void normalize(int[] nArray) {
        int n = nArray[9] >>> 23 & 1;
        X25519Field.reduce(nArray, n);
        X25519Field.reduce(nArray, -n);
    }

    public static void one(int[] nArray) {
        nArray[0] = 1;
        for (int i = 1; i < 10; ++i) {
            nArray[i] = 0;
        }
    }

    private static void powPm5d8(int[] nArray, int[] nArray2, int[] nArray3) {
        int[] nArray4 = nArray2;
        X25519Field.sqr(nArray, nArray4);
        X25519Field.mul(nArray, nArray4, nArray4);
        int[] nArray5 = X25519Field.create();
        X25519Field.sqr(nArray4, nArray5);
        X25519Field.mul(nArray, nArray5, nArray5);
        int[] nArray6 = nArray5;
        X25519Field.sqr(nArray5, 2, nArray6);
        X25519Field.mul(nArray4, nArray6, nArray6);
        int[] nArray7 = X25519Field.create();
        X25519Field.sqr(nArray6, 5, nArray7);
        X25519Field.mul(nArray6, nArray7, nArray7);
        int[] nArray8 = X25519Field.create();
        X25519Field.sqr(nArray7, 5, nArray8);
        X25519Field.mul(nArray6, nArray8, nArray8);
        int[] nArray9 = nArray6;
        X25519Field.sqr(nArray8, 10, nArray9);
        X25519Field.mul(nArray7, nArray9, nArray9);
        int[] nArray10 = nArray7;
        X25519Field.sqr(nArray9, 25, nArray10);
        X25519Field.mul(nArray9, nArray10, nArray10);
        int[] nArray11 = nArray8;
        X25519Field.sqr(nArray10, 25, nArray11);
        X25519Field.mul(nArray9, nArray11, nArray11);
        int[] nArray12 = nArray9;
        X25519Field.sqr(nArray11, 50, nArray12);
        X25519Field.mul(nArray10, nArray12, nArray12);
        int[] nArray13 = nArray10;
        X25519Field.sqr(nArray12, 125, nArray13);
        X25519Field.mul(nArray12, nArray13, nArray13);
        int[] nArray14 = nArray12;
        X25519Field.sqr(nArray13, 2, nArray14);
        X25519Field.mul(nArray14, nArray, nArray3);
    }

    private static void reduce(int[] nArray, int n) {
        int n2 = nArray[9];
        int n3 = n2 & 0xFFFFFF;
        n2 = (n2 >> 24) + n;
        long l = n2 * 19;
        nArray[0] = (int)(l += (long)nArray[0]) & 0x3FFFFFF;
        l >>= 26;
        nArray[1] = (int)(l += (long)nArray[1]) & 0x3FFFFFF;
        l >>= 26;
        nArray[2] = (int)(l += (long)nArray[2]) & 0x1FFFFFF;
        l >>= 25;
        nArray[3] = (int)(l += (long)nArray[3]) & 0x3FFFFFF;
        l >>= 26;
        nArray[4] = (int)(l += (long)nArray[4]) & 0x1FFFFFF;
        l >>= 25;
        nArray[5] = (int)(l += (long)nArray[5]) & 0x3FFFFFF;
        l >>= 26;
        nArray[6] = (int)(l += (long)nArray[6]) & 0x3FFFFFF;
        l >>= 26;
        nArray[7] = (int)(l += (long)nArray[7]) & 0x1FFFFFF;
        l >>= 25;
        nArray[8] = (int)(l += (long)nArray[8]) & 0x3FFFFFF;
        nArray[9] = n3 + (int)(l >>= 26);
    }

    public static void sqr(int[] nArray, int[] nArray2) {
        int n = nArray[0];
        int n2 = nArray[1];
        int n3 = nArray[2];
        int n4 = nArray[3];
        int n5 = nArray[4];
        int n6 = nArray[5];
        int n7 = nArray[6];
        int n8 = nArray[7];
        int n9 = nArray[8];
        int n10 = nArray[9];
        int n11 = n2 * 2;
        int n12 = n3 * 2;
        int n13 = n4 * 2;
        int n14 = n5 * 2;
        long l = (long)n * (long)n;
        long l2 = (long)n * (long)n11;
        long l3 = (long)n * (long)n12 + (long)n2 * (long)n2;
        long l4 = (long)n11 * (long)n12 + (long)n * (long)n13;
        long l5 = (long)n3 * (long)n12 + (long)n * (long)n14 + (long)n2 * (long)n13;
        long l6 = (long)n11 * (long)n14 + (long)n12 * (long)n13;
        long l7 = (long)n12 * (long)n14 + (long)n4 * (long)n4;
        long l8 = (long)n4 * (long)n14;
        long l9 = (long)n5 * (long)n14;
        int n15 = n7 * 2;
        int n16 = n8 * 2;
        int n17 = n9 * 2;
        int n18 = n10 * 2;
        long l10 = (long)n6 * (long)n6;
        long l11 = (long)n6 * (long)n15;
        long l12 = (long)n6 * (long)n16 + (long)n7 * (long)n7;
        long l13 = (long)n15 * (long)n16 + (long)n6 * (long)n17;
        long l14 = (long)n8 * (long)n16 + (long)n6 * (long)n18 + (long)n7 * (long)n17;
        long l15 = (long)n15 * (long)n18 + (long)n16 * (long)n17;
        long l16 = (long)n16 * (long)n18 + (long)n9 * (long)n9;
        long l17 = (long)n9 * (long)n18;
        long l18 = (long)n10 * (long)n18;
        l -= l15 * 38L;
        l2 -= l16 * 38L;
        l3 -= l17 * 38L;
        l4 -= l18 * 38L;
        l6 -= l10;
        l7 -= l11;
        l8 -= l12;
        l9 -= l13;
        n11 = (n2 += n7) * 2;
        n12 = (n3 += n8) * 2;
        n13 = (n4 += n9) * 2;
        n14 = (n5 += n10) * 2;
        long l19 = (long)(n += n6) * (long)n;
        long l20 = (long)n * (long)n11;
        long l21 = (long)n * (long)n12 + (long)n2 * (long)n2;
        long l22 = (long)n11 * (long)n12 + (long)n * (long)n13;
        long l23 = (long)n3 * (long)n12 + (long)n * (long)n14 + (long)n2 * (long)n13;
        long l24 = (long)n11 * (long)n14 + (long)n12 * (long)n13;
        long l25 = (long)n12 * (long)n14 + (long)n4 * (long)n4;
        long l26 = (long)n4 * (long)n14;
        long l27 = (long)n5 * (long)n14;
        long l28 = l9 + (l22 - l4);
        int n19 = (int)l28 & 0x3FFFFFF;
        l28 >>= 26;
        int n20 = (int)(l28 += l23 - l5 - l14) & 0x1FFFFFF;
        l28 >>= 25;
        l28 = l + (l28 + l24 - l6) * 38L;
        nArray2[0] = (int)l28 & 0x3FFFFFF;
        l28 >>= 26;
        nArray2[1] = (int)(l28 += l2 + (l25 - l7) * 38L) & 0x3FFFFFF;
        l28 >>= 26;
        nArray2[2] = (int)(l28 += l3 + (l26 - l8) * 38L) & 0x1FFFFFF;
        l28 >>= 25;
        nArray2[3] = (int)(l28 += l4 + (l27 - l9) * 38L) & 0x3FFFFFF;
        l28 >>= 26;
        nArray2[4] = (int)(l28 += l5 + l14 * 38L) & 0x1FFFFFF;
        l28 >>= 25;
        nArray2[5] = (int)(l28 += l6 + (l19 - l)) & 0x3FFFFFF;
        l28 >>= 26;
        nArray2[6] = (int)(l28 += l7 + (l20 - l2)) & 0x3FFFFFF;
        l28 >>= 26;
        nArray2[7] = (int)(l28 += l8 + (l21 - l3)) & 0x1FFFFFF;
        l28 >>= 25;
        nArray2[8] = (int)(l28 += (long)n19) & 0x3FFFFFF;
        nArray2[9] = n20 + (int)(l28 >>= 26);
    }

    public static void sqr(int[] nArray, int n, int[] nArray2) {
        X25519Field.sqr(nArray, nArray2);
        while (--n > 0) {
            X25519Field.sqr(nArray2, nArray2);
        }
    }

    public static boolean sqrtRatioVar(int[] nArray, int[] nArray2, int[] nArray3) {
        int[] nArray4 = X25519Field.create();
        int[] nArray5 = X25519Field.create();
        X25519Field.mul(nArray, nArray2, nArray4);
        X25519Field.sqr(nArray2, nArray5);
        X25519Field.mul(nArray4, nArray5, nArray4);
        X25519Field.sqr(nArray5, nArray5);
        X25519Field.mul(nArray5, nArray4, nArray5);
        int[] nArray6 = X25519Field.create();
        int[] nArray7 = X25519Field.create();
        X25519Field.powPm5d8(nArray5, nArray6, nArray7);
        X25519Field.mul(nArray7, nArray4, nArray7);
        int[] nArray8 = X25519Field.create();
        X25519Field.sqr(nArray7, nArray8);
        X25519Field.mul(nArray8, nArray2, nArray8);
        X25519Field.sub(nArray8, nArray, nArray6);
        X25519Field.normalize(nArray6);
        if (X25519Field.isZeroVar(nArray6)) {
            X25519Field.copy(nArray7, 0, nArray3, 0);
            return true;
        }
        X25519Field.add(nArray8, nArray, nArray6);
        X25519Field.normalize(nArray6);
        if (X25519Field.isZeroVar(nArray6)) {
            X25519Field.mul(nArray7, ROOT_NEG_ONE, nArray3);
            return true;
        }
        return false;
    }

    public static void sub(int[] nArray, int[] nArray2, int[] nArray3) {
        for (int i = 0; i < 10; ++i) {
            nArray3[i] = nArray[i] - nArray2[i];
        }
    }

    public static void subOne(int[] nArray) {
        nArray[0] = nArray[0] - 1;
    }

    public static void zero(int[] nArray) {
        for (int i = 0; i < 10; ++i) {
            nArray[i] = 0;
        }
    }
}


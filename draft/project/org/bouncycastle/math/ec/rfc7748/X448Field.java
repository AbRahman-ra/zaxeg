/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec.rfc7748;

import org.bouncycastle.math.raw.Mod;

public abstract class X448Field {
    public static final int SIZE = 16;
    private static final int M28 = 0xFFFFFFF;
    private static final long U32 = 0xFFFFFFFFL;
    private static final int[] P32 = new int[]{-1, -1, -1, -1, -1, -1, -1, -2, -1, -1, -1, -1, -1, -1};

    protected X448Field() {
    }

    public static void add(int[] nArray, int[] nArray2, int[] nArray3) {
        for (int i = 0; i < 16; ++i) {
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

    public static int areEqual(int[] nArray, int[] nArray2) {
        int n = 0;
        for (int i = 0; i < 16; ++i) {
            n |= nArray[i] ^ nArray2[i];
        }
        n = n >>> 1 | n & 1;
        return n - 1 >> 31;
    }

    public static boolean areEqualVar(int[] nArray, int[] nArray2) {
        return 0 != X448Field.areEqual(nArray, nArray2);
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
        int n11 = nArray[10];
        int n12 = nArray[11];
        int n13 = nArray[12];
        int n14 = nArray[13];
        int n15 = nArray[14];
        int n16 = nArray[15];
        n2 += n >>> 28;
        n &= 0xFFFFFFF;
        n6 += n5 >>> 28;
        n5 &= 0xFFFFFFF;
        n10 += n9 >>> 28;
        n9 &= 0xFFFFFFF;
        n14 += n13 >>> 28;
        n13 &= 0xFFFFFFF;
        n3 += n2 >>> 28;
        n2 &= 0xFFFFFFF;
        n7 += n6 >>> 28;
        n6 &= 0xFFFFFFF;
        n11 += n10 >>> 28;
        n10 &= 0xFFFFFFF;
        n15 += n14 >>> 28;
        n14 &= 0xFFFFFFF;
        n4 += n3 >>> 28;
        n3 &= 0xFFFFFFF;
        n8 += n7 >>> 28;
        n7 &= 0xFFFFFFF;
        n12 += n11 >>> 28;
        n11 &= 0xFFFFFFF;
        n16 += n15 >>> 28;
        n15 &= 0xFFFFFFF;
        int n17 = n16 >>> 28;
        n16 &= 0xFFFFFFF;
        n += n17;
        n9 += n17;
        n5 += n4 >>> 28;
        n4 &= 0xFFFFFFF;
        n9 += n8 >>> 28;
        n8 &= 0xFFFFFFF;
        n13 += n12 >>> 28;
        n12 &= 0xFFFFFFF;
        n2 += n >>> 28;
        n &= 0xFFFFFFF;
        n6 += n5 >>> 28;
        n5 &= 0xFFFFFFF;
        n10 += n9 >>> 28;
        n9 &= 0xFFFFFFF;
        n14 += n13 >>> 28;
        n13 &= 0xFFFFFFF;
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
        nArray[10] = n11;
        nArray[11] = n12;
        nArray[12] = n13;
        nArray[13] = n14;
        nArray[14] = n15;
        nArray[15] = n16;
    }

    public static void cmov(int n, int[] nArray, int n2, int[] nArray2, int n3) {
        for (int i = 0; i < 16; ++i) {
            int n4 = nArray2[n3 + i];
            int n5 = n4 ^ nArray[n2 + i];
            nArray2[n3 + i] = n4 ^= n5 & n;
        }
    }

    public static void cnegate(int n, int[] nArray) {
        int[] nArray2 = X448Field.create();
        X448Field.sub(nArray2, nArray, nArray2);
        X448Field.cmov(-n, nArray2, 0, nArray, 0);
    }

    public static void copy(int[] nArray, int n, int[] nArray2, int n2) {
        for (int i = 0; i < 16; ++i) {
            nArray2[n2 + i] = nArray[n + i];
        }
    }

    public static int[] create() {
        return new int[16];
    }

    public static int[] createTable(int n) {
        return new int[16 * n];
    }

    public static void cswap(int n, int[] nArray, int[] nArray2) {
        int n2 = 0 - n;
        for (int i = 0; i < 16; ++i) {
            int n3 = nArray[i];
            int n4 = nArray2[i];
            int n5 = n2 & (n3 ^ n4);
            nArray[i] = n3 ^ n5;
            nArray2[i] = n4 ^ n5;
        }
    }

    public static void decode(int[] nArray, int n, int[] nArray2) {
        X448Field.decode224(nArray, n, nArray2, 0);
        X448Field.decode224(nArray, n + 7, nArray2, 8);
    }

    public static void decode(byte[] byArray, int n, int[] nArray) {
        X448Field.decode56(byArray, n, nArray, 0);
        X448Field.decode56(byArray, n + 7, nArray, 2);
        X448Field.decode56(byArray, n + 14, nArray, 4);
        X448Field.decode56(byArray, n + 21, nArray, 6);
        X448Field.decode56(byArray, n + 28, nArray, 8);
        X448Field.decode56(byArray, n + 35, nArray, 10);
        X448Field.decode56(byArray, n + 42, nArray, 12);
        X448Field.decode56(byArray, n + 49, nArray, 14);
    }

    private static void decode224(int[] nArray, int n, int[] nArray2, int n2) {
        int n3 = nArray[n + 0];
        int n4 = nArray[n + 1];
        int n5 = nArray[n + 2];
        int n6 = nArray[n + 3];
        int n7 = nArray[n + 4];
        int n8 = nArray[n + 5];
        int n9 = nArray[n + 6];
        nArray2[n2 + 0] = n3 & 0xFFFFFFF;
        nArray2[n2 + 1] = (n3 >>> 28 | n4 << 4) & 0xFFFFFFF;
        nArray2[n2 + 2] = (n4 >>> 24 | n5 << 8) & 0xFFFFFFF;
        nArray2[n2 + 3] = (n5 >>> 20 | n6 << 12) & 0xFFFFFFF;
        nArray2[n2 + 4] = (n6 >>> 16 | n7 << 16) & 0xFFFFFFF;
        nArray2[n2 + 5] = (n7 >>> 12 | n8 << 20) & 0xFFFFFFF;
        nArray2[n2 + 6] = (n8 >>> 8 | n9 << 24) & 0xFFFFFFF;
        nArray2[n2 + 7] = n9 >>> 4;
    }

    private static int decode24(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        return n2 |= (byArray[++n] & 0xFF) << 16;
    }

    private static int decode32(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        n2 |= (byArray[++n] & 0xFF) << 16;
        return n2 |= byArray[++n] << 24;
    }

    private static void decode56(byte[] byArray, int n, int[] nArray, int n2) {
        int n3 = X448Field.decode32(byArray, n);
        int n4 = X448Field.decode24(byArray, n + 4);
        nArray[n2] = n3 & 0xFFFFFFF;
        nArray[n2 + 1] = n3 >>> 28 | n4 << 4;
    }

    public static void encode(int[] nArray, int[] nArray2, int n) {
        X448Field.encode224(nArray, 0, nArray2, n);
        X448Field.encode224(nArray, 8, nArray2, n + 7);
    }

    public static void encode(int[] nArray, byte[] byArray, int n) {
        X448Field.encode56(nArray, 0, byArray, n);
        X448Field.encode56(nArray, 2, byArray, n + 7);
        X448Field.encode56(nArray, 4, byArray, n + 14);
        X448Field.encode56(nArray, 6, byArray, n + 21);
        X448Field.encode56(nArray, 8, byArray, n + 28);
        X448Field.encode56(nArray, 10, byArray, n + 35);
        X448Field.encode56(nArray, 12, byArray, n + 42);
        X448Field.encode56(nArray, 14, byArray, n + 49);
    }

    private static void encode224(int[] nArray, int n, int[] nArray2, int n2) {
        int n3 = nArray[n + 0];
        int n4 = nArray[n + 1];
        int n5 = nArray[n + 2];
        int n6 = nArray[n + 3];
        int n7 = nArray[n + 4];
        int n8 = nArray[n + 5];
        int n9 = nArray[n + 6];
        int n10 = nArray[n + 7];
        nArray2[n2 + 0] = n3 | n4 << 28;
        nArray2[n2 + 1] = n4 >>> 4 | n5 << 24;
        nArray2[n2 + 2] = n5 >>> 8 | n6 << 20;
        nArray2[n2 + 3] = n6 >>> 12 | n7 << 16;
        nArray2[n2 + 4] = n7 >>> 16 | n8 << 12;
        nArray2[n2 + 5] = n8 >>> 20 | n9 << 8;
        nArray2[n2 + 6] = n9 >>> 24 | n10 << 4;
    }

    private static void encode24(int n, byte[] byArray, int n2) {
        byArray[n2] = (byte)n;
        byArray[++n2] = (byte)(n >>> 8);
        byArray[++n2] = (byte)(n >>> 16);
    }

    private static void encode32(int n, byte[] byArray, int n2) {
        byArray[n2] = (byte)n;
        byArray[++n2] = (byte)(n >>> 8);
        byArray[++n2] = (byte)(n >>> 16);
        byArray[++n2] = (byte)(n >>> 24);
    }

    private static void encode56(int[] nArray, int n, byte[] byArray, int n2) {
        int n3 = nArray[n];
        int n4 = nArray[n + 1];
        X448Field.encode32(n3 | n4 << 28, byArray, n2);
        X448Field.encode24(n4 >>> 4, byArray, n2 + 4);
    }

    public static void inv(int[] nArray, int[] nArray2) {
        int[] nArray3 = X448Field.create();
        int[] nArray4 = new int[14];
        X448Field.copy(nArray, 0, nArray3, 0);
        X448Field.normalize(nArray3);
        X448Field.encode(nArray3, nArray4, 0);
        Mod.modOddInverse(P32, nArray4, nArray4);
        X448Field.decode(nArray4, 0, nArray2);
    }

    public static void invVar(int[] nArray, int[] nArray2) {
        int[] nArray3 = X448Field.create();
        int[] nArray4 = new int[14];
        X448Field.copy(nArray, 0, nArray3, 0);
        X448Field.normalize(nArray3);
        X448Field.encode(nArray3, nArray4, 0);
        Mod.modOddInverseVar(P32, nArray4, nArray4);
        X448Field.decode(nArray4, 0, nArray2);
    }

    public static int isOne(int[] nArray) {
        int n = nArray[0] ^ 1;
        for (int i = 1; i < 16; ++i) {
            n |= nArray[i];
        }
        n = n >>> 1 | n & 1;
        return n - 1 >> 31;
    }

    public static boolean isOneVar(int[] nArray) {
        return 0 != X448Field.isOne(nArray);
    }

    public static int isZero(int[] nArray) {
        int n = 0;
        for (int i = 0; i < 16; ++i) {
            n |= nArray[i];
        }
        n = n >>> 1 | n & 1;
        return n - 1 >> 31;
    }

    public static boolean isZeroVar(int[] nArray) {
        return 0 != X448Field.isZero(nArray);
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
        int n12 = nArray[10];
        int n13 = nArray[11];
        int n14 = nArray[12];
        int n15 = nArray[13];
        int n16 = nArray[14];
        int n17 = nArray[15];
        long l = (long)n3 * (long)n;
        int n18 = (int)l & 0xFFFFFFF;
        l >>>= 28;
        long l2 = (long)n7 * (long)n;
        int n19 = (int)l2 & 0xFFFFFFF;
        l2 >>>= 28;
        long l3 = (long)n11 * (long)n;
        int n20 = (int)l3 & 0xFFFFFFF;
        l3 >>>= 28;
        long l4 = (long)n15 * (long)n;
        int n21 = (int)l4 & 0xFFFFFFF;
        l4 >>>= 28;
        nArray2[2] = (int)(l += (long)n4 * (long)n) & 0xFFFFFFF;
        l >>>= 28;
        nArray2[6] = (int)(l2 += (long)n8 * (long)n) & 0xFFFFFFF;
        l2 >>>= 28;
        nArray2[10] = (int)(l3 += (long)n12 * (long)n) & 0xFFFFFFF;
        l3 >>>= 28;
        nArray2[14] = (int)(l4 += (long)n16 * (long)n) & 0xFFFFFFF;
        l4 >>>= 28;
        nArray2[3] = (int)(l += (long)n5 * (long)n) & 0xFFFFFFF;
        l >>>= 28;
        nArray2[7] = (int)(l2 += (long)n9 * (long)n) & 0xFFFFFFF;
        l2 >>>= 28;
        nArray2[11] = (int)(l3 += (long)n13 * (long)n) & 0xFFFFFFF;
        l3 >>>= 28;
        nArray2[15] = (int)(l4 += (long)n17 * (long)n) & 0xFFFFFFF;
        l2 += (l4 >>>= 28);
        nArray2[4] = (int)(l += (long)n6 * (long)n) & 0xFFFFFFF;
        l >>>= 28;
        nArray2[8] = (int)(l2 += (long)n10 * (long)n) & 0xFFFFFFF;
        nArray2[12] = (int)(l3 += (long)n14 * (long)n) & 0xFFFFFFF;
        nArray2[0] = (int)(l4 += (long)n2 * (long)n) & 0xFFFFFFF;
        nArray2[1] = n18 + (int)(l4 >>>= 28);
        nArray2[5] = n19 + (int)l;
        nArray2[9] = n20 + (int)(l2 >>>= 28);
        nArray2[13] = n21 + (int)(l3 >>>= 28);
    }

    public static void mul(int[] nArray, int[] nArray2, int[] nArray3) {
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
        int n11 = nArray[10];
        int n12 = nArray[11];
        int n13 = nArray[12];
        int n14 = nArray[13];
        int n15 = nArray[14];
        int n16 = nArray[15];
        int n17 = nArray2[0];
        int n18 = nArray2[1];
        int n19 = nArray2[2];
        int n20 = nArray2[3];
        int n21 = nArray2[4];
        int n22 = nArray2[5];
        int n23 = nArray2[6];
        int n24 = nArray2[7];
        int n25 = nArray2[8];
        int n26 = nArray2[9];
        int n27 = nArray2[10];
        int n28 = nArray2[11];
        int n29 = nArray2[12];
        int n30 = nArray2[13];
        int n31 = nArray2[14];
        int n32 = nArray2[15];
        int n33 = n + n9;
        int n34 = n2 + n10;
        int n35 = n3 + n11;
        int n36 = n4 + n12;
        int n37 = n5 + n13;
        int n38 = n6 + n14;
        int n39 = n7 + n15;
        int n40 = n8 + n16;
        int n41 = n17 + n25;
        int n42 = n18 + n26;
        int n43 = n19 + n27;
        int n44 = n20 + n28;
        int n45 = n21 + n29;
        int n46 = n22 + n30;
        int n47 = n23 + n31;
        int n48 = n24 + n32;
        long l = (long)n * (long)n17;
        long l2 = (long)n8 * (long)n18 + (long)n7 * (long)n19 + (long)n6 * (long)n20 + (long)n5 * (long)n21 + (long)n4 * (long)n22 + (long)n3 * (long)n23 + (long)n2 * (long)n24;
        long l3 = (long)n9 * (long)n25;
        long l4 = (long)n16 * (long)n26 + (long)n15 * (long)n27 + (long)n14 * (long)n28 + (long)n13 * (long)n29 + (long)n12 * (long)n30 + (long)n11 * (long)n31 + (long)n10 * (long)n32;
        long l5 = (long)n33 * (long)n41;
        long l6 = (long)n40 * (long)n42 + (long)n39 * (long)n43 + (long)n38 * (long)n44 + (long)n37 * (long)n45 + (long)n36 * (long)n46 + (long)n35 * (long)n47 + (long)n34 * (long)n48;
        long l7 = l + l3 + l6 - l2;
        int n49 = (int)l7 & 0xFFFFFFF;
        l7 >>>= 28;
        long l8 = l4 + l5 - l + l6;
        int n50 = (int)l8 & 0xFFFFFFF;
        l8 >>>= 28;
        long l9 = (long)n2 * (long)n17 + (long)n * (long)n18;
        long l10 = (long)n8 * (long)n19 + (long)n7 * (long)n20 + (long)n6 * (long)n21 + (long)n5 * (long)n22 + (long)n4 * (long)n23 + (long)n3 * (long)n24;
        long l11 = (long)n10 * (long)n25 + (long)n9 * (long)n26;
        long l12 = (long)n16 * (long)n27 + (long)n15 * (long)n28 + (long)n14 * (long)n29 + (long)n13 * (long)n30 + (long)n12 * (long)n31 + (long)n11 * (long)n32;
        long l13 = (long)n34 * (long)n41 + (long)n33 * (long)n42;
        long l14 = (long)n40 * (long)n43 + (long)n39 * (long)n44 + (long)n38 * (long)n45 + (long)n37 * (long)n46 + (long)n36 * (long)n47 + (long)n35 * (long)n48;
        int n51 = (int)(l7 += l9 + l11 + l14 - l10) & 0xFFFFFFF;
        l7 >>>= 28;
        int n52 = (int)(l8 += l12 + l13 - l9 + l14) & 0xFFFFFFF;
        l8 >>>= 28;
        long l15 = (long)n3 * (long)n17 + (long)n2 * (long)n18 + (long)n * (long)n19;
        long l16 = (long)n8 * (long)n20 + (long)n7 * (long)n21 + (long)n6 * (long)n22 + (long)n5 * (long)n23 + (long)n4 * (long)n24;
        long l17 = (long)n11 * (long)n25 + (long)n10 * (long)n26 + (long)n9 * (long)n27;
        long l18 = (long)n16 * (long)n28 + (long)n15 * (long)n29 + (long)n14 * (long)n30 + (long)n13 * (long)n31 + (long)n12 * (long)n32;
        long l19 = (long)n35 * (long)n41 + (long)n34 * (long)n42 + (long)n33 * (long)n43;
        long l20 = (long)n40 * (long)n44 + (long)n39 * (long)n45 + (long)n38 * (long)n46 + (long)n37 * (long)n47 + (long)n36 * (long)n48;
        int n53 = (int)(l7 += l15 + l17 + l20 - l16) & 0xFFFFFFF;
        l7 >>>= 28;
        int n54 = (int)(l8 += l18 + l19 - l15 + l20) & 0xFFFFFFF;
        l8 >>>= 28;
        long l21 = (long)n4 * (long)n17 + (long)n3 * (long)n18 + (long)n2 * (long)n19 + (long)n * (long)n20;
        long l22 = (long)n8 * (long)n21 + (long)n7 * (long)n22 + (long)n6 * (long)n23 + (long)n5 * (long)n24;
        long l23 = (long)n12 * (long)n25 + (long)n11 * (long)n26 + (long)n10 * (long)n27 + (long)n9 * (long)n28;
        long l24 = (long)n16 * (long)n29 + (long)n15 * (long)n30 + (long)n14 * (long)n31 + (long)n13 * (long)n32;
        long l25 = (long)n36 * (long)n41 + (long)n35 * (long)n42 + (long)n34 * (long)n43 + (long)n33 * (long)n44;
        long l26 = (long)n40 * (long)n45 + (long)n39 * (long)n46 + (long)n38 * (long)n47 + (long)n37 * (long)n48;
        int n55 = (int)(l7 += l21 + l23 + l26 - l22) & 0xFFFFFFF;
        l7 >>>= 28;
        int n56 = (int)(l8 += l24 + l25 - l21 + l26) & 0xFFFFFFF;
        l8 >>>= 28;
        long l27 = (long)n5 * (long)n17 + (long)n4 * (long)n18 + (long)n3 * (long)n19 + (long)n2 * (long)n20 + (long)n * (long)n21;
        long l28 = (long)n8 * (long)n22 + (long)n7 * (long)n23 + (long)n6 * (long)n24;
        long l29 = (long)n13 * (long)n25 + (long)n12 * (long)n26 + (long)n11 * (long)n27 + (long)n10 * (long)n28 + (long)n9 * (long)n29;
        long l30 = (long)n16 * (long)n30 + (long)n15 * (long)n31 + (long)n14 * (long)n32;
        long l31 = (long)n37 * (long)n41 + (long)n36 * (long)n42 + (long)n35 * (long)n43 + (long)n34 * (long)n44 + (long)n33 * (long)n45;
        long l32 = (long)n40 * (long)n46 + (long)n39 * (long)n47 + (long)n38 * (long)n48;
        int n57 = (int)(l7 += l27 + l29 + l32 - l28) & 0xFFFFFFF;
        l7 >>>= 28;
        int n58 = (int)(l8 += l30 + l31 - l27 + l32) & 0xFFFFFFF;
        l8 >>>= 28;
        long l33 = (long)n6 * (long)n17 + (long)n5 * (long)n18 + (long)n4 * (long)n19 + (long)n3 * (long)n20 + (long)n2 * (long)n21 + (long)n * (long)n22;
        long l34 = (long)n8 * (long)n23 + (long)n7 * (long)n24;
        long l35 = (long)n14 * (long)n25 + (long)n13 * (long)n26 + (long)n12 * (long)n27 + (long)n11 * (long)n28 + (long)n10 * (long)n29 + (long)n9 * (long)n30;
        long l36 = (long)n16 * (long)n31 + (long)n15 * (long)n32;
        long l37 = (long)n38 * (long)n41 + (long)n37 * (long)n42 + (long)n36 * (long)n43 + (long)n35 * (long)n44 + (long)n34 * (long)n45 + (long)n33 * (long)n46;
        long l38 = (long)n40 * (long)n47 + (long)n39 * (long)n48;
        int n59 = (int)(l7 += l33 + l35 + l38 - l34) & 0xFFFFFFF;
        l7 >>>= 28;
        int n60 = (int)(l8 += l36 + l37 - l33 + l38) & 0xFFFFFFF;
        l8 >>>= 28;
        long l39 = (long)n7 * (long)n17 + (long)n6 * (long)n18 + (long)n5 * (long)n19 + (long)n4 * (long)n20 + (long)n3 * (long)n21 + (long)n2 * (long)n22 + (long)n * (long)n23;
        long l40 = (long)n8 * (long)n24;
        long l41 = (long)n15 * (long)n25 + (long)n14 * (long)n26 + (long)n13 * (long)n27 + (long)n12 * (long)n28 + (long)n11 * (long)n29 + (long)n10 * (long)n30 + (long)n9 * (long)n31;
        long l42 = (long)n16 * (long)n32;
        long l43 = (long)n39 * (long)n41 + (long)n38 * (long)n42 + (long)n37 * (long)n43 + (long)n36 * (long)n44 + (long)n35 * (long)n45 + (long)n34 * (long)n46 + (long)n33 * (long)n47;
        long l44 = (long)n40 * (long)n48;
        int n61 = (int)(l7 += l39 + l41 + l44 - l40) & 0xFFFFFFF;
        l7 >>>= 28;
        int n62 = (int)(l8 += l42 + l43 - l39 + l44) & 0xFFFFFFF;
        l8 >>>= 28;
        long l45 = (long)n8 * (long)n17 + (long)n7 * (long)n18 + (long)n6 * (long)n19 + (long)n5 * (long)n20 + (long)n4 * (long)n21 + (long)n3 * (long)n22 + (long)n2 * (long)n23 + (long)n * (long)n24;
        long l46 = (long)n16 * (long)n25 + (long)n15 * (long)n26 + (long)n14 * (long)n27 + (long)n13 * (long)n28 + (long)n12 * (long)n29 + (long)n11 * (long)n30 + (long)n10 * (long)n31 + (long)n9 * (long)n32;
        long l47 = (long)n40 * (long)n41 + (long)n39 * (long)n42 + (long)n38 * (long)n43 + (long)n37 * (long)n44 + (long)n36 * (long)n45 + (long)n35 * (long)n46 + (long)n34 * (long)n47 + (long)n33 * (long)n48;
        int n63 = (int)(l7 += l45 + l46) & 0xFFFFFFF;
        l7 >>>= 28;
        int n64 = (int)(l8 += l47 - l45) & 0xFFFFFFF;
        l7 += (l8 >>>= 28);
        l7 += (long)n50;
        n50 = (int)l7 & 0xFFFFFFF;
        l8 += (long)n49;
        n49 = (int)l8 & 0xFFFFFFF;
        n52 += (int)(l7 >>>= 28);
        nArray3[0] = n49;
        nArray3[1] = n51 += (int)(l8 >>>= 28);
        nArray3[2] = n53;
        nArray3[3] = n55;
        nArray3[4] = n57;
        nArray3[5] = n59;
        nArray3[6] = n61;
        nArray3[7] = n63;
        nArray3[8] = n50;
        nArray3[9] = n52;
        nArray3[10] = n54;
        nArray3[11] = n56;
        nArray3[12] = n58;
        nArray3[13] = n60;
        nArray3[14] = n62;
        nArray3[15] = n64;
    }

    public static void negate(int[] nArray, int[] nArray2) {
        int[] nArray3 = X448Field.create();
        X448Field.sub(nArray3, nArray, nArray2);
    }

    public static void normalize(int[] nArray) {
        X448Field.reduce(nArray, 1);
        X448Field.reduce(nArray, -1);
    }

    public static void one(int[] nArray) {
        nArray[0] = 1;
        for (int i = 1; i < 16; ++i) {
            nArray[i] = 0;
        }
    }

    private static void powPm3d4(int[] nArray, int[] nArray2) {
        int[] nArray3 = X448Field.create();
        X448Field.sqr(nArray, nArray3);
        X448Field.mul(nArray, nArray3, nArray3);
        int[] nArray4 = X448Field.create();
        X448Field.sqr(nArray3, nArray4);
        X448Field.mul(nArray, nArray4, nArray4);
        int[] nArray5 = X448Field.create();
        X448Field.sqr(nArray4, 3, nArray5);
        X448Field.mul(nArray4, nArray5, nArray5);
        int[] nArray6 = X448Field.create();
        X448Field.sqr(nArray5, 3, nArray6);
        X448Field.mul(nArray4, nArray6, nArray6);
        int[] nArray7 = X448Field.create();
        X448Field.sqr(nArray6, 9, nArray7);
        X448Field.mul(nArray6, nArray7, nArray7);
        int[] nArray8 = X448Field.create();
        X448Field.sqr(nArray7, nArray8);
        X448Field.mul(nArray, nArray8, nArray8);
        int[] nArray9 = X448Field.create();
        X448Field.sqr(nArray8, 18, nArray9);
        X448Field.mul(nArray7, nArray9, nArray9);
        int[] nArray10 = X448Field.create();
        X448Field.sqr(nArray9, 37, nArray10);
        X448Field.mul(nArray9, nArray10, nArray10);
        int[] nArray11 = X448Field.create();
        X448Field.sqr(nArray10, 37, nArray11);
        X448Field.mul(nArray9, nArray11, nArray11);
        int[] nArray12 = X448Field.create();
        X448Field.sqr(nArray11, 111, nArray12);
        X448Field.mul(nArray11, nArray12, nArray12);
        int[] nArray13 = X448Field.create();
        X448Field.sqr(nArray12, nArray13);
        X448Field.mul(nArray, nArray13, nArray13);
        int[] nArray14 = X448Field.create();
        X448Field.sqr(nArray13, 223, nArray14);
        X448Field.mul(nArray14, nArray12, nArray2);
    }

    private static void reduce(int[] nArray, int n) {
        int n2;
        int n3 = nArray[15];
        int n4 = n3 & 0xFFFFFFF;
        n3 = (n3 >>> 28) + n;
        long l = n3;
        for (n2 = 0; n2 < 8; ++n2) {
            nArray[n2] = (int)(l += (long)nArray[n2] & 0xFFFFFFFFL) & 0xFFFFFFF;
            l >>= 28;
        }
        l += (long)n3;
        for (n2 = 8; n2 < 15; ++n2) {
            nArray[n2] = (int)(l += (long)nArray[n2] & 0xFFFFFFFFL) & 0xFFFFFFF;
            l >>= 28;
        }
        nArray[15] = n4 + (int)l;
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
        int n11 = nArray[10];
        int n12 = nArray[11];
        int n13 = nArray[12];
        int n14 = nArray[13];
        int n15 = nArray[14];
        int n16 = nArray[15];
        int n17 = n * 2;
        int n18 = n2 * 2;
        int n19 = n3 * 2;
        int n20 = n4 * 2;
        int n21 = n5 * 2;
        int n22 = n6 * 2;
        int n23 = n7 * 2;
        int n24 = n9 * 2;
        int n25 = n10 * 2;
        int n26 = n11 * 2;
        int n27 = n12 * 2;
        int n28 = n13 * 2;
        int n29 = n14 * 2;
        int n30 = n15 * 2;
        int n31 = n + n9;
        int n32 = n2 + n10;
        int n33 = n3 + n11;
        int n34 = n4 + n12;
        int n35 = n5 + n13;
        int n36 = n6 + n14;
        int n37 = n7 + n15;
        int n38 = n8 + n16;
        int n39 = n31 * 2;
        int n40 = n32 * 2;
        int n41 = n33 * 2;
        int n42 = n34 * 2;
        int n43 = n35 * 2;
        int n44 = n36 * 2;
        int n45 = n37 * 2;
        long l = (long)n * (long)n;
        long l2 = (long)n8 * (long)n18 + (long)n7 * (long)n19 + (long)n6 * (long)n20 + (long)n5 * (long)n5;
        long l3 = (long)n9 * (long)n9;
        long l4 = (long)n16 * (long)n25 + (long)n15 * (long)n26 + (long)n14 * (long)n27 + (long)n13 * (long)n13;
        long l5 = (long)n31 * (long)n31;
        long l6 = (long)n38 * ((long)n40 & 0xFFFFFFFFL) + (long)n37 * ((long)n41 & 0xFFFFFFFFL) + (long)n36 * ((long)n42 & 0xFFFFFFFFL) + (long)n35 * (long)n35;
        long l7 = l + l3 + l6 - l2;
        int n46 = (int)l7 & 0xFFFFFFF;
        l7 >>>= 28;
        long l8 = l4 + l5 - l + l6;
        int n47 = (int)l8 & 0xFFFFFFF;
        l8 >>>= 28;
        long l9 = (long)n2 * (long)n17;
        long l10 = (long)n8 * (long)n19 + (long)n7 * (long)n20 + (long)n6 * (long)n21;
        long l11 = (long)n10 * (long)n24;
        long l12 = (long)n16 * (long)n26 + (long)n15 * (long)n27 + (long)n14 * (long)n28;
        long l13 = (long)n32 * ((long)n39 & 0xFFFFFFFFL);
        long l14 = (long)n38 * ((long)n41 & 0xFFFFFFFFL) + (long)n37 * ((long)n42 & 0xFFFFFFFFL) + (long)n36 * ((long)n43 & 0xFFFFFFFFL);
        int n48 = (int)(l7 += l9 + l11 + l14 - l10) & 0xFFFFFFF;
        l7 >>>= 28;
        int n49 = (int)(l8 += l12 + l13 - l9 + l14) & 0xFFFFFFF;
        l8 >>>= 28;
        long l15 = (long)n3 * (long)n17 + (long)n2 * (long)n2;
        long l16 = (long)n8 * (long)n20 + (long)n7 * (long)n21 + (long)n6 * (long)n6;
        long l17 = (long)n11 * (long)n24 + (long)n10 * (long)n10;
        long l18 = (long)n16 * (long)n27 + (long)n15 * (long)n28 + (long)n14 * (long)n14;
        long l19 = (long)n33 * ((long)n39 & 0xFFFFFFFFL) + (long)n32 * (long)n32;
        long l20 = (long)n38 * ((long)n42 & 0xFFFFFFFFL) + (long)n37 * ((long)n43 & 0xFFFFFFFFL) + (long)n36 * (long)n36;
        int n50 = (int)(l7 += l15 + l17 + l20 - l16) & 0xFFFFFFF;
        l7 >>>= 28;
        int n51 = (int)(l8 += l18 + l19 - l15 + l20) & 0xFFFFFFF;
        l8 >>>= 28;
        long l21 = (long)n4 * (long)n17 + (long)n3 * (long)n18;
        long l22 = (long)n8 * (long)n21 + (long)n7 * (long)n22;
        long l23 = (long)n12 * (long)n24 + (long)n11 * (long)n25;
        long l24 = (long)n16 * (long)n28 + (long)n15 * (long)n29;
        long l25 = (long)n34 * ((long)n39 & 0xFFFFFFFFL) + (long)n33 * ((long)n40 & 0xFFFFFFFFL);
        long l26 = (long)n38 * ((long)n43 & 0xFFFFFFFFL) + (long)n37 * ((long)n44 & 0xFFFFFFFFL);
        int n52 = (int)(l7 += l21 + l23 + l26 - l22) & 0xFFFFFFF;
        l7 >>>= 28;
        int n53 = (int)(l8 += l24 + l25 - l21 + l26) & 0xFFFFFFF;
        l8 >>>= 28;
        long l27 = (long)n5 * (long)n17 + (long)n4 * (long)n18 + (long)n3 * (long)n3;
        long l28 = (long)n8 * (long)n22 + (long)n7 * (long)n7;
        long l29 = (long)n13 * (long)n24 + (long)n12 * (long)n25 + (long)n11 * (long)n11;
        long l30 = (long)n16 * (long)n29 + (long)n15 * (long)n15;
        long l31 = (long)n35 * ((long)n39 & 0xFFFFFFFFL) + (long)n34 * ((long)n40 & 0xFFFFFFFFL) + (long)n33 * (long)n33;
        long l32 = (long)n38 * ((long)n44 & 0xFFFFFFFFL) + (long)n37 * (long)n37;
        int n54 = (int)(l7 += l27 + l29 + l32 - l28) & 0xFFFFFFF;
        l7 >>>= 28;
        int n55 = (int)(l8 += l30 + l31 - l27 + l32) & 0xFFFFFFF;
        l8 >>>= 28;
        long l33 = (long)n6 * (long)n17 + (long)n5 * (long)n18 + (long)n4 * (long)n19;
        long l34 = (long)n8 * (long)n23;
        long l35 = (long)n14 * (long)n24 + (long)n13 * (long)n25 + (long)n12 * (long)n26;
        long l36 = (long)n16 * (long)n30;
        long l37 = (long)n36 * ((long)n39 & 0xFFFFFFFFL) + (long)n35 * ((long)n40 & 0xFFFFFFFFL) + (long)n34 * ((long)n41 & 0xFFFFFFFFL);
        long l38 = (long)n38 * ((long)n45 & 0xFFFFFFFFL);
        int n56 = (int)(l7 += l33 + l35 + l38 - l34) & 0xFFFFFFF;
        l7 >>>= 28;
        int n57 = (int)(l8 += l36 + l37 - l33 + l38) & 0xFFFFFFF;
        l8 >>>= 28;
        long l39 = (long)n7 * (long)n17 + (long)n6 * (long)n18 + (long)n5 * (long)n19 + (long)n4 * (long)n4;
        long l40 = (long)n8 * (long)n8;
        long l41 = (long)n15 * (long)n24 + (long)n14 * (long)n25 + (long)n13 * (long)n26 + (long)n12 * (long)n12;
        long l42 = (long)n16 * (long)n16;
        long l43 = (long)n37 * ((long)n39 & 0xFFFFFFFFL) + (long)n36 * ((long)n40 & 0xFFFFFFFFL) + (long)n35 * ((long)n41 & 0xFFFFFFFFL) + (long)n34 * (long)n34;
        long l44 = (long)n38 * (long)n38;
        int n58 = (int)(l7 += l39 + l41 + l44 - l40) & 0xFFFFFFF;
        l7 >>>= 28;
        int n59 = (int)(l8 += l42 + l43 - l39 + l44) & 0xFFFFFFF;
        l8 >>>= 28;
        long l45 = (long)n8 * (long)n17 + (long)n7 * (long)n18 + (long)n6 * (long)n19 + (long)n5 * (long)n20;
        long l46 = (long)n16 * (long)n24 + (long)n15 * (long)n25 + (long)n14 * (long)n26 + (long)n13 * (long)n27;
        long l47 = (long)n38 * ((long)n39 & 0xFFFFFFFFL) + (long)n37 * ((long)n40 & 0xFFFFFFFFL) + (long)n36 * ((long)n41 & 0xFFFFFFFFL) + (long)n35 * ((long)n42 & 0xFFFFFFFFL);
        int n60 = (int)(l7 += l45 + l46) & 0xFFFFFFF;
        l7 >>>= 28;
        int n61 = (int)(l8 += l47 - l45) & 0xFFFFFFF;
        l7 += (l8 >>>= 28);
        l7 += (long)n47;
        n47 = (int)l7 & 0xFFFFFFF;
        l8 += (long)n46;
        n46 = (int)l8 & 0xFFFFFFF;
        n49 += (int)(l7 >>>= 28);
        nArray2[0] = n46;
        nArray2[1] = n48 += (int)(l8 >>>= 28);
        nArray2[2] = n50;
        nArray2[3] = n52;
        nArray2[4] = n54;
        nArray2[5] = n56;
        nArray2[6] = n58;
        nArray2[7] = n60;
        nArray2[8] = n47;
        nArray2[9] = n49;
        nArray2[10] = n51;
        nArray2[11] = n53;
        nArray2[12] = n55;
        nArray2[13] = n57;
        nArray2[14] = n59;
        nArray2[15] = n61;
    }

    public static void sqr(int[] nArray, int n, int[] nArray2) {
        X448Field.sqr(nArray, nArray2);
        while (--n > 0) {
            X448Field.sqr(nArray2, nArray2);
        }
    }

    public static boolean sqrtRatioVar(int[] nArray, int[] nArray2, int[] nArray3) {
        int[] nArray4 = X448Field.create();
        int[] nArray5 = X448Field.create();
        X448Field.sqr(nArray, nArray4);
        X448Field.mul(nArray4, nArray2, nArray4);
        X448Field.sqr(nArray4, nArray5);
        X448Field.mul(nArray4, nArray, nArray4);
        X448Field.mul(nArray5, nArray, nArray5);
        X448Field.mul(nArray5, nArray2, nArray5);
        int[] nArray6 = X448Field.create();
        X448Field.powPm3d4(nArray5, nArray6);
        X448Field.mul(nArray6, nArray4, nArray6);
        int[] nArray7 = X448Field.create();
        X448Field.sqr(nArray6, nArray7);
        X448Field.mul(nArray7, nArray2, nArray7);
        X448Field.sub(nArray, nArray7, nArray7);
        X448Field.normalize(nArray7);
        if (X448Field.isZeroVar(nArray7)) {
            X448Field.copy(nArray6, 0, nArray3, 0);
            return true;
        }
        return false;
    }

    public static void sub(int[] nArray, int[] nArray2, int[] nArray3) {
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
        int n11 = nArray[10];
        int n12 = nArray[11];
        int n13 = nArray[12];
        int n14 = nArray[13];
        int n15 = nArray[14];
        int n16 = nArray[15];
        int n17 = nArray2[0];
        int n18 = nArray2[1];
        int n19 = nArray2[2];
        int n20 = nArray2[3];
        int n21 = nArray2[4];
        int n22 = nArray2[5];
        int n23 = nArray2[6];
        int n24 = nArray2[7];
        int n25 = nArray2[8];
        int n26 = nArray2[9];
        int n27 = nArray2[10];
        int n28 = nArray2[11];
        int n29 = nArray2[12];
        int n30 = nArray2[13];
        int n31 = nArray2[14];
        int n32 = nArray2[15];
        int n33 = n + 0x1FFFFFFE - n17;
        int n34 = n2 + 0x1FFFFFFE - n18;
        int n35 = n3 + 0x1FFFFFFE - n19;
        int n36 = n4 + 0x1FFFFFFE - n20;
        int n37 = n5 + 0x1FFFFFFE - n21;
        int n38 = n6 + 0x1FFFFFFE - n22;
        int n39 = n7 + 0x1FFFFFFE - n23;
        int n40 = n8 + 0x1FFFFFFE - n24;
        int n41 = n9 + 0x1FFFFFFC - n25;
        int n42 = n10 + 0x1FFFFFFE - n26;
        int n43 = n11 + 0x1FFFFFFE - n27;
        int n44 = n12 + 0x1FFFFFFE - n28;
        int n45 = n13 + 0x1FFFFFFE - n29;
        int n46 = n14 + 0x1FFFFFFE - n30;
        int n47 = n15 + 0x1FFFFFFE - n31;
        int n48 = n16 + 0x1FFFFFFE - n32;
        n35 += n34 >>> 28;
        n34 &= 0xFFFFFFF;
        n39 += n38 >>> 28;
        n38 &= 0xFFFFFFF;
        n43 += n42 >>> 28;
        n42 &= 0xFFFFFFF;
        n47 += n46 >>> 28;
        n46 &= 0xFFFFFFF;
        n36 += n35 >>> 28;
        n35 &= 0xFFFFFFF;
        n40 += n39 >>> 28;
        n39 &= 0xFFFFFFF;
        n44 += n43 >>> 28;
        n43 &= 0xFFFFFFF;
        n48 += n47 >>> 28;
        n47 &= 0xFFFFFFF;
        int n49 = n48 >>> 28;
        n48 &= 0xFFFFFFF;
        n33 += n49;
        n41 += n49;
        n37 += n36 >>> 28;
        n36 &= 0xFFFFFFF;
        n41 += n40 >>> 28;
        n40 &= 0xFFFFFFF;
        n45 += n44 >>> 28;
        n44 &= 0xFFFFFFF;
        n34 += n33 >>> 28;
        n33 &= 0xFFFFFFF;
        n38 += n37 >>> 28;
        n37 &= 0xFFFFFFF;
        n42 += n41 >>> 28;
        n41 &= 0xFFFFFFF;
        n46 += n45 >>> 28;
        n45 &= 0xFFFFFFF;
        nArray3[0] = n33;
        nArray3[1] = n34;
        nArray3[2] = n35;
        nArray3[3] = n36;
        nArray3[4] = n37;
        nArray3[5] = n38;
        nArray3[6] = n39;
        nArray3[7] = n40;
        nArray3[8] = n41;
        nArray3[9] = n42;
        nArray3[10] = n43;
        nArray3[11] = n44;
        nArray3[12] = n45;
        nArray3[13] = n46;
        nArray3[14] = n47;
        nArray3[15] = n48;
    }

    public static void subOne(int[] nArray) {
        int[] nArray2 = X448Field.create();
        nArray2[0] = 1;
        X448Field.sub(nArray, nArray2, nArray);
    }

    public static void zero(int[] nArray) {
        for (int i = 0; i < 16; ++i) {
            nArray[i] = 0;
        }
    }
}


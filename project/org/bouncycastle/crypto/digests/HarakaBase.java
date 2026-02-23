/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.Digest;

public abstract class HarakaBase
implements Digest {
    protected static final int DIGEST_SIZE = 32;
    private static final byte[][] S = new byte[][]{{99, 124, 119, 123, -14, 107, 111, -59, 48, 1, 103, 43, -2, -41, -85, 118}, {-54, -126, -55, 125, -6, 89, 71, -16, -83, -44, -94, -81, -100, -92, 114, -64}, {-73, -3, -109, 38, 54, 63, -9, -52, 52, -91, -27, -15, 113, -40, 49, 21}, {4, -57, 35, -61, 24, -106, 5, -102, 7, 18, -128, -30, -21, 39, -78, 117}, {9, -125, 44, 26, 27, 110, 90, -96, 82, 59, -42, -77, 41, -29, 47, -124}, {83, -47, 0, -19, 32, -4, -79, 91, 106, -53, -66, 57, 74, 76, 88, -49}, {-48, -17, -86, -5, 67, 77, 51, -123, 69, -7, 2, 127, 80, 60, -97, -88}, {81, -93, 64, -113, -110, -99, 56, -11, -68, -74, -38, 33, 16, -1, -13, -46}, {-51, 12, 19, -20, 95, -105, 68, 23, -60, -89, 126, 61, 100, 93, 25, 115}, {96, -127, 79, -36, 34, 42, -112, -120, 70, -18, -72, 20, -34, 94, 11, -37}, {-32, 50, 58, 10, 73, 6, 36, 92, -62, -45, -84, 98, -111, -107, -28, 121}, {-25, -56, 55, 109, -115, -43, 78, -87, 108, 86, -12, -22, 101, 122, -82, 8}, {-70, 120, 37, 46, 28, -90, -76, -58, -24, -35, 116, 31, 75, -67, -117, -118}, {112, 62, -75, 102, 72, 3, -10, 14, 97, 53, 87, -71, -122, -63, 29, -98}, {-31, -8, -104, 17, 105, -39, -114, -108, -101, 30, -121, -23, -50, 85, 40, -33}, {-116, -95, -119, 13, -65, -26, 66, 104, 65, -103, 45, 15, -80, 84, -69, 22}};

    static byte sBox(byte by) {
        return S[(by & 0xFF) >>> 4][by & 0xF];
    }

    static byte[] subBytes(byte[] byArray) {
        byte[] byArray2 = new byte[byArray.length];
        byArray2[0] = HarakaBase.sBox(byArray[0]);
        byArray2[1] = HarakaBase.sBox(byArray[1]);
        byArray2[2] = HarakaBase.sBox(byArray[2]);
        byArray2[3] = HarakaBase.sBox(byArray[3]);
        byArray2[4] = HarakaBase.sBox(byArray[4]);
        byArray2[5] = HarakaBase.sBox(byArray[5]);
        byArray2[6] = HarakaBase.sBox(byArray[6]);
        byArray2[7] = HarakaBase.sBox(byArray[7]);
        byArray2[8] = HarakaBase.sBox(byArray[8]);
        byArray2[9] = HarakaBase.sBox(byArray[9]);
        byArray2[10] = HarakaBase.sBox(byArray[10]);
        byArray2[11] = HarakaBase.sBox(byArray[11]);
        byArray2[12] = HarakaBase.sBox(byArray[12]);
        byArray2[13] = HarakaBase.sBox(byArray[13]);
        byArray2[14] = HarakaBase.sBox(byArray[14]);
        byArray2[15] = HarakaBase.sBox(byArray[15]);
        return byArray2;
    }

    static byte[] shiftRows(byte[] byArray) {
        return new byte[]{byArray[0], byArray[5], byArray[10], byArray[15], byArray[4], byArray[9], byArray[14], byArray[3], byArray[8], byArray[13], byArray[2], byArray[7], byArray[12], byArray[1], byArray[6], byArray[11]};
    }

    static byte[] aesEnc(byte[] byArray, byte[] byArray2) {
        byArray = HarakaBase.subBytes(byArray);
        byArray = HarakaBase.shiftRows(byArray);
        byArray = HarakaBase.mixColumns(byArray);
        HarakaBase.xorReverse(byArray, byArray2);
        return byArray;
    }

    static byte xTime(byte by) {
        if (by >>> 7 > 0) {
            return (byte)((by << 1 ^ 0x1B) & 0xFF);
        }
        return (byte)(by << 1 & 0xFF);
    }

    static void xorReverse(byte[] byArray, byte[] byArray2) {
        byArray[0] = (byte)(byArray[0] ^ byArray2[15]);
        byArray[1] = (byte)(byArray[1] ^ byArray2[14]);
        byArray[2] = (byte)(byArray[2] ^ byArray2[13]);
        byArray[3] = (byte)(byArray[3] ^ byArray2[12]);
        byArray[4] = (byte)(byArray[4] ^ byArray2[11]);
        byArray[5] = (byte)(byArray[5] ^ byArray2[10]);
        byArray[6] = (byte)(byArray[6] ^ byArray2[9]);
        byArray[7] = (byte)(byArray[7] ^ byArray2[8]);
        byArray[8] = (byte)(byArray[8] ^ byArray2[7]);
        byArray[9] = (byte)(byArray[9] ^ byArray2[6]);
        byArray[10] = (byte)(byArray[10] ^ byArray2[5]);
        byArray[11] = (byte)(byArray[11] ^ byArray2[4]);
        byArray[12] = (byte)(byArray[12] ^ byArray2[3]);
        byArray[13] = (byte)(byArray[13] ^ byArray2[2]);
        byArray[14] = (byte)(byArray[14] ^ byArray2[1]);
        byArray[15] = (byte)(byArray[15] ^ byArray2[0]);
    }

    static byte[] xor(byte[] byArray, byte[] byArray2, int n) {
        byte[] byArray3 = new byte[16];
        for (int i = 0; i < byArray3.length; ++i) {
            byArray3[i] = (byte)(byArray[i] ^ byArray2[n++]);
        }
        return byArray3;
    }

    private static byte[] mixColumns(byte[] byArray) {
        byte[] byArray2 = new byte[byArray.length];
        int n = 0;
        for (int i = 0; i < 4; ++i) {
            byArray2[n++] = (byte)(HarakaBase.xTime(byArray[4 * i]) ^ HarakaBase.xTime(byArray[4 * i + 1]) ^ byArray[4 * i + 1] ^ byArray[4 * i + 2] ^ byArray[4 * i + 3]);
            byArray2[n++] = (byte)(byArray[4 * i] ^ HarakaBase.xTime(byArray[4 * i + 1]) ^ HarakaBase.xTime(byArray[4 * i + 2]) ^ byArray[4 * i + 2] ^ byArray[4 * i + 3]);
            byArray2[n++] = (byte)(byArray[4 * i] ^ byArray[4 * i + 1] ^ HarakaBase.xTime(byArray[4 * i + 2]) ^ HarakaBase.xTime(byArray[4 * i + 3]) ^ byArray[4 * i + 3]);
            byArray2[n++] = (byte)(HarakaBase.xTime(byArray[4 * i]) ^ byArray[4 * i] ^ byArray[4 * i + 1] ^ byArray[4 * i + 2] ^ HarakaBase.xTime(byArray[4 * i + 3]));
        }
        return byArray2;
    }

    public int getDigestSize() {
        return 32;
    }
}


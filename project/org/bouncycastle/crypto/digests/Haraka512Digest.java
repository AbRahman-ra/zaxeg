/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.digests.HarakaBase;
import org.bouncycastle.util.Arrays;

public class Haraka512Digest
extends HarakaBase {
    private static byte[][] RC = new byte[][]{{6, -124, 112, 76, -26, 32, -64, 10, -78, -59, -2, -16, 117, -127, 123, -99}, {-117, 102, -76, -31, -120, -13, -96, 107, 100, 15, 107, -92, 47, 8, -9, 23}, {52, 2, -34, 45, 83, -14, -124, -104, -49, 2, -99, 96, -97, 2, -111, 20}, {14, -42, -22, -26, 46, 123, 79, 8, -69, -13, -68, -81, -3, 91, 79, 121}, {-53, -49, -80, -53, 72, 114, 68, -117, 121, -18, -51, 28, -66, 57, 112, 68}, {126, -22, -51, -18, 110, -112, 50, -73, -115, 83, 53, -19, 43, -118, 5, 123}, {103, -62, -113, 67, 94, 46, 124, -48, -30, 65, 39, 97, -38, 79, -17, 27}, {41, 36, -39, -80, -81, -54, -52, 7, 103, 95, -3, -30, 31, -57, 11, 59}, {-85, 77, 99, -15, -26, -122, 127, -23, -20, -37, -113, -54, -71, -44, 101, -18}, {28, 48, -65, -124, -44, -73, -51, 100, 91, 42, 64, 79, -83, 3, 126, 51}, {-78, -52, 11, -71, -108, 23, 35, -65, 105, 2, -117, 46, -115, -10, -104, 0}, {-6, 4, 120, -90, -34, 111, 85, 114, 74, -86, -98, -56, 92, -99, 45, -118}, {-33, -76, -97, 43, 107, 119, 42, 18, 14, -6, 79, 46, 41, 18, -97, -44}, {30, -95, 3, 68, -12, 73, -94, 54, 50, -42, 17, -82, -69, 106, 18, -18}, {-81, 4, 73, -120, 75, 5, 0, -124, 95, -106, 0, -55, -100, -88, -20, -90}, {33, 2, 94, -40, -99, 25, -100, 79, 120, -94, -57, -29, 39, -27, -109, -20}, {-65, 58, -86, -8, -89, 89, -55, -73, -71, 40, 46, -51, -126, -44, 1, 115}, {98, 96, 112, 13, 97, -122, -80, 23, 55, -14, -17, -39, 16, 48, 125, 107}, {90, -54, 69, -62, 33, 48, 4, 67, -127, -62, -111, 83, -10, -4, -102, -58}, {-110, 35, -105, 60, 34, 107, 104, -69, 44, -81, -110, -24, 54, -47, -108, 58}, {-45, -65, -110, 56, 34, 88, -122, -21, 108, -70, -71, 88, -27, 16, 113, -76}, {-37, -122, 60, -27, -82, -16, -58, 119, -109, 61, -3, -35, 36, -31, 18, -115}, {-69, 96, 98, 104, -1, -21, -96, -100, -125, -28, -115, -29, -53, 34, 18, -79}, {115, 75, -45, -36, -30, -28, -47, -100, 45, -71, 26, 78, -57, 43, -9, 125}, {67, -69, 71, -61, 97, 48, 27, 67, 75, 20, 21, -60, 44, -77, -110, 78}, {-37, -89, 117, -88, -25, 7, -17, -10, 3, -78, 49, -35, 22, -21, 104, -103}, {109, -13, 97, 75, 60, 117, 89, 119, -114, 94, 35, 2, 126, -54, 71, 44}, {-51, -89, 90, 23, -42, -34, 125, 119, 109, 27, -27, -71, -72, -122, 23, -7}, {-20, 107, 67, -16, 107, -88, -23, -86, -99, 108, 6, -99, -87, 70, -18, 93}, {-53, 30, 105, 80, -7, 87, 51, 43, -94, 83, 17, 89, 59, -13, 39, -63}, {44, -18, 12, 117, 0, -38, 97, -100, -28, -19, 3, 83, 96, 14, -48, -39}, {-16, -79, -91, -95, -106, -23, 12, -85, -128, -69, -70, -68, 99, -92, -93, 80}, {-82, 61, -79, 2, 94, -106, 41, -120, -85, 13, -34, 48, -109, -115, -54, 57}, {23, -69, -113, 56, -43, 84, -92, 11, -120, 20, -13, -88, 46, 117, -76, 66}, {52, -69, -118, 91, 95, 66, 127, -41, -82, -74, -73, 121, 54, 10, 22, -10}, {38, -10, 82, 65, -53, -27, 84, 56, 67, -50, 89, 24, -1, -70, -81, -34}, {76, -23, -102, 84, -71, -13, 2, 106, -94, -54, -100, -9, -125, -98, -55, 120}, {-82, 81, -91, 26, 27, -33, -9, -66, 64, -64, 110, 40, 34, -112, 18, 53}, {-96, -63, 97, 60, -70, 126, -46, 43, -63, 115, -68, 15, 72, -90, 89, -49}, {117, 106, -52, 3, 2, 40, -126, -120, 74, -42, -67, -3, -23, -59, -99, -95}};
    private final byte[] buffer;
    private int off;

    public Haraka512Digest() {
        this.buffer = new byte[64];
    }

    public Haraka512Digest(Haraka512Digest haraka512Digest) {
        this.buffer = Arrays.clone(haraka512Digest.buffer);
        this.off = haraka512Digest.off;
    }

    private void mix512(byte[][] byArray, byte[][] byArray2) {
        System.arraycopy(byArray[0], 12, byArray2[0], 0, 4);
        System.arraycopy(byArray[2], 12, byArray2[0], 4, 4);
        System.arraycopy(byArray[1], 12, byArray2[0], 8, 4);
        System.arraycopy(byArray[3], 12, byArray2[0], 12, 4);
        System.arraycopy(byArray[2], 0, byArray2[1], 0, 4);
        System.arraycopy(byArray[0], 0, byArray2[1], 4, 4);
        System.arraycopy(byArray[3], 0, byArray2[1], 8, 4);
        System.arraycopy(byArray[1], 0, byArray2[1], 12, 4);
        System.arraycopy(byArray[2], 4, byArray2[2], 0, 4);
        System.arraycopy(byArray[0], 4, byArray2[2], 4, 4);
        System.arraycopy(byArray[3], 4, byArray2[2], 8, 4);
        System.arraycopy(byArray[1], 4, byArray2[2], 12, 4);
        System.arraycopy(byArray[0], 8, byArray2[3], 0, 4);
        System.arraycopy(byArray[2], 8, byArray2[3], 4, 4);
        System.arraycopy(byArray[1], 8, byArray2[3], 8, 4);
        System.arraycopy(byArray[3], 8, byArray2[3], 12, 4);
    }

    private int haraka512256(byte[] byArray, byte[] byArray2, int n) {
        byte[][] byArray3 = new byte[4][16];
        byte[][] byArray4 = new byte[4][16];
        System.arraycopy(byArray, 0, byArray3[0], 0, 16);
        System.arraycopy(byArray, 16, byArray3[1], 0, 16);
        System.arraycopy(byArray, 32, byArray3[2], 0, 16);
        System.arraycopy(byArray, 48, byArray3[3], 0, 16);
        byArray3[0] = Haraka512Digest.aesEnc(byArray3[0], RC[0]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray3[1], RC[1]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray3[2], RC[2]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray3[3], RC[3]);
        byArray3[0] = Haraka512Digest.aesEnc(byArray3[0], RC[4]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray3[1], RC[5]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray3[2], RC[6]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray3[3], RC[7]);
        this.mix512(byArray3, byArray4);
        byArray3[0] = Haraka512Digest.aesEnc(byArray4[0], RC[8]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray4[1], RC[9]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray4[2], RC[10]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray4[3], RC[11]);
        byArray3[0] = Haraka512Digest.aesEnc(byArray3[0], RC[12]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray3[1], RC[13]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray3[2], RC[14]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray3[3], RC[15]);
        this.mix512(byArray3, byArray4);
        byArray3[0] = Haraka512Digest.aesEnc(byArray4[0], RC[16]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray4[1], RC[17]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray4[2], RC[18]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray4[3], RC[19]);
        byArray3[0] = Haraka512Digest.aesEnc(byArray3[0], RC[20]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray3[1], RC[21]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray3[2], RC[22]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray3[3], RC[23]);
        this.mix512(byArray3, byArray4);
        byArray3[0] = Haraka512Digest.aesEnc(byArray4[0], RC[24]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray4[1], RC[25]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray4[2], RC[26]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray4[3], RC[27]);
        byArray3[0] = Haraka512Digest.aesEnc(byArray3[0], RC[28]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray3[1], RC[29]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray3[2], RC[30]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray3[3], RC[31]);
        this.mix512(byArray3, byArray4);
        byArray3[0] = Haraka512Digest.aesEnc(byArray4[0], RC[32]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray4[1], RC[33]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray4[2], RC[34]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray4[3], RC[35]);
        byArray3[0] = Haraka512Digest.aesEnc(byArray3[0], RC[36]);
        byArray3[1] = Haraka512Digest.aesEnc(byArray3[1], RC[37]);
        byArray3[2] = Haraka512Digest.aesEnc(byArray3[2], RC[38]);
        byArray3[3] = Haraka512Digest.aesEnc(byArray3[3], RC[39]);
        this.mix512(byArray3, byArray4);
        byArray3[0] = Haraka512Digest.xor(byArray4[0], byArray, 0);
        byArray3[1] = Haraka512Digest.xor(byArray4[1], byArray, 16);
        byArray3[2] = Haraka512Digest.xor(byArray4[2], byArray, 32);
        byArray3[3] = Haraka512Digest.xor(byArray4[3], byArray, 48);
        System.arraycopy(byArray3[0], 8, byArray2, n, 8);
        System.arraycopy(byArray3[1], 8, byArray2, n + 8, 8);
        System.arraycopy(byArray3[2], 0, byArray2, n + 16, 8);
        System.arraycopy(byArray3[3], 0, byArray2, n + 24, 8);
        return 32;
    }

    public String getAlgorithmName() {
        return "Haraka-512";
    }

    public void update(byte by) {
        if (this.off + 1 > 64) {
            throw new IllegalArgumentException("total input cannot be more than 64 bytes");
        }
        this.buffer[this.off++] = by;
    }

    public void update(byte[] byArray, int n, int n2) {
        if (this.off + n2 > 64) {
            throw new IllegalArgumentException("total input cannot be more than 64 bytes");
        }
        System.arraycopy(byArray, n, this.buffer, this.off, n2);
        this.off += n2;
    }

    public int doFinal(byte[] byArray, int n) {
        if (this.off != 64) {
            throw new IllegalStateException("input must be exactly 64 bytes");
        }
        if (byArray.length - n < 32) {
            throw new IllegalArgumentException("output too short to receive digest");
        }
        int n2 = this.haraka512256(this.buffer, byArray, n);
        this.reset();
        return n2;
    }

    public void reset() {
        this.off = 0;
        Arrays.clear(this.buffer);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.digests.HarakaBase;
import org.bouncycastle.util.Arrays;

public class Haraka256Digest
extends HarakaBase {
    private static final byte[][] RC = new byte[][]{{6, -124, 112, 76, -26, 32, -64, 10, -78, -59, -2, -16, 117, -127, 123, -99}, {-117, 102, -76, -31, -120, -13, -96, 107, 100, 15, 107, -92, 47, 8, -9, 23}, {52, 2, -34, 45, 83, -14, -124, -104, -49, 2, -99, 96, -97, 2, -111, 20}, {14, -42, -22, -26, 46, 123, 79, 8, -69, -13, -68, -81, -3, 91, 79, 121}, {-53, -49, -80, -53, 72, 114, 68, -117, 121, -18, -51, 28, -66, 57, 112, 68}, {126, -22, -51, -18, 110, -112, 50, -73, -115, 83, 53, -19, 43, -118, 5, 123}, {103, -62, -113, 67, 94, 46, 124, -48, -30, 65, 39, 97, -38, 79, -17, 27}, {41, 36, -39, -80, -81, -54, -52, 7, 103, 95, -3, -30, 31, -57, 11, 59}, {-85, 77, 99, -15, -26, -122, 127, -23, -20, -37, -113, -54, -71, -44, 101, -18}, {28, 48, -65, -124, -44, -73, -51, 100, 91, 42, 64, 79, -83, 3, 126, 51}, {-78, -52, 11, -71, -108, 23, 35, -65, 105, 2, -117, 46, -115, -10, -104, 0}, {-6, 4, 120, -90, -34, 111, 85, 114, 74, -86, -98, -56, 92, -99, 45, -118}, {-33, -76, -97, 43, 107, 119, 42, 18, 14, -6, 79, 46, 41, 18, -97, -44}, {30, -95, 3, 68, -12, 73, -94, 54, 50, -42, 17, -82, -69, 106, 18, -18}, {-81, 4, 73, -120, 75, 5, 0, -124, 95, -106, 0, -55, -100, -88, -20, -90}, {33, 2, 94, -40, -99, 25, -100, 79, 120, -94, -57, -29, 39, -27, -109, -20}, {-65, 58, -86, -8, -89, 89, -55, -73, -71, 40, 46, -51, -126, -44, 1, 115}, {98, 96, 112, 13, 97, -122, -80, 23, 55, -14, -17, -39, 16, 48, 125, 107}, {90, -54, 69, -62, 33, 48, 4, 67, -127, -62, -111, 83, -10, -4, -102, -58}, {-110, 35, -105, 60, 34, 107, 104, -69, 44, -81, -110, -24, 54, -47, -108, 58}};
    private final byte[] buffer;
    private int off;

    private void mix256(byte[][] byArray, byte[][] byArray2) {
        System.arraycopy(byArray[0], 0, byArray2[0], 0, 4);
        System.arraycopy(byArray[1], 0, byArray2[0], 4, 4);
        System.arraycopy(byArray[0], 4, byArray2[0], 8, 4);
        System.arraycopy(byArray[1], 4, byArray2[0], 12, 4);
        System.arraycopy(byArray[0], 8, byArray2[1], 0, 4);
        System.arraycopy(byArray[1], 8, byArray2[1], 4, 4);
        System.arraycopy(byArray[0], 12, byArray2[1], 8, 4);
        System.arraycopy(byArray[1], 12, byArray2[1], 12, 4);
    }

    private int haraka256256(byte[] byArray, byte[] byArray2, int n) {
        byte[][] byArray3 = new byte[2][16];
        byte[][] byArray4 = new byte[2][16];
        System.arraycopy(byArray, 0, byArray3[0], 0, 16);
        System.arraycopy(byArray, 16, byArray3[1], 0, 16);
        byArray3[0] = Haraka256Digest.aesEnc(byArray3[0], RC[0]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray3[1], RC[1]);
        byArray3[0] = Haraka256Digest.aesEnc(byArray3[0], RC[2]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray3[1], RC[3]);
        this.mix256(byArray3, byArray4);
        byArray3[0] = Haraka256Digest.aesEnc(byArray4[0], RC[4]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray4[1], RC[5]);
        byArray3[0] = Haraka256Digest.aesEnc(byArray3[0], RC[6]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray3[1], RC[7]);
        this.mix256(byArray3, byArray4);
        byArray3[0] = Haraka256Digest.aesEnc(byArray4[0], RC[8]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray4[1], RC[9]);
        byArray3[0] = Haraka256Digest.aesEnc(byArray3[0], RC[10]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray3[1], RC[11]);
        this.mix256(byArray3, byArray4);
        byArray3[0] = Haraka256Digest.aesEnc(byArray4[0], RC[12]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray4[1], RC[13]);
        byArray3[0] = Haraka256Digest.aesEnc(byArray3[0], RC[14]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray3[1], RC[15]);
        this.mix256(byArray3, byArray4);
        byArray3[0] = Haraka256Digest.aesEnc(byArray4[0], RC[16]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray4[1], RC[17]);
        byArray3[0] = Haraka256Digest.aesEnc(byArray3[0], RC[18]);
        byArray3[1] = Haraka256Digest.aesEnc(byArray3[1], RC[19]);
        this.mix256(byArray3, byArray4);
        byArray3[0] = Haraka256Digest.xor(byArray4[0], byArray, 0);
        byArray3[1] = Haraka256Digest.xor(byArray4[1], byArray, 16);
        System.arraycopy(byArray3[0], 0, byArray2, n, 16);
        System.arraycopy(byArray3[1], 0, byArray2, n + 16, 16);
        return 32;
    }

    public Haraka256Digest() {
        this.buffer = new byte[32];
    }

    public Haraka256Digest(Haraka256Digest haraka256Digest) {
        this.buffer = Arrays.clone(haraka256Digest.buffer);
        this.off = haraka256Digest.off;
    }

    public String getAlgorithmName() {
        return "Haraka-256";
    }

    public void update(byte by) {
        if (this.off + 1 > 32) {
            throw new IllegalArgumentException("total input cannot be more than 32 bytes");
        }
        this.buffer[this.off++] = by;
    }

    public void update(byte[] byArray, int n, int n2) {
        if (this.off + n2 > 32) {
            throw new IllegalArgumentException("total input cannot be more than 32 bytes");
        }
        System.arraycopy(byArray, n, this.buffer, this.off, n2);
        this.off += n2;
    }

    public int doFinal(byte[] byArray, int n) {
        if (this.off != 32) {
            throw new IllegalStateException("input must be exactly 32 bytes");
        }
        if (byArray.length - n < 32) {
            throw new IllegalArgumentException("output too short to receive digest");
        }
        int n2 = this.haraka256256(this.buffer, byArray, n);
        this.reset();
        return n2;
    }

    public void reset() {
        this.off = 0;
        Arrays.clear(this.buffer);
    }
}


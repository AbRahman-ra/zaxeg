/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class DSTU7564Digest
implements ExtendedDigest,
Memoable {
    private static final int NB_512 = 8;
    private static final int NB_1024 = 16;
    private static final int NR_512 = 10;
    private static final int NR_1024 = 14;
    private int hashSize;
    private int blockSize;
    private int columns;
    private int rounds;
    private long[] state;
    private long[] tempState1;
    private long[] tempState2;
    private long inputBlocks;
    private int bufOff;
    private byte[] buf;
    private static final byte[] S0 = new byte[]{-88, 67, 95, 6, 107, 117, 108, 89, 113, -33, -121, -107, 23, -16, -40, 9, 109, -13, 29, -53, -55, 77, 44, -81, 121, -32, -105, -3, 111, 75, 69, 57, 62, -35, -93, 79, -76, -74, -102, 14, 31, -65, 21, -31, 73, -46, -109, -58, -110, 114, -98, 97, -47, 99, -6, -18, -12, 25, -43, -83, 88, -92, -69, -95, -36, -14, -125, 55, 66, -28, 122, 50, -100, -52, -85, 74, -113, 110, 4, 39, 46, -25, -30, 90, -106, 22, 35, 43, -62, 101, 102, 15, -68, -87, 71, 65, 52, 72, -4, -73, 106, -120, -91, 83, -122, -7, 91, -37, 56, 123, -61, 30, 34, 51, 36, 40, 54, -57, -78, 59, -114, 119, -70, -11, 20, -97, 8, 85, -101, 76, -2, 96, 92, -38, 24, 70, -51, 125, 33, -80, 63, 27, -119, -1, -21, -124, 105, 58, -99, -41, -45, 112, 103, 64, -75, -34, 93, 48, -111, -79, 120, 17, 1, -27, 0, 104, -104, -96, -59, 2, -90, 116, 45, 11, -94, 118, -77, -66, -50, -67, -82, -23, -118, 49, 28, -20, -15, -103, -108, -86, -10, 38, 47, -17, -24, -116, 53, 3, -44, 127, -5, 5, -63, 94, -112, 32, 61, -126, -9, -22, 10, 13, 126, -8, 80, 26, -60, 7, 87, -72, 60, 98, -29, -56, -84, 82, 100, 16, -48, -39, 19, 12, 18, 41, 81, -71, -49, -42, 115, -115, -127, 84, -64, -19, 78, 68, -89, 42, -123, 37, -26, -54, 124, -117, 86, -128};
    private static final byte[] S1 = new byte[]{-50, -69, -21, -110, -22, -53, 19, -63, -23, 58, -42, -78, -46, -112, 23, -8, 66, 21, 86, -76, 101, 28, -120, 67, -59, 92, 54, -70, -11, 87, 103, -115, 49, -10, 100, 88, -98, -12, 34, -86, 117, 15, 2, -79, -33, 109, 115, 77, 124, 38, 46, -9, 8, 93, 68, 62, -97, 20, -56, -82, 84, 16, -40, -68, 26, 107, 105, -13, -67, 51, -85, -6, -47, -101, 104, 78, 22, -107, -111, -18, 76, 99, -114, 91, -52, 60, 25, -95, -127, 73, 123, -39, 111, 55, 96, -54, -25, 43, 72, -3, -106, 69, -4, 65, 18, 13, 121, -27, -119, -116, -29, 32, 48, -36, -73, 108, 74, -75, 63, -105, -44, 98, 45, 6, -92, -91, -125, 95, 42, -38, -55, 0, 126, -94, 85, -65, 17, -43, -100, -49, 14, 10, 61, 81, 125, -109, 27, -2, -60, 71, 9, -122, 11, -113, -99, 106, 7, -71, -80, -104, 24, 50, 113, 75, -17, 59, 112, -96, -28, 64, -1, -61, -87, -26, 120, -7, -117, 70, -128, 30, 56, -31, -72, -88, -32, 12, 35, 118, 29, 37, 36, 5, -15, 110, -108, 40, -102, -124, -24, -93, 79, 119, -45, -123, -30, 82, -14, -126, 80, 122, 47, 116, 83, -77, 97, -81, 57, 53, -34, -51, 31, -103, -84, -83, 114, 44, -35, -48, -121, -66, 94, -90, -20, 4, -58, 3, 52, -5, -37, 89, -74, -62, 1, -16, 90, -19, -89, 102, 33, 127, -118, 39, -57, -64, 41, -41};
    private static final byte[] S2 = new byte[]{-109, -39, -102, -75, -104, 34, 69, -4, -70, 106, -33, 2, -97, -36, 81, 89, 74, 23, 43, -62, -108, -12, -69, -93, 98, -28, 113, -44, -51, 112, 22, -31, 73, 60, -64, -40, 92, -101, -83, -123, 83, -95, 122, -56, 45, -32, -47, 114, -90, 44, -60, -29, 118, 120, -73, -76, 9, 59, 14, 65, 76, -34, -78, -112, 37, -91, -41, 3, 17, 0, -61, 46, -110, -17, 78, 18, -99, 125, -53, 53, 16, -43, 79, -98, 77, -87, 85, -58, -48, 123, 24, -105, -45, 54, -26, 72, 86, -127, -113, 119, -52, -100, -71, -30, -84, -72, 47, 21, -92, 124, -38, 56, 30, 11, 5, -42, 20, 110, 108, 126, 102, -3, -79, -27, 96, -81, 94, 51, -121, -55, -16, 93, 109, 63, -120, -115, -57, -9, 29, -23, -20, -19, -128, 41, 39, -49, -103, -88, 80, 15, 55, 36, 40, 48, -107, -46, 62, 91, 64, -125, -77, 105, 87, 31, 7, 28, -118, -68, 32, -21, -50, -114, -85, -18, 49, -94, 115, -7, -54, 58, 26, -5, 13, -63, -2, -6, -14, 111, -67, -106, -35, 67, 82, -74, 8, -13, -82, -66, 25, -119, 50, 38, -80, -22, 75, 100, -124, -126, 107, -11, 121, -65, 1, 95, 117, 99, 27, 35, 61, 104, 42, 101, -24, -111, -10, -1, 19, 88, -15, 71, 10, 127, -59, -89, -25, 97, 90, 6, 70, 68, 66, 4, -96, -37, 57, -122, 84, -86, -116, 52, 33, -117, -8, 12, 116, 103};
    private static final byte[] S3 = new byte[]{104, -115, -54, 77, 115, 75, 78, 42, -44, 82, 38, -77, 84, 30, 25, 31, 34, 3, 70, 61, 45, 74, 83, -125, 19, -118, -73, -43, 37, 121, -11, -67, 88, 47, 13, 2, -19, 81, -98, 17, -14, 62, 85, 94, -47, 22, 60, 102, 112, 93, -13, 69, 64, -52, -24, -108, 86, 8, -50, 26, 58, -46, -31, -33, -75, 56, 110, 14, -27, -12, -7, -122, -23, 79, -42, -123, 35, -49, 50, -103, 49, 20, -82, -18, -56, 72, -45, 48, -95, -110, 65, -79, 24, -60, 44, 113, 114, 68, 21, -3, 55, -66, 95, -86, -101, -120, -40, -85, -119, -100, -6, 96, -22, -68, 98, 12, 36, -90, -88, -20, 103, 32, -37, 124, 40, -35, -84, 91, 52, 126, 16, -15, 123, -113, 99, -96, 5, -102, 67, 119, 33, -65, 39, 9, -61, -97, -74, -41, 41, -62, -21, -64, -92, -117, -116, 29, -5, -1, -63, -78, -105, 46, -8, 101, -10, 117, 7, 4, 73, 51, -28, -39, -71, -48, 66, -57, 108, -112, 0, -114, 111, 80, 1, -59, -38, 71, 63, -51, 105, -94, -30, 122, -89, -58, -109, 15, 10, 6, -26, 43, -106, -93, 28, -81, 106, 18, -124, 57, -25, -80, -126, -9, -2, -99, -121, 92, -127, 53, -34, -76, -91, -4, -128, -17, -53, -69, 107, 118, -70, 90, 125, 120, 11, -107, -29, -83, 116, -104, 59, 54, 100, 109, -36, -16, 89, -87, 76, 23, 127, -111, -72, -55, 87, 27, -32, 97};

    public DSTU7564Digest(DSTU7564Digest dSTU7564Digest) {
        this.copyIn(dSTU7564Digest);
    }

    private void copyIn(DSTU7564Digest dSTU7564Digest) {
        this.hashSize = dSTU7564Digest.hashSize;
        this.blockSize = dSTU7564Digest.blockSize;
        this.rounds = dSTU7564Digest.rounds;
        if (this.columns > 0 && this.columns == dSTU7564Digest.columns) {
            System.arraycopy(dSTU7564Digest.state, 0, this.state, 0, this.columns);
            System.arraycopy(dSTU7564Digest.buf, 0, this.buf, 0, this.blockSize);
        } else {
            this.columns = dSTU7564Digest.columns;
            this.state = Arrays.clone(dSTU7564Digest.state);
            this.tempState1 = new long[this.columns];
            this.tempState2 = new long[this.columns];
            this.buf = Arrays.clone(dSTU7564Digest.buf);
        }
        this.inputBlocks = dSTU7564Digest.inputBlocks;
        this.bufOff = dSTU7564Digest.bufOff;
    }

    public DSTU7564Digest(int n) {
        if (n != 256 && n != 384 && n != 512) {
            throw new IllegalArgumentException("Hash size is not recommended. Use 256/384/512 instead");
        }
        this.hashSize = n >>> 3;
        if (n > 256) {
            this.columns = 16;
            this.rounds = 14;
        } else {
            this.columns = 8;
            this.rounds = 10;
        }
        this.blockSize = this.columns << 3;
        this.state = new long[this.columns];
        this.state[0] = this.blockSize;
        this.tempState1 = new long[this.columns];
        this.tempState2 = new long[this.columns];
        this.buf = new byte[this.blockSize];
    }

    public String getAlgorithmName() {
        return "DSTU7564";
    }

    public int getDigestSize() {
        return this.hashSize;
    }

    public int getByteLength() {
        return this.blockSize;
    }

    public void update(byte by) {
        this.buf[this.bufOff++] = by;
        if (this.bufOff == this.blockSize) {
            this.processBlock(this.buf, 0);
            this.bufOff = 0;
            ++this.inputBlocks;
        }
    }

    public void update(byte[] byArray, int n, int n2) {
        while (this.bufOff != 0 && n2 > 0) {
            this.update(byArray[n++]);
            --n2;
        }
        if (n2 > 0) {
            while (n2 >= this.blockSize) {
                this.processBlock(byArray, n);
                n += this.blockSize;
                n2 -= this.blockSize;
                ++this.inputBlocks;
            }
            while (n2 > 0) {
                this.update(byArray[n++]);
                --n2;
            }
        }
    }

    public int doFinal(byte[] byArray, int n) {
        int n2 = this.bufOff;
        this.buf[this.bufOff++] = -128;
        int n3 = this.blockSize - 12;
        if (this.bufOff > n3) {
            while (this.bufOff < this.blockSize) {
                this.buf[this.bufOff++] = 0;
            }
            this.bufOff = 0;
            this.processBlock(this.buf, 0);
        }
        while (this.bufOff < n3) {
            this.buf[this.bufOff++] = 0;
        }
        long l = (this.inputBlocks & 0xFFFFFFFFL) * (long)this.blockSize + (long)n2 << 3;
        Pack.intToLittleEndian((int)l, this.buf, this.bufOff);
        this.bufOff += 4;
        l >>>= 32;
        Pack.longToLittleEndian(l += (this.inputBlocks >>> 32) * (long)this.blockSize << 3, this.buf, this.bufOff);
        this.processBlock(this.buf, 0);
        System.arraycopy(this.state, 0, this.tempState1, 0, this.columns);
        this.P(this.tempState1);
        for (n2 = 0; n2 < this.columns; ++n2) {
            int n4 = n2;
            this.state[n4] = this.state[n4] ^ this.tempState1[n2];
        }
        n2 = this.hashSize >>> 3;
        for (n3 = this.columns - n2; n3 < this.columns; ++n3) {
            Pack.longToLittleEndian(this.state[n3], byArray, n);
            n += 8;
        }
        this.reset();
        return this.hashSize;
    }

    public void reset() {
        Arrays.fill(this.state, 0L);
        this.state[0] = this.blockSize;
        this.inputBlocks = 0L;
        this.bufOff = 0;
    }

    private void processBlock(byte[] byArray, int n) {
        int n2;
        int n3 = n;
        for (n2 = 0; n2 < this.columns; ++n2) {
            long l = Pack.littleEndianToLong(byArray, n3);
            n3 += 8;
            this.tempState1[n2] = this.state[n2] ^ l;
            this.tempState2[n2] = l;
        }
        this.P(this.tempState1);
        this.Q(this.tempState2);
        for (n2 = 0; n2 < this.columns; ++n2) {
            int n4 = n2;
            this.state[n4] = this.state[n4] ^ (this.tempState1[n2] ^ this.tempState2[n2]);
        }
    }

    private void P(long[] lArray) {
        for (int i = 0; i < this.rounds; ++i) {
            long l = i;
            int n = 0;
            while (n < this.columns) {
                int n2 = n++;
                lArray[n2] = lArray[n2] ^ l;
                l += 16L;
            }
            this.shiftRows(lArray);
            this.subBytes(lArray);
            this.mixColumns(lArray);
        }
    }

    private void Q(long[] lArray) {
        for (int i = 0; i < this.rounds; ++i) {
            long l = (long)(this.columns - 1 << 4 ^ i) << 56 | 0xF0F0F0F0F0F0F3L;
            int n = 0;
            while (n < this.columns) {
                int n2 = n++;
                lArray[n2] = lArray[n2] + l;
                l -= 0x1000000000000000L;
            }
            this.shiftRows(lArray);
            this.subBytes(lArray);
            this.mixColumns(lArray);
        }
    }

    private static long mixColumn(long l) {
        long l2 = (l & 0x7F7F7F7F7F7F7F7FL) << 1 ^ ((l & 0x8080808080808080L) >>> 7) * 29L;
        long l3 = DSTU7564Digest.rotate(8, l) ^ l;
        l3 ^= DSTU7564Digest.rotate(16, l3);
        long l4 = (l3 ^= DSTU7564Digest.rotate(48, l)) ^ l ^ l2;
        l4 = (l4 & 0x3F3F3F3F3F3F3F3FL) << 2 ^ ((l4 & 0x8080808080808080L) >>> 6) * 29L ^ ((l4 & 0x4040404040404040L) >>> 6) * 29L;
        return l3 ^ DSTU7564Digest.rotate(32, l4) ^ DSTU7564Digest.rotate(40, l2) ^ DSTU7564Digest.rotate(48, l2);
    }

    private void mixColumns(long[] lArray) {
        for (int i = 0; i < this.columns; ++i) {
            lArray[i] = DSTU7564Digest.mixColumn(lArray[i]);
        }
    }

    private static long rotate(int n, long l) {
        return l >>> n | l << -n;
    }

    private void shiftRows(long[] lArray) {
        switch (this.columns) {
            case 8: {
                long l = lArray[0];
                long l2 = lArray[1];
                long l3 = lArray[2];
                long l4 = lArray[3];
                long l5 = lArray[4];
                long l6 = lArray[5];
                long l7 = lArray[6];
                long l8 = lArray[7];
                long l9 = (l ^ l5) & 0xFFFFFFFF00000000L;
                l ^= l9;
                l5 ^= l9;
                l9 = (l2 ^ l6) & 0xFFFFFFFF000000L;
                l2 ^= l9;
                l6 ^= l9;
                l9 = (l3 ^ l7) & 0xFFFFFFFF0000L;
                l3 ^= l9;
                l7 ^= l9;
                l9 = (l4 ^ l8) & 0xFFFFFFFF00L;
                l4 ^= l9;
                l8 ^= l9;
                l9 = (l ^ l3) & 0xFFFF0000FFFF0000L;
                l ^= l9;
                l3 ^= l9;
                l9 = (l2 ^ l4) & 0xFFFF0000FFFF00L;
                l2 ^= l9;
                l4 ^= l9;
                l9 = (l5 ^ l7) & 0xFFFF0000FFFF0000L;
                l5 ^= l9;
                l7 ^= l9;
                l9 = (l6 ^ l8) & 0xFFFF0000FFFF00L;
                l6 ^= l9;
                l8 ^= l9;
                l9 = (l ^ l2) & 0xFF00FF00FF00FF00L;
                l ^= l9;
                l2 ^= l9;
                l9 = (l3 ^ l4) & 0xFF00FF00FF00FF00L;
                l3 ^= l9;
                l4 ^= l9;
                l9 = (l5 ^ l6) & 0xFF00FF00FF00FF00L;
                l5 ^= l9;
                l6 ^= l9;
                l9 = (l7 ^ l8) & 0xFF00FF00FF00FF00L;
                l7 ^= l9;
                l8 ^= l9;
                lArray[0] = l;
                lArray[1] = l2;
                lArray[2] = l3;
                lArray[3] = l4;
                lArray[4] = l5;
                lArray[5] = l6;
                lArray[6] = l7;
                lArray[7] = l8;
                break;
            }
            case 16: {
                long l = lArray[0];
                long l10 = lArray[1];
                long l11 = lArray[2];
                long l12 = lArray[3];
                long l13 = lArray[4];
                long l14 = lArray[5];
                long l15 = lArray[6];
                long l16 = lArray[7];
                long l17 = lArray[8];
                long l18 = lArray[9];
                long l19 = lArray[10];
                long l20 = lArray[11];
                long l21 = lArray[12];
                long l22 = lArray[13];
                long l23 = lArray[14];
                long l24 = lArray[15];
                long l25 = (l ^ l17) & 0xFF00000000000000L;
                l ^= l25;
                l17 ^= l25;
                l25 = (l10 ^ l18) & 0xFF00000000000000L;
                l10 ^= l25;
                l18 ^= l25;
                l25 = (l11 ^ l19) & 0xFFFF000000000000L;
                l11 ^= l25;
                l19 ^= l25;
                l25 = (l12 ^ l20) & 0xFFFFFF0000000000L;
                l12 ^= l25;
                l20 ^= l25;
                l25 = (l13 ^ l21) & 0xFFFFFFFF00000000L;
                l13 ^= l25;
                l21 ^= l25;
                l25 = (l14 ^ l22) & 0xFFFFFFFF000000L;
                l14 ^= l25;
                l22 ^= l25;
                l25 = (l15 ^ l23) & 0xFFFFFFFFFF0000L;
                l15 ^= l25;
                l23 ^= l25;
                l25 = (l16 ^ l24) & 0xFFFFFFFFFFFF00L;
                l16 ^= l25;
                l24 ^= l25;
                l25 = (l ^ l13) & 0xFFFFFF00000000L;
                l ^= l25;
                l13 ^= l25;
                l25 = (l10 ^ l14) & 0xFFFFFFFFFF000000L;
                l10 ^= l25;
                l14 ^= l25;
                l25 = (l11 ^ l15) & 0xFF00FFFFFFFF0000L;
                l11 ^= l25;
                l15 ^= l25;
                l25 = (l12 ^ l16) & 0xFF0000FFFFFFFF00L;
                l12 ^= l25;
                l16 ^= l25;
                l25 = (l17 ^ l21) & 0xFFFFFF00000000L;
                l17 ^= l25;
                l21 ^= l25;
                l25 = (l18 ^ l22) & 0xFFFFFFFFFF000000L;
                l18 ^= l25;
                l22 ^= l25;
                l25 = (l19 ^ l23) & 0xFF00FFFFFFFF0000L;
                l19 ^= l25;
                l23 ^= l25;
                l25 = (l20 ^ l24) & 0xFF0000FFFFFFFF00L;
                l20 ^= l25;
                l24 ^= l25;
                l25 = (l ^ l11) & 0xFFFF0000FFFF0000L;
                l ^= l25;
                l11 ^= l25;
                l25 = (l10 ^ l12) & 0xFFFF0000FFFF00L;
                l10 ^= l25;
                l12 ^= l25;
                l25 = (l13 ^ l15) & 0xFFFF0000FFFF0000L;
                l13 ^= l25;
                l15 ^= l25;
                l25 = (l14 ^ l16) & 0xFFFF0000FFFF00L;
                l14 ^= l25;
                l16 ^= l25;
                l25 = (l17 ^ l19) & 0xFFFF0000FFFF0000L;
                l17 ^= l25;
                l19 ^= l25;
                l25 = (l18 ^ l20) & 0xFFFF0000FFFF00L;
                l18 ^= l25;
                l20 ^= l25;
                l25 = (l21 ^ l23) & 0xFFFF0000FFFF0000L;
                l21 ^= l25;
                l23 ^= l25;
                l25 = (l22 ^ l24) & 0xFFFF0000FFFF00L;
                l22 ^= l25;
                l24 ^= l25;
                l25 = (l ^ l10) & 0xFF00FF00FF00FF00L;
                l ^= l25;
                l10 ^= l25;
                l25 = (l11 ^ l12) & 0xFF00FF00FF00FF00L;
                l11 ^= l25;
                l12 ^= l25;
                l25 = (l13 ^ l14) & 0xFF00FF00FF00FF00L;
                l13 ^= l25;
                l14 ^= l25;
                l25 = (l15 ^ l16) & 0xFF00FF00FF00FF00L;
                l15 ^= l25;
                l16 ^= l25;
                l25 = (l17 ^ l18) & 0xFF00FF00FF00FF00L;
                l17 ^= l25;
                l18 ^= l25;
                l25 = (l19 ^ l20) & 0xFF00FF00FF00FF00L;
                l19 ^= l25;
                l20 ^= l25;
                l25 = (l21 ^ l22) & 0xFF00FF00FF00FF00L;
                l21 ^= l25;
                l22 ^= l25;
                l25 = (l23 ^ l24) & 0xFF00FF00FF00FF00L;
                l23 ^= l25;
                l24 ^= l25;
                lArray[0] = l;
                lArray[1] = l10;
                lArray[2] = l11;
                lArray[3] = l12;
                lArray[4] = l13;
                lArray[5] = l14;
                lArray[6] = l15;
                lArray[7] = l16;
                lArray[8] = l17;
                lArray[9] = l18;
                lArray[10] = l19;
                lArray[11] = l20;
                lArray[12] = l21;
                lArray[13] = l22;
                lArray[14] = l23;
                lArray[15] = l24;
                break;
            }
            default: {
                throw new IllegalStateException("unsupported state size: only 512/1024 are allowed");
            }
        }
    }

    private void subBytes(long[] lArray) {
        for (int i = 0; i < this.columns; ++i) {
            long l = lArray[i];
            int n = (int)l;
            int n2 = (int)(l >>> 32);
            byte by = S0[n & 0xFF];
            byte by2 = S1[n >>> 8 & 0xFF];
            byte by3 = S2[n >>> 16 & 0xFF];
            byte by4 = S3[n >>> 24];
            n = by & 0xFF | (by2 & 0xFF) << 8 | (by3 & 0xFF) << 16 | by4 << 24;
            byte by5 = S0[n2 & 0xFF];
            byte by6 = S1[n2 >>> 8 & 0xFF];
            byte by7 = S2[n2 >>> 16 & 0xFF];
            byte by8 = S3[n2 >>> 24];
            n2 = by5 & 0xFF | (by6 & 0xFF) << 8 | (by7 & 0xFF) << 16 | by8 << 24;
            lArray[i] = (long)n & 0xFFFFFFFFL | (long)n2 << 32;
        }
    }

    public Memoable copy() {
        return new DSTU7564Digest(this);
    }

    public void reset(Memoable memoable) {
        DSTU7564Digest dSTU7564Digest = (DSTU7564Digest)memoable;
        this.copyIn(dSTU7564Digest);
    }
}


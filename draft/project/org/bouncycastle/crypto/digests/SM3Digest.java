/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class SM3Digest
extends GeneralDigest {
    private static final int DIGEST_LENGTH = 32;
    private static final int BLOCK_SIZE = 16;
    private int[] V = new int[8];
    private int[] inwords = new int[16];
    private int xOff;
    private int[] W = new int[68];
    private static final int[] T;

    public SM3Digest() {
        this.reset();
    }

    public SM3Digest(SM3Digest sM3Digest) {
        super(sM3Digest);
        this.copyIn(sM3Digest);
    }

    private void copyIn(SM3Digest sM3Digest) {
        System.arraycopy(sM3Digest.V, 0, this.V, 0, this.V.length);
        System.arraycopy(sM3Digest.inwords, 0, this.inwords, 0, this.inwords.length);
        this.xOff = sM3Digest.xOff;
    }

    public String getAlgorithmName() {
        return "SM3";
    }

    public int getDigestSize() {
        return 32;
    }

    public Memoable copy() {
        return new SM3Digest(this);
    }

    public void reset(Memoable memoable) {
        SM3Digest sM3Digest = (SM3Digest)memoable;
        super.copyIn(sM3Digest);
        this.copyIn(sM3Digest);
    }

    public void reset() {
        super.reset();
        this.V[0] = 1937774191;
        this.V[1] = 1226093241;
        this.V[2] = 388252375;
        this.V[3] = -628488704;
        this.V[4] = -1452330820;
        this.V[5] = 372324522;
        this.V[6] = -477237683;
        this.V[7] = -1325724082;
        this.xOff = 0;
    }

    public int doFinal(byte[] byArray, int n) {
        this.finish();
        Pack.intToBigEndian(this.V, byArray, n);
        this.reset();
        return 32;
    }

    protected void processWord(byte[] byArray, int n) {
        int n2;
        this.inwords[this.xOff] = n2 = (byArray[n] & 0xFF) << 24 | (byArray[++n] & 0xFF) << 16 | (byArray[++n] & 0xFF) << 8 | byArray[++n] & 0xFF;
        ++this.xOff;
        if (this.xOff >= 16) {
            this.processBlock();
        }
    }

    protected void processLength(long l) {
        if (this.xOff > 14) {
            this.inwords[this.xOff] = 0;
            ++this.xOff;
            this.processBlock();
        }
        while (this.xOff < 14) {
            this.inwords[this.xOff] = 0;
            ++this.xOff;
        }
        this.inwords[this.xOff++] = (int)(l >>> 32);
        this.inwords[this.xOff++] = (int)l;
    }

    private int P0(int n) {
        int n2 = n << 9 | n >>> 23;
        int n3 = n << 17 | n >>> 15;
        return n ^ n2 ^ n3;
    }

    private int P1(int n) {
        int n2 = n << 15 | n >>> 17;
        int n3 = n << 23 | n >>> 9;
        return n ^ n2 ^ n3;
    }

    private int FF0(int n, int n2, int n3) {
        return n ^ n2 ^ n3;
    }

    private int FF1(int n, int n2, int n3) {
        return n & n2 | n & n3 | n2 & n3;
    }

    private int GG0(int n, int n2, int n3) {
        return n ^ n2 ^ n3;
    }

    private int GG1(int n, int n2, int n3) {
        return n & n2 | ~n & n3;
    }

    protected void processBlock() {
        int n;
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10;
        int n11;
        int n12;
        int n13;
        int n14;
        for (n14 = 0; n14 < 16; ++n14) {
            this.W[n14] = this.inwords[n14];
        }
        for (n14 = 16; n14 < 68; ++n14) {
            n13 = this.W[n14 - 3];
            n12 = n13 << 15 | n13 >>> 17;
            n11 = this.W[n14 - 13];
            n10 = n11 << 7 | n11 >>> 25;
            this.W[n14] = this.P1(this.W[n14 - 16] ^ this.W[n14 - 9] ^ n12) ^ n10 ^ this.W[n14 - 6];
        }
        n14 = this.V[0];
        n13 = this.V[1];
        n12 = this.V[2];
        n11 = this.V[3];
        n10 = this.V[4];
        int n15 = this.V[5];
        int n16 = this.V[6];
        int n17 = this.V[7];
        for (n9 = 0; n9 < 16; ++n9) {
            n8 = n14 << 12 | n14 >>> 20;
            n7 = n8 + n10 + T[n9];
            n6 = n7 << 7 | n7 >>> 25;
            n5 = n6 ^ n8;
            n4 = this.W[n9];
            n3 = n4 ^ this.W[n9 + 4];
            n2 = this.FF0(n14, n13, n12) + n11 + n5 + n3;
            n = this.GG0(n10, n15, n16) + n17 + n6 + n4;
            n11 = n12;
            n12 = n13 << 9 | n13 >>> 23;
            n13 = n14;
            n14 = n2;
            n17 = n16;
            n16 = n15 << 19 | n15 >>> 13;
            n15 = n10;
            n10 = this.P0(n);
        }
        for (n9 = 16; n9 < 64; ++n9) {
            n8 = n14 << 12 | n14 >>> 20;
            n7 = n8 + n10 + T[n9];
            n6 = n7 << 7 | n7 >>> 25;
            n5 = n6 ^ n8;
            n4 = this.W[n9];
            n3 = n4 ^ this.W[n9 + 4];
            n2 = this.FF1(n14, n13, n12) + n11 + n5 + n3;
            n = this.GG1(n10, n15, n16) + n17 + n6 + n4;
            n11 = n12;
            n12 = n13 << 9 | n13 >>> 23;
            n13 = n14;
            n14 = n2;
            n17 = n16;
            n16 = n15 << 19 | n15 >>> 13;
            n15 = n10;
            n10 = this.P0(n);
        }
        this.V[0] = this.V[0] ^ n14;
        this.V[1] = this.V[1] ^ n13;
        this.V[2] = this.V[2] ^ n12;
        this.V[3] = this.V[3] ^ n11;
        this.V[4] = this.V[4] ^ n10;
        this.V[5] = this.V[5] ^ n15;
        this.V[6] = this.V[6] ^ n16;
        this.V[7] = this.V[7] ^ n17;
        this.xOff = 0;
    }

    static {
        int n;
        int n2;
        T = new int[64];
        for (n2 = 0; n2 < 16; ++n2) {
            n = 2043430169;
            SM3Digest.T[n2] = n << n2 | n >>> 32 - n2;
        }
        for (n2 = 16; n2 < 64; ++n2) {
            n = n2 % 32;
            int n3 = 2055708042;
            SM3Digest.T[n2] = n3 << n | n3 >>> 32 - n;
        }
    }
}


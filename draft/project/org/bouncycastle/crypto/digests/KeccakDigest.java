/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;
import org.bouncycastle.util.encoders.Hex;

public class KeccakDigest
implements ExtendedDigest {
    private static long[] KeccakRoundConstants = new long[]{1L, 32898L, -9223372036854742902L, -9223372034707259392L, 32907L, 0x80000001L, -9223372034707259263L, -9223372036854743031L, 138L, 136L, 0x80008009L, 0x8000000AL, 0x8000808BL, -9223372036854775669L, -9223372036854742903L, -9223372036854743037L, -9223372036854743038L, -9223372036854775680L, 32778L, -9223372034707292150L, -9223372034707259263L, -9223372036854742912L, 0x80000001L, -9223372034707259384L};
    protected long[] state = new long[25];
    protected byte[] dataQueue = new byte[192];
    protected int rate;
    protected int bitsInQueue;
    protected int fixedOutputLength;
    protected boolean squeezing;

    public KeccakDigest() {
        this(288);
    }

    public KeccakDigest(int n) {
        this.init(n);
    }

    public KeccakDigest(KeccakDigest keccakDigest) {
        System.arraycopy(keccakDigest.state, 0, this.state, 0, keccakDigest.state.length);
        System.arraycopy(keccakDigest.dataQueue, 0, this.dataQueue, 0, keccakDigest.dataQueue.length);
        this.rate = keccakDigest.rate;
        this.bitsInQueue = keccakDigest.bitsInQueue;
        this.fixedOutputLength = keccakDigest.fixedOutputLength;
        this.squeezing = keccakDigest.squeezing;
    }

    public String getAlgorithmName() {
        return "Keccak-" + this.fixedOutputLength;
    }

    public int getDigestSize() {
        return this.fixedOutputLength / 8;
    }

    public void update(byte by) {
        this.absorb(by);
    }

    public void update(byte[] byArray, int n, int n2) {
        this.absorb(byArray, n, n2);
    }

    public int doFinal(byte[] byArray, int n) {
        this.squeeze(byArray, n, this.fixedOutputLength);
        this.reset();
        return this.getDigestSize();
    }

    protected int doFinal(byte[] byArray, int n, byte by, int n2) {
        if (n2 > 0) {
            this.absorbBits(by, n2);
        }
        this.squeeze(byArray, n, this.fixedOutputLength);
        this.reset();
        return this.getDigestSize();
    }

    public void reset() {
        this.init(this.fixedOutputLength);
    }

    public int getByteLength() {
        return this.rate / 8;
    }

    private void init(int n) {
        switch (n) {
            case 128: 
            case 224: 
            case 256: 
            case 288: 
            case 384: 
            case 512: {
                this.initSponge(1600 - (n << 1));
                break;
            }
            default: {
                throw new IllegalArgumentException("bitLength must be one of 128, 224, 256, 288, 384, or 512.");
            }
        }
    }

    private void initSponge(int n) {
        if (n <= 0 || n >= 1600 || n % 64 != 0) {
            throw new IllegalStateException("invalid rate value");
        }
        this.rate = n;
        for (int i = 0; i < this.state.length; ++i) {
            this.state[i] = 0L;
        }
        Arrays.fill(this.dataQueue, (byte)0);
        this.bitsInQueue = 0;
        this.squeezing = false;
        this.fixedOutputLength = (1600 - n) / 2;
    }

    protected void absorb(byte by) {
        if (this.bitsInQueue % 8 != 0) {
            throw new IllegalStateException("attempt to absorb with odd length queue");
        }
        if (this.squeezing) {
            throw new IllegalStateException("attempt to absorb while squeezing");
        }
        this.dataQueue[this.bitsInQueue >>> 3] = by;
        if ((this.bitsInQueue += 8) == this.rate) {
            this.KeccakAbsorb(this.dataQueue, 0);
            this.bitsInQueue = 0;
        }
    }

    protected void absorb(byte[] byArray, int n, int n2) {
        int n3;
        if (this.bitsInQueue % 8 != 0) {
            throw new IllegalStateException("attempt to absorb with odd length queue");
        }
        if (this.squeezing) {
            throw new IllegalStateException("attempt to absorb while squeezing");
        }
        int n4 = this.rate >>> 3;
        int n5 = this.bitsInQueue >>> 3;
        int n6 = n4 - n5;
        if (n2 < n6) {
            System.arraycopy(byArray, n, this.dataQueue, n5, n2);
            this.bitsInQueue += n2 << 3;
            return;
        }
        int n7 = 0;
        if (n5 > 0) {
            System.arraycopy(byArray, n, this.dataQueue, n5, n6);
            n7 += n6;
            this.KeccakAbsorb(this.dataQueue, 0);
        }
        while ((n3 = n2 - n7) >= n4) {
            this.KeccakAbsorb(byArray, n + n7);
            n7 += n4;
        }
        System.arraycopy(byArray, n + n7, this.dataQueue, 0, n3);
        this.bitsInQueue = n3 << 3;
    }

    protected void absorbBits(int n, int n2) {
        if (n2 < 1 || n2 > 7) {
            throw new IllegalArgumentException("'bits' must be in the range 1 to 7");
        }
        if (this.bitsInQueue % 8 != 0) {
            throw new IllegalStateException("attempt to absorb with odd length queue");
        }
        if (this.squeezing) {
            throw new IllegalStateException("attempt to absorb while squeezing");
        }
        int n3 = (1 << n2) - 1;
        this.dataQueue[this.bitsInQueue >>> 3] = (byte)(n & n3);
        this.bitsInQueue += n2;
    }

    protected byte[] dumpState() {
        byte[] byArray = new byte[this.state.length * 8];
        int n = 0;
        for (int i = 0; i != this.state.length; ++i) {
            Pack.longToLittleEndian(this.state[i], byArray, n);
            n += 8;
        }
        return byArray;
    }

    private void padAndSwitchToSqueezingPhase() {
        int n = this.bitsInQueue >>> 3;
        this.dataQueue[n] = (byte)(this.dataQueue[n] | (byte)(1 << (this.bitsInQueue & 7)));
        if (++this.bitsInQueue == this.rate) {
            this.KeccakAbsorb(this.dataQueue, 0);
        } else {
            int n2 = this.bitsInQueue >>> 6;
            int n3 = this.bitsInQueue & 0x3F;
            int n4 = 0;
            int n5 = 0;
            while (n5 < n2) {
                int n6 = n5++;
                this.state[n6] = this.state[n6] ^ Pack.littleEndianToLong(this.dataQueue, n4);
                n4 += 8;
            }
            byte[] byArray = this.dumpState();
            if (n3 > 0) {
                long l = (1L << n3) - 1L;
                int n7 = n2;
                this.state[n7] = this.state[n7] ^ Pack.littleEndianToLong(this.dataQueue, n4) & l;
            }
        }
        int n8 = this.rate - 1 >>> 6;
        this.state[n8] = this.state[n8] ^ Long.MIN_VALUE;
        this.bitsInQueue = 0;
        this.squeezing = true;
    }

    protected void squeeze(byte[] byArray, int n, long l) {
        int n2;
        if (!this.squeezing) {
            this.padAndSwitchToSqueezingPhase();
        }
        byte[] byArray2 = this.dumpState();
        if (l % 8L != 0L) {
            throw new IllegalStateException("outputLength not a multiple of 8");
        }
        for (long i = 0L; i < l; i += (long)n2) {
            if (this.bitsInQueue == 0) {
                this.KeccakExtract();
            }
            n2 = (int)Math.min((long)this.bitsInQueue, l - i);
            System.arraycopy(this.dataQueue, (this.rate - this.bitsInQueue) / 8, byArray, n + (int)(i / 8L), n2 / 8);
            this.bitsInQueue -= n2;
        }
        byArray2 = this.dumpState();
    }

    private void KeccakAbsorb(byte[] byArray, int n) {
        int n2 = this.rate >>> 6;
        int n3 = 0;
        while (n3 < n2) {
            int n4 = n3++;
            this.state[n4] = this.state[n4] ^ Pack.littleEndianToLong(byArray, n);
            n += 8;
        }
        String string = Hex.toHexString(this.dumpState()).toLowerCase();
        this.KeccakPermutation();
    }

    private void KeccakExtract() {
        this.KeccakPermutation();
        byte[] byArray = this.dumpState();
        Pack.longToLittleEndian(this.state, 0, this.rate >>> 6, this.dataQueue, 0);
        this.bitsInQueue = this.rate;
    }

    private void KeccakPermutation() {
        long[] lArray = this.state;
        long l = lArray[0];
        long l2 = lArray[1];
        long l3 = lArray[2];
        long l4 = lArray[3];
        long l5 = lArray[4];
        long l6 = lArray[5];
        long l7 = lArray[6];
        long l8 = lArray[7];
        long l9 = lArray[8];
        long l10 = lArray[9];
        long l11 = lArray[10];
        long l12 = lArray[11];
        long l13 = lArray[12];
        long l14 = lArray[13];
        long l15 = lArray[14];
        long l16 = lArray[15];
        long l17 = lArray[16];
        long l18 = lArray[17];
        long l19 = lArray[18];
        long l20 = lArray[19];
        long l21 = lArray[20];
        long l22 = lArray[21];
        long l23 = lArray[22];
        long l24 = lArray[23];
        long l25 = lArray[24];
        for (int i = 0; i < 24; ++i) {
            long l26 = l ^ l6 ^ l11 ^ l16 ^ l21;
            long l27 = l2 ^ l7 ^ l12 ^ l17 ^ l22;
            long l28 = l3 ^ l8 ^ l13 ^ l18 ^ l23;
            long l29 = l4 ^ l9 ^ l14 ^ l19 ^ l24;
            long l30 = l5 ^ l10 ^ l15 ^ l20 ^ l25;
            long l31 = (l27 << 1 | l27 >>> -1) ^ l30;
            long l32 = (l28 << 1 | l28 >>> -1) ^ l26;
            long l33 = (l29 << 1 | l29 >>> -1) ^ l27;
            long l34 = (l30 << 1 | l30 >>> -1) ^ l28;
            long l35 = (l26 << 1 | l26 >>> -1) ^ l29;
            l ^= l31;
            l6 ^= l31;
            l11 ^= l31;
            l16 ^= l31;
            l21 ^= l31;
            l2 ^= l32;
            l7 ^= l32;
            l12 ^= l32;
            l17 ^= l32;
            l22 ^= l32;
            l3 ^= l33;
            l8 ^= l33;
            l13 ^= l33;
            l18 ^= l33;
            l23 ^= l33;
            l4 ^= l34;
            l9 ^= l34;
            l14 ^= l34;
            l19 ^= l34;
            l24 ^= l34;
            l5 ^= l35;
            l10 ^= l35;
            l15 ^= l35;
            l20 ^= l35;
            l25 ^= l35;
            l27 = l2 << 1 | l2 >>> 63;
            l2 = l7 << 44 | l7 >>> 20;
            l7 = l10 << 20 | l10 >>> 44;
            l10 = l23 << 61 | l23 >>> 3;
            l23 = l15 << 39 | l15 >>> 25;
            l15 = l21 << 18 | l21 >>> 46;
            l21 = l3 << 62 | l3 >>> 2;
            l3 = l13 << 43 | l13 >>> 21;
            l13 = l14 << 25 | l14 >>> 39;
            l14 = l20 << 8 | l20 >>> 56;
            l20 = l24 << 56 | l24 >>> 8;
            l24 = l16 << 41 | l16 >>> 23;
            l16 = l5 << 27 | l5 >>> 37;
            l5 = l25 << 14 | l25 >>> 50;
            l25 = l22 << 2 | l22 >>> 62;
            l22 = l9 << 55 | l9 >>> 9;
            l9 = l17 << 45 | l17 >>> 19;
            l17 = l6 << 36 | l6 >>> 28;
            l6 = l4 << 28 | l4 >>> 36;
            l4 = l19 << 21 | l19 >>> 43;
            l19 = l18 << 15 | l18 >>> 49;
            l18 = l12 << 10 | l12 >>> 54;
            l12 = l8 << 6 | l8 >>> 58;
            l8 = l11 << 3 | l11 >>> 61;
            l11 = l27;
            l26 = l ^ (l2 ^ 0xFFFFFFFFFFFFFFFFL) & l3;
            l27 = l2 ^ (l3 ^ 0xFFFFFFFFFFFFFFFFL) & l4;
            l3 ^= (l4 ^ 0xFFFFFFFFFFFFFFFFL) & l5;
            l4 ^= (l5 ^ 0xFFFFFFFFFFFFFFFFL) & l;
            l5 ^= (l ^ 0xFFFFFFFFFFFFFFFFL) & l2;
            l = l26;
            l2 = l27;
            l26 = l6 ^ (l7 ^ 0xFFFFFFFFFFFFFFFFL) & l8;
            l27 = l7 ^ (l8 ^ 0xFFFFFFFFFFFFFFFFL) & l9;
            l8 ^= (l9 ^ 0xFFFFFFFFFFFFFFFFL) & l10;
            l9 ^= (l10 ^ 0xFFFFFFFFFFFFFFFFL) & l6;
            l10 ^= (l6 ^ 0xFFFFFFFFFFFFFFFFL) & l7;
            l6 = l26;
            l7 = l27;
            l26 = l11 ^ (l12 ^ 0xFFFFFFFFFFFFFFFFL) & l13;
            l27 = l12 ^ (l13 ^ 0xFFFFFFFFFFFFFFFFL) & l14;
            l13 ^= (l14 ^ 0xFFFFFFFFFFFFFFFFL) & l15;
            l14 ^= (l15 ^ 0xFFFFFFFFFFFFFFFFL) & l11;
            l15 ^= (l11 ^ 0xFFFFFFFFFFFFFFFFL) & l12;
            l11 = l26;
            l12 = l27;
            l26 = l16 ^ (l17 ^ 0xFFFFFFFFFFFFFFFFL) & l18;
            l27 = l17 ^ (l18 ^ 0xFFFFFFFFFFFFFFFFL) & l19;
            l18 ^= (l19 ^ 0xFFFFFFFFFFFFFFFFL) & l20;
            l19 ^= (l20 ^ 0xFFFFFFFFFFFFFFFFL) & l16;
            l20 ^= (l16 ^ 0xFFFFFFFFFFFFFFFFL) & l17;
            l16 = l26;
            l17 = l27;
            l26 = l21 ^ (l22 ^ 0xFFFFFFFFFFFFFFFFL) & l23;
            l27 = l22 ^ (l23 ^ 0xFFFFFFFFFFFFFFFFL) & l24;
            l23 ^= (l24 ^ 0xFFFFFFFFFFFFFFFFL) & l25;
            l24 ^= (l25 ^ 0xFFFFFFFFFFFFFFFFL) & l21;
            l25 ^= (l21 ^ 0xFFFFFFFFFFFFFFFFL) & l22;
            l21 = l26;
            l22 = l27;
            l ^= KeccakRoundConstants[i];
        }
        lArray[0] = l;
        lArray[1] = l2;
        lArray[2] = l3;
        lArray[3] = l4;
        lArray[4] = l5;
        lArray[5] = l6;
        lArray[6] = l7;
        lArray[7] = l8;
        lArray[8] = l9;
        lArray[9] = l10;
        lArray[10] = l11;
        lArray[11] = l12;
        lArray[12] = l13;
        lArray[13] = l14;
        lArray[14] = l15;
        lArray[15] = l16;
        lArray[16] = l17;
        lArray[17] = l18;
        lArray[18] = l19;
        lArray[19] = l20;
        lArray[20] = l21;
        lArray[21] = l22;
        lArray[22] = l23;
        lArray[23] = l24;
        lArray[24] = l25;
    }
}


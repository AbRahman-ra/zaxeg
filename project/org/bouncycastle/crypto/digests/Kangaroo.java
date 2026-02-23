/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.Xof;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public final class Kangaroo {
    private static final int DIGESTLEN = 32;

    static abstract class KangarooBase
    implements ExtendedDigest,
    Xof {
        private static final int BLKSIZE = 8192;
        private static final byte[] SINGLE = new byte[]{7};
        private static final byte[] INTERMEDIATE = new byte[]{11};
        private static final byte[] FINAL = new byte[]{-1, -1, 6};
        private static final byte[] FIRST = new byte[]{3, 0, 0, 0, 0, 0, 0, 0};
        private final byte[] singleByte = new byte[1];
        private final KangarooSponge theTree;
        private final KangarooSponge theLeaf;
        private final int theChainLen;
        private byte[] thePersonal;
        private long theXofLen;
        private long theXofRemaining;
        private boolean squeezing;
        private int theCurrNode;
        private int theProcessed;

        KangarooBase(int n, int n2, int n3) {
            this.theTree = new KangarooSponge(n, n2);
            this.theLeaf = new KangarooSponge(n, n2);
            this.theChainLen = n >> 2;
            this.theXofLen = n3;
            this.theXofRemaining = -1L;
            this.buildPersonal(null);
        }

        private void buildPersonal(byte[] byArray) {
            int n = byArray == null ? 0 : byArray.length;
            byte[] byArray2 = KangarooBase.lengthEncode(n);
            this.thePersonal = byArray == null ? new byte[n + byArray2.length] : Arrays.copyOf(byArray, n + byArray2.length);
            System.arraycopy(byArray2, 0, this.thePersonal, n, byArray2.length);
        }

        public int getByteLength() {
            return this.theTree.theRateBytes;
        }

        public int getDigestSize() {
            return this.theXofLen == 0L ? this.theChainLen >> 1 : (int)this.theXofLen;
        }

        public void init(KangarooParameters kangarooParameters) {
            this.buildPersonal(kangarooParameters.getPersonalisation());
            long l = kangarooParameters.getMaxOutputLength();
            if (l < -1L) {
                throw new IllegalArgumentException("Invalid output length");
            }
            this.theXofLen = l;
            this.reset();
        }

        public void update(byte by) {
            this.singleByte[0] = by;
            this.update(this.singleByte, 0, 1);
        }

        public void update(byte[] byArray, int n, int n2) {
            this.processData(byArray, n, n2);
        }

        public int doFinal(byte[] byArray, int n) {
            if (this.getDigestSize() == -1) {
                throw new IllegalStateException("No defined output length");
            }
            return this.doFinal(byArray, n, this.getDigestSize());
        }

        public int doFinal(byte[] byArray, int n, int n2) {
            if (this.squeezing) {
                throw new IllegalStateException("Already outputting");
            }
            int n3 = this.doOutput(byArray, n, n2);
            this.reset();
            return n3;
        }

        public int doOutput(byte[] byArray, int n, int n2) {
            if (!this.squeezing) {
                this.switchToSqueezing();
            }
            if (n2 < 0 || this.theXofRemaining > 0L && (long)n2 > this.theXofRemaining) {
                throw new IllegalArgumentException("Insufficient bytes remaining");
            }
            this.theTree.squeeze(byArray, n, n2);
            return n2;
        }

        private void processData(byte[] byArray, int n, int n2) {
            int n3;
            if (this.squeezing) {
                throw new IllegalStateException("attempt to absorb while squeezing");
            }
            KangarooSponge kangarooSponge = this.theCurrNode == 0 ? this.theTree : this.theLeaf;
            int n4 = 8192 - this.theProcessed;
            if (n4 >= n2) {
                kangarooSponge.absorb(byArray, n, n2);
                this.theProcessed += n2;
                return;
            }
            if (n4 > 0) {
                kangarooSponge.absorb(byArray, n, n4);
                this.theProcessed += n4;
            }
            for (int i = n4; i < n2; i += n3) {
                if (this.theProcessed == 8192) {
                    this.switchLeaf(true);
                }
                n3 = Math.min(n2 - i, 8192);
                this.theLeaf.absorb(byArray, n + i, n3);
                this.theProcessed += n3;
            }
        }

        public void reset() {
            this.theTree.initSponge();
            this.theLeaf.initSponge();
            this.theCurrNode = 0;
            this.theProcessed = 0;
            this.theXofRemaining = -1L;
            this.squeezing = false;
        }

        private void switchLeaf(boolean bl) {
            if (this.theCurrNode == 0) {
                this.theTree.absorb(KangarooBase.FIRST, 0, KangarooBase.FIRST.length);
            } else {
                this.theLeaf.absorb(KangarooBase.INTERMEDIATE, 0, KangarooBase.INTERMEDIATE.length);
                byte[] byArray = new byte[this.theChainLen];
                this.theLeaf.squeeze(byArray, 0, this.theChainLen);
                this.theTree.absorb(byArray, 0, this.theChainLen);
                this.theLeaf.initSponge();
            }
            if (bl) {
                ++this.theCurrNode;
            }
            this.theProcessed = 0;
        }

        private void switchToSqueezing() {
            this.processData(this.thePersonal, 0, this.thePersonal.length);
            if (this.theCurrNode == 0) {
                this.switchSingle();
            } else {
                this.switchFinal();
            }
            this.theXofRemaining = this.theXofLen == 0L ? (long)this.getDigestSize() : (this.theXofLen == -1L ? -2L : this.theXofLen);
        }

        private void switchSingle() {
            this.theTree.absorb(KangarooBase.SINGLE, 0, 1);
            this.theTree.padAndSwitchToSqueezingPhase();
        }

        private void switchFinal() {
            this.switchLeaf(false);
            byte[] byArray = KangarooBase.lengthEncode(this.theCurrNode);
            this.theTree.absorb(byArray, 0, byArray.length);
            this.theTree.absorb(KangarooBase.FINAL, 0, KangarooBase.FINAL.length);
            this.theTree.padAndSwitchToSqueezingPhase();
        }

        private static byte[] lengthEncode(long l) {
            int n = 0;
            long l2 = l;
            if (l2 != 0L) {
                n = 1;
                while ((l2 >>= 8) != 0L) {
                    n = (byte)(n + 1);
                }
            }
            byte[] byArray = new byte[n + 1];
            byArray[n] = n;
            for (int i = 0; i < n; ++i) {
                byArray[i] = (byte)(l >> 8 * (n - i - 1));
            }
            return byArray;
        }
    }

    public static class KangarooParameters
    implements CipherParameters {
        private byte[] thePersonal;
        private long theMaxXofLen;

        public byte[] getPersonalisation() {
            return Arrays.clone(this.thePersonal);
        }

        public long getMaxOutputLength() {
            return this.theMaxXofLen;
        }

        static /* synthetic */ byte[] access$002(KangarooParameters kangarooParameters, byte[] byArray) {
            kangarooParameters.thePersonal = byArray;
            return byArray;
        }

        public static class Builder {
            private byte[] thePersonal;
            private long theMaxXofLen;

            public Builder setPersonalisation(byte[] byArray) {
                this.thePersonal = Arrays.clone(byArray);
                return this;
            }

            public Builder setMaxOutputLen(long l) {
                this.theMaxXofLen = l;
                return this;
            }

            public KangarooParameters build() {
                KangarooParameters kangarooParameters = new KangarooParameters();
                if (this.thePersonal != null) {
                    KangarooParameters.access$002(kangarooParameters, this.thePersonal);
                }
                kangarooParameters.theMaxXofLen = this.theMaxXofLen;
                return kangarooParameters;
            }
        }
    }

    private static class KangarooSponge {
        private static long[] KeccakRoundConstants = new long[]{1L, 32898L, -9223372036854742902L, -9223372034707259392L, 32907L, 0x80000001L, -9223372034707259263L, -9223372036854743031L, 138L, 136L, 0x80008009L, 0x8000000AL, 0x8000808BL, -9223372036854775669L, -9223372036854742903L, -9223372036854743037L, -9223372036854743038L, -9223372036854775680L, 32778L, -9223372034707292150L, -9223372034707259263L, -9223372036854742912L, 0x80000001L, -9223372034707259384L};
        private final int theRounds;
        private final int theRateBytes;
        private final long[] theState = new long[25];
        private final byte[] theQueue;
        private int bytesInQueue;
        private boolean squeezing;

        KangarooSponge(int n, int n2) {
            this.theRateBytes = 1600 - (n << 1) >> 3;
            this.theRounds = n2;
            this.theQueue = new byte[this.theRateBytes];
            this.initSponge();
        }

        private void initSponge() {
            Arrays.fill(this.theState, 0L);
            Arrays.fill(this.theQueue, (byte)0);
            this.bytesInQueue = 0;
            this.squeezing = false;
        }

        private void absorb(byte[] byArray, int n, int n2) {
            if (this.squeezing) {
                throw new IllegalStateException("attempt to absorb while squeezing");
            }
            int n3 = 0;
            while (n3 < n2) {
                if (this.bytesInQueue == 0 && n3 <= n2 - this.theRateBytes) {
                    do {
                        this.KangarooAbsorb(byArray, n + n3);
                    } while ((n3 += this.theRateBytes) <= n2 - this.theRateBytes);
                    continue;
                }
                int n4 = Math.min(this.theRateBytes - this.bytesInQueue, n2 - n3);
                System.arraycopy(byArray, n + n3, this.theQueue, this.bytesInQueue, n4);
                this.bytesInQueue += n4;
                n3 += n4;
                if (this.bytesInQueue != this.theRateBytes) continue;
                this.KangarooAbsorb(this.theQueue, 0);
                this.bytesInQueue = 0;
            }
        }

        private void padAndSwitchToSqueezingPhase() {
            for (int i = this.bytesInQueue; i < this.theRateBytes; ++i) {
                this.theQueue[i] = 0;
            }
            int n = this.theRateBytes - 1;
            this.theQueue[n] = (byte)(this.theQueue[n] ^ 0x80);
            this.KangarooAbsorb(this.theQueue, 0);
            this.KangarooExtract();
            this.bytesInQueue = this.theRateBytes;
            this.squeezing = true;
        }

        private void squeeze(byte[] byArray, int n, int n2) {
            int n3;
            if (!this.squeezing) {
                this.padAndSwitchToSqueezingPhase();
            }
            for (int i = 0; i < n2; i += n3) {
                if (this.bytesInQueue == 0) {
                    this.KangarooPermutation();
                    this.KangarooExtract();
                    this.bytesInQueue = this.theRateBytes;
                }
                n3 = Math.min(this.bytesInQueue, n2 - i);
                System.arraycopy(this.theQueue, this.theRateBytes - this.bytesInQueue, byArray, n + i, n3);
                this.bytesInQueue -= n3;
            }
        }

        private void KangarooAbsorb(byte[] byArray, int n) {
            int n2 = this.theRateBytes >> 3;
            int n3 = n;
            int n4 = 0;
            while (n4 < n2) {
                int n5 = n4++;
                this.theState[n5] = this.theState[n5] ^ Pack.littleEndianToLong(byArray, n3);
                n3 += 8;
            }
            this.KangarooPermutation();
        }

        private void KangarooExtract() {
            Pack.longToLittleEndian(this.theState, 0, this.theRateBytes >> 3, this.theQueue, 0);
        }

        private void KangarooPermutation() {
            long[] lArray = this.theState;
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
            int n = KeccakRoundConstants.length - this.theRounds;
            for (int i = 0; i < this.theRounds; ++i) {
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
                l ^= KeccakRoundConstants[n + i];
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

    public static class KangarooTwelve
    extends KangarooBase {
        public KangarooTwelve() {
            this(32);
        }

        public KangarooTwelve(int n) {
            super(128, 12, n);
        }

        public String getAlgorithmName() {
            return "KangarooTwelve";
        }
    }

    public static class MarsupilamiFourteen
    extends KangarooBase {
        public MarsupilamiFourteen() {
            this(32);
        }

        public MarsupilamiFourteen(int n) {
            super(256, 14, n);
        }

        public String getAlgorithmName() {
            return "MarsupilamiFourteen";
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import java.util.Iterator;
import java.util.Stack;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.Xof;
import org.bouncycastle.crypto.params.Blake3Parameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class Blake3Digest
implements ExtendedDigest,
Memoable,
Xof {
    private static final String ERR_OUTPUTTING = "Already outputting";
    private static final int NUMWORDS = 8;
    private static final int ROUNDS = 7;
    private static final int BLOCKLEN = 64;
    private static final int CHUNKLEN = 1024;
    private static final int CHUNKSTART = 1;
    private static final int CHUNKEND = 2;
    private static final int PARENT = 4;
    private static final int ROOT = 8;
    private static final int KEYEDHASH = 16;
    private static final int DERIVECONTEXT = 32;
    private static final int DERIVEKEY = 64;
    private static final int CHAINING0 = 0;
    private static final int CHAINING1 = 1;
    private static final int CHAINING2 = 2;
    private static final int CHAINING3 = 3;
    private static final int CHAINING4 = 4;
    private static final int CHAINING5 = 5;
    private static final int CHAINING6 = 6;
    private static final int CHAINING7 = 7;
    private static final int IV0 = 8;
    private static final int IV1 = 9;
    private static final int IV2 = 10;
    private static final int IV3 = 11;
    private static final int COUNT0 = 12;
    private static final int COUNT1 = 13;
    private static final int DATALEN = 14;
    private static final int FLAGS = 15;
    private static final byte[] SIGMA = new byte[]{2, 6, 3, 10, 7, 0, 4, 13, 1, 11, 12, 5, 9, 14, 15, 8};
    private static final byte[] ROTATE = new byte[]{16, 12, 8, 7};
    private static final int[] IV = new int[]{1779033703, -1150833019, 1013904242, -1521486534, 1359893119, -1694144372, 528734635, 1541459225};
    private final byte[] theBuffer = new byte[64];
    private final int[] theK = new int[8];
    private final int[] theChaining = new int[8];
    private final int[] theV = new int[16];
    private final int[] theM = new int[16];
    private final byte[] theIndices = new byte[16];
    private final Stack theStack = new Stack();
    private final int theDigestLen;
    private boolean outputting;
    private int theMode;
    private int theOutputMode;
    private int theOutputDataLen;
    private long theCounter;
    private int theCurrBytes;
    private int thePos;

    public Blake3Digest() {
        this(32);
    }

    public Blake3Digest(int n) {
        this.theDigestLen = n;
        this.init(null);
    }

    private Blake3Digest(Blake3Digest blake3Digest) {
        this.theDigestLen = blake3Digest.theDigestLen;
        this.reset(blake3Digest);
    }

    public int getByteLength() {
        return 64;
    }

    public String getAlgorithmName() {
        return "BLAKE3";
    }

    public int getDigestSize() {
        return this.theDigestLen;
    }

    public void init(Blake3Parameters blake3Parameters) {
        byte[] byArray = blake3Parameters == null ? null : blake3Parameters.getKey();
        byte[] byArray2 = blake3Parameters == null ? null : blake3Parameters.getContext();
        this.reset();
        if (byArray != null) {
            this.initKey(byArray);
            Arrays.fill(byArray, (byte)0);
        } else if (byArray2 != null) {
            this.initNullKey();
            this.theMode = 32;
            this.update(byArray2, 0, byArray2.length);
            this.doFinal(this.theBuffer, 0);
            this.initKeyFromContext();
            this.reset();
        } else {
            this.initNullKey();
            this.theMode = 0;
        }
    }

    public void update(byte by) {
        if (this.outputting) {
            throw new IllegalStateException(ERR_OUTPUTTING);
        }
        int n = this.theBuffer.length;
        int n2 = n - this.thePos;
        if (n2 == 0) {
            this.compressBlock(this.theBuffer, 0);
            Arrays.fill(this.theBuffer, (byte)0);
            this.thePos = 0;
        }
        this.theBuffer[this.thePos] = by;
        ++this.thePos;
    }

    public void update(byte[] byArray, int n, int n2) {
        int n3;
        if (byArray == null || n2 == 0) {
            return;
        }
        if (this.outputting) {
            throw new IllegalStateException(ERR_OUTPUTTING);
        }
        int n4 = 0;
        if (this.thePos != 0) {
            n4 = 64 - this.thePos;
            if (n4 >= n2) {
                System.arraycopy(byArray, n, this.theBuffer, this.thePos, n2);
                this.thePos += n2;
                return;
            }
            System.arraycopy(byArray, n, this.theBuffer, this.thePos, n4);
            this.compressBlock(this.theBuffer, 0);
            this.thePos = 0;
            Arrays.fill(this.theBuffer, (byte)0);
        }
        int n5 = n + n2 - 64;
        for (n3 = n + n4; n3 < n5; n3 += 64) {
            this.compressBlock(byArray, n3);
        }
        int n6 = n2 - n3;
        System.arraycopy(byArray, n3, this.theBuffer, 0, n + n6);
        this.thePos += n + n6;
    }

    public int doFinal(byte[] byArray, int n) {
        return this.doFinal(byArray, n, this.getDigestSize());
    }

    public int doFinal(byte[] byArray, int n, int n2) {
        if (this.outputting) {
            throw new IllegalStateException(ERR_OUTPUTTING);
        }
        int n3 = this.doOutput(byArray, n, n2);
        this.reset();
        return n3;
    }

    public int doOutput(byte[] byArray, int n, int n2) {
        int n3;
        if (!this.outputting) {
            this.compressFinalBlock(this.thePos);
        }
        int n4 = n2;
        int n5 = n;
        if (this.thePos < 64) {
            n3 = Math.min(n4, 64 - this.thePos);
            System.arraycopy(this.theBuffer, this.thePos, byArray, n5, n3);
            this.thePos += n3;
            n5 += n3;
            n4 -= n3;
        }
        while (n4 > 0) {
            this.nextOutputBlock();
            n3 = Math.min(n4, 64);
            System.arraycopy(this.theBuffer, 0, byArray, n5, n3);
            this.thePos += n3;
            n5 += n3;
            n4 -= n3;
        }
        return n2;
    }

    public void reset() {
        this.resetBlockCount();
        this.thePos = 0;
        this.outputting = false;
        Arrays.fill(this.theBuffer, (byte)0);
    }

    public void reset(Memoable memoable) {
        Blake3Digest blake3Digest = (Blake3Digest)memoable;
        this.theCounter = blake3Digest.theCounter;
        this.theCurrBytes = blake3Digest.theCurrBytes;
        this.theMode = blake3Digest.theMode;
        this.outputting = blake3Digest.outputting;
        this.theOutputMode = blake3Digest.theOutputMode;
        this.theOutputDataLen = blake3Digest.theOutputDataLen;
        System.arraycopy(blake3Digest.theChaining, 0, this.theChaining, 0, this.theChaining.length);
        System.arraycopy(blake3Digest.theK, 0, this.theK, 0, this.theK.length);
        System.arraycopy(blake3Digest.theM, 0, this.theM, 0, this.theM.length);
        this.theStack.clear();
        Iterator iterator = blake3Digest.theStack.iterator();
        while (iterator.hasNext()) {
            this.theStack.push(Arrays.clone((int[])iterator.next()));
        }
        System.arraycopy(blake3Digest.theBuffer, 0, this.theBuffer, 0, this.theBuffer.length);
        this.thePos = blake3Digest.thePos;
    }

    public Memoable copy() {
        return new Blake3Digest(this);
    }

    private void compressBlock(byte[] byArray, int n) {
        this.initChunkBlock(64, false);
        this.initM(byArray, n);
        this.compress();
        if (this.theCurrBytes == 0) {
            this.adjustStack();
        }
    }

    private void adjustStack() {
        for (long i = this.theCounter; i > 0L && (i & 1L) != 1L; i >>= 1) {
            int[] nArray = (int[])this.theStack.pop();
            System.arraycopy(nArray, 0, this.theM, 0, 8);
            System.arraycopy(this.theChaining, 0, this.theM, 8, 8);
            this.initParentBlock();
            this.compress();
        }
        this.theStack.push(Arrays.copyOf(this.theChaining, 8));
    }

    private void compressFinalBlock(int n) {
        this.initChunkBlock(n, true);
        this.initM(this.theBuffer, 0);
        this.compress();
        this.processStack();
    }

    private void processStack() {
        while (!this.theStack.isEmpty()) {
            int[] nArray = (int[])this.theStack.pop();
            System.arraycopy(nArray, 0, this.theM, 0, 8);
            System.arraycopy(this.theChaining, 0, this.theM, 8, 8);
            this.initParentBlock();
            if (this.theStack.isEmpty()) {
                this.setRoot();
            }
            this.compress();
        }
    }

    private void compress() {
        this.initIndices();
        for (int i = 0; i < 6; ++i) {
            this.performRound();
            this.permuteIndices();
        }
        this.performRound();
        this.adjustChaining();
    }

    private void performRound() {
        int n = 0;
        this.mixG(n++, 0, 4, 8, 12);
        this.mixG(n++, 1, 5, 9, 13);
        this.mixG(n++, 2, 6, 10, 14);
        this.mixG(n++, 3, 7, 11, 15);
        this.mixG(n++, 0, 5, 10, 15);
        this.mixG(n++, 1, 6, 11, 12);
        this.mixG(n++, 2, 7, 8, 13);
        this.mixG(n, 3, 4, 9, 14);
    }

    private void initM(byte[] byArray, int n) {
        for (int i = 0; i < 16; ++i) {
            this.theM[i] = Pack.littleEndianToInt(byArray, n + i * 4);
        }
    }

    private void adjustChaining() {
        if (this.outputting) {
            int n;
            for (n = 0; n < 8; ++n) {
                int n2 = n;
                this.theV[n2] = this.theV[n2] ^ this.theV[n + 8];
                int n3 = n + 8;
                this.theV[n3] = this.theV[n3] ^ this.theChaining[n];
            }
            for (n = 0; n < 16; ++n) {
                Pack.intToLittleEndian(this.theV[n], this.theBuffer, n * 4);
            }
            this.thePos = 0;
        } else {
            for (int i = 0; i < 8; ++i) {
                this.theChaining[i] = this.theV[i] ^ this.theV[i + 8];
            }
        }
    }

    private void mixG(int n, int n2, int n3, int n4, int n5) {
        int n6 = n << 1;
        int n7 = 0;
        int n8 = n2;
        this.theV[n8] = this.theV[n8] + (this.theV[n3] + this.theM[this.theIndices[n6++]]);
        this.theV[n5] = Integers.rotateRight(this.theV[n5] ^ this.theV[n2], ROTATE[n7++]);
        int n9 = n4;
        this.theV[n9] = this.theV[n9] + this.theV[n5];
        this.theV[n3] = Integers.rotateRight(this.theV[n3] ^ this.theV[n4], ROTATE[n7++]);
        int n10 = n2;
        this.theV[n10] = this.theV[n10] + (this.theV[n3] + this.theM[this.theIndices[n6]]);
        this.theV[n5] = Integers.rotateRight(this.theV[n5] ^ this.theV[n2], ROTATE[n7++]);
        int n11 = n4;
        this.theV[n11] = this.theV[n11] + this.theV[n5];
        this.theV[n3] = Integers.rotateRight(this.theV[n3] ^ this.theV[n4], ROTATE[n7]);
    }

    private void initIndices() {
        for (int n = 0; n < this.theIndices.length; n = (int)((byte)(n + 1))) {
            this.theIndices[n] = n;
        }
    }

    private void permuteIndices() {
        for (int n = 0; n < this.theIndices.length; n = (int)((byte)(n + 1))) {
            this.theIndices[n] = SIGMA[this.theIndices[n]];
        }
    }

    private void initNullKey() {
        System.arraycopy(IV, 0, this.theK, 0, 8);
    }

    private void initKey(byte[] byArray) {
        for (int i = 0; i < 8; ++i) {
            this.theK[i] = Pack.littleEndianToInt(byArray, i * 4);
        }
        this.theMode = 16;
    }

    private void initKeyFromContext() {
        System.arraycopy(this.theV, 0, this.theK, 0, 8);
        this.theMode = 64;
    }

    private void initChunkBlock(int n, boolean bl) {
        System.arraycopy(this.theCurrBytes == 0 ? this.theK : this.theChaining, 0, this.theV, 0, 8);
        System.arraycopy(IV, 0, this.theV, 8, 4);
        this.theV[12] = (int)this.theCounter;
        this.theV[13] = (int)(this.theCounter >> 32);
        this.theV[14] = n;
        this.theV[15] = this.theMode + (this.theCurrBytes == 0 ? 1 : 0) + (bl ? 2 : 0);
        this.theCurrBytes += n;
        if (this.theCurrBytes >= 1024) {
            this.incrementBlockCount();
            this.theV[15] = this.theV[15] | 2;
        }
        if (bl && this.theStack.isEmpty()) {
            this.setRoot();
        }
    }

    private void initParentBlock() {
        System.arraycopy(this.theK, 0, this.theV, 0, 8);
        System.arraycopy(IV, 0, this.theV, 8, 4);
        this.theV[12] = 0;
        this.theV[13] = 0;
        this.theV[14] = 64;
        this.theV[15] = this.theMode | 4;
    }

    private void nextOutputBlock() {
        ++this.theCounter;
        System.arraycopy(this.theChaining, 0, this.theV, 0, 8);
        System.arraycopy(IV, 0, this.theV, 8, 4);
        this.theV[12] = (int)this.theCounter;
        this.theV[13] = (int)(this.theCounter >> 32);
        this.theV[14] = this.theOutputDataLen;
        this.theV[15] = this.theOutputMode;
        this.compress();
    }

    private void incrementBlockCount() {
        ++this.theCounter;
        this.theCurrBytes = 0;
    }

    private void resetBlockCount() {
        this.theCounter = 0L;
        this.theCurrBytes = 0;
    }

    private void setRoot() {
        this.theV[15] = this.theV[15] | 8;
        this.theOutputMode = this.theV[15];
        this.theOutputDataLen = this.theV[14];
        this.theCounter = 0L;
        this.outputting = true;
        System.arraycopy(this.theV, 0, this.theChaining, 0, 8);
    }
}


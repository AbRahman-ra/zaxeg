/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.Xof;
import org.bouncycastle.crypto.digests.Blake2sDigest;
import org.bouncycastle.util.Arrays;

public class Blake2xsDigest
implements Xof {
    public static final int UNKNOWN_DIGEST_LENGTH = 65535;
    private static final int DIGEST_LENGTH = 32;
    private static final long MAX_NUMBER_BLOCKS = 0x100000000L;
    private int digestLength;
    private Blake2sDigest hash;
    private byte[] h0 = null;
    private byte[] buf = new byte[32];
    private int bufPos = 32;
    private int digestPos = 0;
    private long blockPos = 0L;
    private long nodeOffset;

    public Blake2xsDigest() {
        this(65535);
    }

    public Blake2xsDigest(int n) {
        this(n, null, null, null);
    }

    public Blake2xsDigest(int n, byte[] byArray) {
        this(n, byArray, null, null);
    }

    public Blake2xsDigest(int n, byte[] byArray, byte[] byArray2, byte[] byArray3) {
        if (n < 1 || n > 65535) {
            throw new IllegalArgumentException("BLAKE2xs digest length must be between 1 and 2^16-1");
        }
        this.digestLength = n;
        this.nodeOffset = this.computeNodeOffset();
        this.hash = new Blake2sDigest(32, byArray, byArray2, byArray3, this.nodeOffset);
    }

    public Blake2xsDigest(Blake2xsDigest blake2xsDigest) {
        this.digestLength = blake2xsDigest.digestLength;
        this.hash = new Blake2sDigest(blake2xsDigest.hash);
        this.h0 = Arrays.clone(blake2xsDigest.h0);
        this.buf = Arrays.clone(blake2xsDigest.buf);
        this.bufPos = blake2xsDigest.bufPos;
        this.digestPos = blake2xsDigest.digestPos;
        this.blockPos = blake2xsDigest.blockPos;
        this.nodeOffset = blake2xsDigest.nodeOffset;
    }

    public String getAlgorithmName() {
        return "BLAKE2xs";
    }

    public int getDigestSize() {
        return this.digestLength;
    }

    public int getByteLength() {
        return this.hash.getByteLength();
    }

    public long getUnknownMaxLength() {
        return 0x2000000000L;
    }

    public void update(byte by) {
        this.hash.update(by);
    }

    public void update(byte[] byArray, int n, int n2) {
        this.hash.update(byArray, n, n2);
    }

    public void reset() {
        this.hash.reset();
        this.h0 = null;
        this.bufPos = 32;
        this.digestPos = 0;
        this.blockPos = 0L;
        this.nodeOffset = this.computeNodeOffset();
    }

    public int doFinal(byte[] byArray, int n) {
        return this.doFinal(byArray, n, byArray.length);
    }

    public int doFinal(byte[] byArray, int n, int n2) {
        int n3 = this.doOutput(byArray, n, n2);
        this.reset();
        return n3;
    }

    public int doOutput(byte[] byArray, int n, int n2) {
        if (this.h0 == null) {
            this.h0 = new byte[this.hash.getDigestSize()];
            this.hash.doFinal(this.h0, 0);
        }
        if (this.digestLength != 65535) {
            if (this.digestPos + n2 > this.digestLength) {
                throw new IllegalArgumentException("Output length is above the digest length");
            }
        } else if (this.blockPos << 5 >= this.getUnknownMaxLength()) {
            throw new IllegalArgumentException("Maximum length is 2^32 blocks of 32 bytes");
        }
        for (int i = 0; i < n2; ++i) {
            if (this.bufPos >= 32) {
                Blake2sDigest blake2sDigest = new Blake2sDigest(this.computeStepLength(), 32, this.nodeOffset);
                blake2sDigest.update(this.h0, 0, this.h0.length);
                Arrays.fill(this.buf, (byte)0);
                blake2sDigest.doFinal(this.buf, 0);
                this.bufPos = 0;
                ++this.nodeOffset;
                ++this.blockPos;
            }
            byArray[i] = this.buf[this.bufPos];
            ++this.bufPos;
            ++this.digestPos;
        }
        return n2;
    }

    private int computeStepLength() {
        if (this.digestLength == 65535) {
            return 32;
        }
        return Math.min(32, this.digestLength - this.digestPos);
    }

    private long computeNodeOffset() {
        return (long)this.digestLength * 0x100000000L;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class Blake2sDigest
implements ExtendedDigest {
    private static final int[] blake2s_IV = new int[]{1779033703, -1150833019, 1013904242, -1521486534, 1359893119, -1694144372, 528734635, 1541459225};
    private static final byte[][] blake2s_sigma = new byte[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3}, {11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4}, {7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8}, {9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13}, {2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9}, {12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11}, {13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10}, {6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5}, {10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13, 0}};
    private static final int ROUNDS = 10;
    private static final int BLOCK_LENGTH_BYTES = 64;
    private int digestLength = 32;
    private int keyLength = 0;
    private byte[] salt = null;
    private byte[] personalization = null;
    private byte[] key = null;
    private int fanout = 1;
    private int depth = 1;
    private int leafLength = 0;
    private long nodeOffset = 0L;
    private int nodeDepth = 0;
    private int innerHashLength = 0;
    private byte[] buffer = null;
    private int bufferPos = 0;
    private int[] internalState = new int[16];
    private int[] chainValue = null;
    private int t0 = 0;
    private int t1 = 0;
    private int f0 = 0;

    public Blake2sDigest() {
        this(256);
    }

    public Blake2sDigest(Blake2sDigest blake2sDigest) {
        this.bufferPos = blake2sDigest.bufferPos;
        this.buffer = Arrays.clone(blake2sDigest.buffer);
        this.keyLength = blake2sDigest.keyLength;
        this.key = Arrays.clone(blake2sDigest.key);
        this.digestLength = blake2sDigest.digestLength;
        this.internalState = Arrays.clone(this.internalState);
        this.chainValue = Arrays.clone(blake2sDigest.chainValue);
        this.t0 = blake2sDigest.t0;
        this.t1 = blake2sDigest.t1;
        this.f0 = blake2sDigest.f0;
        this.salt = Arrays.clone(blake2sDigest.salt);
        this.personalization = Arrays.clone(blake2sDigest.personalization);
        this.fanout = blake2sDigest.fanout;
        this.depth = blake2sDigest.depth;
        this.leafLength = blake2sDigest.leafLength;
        this.nodeOffset = blake2sDigest.nodeOffset;
        this.nodeDepth = blake2sDigest.nodeDepth;
        this.innerHashLength = blake2sDigest.innerHashLength;
    }

    public Blake2sDigest(int n) {
        if (n < 8 || n > 256 || n % 8 != 0) {
            throw new IllegalArgumentException("BLAKE2s digest bit length must be a multiple of 8 and not greater than 256");
        }
        this.digestLength = n / 8;
        this.init(null, null, null);
    }

    public Blake2sDigest(byte[] byArray) {
        this.init(null, null, byArray);
    }

    public Blake2sDigest(byte[] byArray, int n, byte[] byArray2, byte[] byArray3) {
        if (n < 1 || n > 32) {
            throw new IllegalArgumentException("Invalid digest length (required: 1 - 32)");
        }
        this.digestLength = n;
        this.init(byArray2, byArray3, byArray);
    }

    Blake2sDigest(int n, byte[] byArray, byte[] byArray2, byte[] byArray3, long l) {
        this.digestLength = n;
        this.nodeOffset = l;
        this.init(byArray2, byArray3, byArray);
    }

    Blake2sDigest(int n, int n2, long l) {
        this.digestLength = n;
        this.nodeOffset = l;
        this.fanout = 0;
        this.depth = 0;
        this.leafLength = n2;
        this.innerHashLength = n2;
        this.nodeDepth = 0;
        this.init(null, null, null);
    }

    private void init(byte[] byArray, byte[] byArray2, byte[] byArray3) {
        this.buffer = new byte[64];
        if (byArray3 != null && byArray3.length > 0) {
            if (byArray3.length > 32) {
                throw new IllegalArgumentException("Keys > 32 bytes are not supported");
            }
            this.key = new byte[byArray3.length];
            System.arraycopy(byArray3, 0, this.key, 0, byArray3.length);
            this.keyLength = byArray3.length;
            System.arraycopy(byArray3, 0, this.buffer, 0, byArray3.length);
            this.bufferPos = 64;
        }
        if (this.chainValue == null) {
            this.chainValue = new int[8];
            this.chainValue[0] = blake2s_IV[0] ^ (this.digestLength | this.keyLength << 8 | (this.fanout << 16 | this.depth << 24));
            this.chainValue[1] = blake2s_IV[1] ^ this.leafLength;
            int n = (int)(this.nodeOffset >> 32);
            int n2 = (int)this.nodeOffset;
            this.chainValue[2] = blake2s_IV[2] ^ n2;
            this.chainValue[3] = blake2s_IV[3] ^ (n | this.nodeDepth << 16 | this.innerHashLength << 24);
            this.chainValue[4] = blake2s_IV[4];
            this.chainValue[5] = blake2s_IV[5];
            if (byArray != null) {
                if (byArray.length != 8) {
                    throw new IllegalArgumentException("Salt length must be exactly 8 bytes");
                }
                this.salt = new byte[8];
                System.arraycopy(byArray, 0, this.salt, 0, byArray.length);
                this.chainValue[4] = this.chainValue[4] ^ Pack.littleEndianToInt(byArray, 0);
                this.chainValue[5] = this.chainValue[5] ^ Pack.littleEndianToInt(byArray, 4);
            }
            this.chainValue[6] = blake2s_IV[6];
            this.chainValue[7] = blake2s_IV[7];
            if (byArray2 != null) {
                if (byArray2.length != 8) {
                    throw new IllegalArgumentException("Personalization length must be exactly 8 bytes");
                }
                this.personalization = new byte[8];
                System.arraycopy(byArray2, 0, this.personalization, 0, byArray2.length);
                this.chainValue[6] = this.chainValue[6] ^ Pack.littleEndianToInt(byArray2, 0);
                this.chainValue[7] = this.chainValue[7] ^ Pack.littleEndianToInt(byArray2, 4);
            }
        }
    }

    private void initializeInternalState() {
        System.arraycopy(this.chainValue, 0, this.internalState, 0, this.chainValue.length);
        System.arraycopy(blake2s_IV, 0, this.internalState, this.chainValue.length, 4);
        this.internalState[12] = this.t0 ^ blake2s_IV[4];
        this.internalState[13] = this.t1 ^ blake2s_IV[5];
        this.internalState[14] = this.f0 ^ blake2s_IV[6];
        this.internalState[15] = blake2s_IV[7];
    }

    public void update(byte by) {
        int n = 64 - this.bufferPos;
        if (n == 0) {
            this.t0 += 64;
            if (this.t0 == 0) {
                ++this.t1;
            }
            this.compress(this.buffer, 0);
            Arrays.fill(this.buffer, (byte)0);
            this.buffer[0] = by;
            this.bufferPos = 1;
        } else {
            this.buffer[this.bufferPos] = by;
            ++this.bufferPos;
        }
    }

    public void update(byte[] byArray, int n, int n2) {
        int n3;
        if (byArray == null || n2 == 0) {
            return;
        }
        int n4 = 0;
        if (this.bufferPos != 0) {
            n4 = 64 - this.bufferPos;
            if (n4 < n2) {
                System.arraycopy(byArray, n, this.buffer, this.bufferPos, n4);
                this.t0 += 64;
                if (this.t0 == 0) {
                    ++this.t1;
                }
                this.compress(this.buffer, 0);
                this.bufferPos = 0;
                Arrays.fill(this.buffer, (byte)0);
            } else {
                System.arraycopy(byArray, n, this.buffer, this.bufferPos, n2);
                this.bufferPos += n2;
                return;
            }
        }
        int n5 = n + n2 - 64;
        for (n3 = n + n4; n3 < n5; n3 += 64) {
            this.t0 += 64;
            if (this.t0 == 0) {
                ++this.t1;
            }
            this.compress(byArray, n3);
        }
        System.arraycopy(byArray, n3, this.buffer, 0, n + n2 - n3);
        this.bufferPos += n + n2 - n3;
    }

    public int doFinal(byte[] byArray, int n) {
        this.f0 = -1;
        this.t0 += this.bufferPos;
        if (this.t0 < 0 && this.bufferPos > -this.t0) {
            ++this.t1;
        }
        this.compress(this.buffer, 0);
        Arrays.fill(this.buffer, (byte)0);
        Arrays.fill(this.internalState, 0);
        for (int i = 0; i < this.chainValue.length && i * 4 < this.digestLength; ++i) {
            byte[] byArray2 = Pack.intToLittleEndian(this.chainValue[i]);
            if (i * 4 < this.digestLength - 4) {
                System.arraycopy(byArray2, 0, byArray, n + i * 4, 4);
                continue;
            }
            System.arraycopy(byArray2, 0, byArray, n + i * 4, this.digestLength - i * 4);
        }
        Arrays.fill(this.chainValue, 0);
        this.reset();
        return this.digestLength;
    }

    public void reset() {
        this.bufferPos = 0;
        this.f0 = 0;
        this.t0 = 0;
        this.t1 = 0;
        this.chainValue = null;
        Arrays.fill(this.buffer, (byte)0);
        if (this.key != null) {
            System.arraycopy(this.key, 0, this.buffer, 0, this.key.length);
            this.bufferPos = 64;
        }
        this.init(this.salt, this.personalization, this.key);
    }

    private void compress(byte[] byArray, int n) {
        int n2;
        this.initializeInternalState();
        int[] nArray = new int[16];
        for (n2 = 0; n2 < 16; ++n2) {
            nArray[n2] = Pack.littleEndianToInt(byArray, n + n2 * 4);
        }
        for (n2 = 0; n2 < 10; ++n2) {
            this.G(nArray[blake2s_sigma[n2][0]], nArray[blake2s_sigma[n2][1]], 0, 4, 8, 12);
            this.G(nArray[blake2s_sigma[n2][2]], nArray[blake2s_sigma[n2][3]], 1, 5, 9, 13);
            this.G(nArray[blake2s_sigma[n2][4]], nArray[blake2s_sigma[n2][5]], 2, 6, 10, 14);
            this.G(nArray[blake2s_sigma[n2][6]], nArray[blake2s_sigma[n2][7]], 3, 7, 11, 15);
            this.G(nArray[blake2s_sigma[n2][8]], nArray[blake2s_sigma[n2][9]], 0, 5, 10, 15);
            this.G(nArray[blake2s_sigma[n2][10]], nArray[blake2s_sigma[n2][11]], 1, 6, 11, 12);
            this.G(nArray[blake2s_sigma[n2][12]], nArray[blake2s_sigma[n2][13]], 2, 7, 8, 13);
            this.G(nArray[blake2s_sigma[n2][14]], nArray[blake2s_sigma[n2][15]], 3, 4, 9, 14);
        }
        for (n2 = 0; n2 < this.chainValue.length; ++n2) {
            this.chainValue[n2] = this.chainValue[n2] ^ this.internalState[n2] ^ this.internalState[n2 + 8];
        }
    }

    private void G(int n, int n2, int n3, int n4, int n5, int n6) {
        this.internalState[n3] = this.internalState[n3] + this.internalState[n4] + n;
        this.internalState[n6] = this.rotr32(this.internalState[n6] ^ this.internalState[n3], 16);
        this.internalState[n5] = this.internalState[n5] + this.internalState[n6];
        this.internalState[n4] = this.rotr32(this.internalState[n4] ^ this.internalState[n5], 12);
        this.internalState[n3] = this.internalState[n3] + this.internalState[n4] + n2;
        this.internalState[n6] = this.rotr32(this.internalState[n6] ^ this.internalState[n3], 8);
        this.internalState[n5] = this.internalState[n5] + this.internalState[n6];
        this.internalState[n4] = this.rotr32(this.internalState[n4] ^ this.internalState[n5], 7);
    }

    private int rotr32(int n, int n2) {
        return n >>> n2 | n << 32 - n2;
    }

    public String getAlgorithmName() {
        return "BLAKE2s";
    }

    public int getDigestSize() {
        return this.digestLength;
    }

    public int getByteLength() {
        return 64;
    }

    public void clearKey() {
        if (this.key != null) {
            Arrays.fill(this.key, (byte)0);
            Arrays.fill(this.buffer, (byte)0);
        }
    }

    public void clearSalt() {
        if (this.salt != null) {
            Arrays.fill(this.salt, (byte)0);
        }
    }
}


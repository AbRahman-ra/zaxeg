/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.generators;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Longs;
import org.bouncycastle.util.Pack;

public class Argon2BytesGenerator {
    private static final int ARGON2_BLOCK_SIZE = 1024;
    private static final int ARGON2_QWORDS_IN_BLOCK = 128;
    private static final int ARGON2_ADDRESSES_IN_BLOCK = 128;
    private static final int ARGON2_PREHASH_DIGEST_LENGTH = 64;
    private static final int ARGON2_PREHASH_SEED_LENGTH = 72;
    private static final int ARGON2_SYNC_POINTS = 4;
    private static final int MIN_PARALLELISM = 1;
    private static final int MAX_PARALLELISM = 0x1000000;
    private static final int MIN_OUTLEN = 4;
    private static final int MIN_ITERATIONS = 1;
    private static final long M32L = 0xFFFFFFFFL;
    private static final byte[] ZERO_BYTES = new byte[4];
    private Argon2Parameters parameters;
    private Block[] memory;
    private int segmentLength;
    private int laneLength;

    public void init(Argon2Parameters argon2Parameters) {
        this.parameters = argon2Parameters;
        if (argon2Parameters.getLanes() < 1) {
            throw new IllegalStateException("lanes must be greater than 1");
        }
        if (argon2Parameters.getLanes() > 0x1000000) {
            throw new IllegalStateException("lanes must be less than 16777216");
        }
        if (argon2Parameters.getMemory() < 2 * argon2Parameters.getLanes()) {
            throw new IllegalStateException("memory is less than: " + 2 * argon2Parameters.getLanes() + " expected " + 2 * argon2Parameters.getLanes());
        }
        if (argon2Parameters.getIterations() < 1) {
            throw new IllegalStateException("iterations is less than: 1");
        }
        this.doInit(argon2Parameters);
    }

    public int generateBytes(char[] cArray, byte[] byArray) {
        return this.generateBytes(this.parameters.getCharToByteConverter().convert(cArray), byArray);
    }

    public int generateBytes(char[] cArray, byte[] byArray, int n, int n2) {
        return this.generateBytes(this.parameters.getCharToByteConverter().convert(cArray), byArray, n, n2);
    }

    public int generateBytes(byte[] byArray, byte[] byArray2) {
        return this.generateBytes(byArray, byArray2, 0, byArray2.length);
    }

    public int generateBytes(byte[] byArray, byte[] byArray2, int n, int n2) {
        if (n2 < 4) {
            throw new IllegalStateException("output length less than 4");
        }
        byte[] byArray3 = new byte[1024];
        this.initialize(byArray3, byArray, n2);
        this.fillMemoryBlocks();
        this.digest(byArray3, byArray2, n, n2);
        this.reset();
        return n2;
    }

    private void reset() {
        if (null != this.memory) {
            for (int i = 0; i < this.memory.length; ++i) {
                Block block = this.memory[i];
                if (null == block) continue;
                block.clear();
            }
        }
    }

    private void doInit(Argon2Parameters argon2Parameters) {
        int n = argon2Parameters.getMemory();
        if (n < 8 * argon2Parameters.getLanes()) {
            n = 8 * argon2Parameters.getLanes();
        }
        this.segmentLength = n / (argon2Parameters.getLanes() * 4);
        this.laneLength = this.segmentLength * 4;
        n = this.segmentLength * (argon2Parameters.getLanes() * 4);
        this.initMemory(n);
    }

    private void initMemory(int n) {
        this.memory = new Block[n];
        for (int i = 0; i < this.memory.length; ++i) {
            this.memory[i] = new Block();
        }
    }

    private void fillMemoryBlocks() {
        FillBlock fillBlock = new FillBlock();
        Position position = new Position();
        for (int i = 0; i < this.parameters.getIterations(); ++i) {
            position.pass = i;
            for (int j = 0; j < 4; ++j) {
                position.slice = j;
                int n = 0;
                while (n < this.parameters.getLanes()) {
                    position.lane = n++;
                    this.fillSegment(fillBlock, position);
                }
            }
        }
    }

    private void fillSegment(FillBlock fillBlock, Position position) {
        Block block = null;
        Block block2 = null;
        boolean bl = this.isDataIndependentAddressing(position);
        int n = Argon2BytesGenerator.getStartingIndex(position);
        int n2 = position.lane * this.laneLength + position.slice * this.segmentLength + n;
        int n3 = this.getPrevOffset(n2);
        if (bl) {
            block = fillBlock.addressBlock.clear();
            block2 = fillBlock.inputBlock.clear();
            this.initAddressBlocks(fillBlock, position, block2, block);
        }
        boolean bl2 = this.isWithXor(position);
        for (int i = n; i < this.segmentLength; ++i) {
            long l;
            int n4 = this.getRefLane(position, l = this.getPseudoRandom(fillBlock, i, block, block2, n3, bl));
            int n5 = this.getRefColumn(position, i, l, n4 == position.lane);
            Block block3 = this.memory[n3];
            Block block4 = this.memory[this.laneLength * n4 + n5];
            Block block5 = this.memory[n2];
            if (bl2) {
                fillBlock.fillBlockWithXor(block3, block4, block5);
            } else {
                fillBlock.fillBlock(block3, block4, block5);
            }
            n3 = n2++;
        }
    }

    private boolean isDataIndependentAddressing(Position position) {
        return this.parameters.getType() == 1 || this.parameters.getType() == 2 && position.pass == 0 && position.slice < 2;
    }

    private void initAddressBlocks(FillBlock fillBlock, Position position, Block block, Block block2) {
        ((Block)block).v[0] = this.intToLong(position.pass);
        ((Block)block).v[1] = this.intToLong(position.lane);
        ((Block)block).v[2] = this.intToLong(position.slice);
        ((Block)block).v[3] = this.intToLong(this.memory.length);
        ((Block)block).v[4] = this.intToLong(this.parameters.getIterations());
        ((Block)block).v[5] = this.intToLong(this.parameters.getType());
        if (position.pass == 0 && position.slice == 0) {
            this.nextAddresses(fillBlock, block, block2);
        }
    }

    private boolean isWithXor(Position position) {
        return position.pass != 0 && this.parameters.getVersion() != 16;
    }

    private int getPrevOffset(int n) {
        if (n % this.laneLength == 0) {
            return n + this.laneLength - 1;
        }
        return n - 1;
    }

    private static int getStartingIndex(Position position) {
        if (position.pass == 0 && position.slice == 0) {
            return 2;
        }
        return 0;
    }

    private void nextAddresses(FillBlock fillBlock, Block block, Block block2) {
        long[] lArray = block.v;
        lArray[6] = lArray[6] + 1L;
        fillBlock.fillBlock(block, block2);
        fillBlock.fillBlock(block2, block2);
    }

    private long getPseudoRandom(FillBlock fillBlock, int n, Block block, Block block2, int n2, boolean bl) {
        if (bl) {
            int n3 = n % 128;
            if (n3 == 0) {
                this.nextAddresses(fillBlock, block2, block);
            }
            return block.v[n3];
        }
        return this.memory[n2].v[0];
    }

    private int getRefLane(Position position, long l) {
        int n = (int)((l >>> 32) % (long)this.parameters.getLanes());
        if (position.pass == 0 && position.slice == 0) {
            n = position.lane;
        }
        return n;
    }

    private int getRefColumn(Position position, int n, long l, boolean bl) {
        int n2;
        int n3;
        if (position.pass == 0) {
            n3 = 0;
            n2 = bl ? position.slice * this.segmentLength + n - 1 : position.slice * this.segmentLength + (n == 0 ? -1 : 0);
        } else {
            n3 = (position.slice + 1) * this.segmentLength % this.laneLength;
            n2 = bl ? this.laneLength - this.segmentLength + n - 1 : this.laneLength - this.segmentLength + (n == 0 ? -1 : 0);
        }
        long l2 = l & 0xFFFFFFFFL;
        l2 = l2 * l2 >>> 32;
        l2 = (long)(n2 - 1) - ((long)n2 * l2 >>> 32);
        return (int)((long)n3 + l2) % this.laneLength;
    }

    private void digest(byte[] byArray, byte[] byArray2, int n, int n2) {
        Block block = this.memory[this.laneLength - 1];
        for (int i = 1; i < this.parameters.getLanes(); ++i) {
            int n3 = i * this.laneLength + (this.laneLength - 1);
            block.xorWith(this.memory[n3]);
        }
        block.toBytes(byArray);
        this.hash(byArray, byArray2, n, n2);
    }

    private void hash(byte[] byArray, byte[] byArray2, int n, int n2) {
        byte[] byArray3 = new byte[4];
        Pack.intToLittleEndian(n2, byArray3, 0);
        int n3 = 64;
        if (n2 <= n3) {
            Blake2bDigest blake2bDigest = new Blake2bDigest(n2 * 8);
            blake2bDigest.update(byArray3, 0, byArray3.length);
            blake2bDigest.update(byArray, 0, byArray.length);
            blake2bDigest.doFinal(byArray2, n);
        } else {
            Blake2bDigest blake2bDigest = new Blake2bDigest(n3 * 8);
            byte[] byArray4 = new byte[n3];
            blake2bDigest.update(byArray3, 0, byArray3.length);
            blake2bDigest.update(byArray, 0, byArray.length);
            blake2bDigest.doFinal(byArray4, 0);
            int n4 = n3 / 2;
            int n5 = n;
            System.arraycopy(byArray4, 0, byArray2, n5, n4);
            n5 += n4;
            int n6 = (n2 + 31) / 32 - 2;
            int n7 = 2;
            while (n7 <= n6) {
                blake2bDigest.update(byArray4, 0, byArray4.length);
                blake2bDigest.doFinal(byArray4, 0);
                System.arraycopy(byArray4, 0, byArray2, n5, n4);
                ++n7;
                n5 += n4;
            }
            n7 = n2 - 32 * n6;
            blake2bDigest = new Blake2bDigest(n7 * 8);
            blake2bDigest.update(byArray4, 0, byArray4.length);
            blake2bDigest.doFinal(byArray2, n5);
        }
    }

    private static void roundFunction(Block block, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10, int n11, int n12, int n13, int n14, int n15, int n16) {
        long[] lArray = block.v;
        Argon2BytesGenerator.F(lArray, n, n5, n9, n13);
        Argon2BytesGenerator.F(lArray, n2, n6, n10, n14);
        Argon2BytesGenerator.F(lArray, n3, n7, n11, n15);
        Argon2BytesGenerator.F(lArray, n4, n8, n12, n16);
        Argon2BytesGenerator.F(lArray, n, n6, n11, n16);
        Argon2BytesGenerator.F(lArray, n2, n7, n12, n13);
        Argon2BytesGenerator.F(lArray, n3, n8, n9, n14);
        Argon2BytesGenerator.F(lArray, n4, n5, n10, n15);
    }

    private static void F(long[] lArray, int n, int n2, int n3, int n4) {
        Argon2BytesGenerator.quarterRound(lArray, n, n2, n4, 32);
        Argon2BytesGenerator.quarterRound(lArray, n3, n4, n2, 24);
        Argon2BytesGenerator.quarterRound(lArray, n, n2, n4, 16);
        Argon2BytesGenerator.quarterRound(lArray, n3, n4, n2, 63);
    }

    private static void quarterRound(long[] lArray, int n, int n2, int n3, int n4) {
        long l = lArray[n];
        long l2 = lArray[n2];
        long l3 = lArray[n3];
        l += l2 + 2L * (l & 0xFFFFFFFFL) * (l2 & 0xFFFFFFFFL);
        l3 = Longs.rotateRight(l3 ^ l, n4);
        lArray[n] = l;
        lArray[n3] = l3;
    }

    private void initialize(byte[] byArray, byte[] byArray2, int n) {
        Blake2bDigest blake2bDigest = new Blake2bDigest(512);
        int[] nArray = new int[]{this.parameters.getLanes(), n, this.parameters.getMemory(), this.parameters.getIterations(), this.parameters.getVersion(), this.parameters.getType()};
        Pack.intToLittleEndian(nArray, byArray, 0);
        blake2bDigest.update(byArray, 0, nArray.length * 4);
        Argon2BytesGenerator.addByteString(byArray, blake2bDigest, byArray2);
        Argon2BytesGenerator.addByteString(byArray, blake2bDigest, this.parameters.getSalt());
        Argon2BytesGenerator.addByteString(byArray, blake2bDigest, this.parameters.getSecret());
        Argon2BytesGenerator.addByteString(byArray, blake2bDigest, this.parameters.getAdditional());
        byte[] byArray3 = new byte[72];
        blake2bDigest.doFinal(byArray3, 0);
        this.fillFirstBlocks(byArray, byArray3);
    }

    private static void addByteString(byte[] byArray, Digest digest, byte[] byArray2) {
        if (null == byArray2) {
            digest.update(ZERO_BYTES, 0, 4);
            return;
        }
        Pack.intToLittleEndian(byArray2.length, byArray, 0);
        digest.update(byArray, 0, 4);
        digest.update(byArray2, 0, byArray2.length);
    }

    private void fillFirstBlocks(byte[] byArray, byte[] byArray2) {
        byte[] byArray3 = new byte[72];
        System.arraycopy(byArray2, 0, byArray3, 0, 64);
        byArray3[64] = 1;
        for (int i = 0; i < this.parameters.getLanes(); ++i) {
            Pack.intToLittleEndian(i, byArray2, 68);
            Pack.intToLittleEndian(i, byArray3, 68);
            this.hash(byArray2, byArray, 0, 1024);
            this.memory[i * this.laneLength + 0].fromBytes(byArray);
            this.hash(byArray3, byArray, 0, 1024);
            this.memory[i * this.laneLength + 1].fromBytes(byArray);
        }
    }

    private long intToLong(int n) {
        return (long)n & 0xFFFFFFFFL;
    }

    private static class Block {
        private static final int SIZE = 128;
        private final long[] v = new long[128];

        private Block() {
        }

        void fromBytes(byte[] byArray) {
            if (byArray.length < 1024) {
                throw new IllegalArgumentException("input shorter than blocksize");
            }
            Pack.littleEndianToLong(byArray, 0, this.v);
        }

        void toBytes(byte[] byArray) {
            if (byArray.length < 1024) {
                throw new IllegalArgumentException("output shorter than blocksize");
            }
            Pack.longToLittleEndian(this.v, byArray, 0);
        }

        private void copyBlock(Block block) {
            System.arraycopy(block.v, 0, this.v, 0, 128);
        }

        private void xor(Block block, Block block2) {
            long[] lArray = this.v;
            long[] lArray2 = block.v;
            long[] lArray3 = block2.v;
            for (int i = 0; i < 128; ++i) {
                lArray[i] = lArray2[i] ^ lArray3[i];
            }
        }

        private void xorWith(Block block) {
            long[] lArray = this.v;
            long[] lArray2 = block.v;
            for (int i = 0; i < 128; ++i) {
                int n = i;
                lArray[n] = lArray[n] ^ lArray2[i];
            }
        }

        private void xorWith(Block block, Block block2) {
            long[] lArray = this.v;
            long[] lArray2 = block.v;
            long[] lArray3 = block2.v;
            for (int i = 0; i < 128; ++i) {
                int n = i;
                lArray[n] = lArray[n] ^ (lArray2[i] ^ lArray3[i]);
            }
        }

        public Block clear() {
            Arrays.fill(this.v, 0L);
            return this;
        }
    }

    private static class FillBlock {
        Block R = new Block();
        Block Z = new Block();
        Block addressBlock = new Block();
        Block inputBlock = new Block();

        private FillBlock() {
        }

        private void applyBlake() {
            int n;
            int n2;
            for (n2 = 0; n2 < 8; ++n2) {
                n = 16 * n2;
                Argon2BytesGenerator.roundFunction(this.Z, n, n + 1, n + 2, n + 3, n + 4, n + 5, n + 6, n + 7, n + 8, n + 9, n + 10, n + 11, n + 12, n + 13, n + 14, n + 15);
            }
            for (n2 = 0; n2 < 8; ++n2) {
                n = 2 * n2;
                Argon2BytesGenerator.roundFunction(this.Z, n, n + 1, n + 16, n + 17, n + 32, n + 33, n + 48, n + 49, n + 64, n + 65, n + 80, n + 81, n + 96, n + 97, n + 112, n + 113);
            }
        }

        private void fillBlock(Block block, Block block2) {
            this.Z.copyBlock(block);
            this.applyBlake();
            block2.xor(block, this.Z);
        }

        private void fillBlock(Block block, Block block2, Block block3) {
            this.R.xor(block, block2);
            this.Z.copyBlock(this.R);
            this.applyBlake();
            block3.xor(this.R, this.Z);
        }

        private void fillBlockWithXor(Block block, Block block2, Block block3) {
            this.R.xor(block, block2);
            this.Z.copyBlock(this.R);
            this.applyBlake();
            block3.xorWith(this.R, this.Z);
        }
    }

    private static class Position {
        int pass;
        int lane;
        int slice;

        Position() {
        }
    }
}


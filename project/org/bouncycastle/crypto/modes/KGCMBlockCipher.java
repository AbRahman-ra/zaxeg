/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.KCTRBlockCipher;
import org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier;
import org.bouncycastle.crypto.modes.kgcm.Tables16kKGCMMultiplier_512;
import org.bouncycastle.crypto.modes.kgcm.Tables4kKGCMMultiplier_128;
import org.bouncycastle.crypto.modes.kgcm.Tables8kKGCMMultiplier_256;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class KGCMBlockCipher
implements AEADBlockCipher {
    private static final int MIN_MAC_BITS = 64;
    private BlockCipher engine;
    private BufferedBlockCipher ctrEngine;
    private int macSize;
    private boolean forEncryption;
    private byte[] initialAssociatedText;
    private byte[] macBlock;
    private byte[] iv;
    private KGCMMultiplier multiplier;
    private long[] b;
    private final int blockSize;
    private ExposedByteArrayOutputStream associatedText = new ExposedByteArrayOutputStream();
    private ExposedByteArrayOutputStream data = new ExposedByteArrayOutputStream();

    private static KGCMMultiplier createDefaultMultiplier(int n) {
        switch (n) {
            case 16: {
                return new Tables4kKGCMMultiplier_128();
            }
            case 32: {
                return new Tables8kKGCMMultiplier_256();
            }
            case 64: {
                return new Tables16kKGCMMultiplier_512();
            }
        }
        throw new IllegalArgumentException("Only 128, 256, and 512 -bit block sizes supported");
    }

    public KGCMBlockCipher(BlockCipher blockCipher) {
        this.engine = blockCipher;
        this.ctrEngine = new BufferedBlockCipher(new KCTRBlockCipher(this.engine));
        this.macSize = -1;
        this.blockSize = this.engine.getBlockSize();
        this.initialAssociatedText = new byte[this.blockSize];
        this.iv = new byte[this.blockSize];
        this.multiplier = KGCMBlockCipher.createDefaultMultiplier(this.blockSize);
        this.b = new long[this.blockSize >>> 3];
        this.macBlock = null;
    }

    public void init(boolean bl, CipherParameters cipherParameters) throws IllegalArgumentException {
        KeyParameter keyParameter;
        this.forEncryption = bl;
        if (cipherParameters instanceof AEADParameters) {
            AEADParameters aEADParameters = (AEADParameters)cipherParameters;
            byte[] byArray = aEADParameters.getNonce();
            int n = this.iv.length - byArray.length;
            Arrays.fill(this.iv, (byte)0);
            System.arraycopy(byArray, 0, this.iv, n, byArray.length);
            this.initialAssociatedText = aEADParameters.getAssociatedText();
            int n2 = aEADParameters.getMacSize();
            if (n2 < 64 || n2 > this.blockSize << 3 || (n2 & 7) != 0) {
                throw new IllegalArgumentException("Invalid value for MAC size: " + n2);
            }
            this.macSize = n2 >>> 3;
            keyParameter = aEADParameters.getKey();
            if (this.initialAssociatedText != null) {
                this.processAADBytes(this.initialAssociatedText, 0, this.initialAssociatedText.length);
            }
        } else if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV)cipherParameters;
            byte[] byArray = parametersWithIV.getIV();
            int n = this.iv.length - byArray.length;
            Arrays.fill(this.iv, (byte)0);
            System.arraycopy(byArray, 0, this.iv, n, byArray.length);
            this.initialAssociatedText = null;
            this.macSize = this.blockSize;
            keyParameter = (KeyParameter)parametersWithIV.getParameters();
        } else {
            throw new IllegalArgumentException("Invalid parameter passed");
        }
        this.macBlock = new byte[this.blockSize];
        this.ctrEngine.init(true, new ParametersWithIV(keyParameter, this.iv));
        this.engine.init(true, keyParameter);
    }

    public String getAlgorithmName() {
        return this.engine.getAlgorithmName() + "/KGCM";
    }

    public BlockCipher getUnderlyingCipher() {
        return this.engine;
    }

    public void processAADByte(byte by) {
        this.associatedText.write(by);
    }

    public void processAADBytes(byte[] byArray, int n, int n2) {
        this.associatedText.write(byArray, n, n2);
    }

    private void processAAD(byte[] byArray, int n, int n2) {
        int n3 = n + n2;
        for (int i = n; i < n3; i += this.blockSize) {
            KGCMBlockCipher.xorWithInput(this.b, byArray, i);
            this.multiplier.multiplyH(this.b);
        }
    }

    public int processByte(byte by, byte[] byArray, int n) throws DataLengthException, IllegalStateException {
        this.data.write(by);
        return 0;
    }

    public int processBytes(byte[] byArray, int n, int n2, byte[] byArray2, int n3) throws DataLengthException, IllegalStateException {
        if (byArray.length < n + n2) {
            throw new DataLengthException("input buffer too short");
        }
        this.data.write(byArray, n, n2);
        return 0;
    }

    public int doFinal(byte[] byArray, int n) throws IllegalStateException, InvalidCipherTextException {
        int n2;
        int n3 = this.data.size();
        if (!this.forEncryption && n3 < this.macSize) {
            throw new InvalidCipherTextException("data too short");
        }
        byte[] byArray2 = new byte[this.blockSize];
        this.engine.processBlock(byArray2, 0, byArray2, 0);
        long[] lArray = new long[this.blockSize >>> 3];
        Pack.littleEndianToLong(byArray2, 0, lArray);
        this.multiplier.init(lArray);
        Arrays.fill(byArray2, (byte)0);
        Arrays.fill(lArray, 0L);
        int n4 = this.associatedText.size();
        if (n4 > 0) {
            this.processAAD(this.associatedText.getBuffer(), 0, n4);
        }
        if (this.forEncryption) {
            if (byArray.length - n - this.macSize < n3) {
                throw new OutputLengthException("Output buffer too short");
            }
            n2 = this.ctrEngine.processBytes(this.data.getBuffer(), 0, n3, byArray, n);
            n2 += this.ctrEngine.doFinal(byArray, n + n2);
            this.calculateMac(byArray, n, n3, n4);
        } else {
            int n5 = n3 - this.macSize;
            if (byArray.length - n < n5) {
                throw new OutputLengthException("Output buffer too short");
            }
            this.calculateMac(this.data.getBuffer(), 0, n5, n4);
            n2 = this.ctrEngine.processBytes(this.data.getBuffer(), 0, n5, byArray, n);
            n2 += this.ctrEngine.doFinal(byArray, n + n2);
        }
        if (this.macBlock == null) {
            throw new IllegalStateException("mac is not calculated");
        }
        if (this.forEncryption) {
            System.arraycopy(this.macBlock, 0, byArray, n + n2, this.macSize);
            this.reset();
            return n2 + this.macSize;
        }
        byte[] byArray3 = new byte[this.macSize];
        System.arraycopy(this.data.getBuffer(), n3 - this.macSize, byArray3, 0, this.macSize);
        byte[] byArray4 = new byte[this.macSize];
        System.arraycopy(this.macBlock, 0, byArray4, 0, this.macSize);
        if (!Arrays.constantTimeAreEqual(byArray3, byArray4)) {
            throw new InvalidCipherTextException("mac verification failed");
        }
        this.reset();
        return n2;
    }

    public byte[] getMac() {
        byte[] byArray = new byte[this.macSize];
        System.arraycopy(this.macBlock, 0, byArray, 0, this.macSize);
        return byArray;
    }

    public int getUpdateOutputSize(int n) {
        return 0;
    }

    public int getOutputSize(int n) {
        int n2 = n + this.data.size();
        if (this.forEncryption) {
            return n2 + this.macSize;
        }
        return n2 < this.macSize ? 0 : n2 - this.macSize;
    }

    public void reset() {
        Arrays.fill(this.b, 0L);
        this.engine.reset();
        this.data.reset();
        this.associatedText.reset();
        if (this.initialAssociatedText != null) {
            this.processAADBytes(this.initialAssociatedText, 0, this.initialAssociatedText.length);
        }
    }

    private void calculateMac(byte[] byArray, int n, int n2, int n3) {
        int n4 = n + n2;
        for (int i = n; i < n4; i += this.blockSize) {
            KGCMBlockCipher.xorWithInput(this.b, byArray, i);
            this.multiplier.multiplyH(this.b);
        }
        long l = ((long)n3 & 0xFFFFFFFFL) << 3;
        long l2 = ((long)n2 & 0xFFFFFFFFL) << 3;
        this.b[0] = this.b[0] ^ l;
        int n5 = this.blockSize >>> 4;
        this.b[n5] = this.b[n5] ^ l2;
        this.macBlock = Pack.longToLittleEndian(this.b);
        this.engine.processBlock(this.macBlock, 0, this.macBlock, 0);
    }

    private static void xorWithInput(long[] lArray, byte[] byArray, int n) {
        int n2 = 0;
        while (n2 < lArray.length) {
            int n3 = n2++;
            lArray[n3] = lArray[n3] ^ Pack.littleEndianToLong(byArray, n);
            n += 8;
        }
    }

    private class ExposedByteArrayOutputStream
    extends ByteArrayOutputStream {
        public byte[] getBuffer() {
            return this.buf;
        }
    }
}


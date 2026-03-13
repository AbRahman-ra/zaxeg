/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.modes.GOST3413CipherUtil;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class G3413CTRBlockCipher
extends StreamBlockCipher {
    private final int s;
    private byte[] CTR;
    private byte[] IV;
    private byte[] buf;
    private final int blockSize;
    private final BlockCipher cipher;
    private int byteCount = 0;
    private boolean initialized;

    public G3413CTRBlockCipher(BlockCipher blockCipher) {
        this(blockCipher, blockCipher.getBlockSize() * 8);
    }

    public G3413CTRBlockCipher(BlockCipher blockCipher, int n) {
        super(blockCipher);
        if (n < 0 || n > blockCipher.getBlockSize() * 8) {
            throw new IllegalArgumentException("Parameter bitBlockSize must be in range 0 < bitBlockSize <= " + blockCipher.getBlockSize() * 8);
        }
        this.cipher = blockCipher;
        this.blockSize = blockCipher.getBlockSize();
        this.s = n / 8;
        this.CTR = new byte[this.blockSize];
    }

    public void init(boolean bl, CipherParameters cipherParameters) throws IllegalArgumentException {
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV)cipherParameters;
            this.initArrays();
            this.IV = Arrays.clone(parametersWithIV.getIV());
            if (this.IV.length != this.blockSize / 2) {
                throw new IllegalArgumentException("Parameter IV length must be == blockSize/2");
            }
            System.arraycopy(this.IV, 0, this.CTR, 0, this.IV.length);
            for (int i = this.IV.length; i < this.blockSize; ++i) {
                this.CTR[i] = 0;
            }
            if (parametersWithIV.getParameters() != null) {
                this.cipher.init(true, parametersWithIV.getParameters());
            }
        } else {
            this.initArrays();
            if (cipherParameters != null) {
                this.cipher.init(true, cipherParameters);
            }
        }
        this.initialized = true;
    }

    private void initArrays() {
        this.IV = new byte[this.blockSize / 2];
        this.CTR = new byte[this.blockSize];
        this.buf = new byte[this.s];
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/GCTR";
    }

    public int getBlockSize() {
        return this.s;
    }

    public int processBlock(byte[] byArray, int n, byte[] byArray2, int n2) throws DataLengthException, IllegalStateException {
        this.processBytes(byArray, n, this.s, byArray2, n2);
        return this.s;
    }

    protected byte calculateByte(byte by) {
        if (this.byteCount == 0) {
            this.buf = this.generateBuf();
        }
        byte by2 = (byte)(this.buf[this.byteCount] ^ by);
        ++this.byteCount;
        if (this.byteCount == this.s) {
            this.byteCount = 0;
            this.generateCRT();
        }
        return by2;
    }

    private void generateCRT() {
        int n = this.CTR.length - 1;
        this.CTR[n] = (byte)(this.CTR[n] + 1);
    }

    private byte[] generateBuf() {
        byte[] byArray = new byte[this.CTR.length];
        this.cipher.processBlock(this.CTR, 0, byArray, 0);
        return GOST3413CipherUtil.MSB(byArray, this.s);
    }

    public void reset() {
        if (this.initialized) {
            System.arraycopy(this.IV, 0, this.CTR, 0, this.IV.length);
            for (int i = this.IV.length; i < this.blockSize; ++i) {
                this.CTR[i] = 0;
            }
            this.byteCount = 0;
            this.cipher.reset();
        }
    }
}


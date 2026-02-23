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

public class G3413CFBBlockCipher
extends StreamBlockCipher {
    private final int s;
    private int m;
    private int blockSize;
    private byte[] R;
    private byte[] R_init;
    private BlockCipher cipher;
    private boolean forEncryption;
    private boolean initialized = false;
    private byte[] gamma;
    private byte[] inBuf;
    private int byteCount;

    public G3413CFBBlockCipher(BlockCipher blockCipher) {
        this(blockCipher, blockCipher.getBlockSize() * 8);
    }

    public G3413CFBBlockCipher(BlockCipher blockCipher, int n) {
        super(blockCipher);
        if (n < 0 || n > blockCipher.getBlockSize() * 8) {
            throw new IllegalArgumentException("Parameter bitBlockSize must be in range 0 < bitBlockSize <= " + blockCipher.getBlockSize() * 8);
        }
        this.blockSize = blockCipher.getBlockSize();
        this.cipher = blockCipher;
        this.s = n / 8;
        this.inBuf = new byte[this.getBlockSize()];
    }

    public void init(boolean bl, CipherParameters cipherParameters) throws IllegalArgumentException {
        this.forEncryption = bl;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV)cipherParameters;
            byte[] byArray = parametersWithIV.getIV();
            if (byArray.length < this.blockSize) {
                throw new IllegalArgumentException("Parameter m must blockSize <= m");
            }
            this.m = byArray.length;
            this.initArrays();
            this.R_init = Arrays.clone(byArray);
            System.arraycopy(this.R_init, 0, this.R, 0, this.R_init.length);
            if (parametersWithIV.getParameters() != null) {
                this.cipher.init(true, parametersWithIV.getParameters());
            }
        } else {
            this.setupDefaultParams();
            this.initArrays();
            System.arraycopy(this.R_init, 0, this.R, 0, this.R_init.length);
            if (cipherParameters != null) {
                this.cipher.init(true, cipherParameters);
            }
        }
        this.initialized = true;
    }

    private void initArrays() {
        this.R = new byte[this.m];
        this.R_init = new byte[this.m];
    }

    private void setupDefaultParams() {
        this.m = 2 * this.blockSize;
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/CFB" + this.blockSize * 8;
    }

    public int getBlockSize() {
        return this.s;
    }

    public int processBlock(byte[] byArray, int n, byte[] byArray2, int n2) throws DataLengthException, IllegalStateException {
        this.processBytes(byArray, n, this.getBlockSize(), byArray2, n2);
        return this.getBlockSize();
    }

    protected byte calculateByte(byte by) {
        if (this.byteCount == 0) {
            this.gamma = this.createGamma();
        }
        byte by2 = (byte)(this.gamma[this.byteCount] ^ by);
        byte by3 = this.inBuf[this.byteCount++] = this.forEncryption ? by2 : by;
        if (this.byteCount == this.getBlockSize()) {
            this.byteCount = 0;
            this.generateR(this.inBuf);
        }
        return by2;
    }

    byte[] createGamma() {
        byte[] byArray = GOST3413CipherUtil.MSB(this.R, this.blockSize);
        byte[] byArray2 = new byte[byArray.length];
        this.cipher.processBlock(byArray, 0, byArray2, 0);
        return GOST3413CipherUtil.MSB(byArray2, this.s);
    }

    void generateR(byte[] byArray) {
        byte[] byArray2 = GOST3413CipherUtil.LSB(this.R, this.m - this.s);
        System.arraycopy(byArray2, 0, this.R, 0, byArray2.length);
        System.arraycopy(byArray, 0, this.R, byArray2.length, this.m - byArray2.length);
    }

    public void reset() {
        this.byteCount = 0;
        Arrays.clear(this.inBuf);
        Arrays.clear(this.gamma);
        if (this.initialized) {
            System.arraycopy(this.R_init, 0, this.R, 0, this.R_init.length);
            this.cipher.reset();
        }
    }
}


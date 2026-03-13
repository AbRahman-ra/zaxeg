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

public class G3413OFBBlockCipher
extends StreamBlockCipher {
    private int m;
    private int blockSize;
    private byte[] R;
    private byte[] R_init;
    private byte[] Y;
    private BlockCipher cipher;
    private int byteCount;
    private boolean initialized = false;

    public G3413OFBBlockCipher(BlockCipher blockCipher) {
        super(blockCipher);
        this.blockSize = blockCipher.getBlockSize();
        this.cipher = blockCipher;
        this.Y = new byte[this.blockSize];
    }

    public void init(boolean bl, CipherParameters cipherParameters) throws IllegalArgumentException {
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
        return this.cipher.getAlgorithmName() + "/OFB";
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int processBlock(byte[] byArray, int n, byte[] byArray2, int n2) throws DataLengthException, IllegalStateException {
        this.processBytes(byArray, n, this.blockSize, byArray2, n2);
        return this.blockSize;
    }

    protected byte calculateByte(byte by) {
        if (this.byteCount == 0) {
            this.generateY();
        }
        byte by2 = (byte)(this.Y[this.byteCount] ^ by);
        ++this.byteCount;
        if (this.byteCount == this.getBlockSize()) {
            this.byteCount = 0;
            this.generateR();
        }
        return by2;
    }

    private void generateY() {
        byte[] byArray = GOST3413CipherUtil.MSB(this.R, this.blockSize);
        this.cipher.processBlock(byArray, 0, this.Y, 0);
    }

    private void generateR() {
        byte[] byArray = GOST3413CipherUtil.LSB(this.R, this.m - this.blockSize);
        System.arraycopy(byArray, 0, this.R, 0, byArray.length);
        System.arraycopy(this.Y, 0, this.R, byArray.length, this.m - byArray.length);
    }

    public void reset() {
        if (this.initialized) {
            System.arraycopy(this.R_init, 0, this.R, 0, this.R_init.length);
            Arrays.clear(this.Y);
            this.byteCount = 0;
            this.cipher.reset();
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.modes.GOST3413CipherUtil;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class G3413CBCBlockCipher
implements BlockCipher {
    private int m;
    private int blockSize;
    private byte[] R;
    private byte[] R_init;
    private BlockCipher cipher;
    private boolean initialized = false;
    private boolean forEncryption;

    public G3413CBCBlockCipher(BlockCipher blockCipher) {
        this.blockSize = blockCipher.getBlockSize();
        this.cipher = blockCipher;
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
                this.cipher.init(bl, parametersWithIV.getParameters());
            }
        } else {
            this.setupDefaultParams();
            this.initArrays();
            System.arraycopy(this.R_init, 0, this.R, 0, this.R_init.length);
            if (cipherParameters != null) {
                this.cipher.init(bl, cipherParameters);
            }
        }
        this.initialized = true;
    }

    private void initArrays() {
        this.R = new byte[this.m];
        this.R_init = new byte[this.m];
    }

    private void setupDefaultParams() {
        this.m = this.blockSize;
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/CBC";
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int processBlock(byte[] byArray, int n, byte[] byArray2, int n2) throws DataLengthException, IllegalStateException {
        return this.forEncryption ? this.encrypt(byArray, n, byArray2, n2) : this.decrypt(byArray, n, byArray2, n2);
    }

    private int encrypt(byte[] byArray, int n, byte[] byArray2, int n2) {
        byte[] byArray3 = GOST3413CipherUtil.MSB(this.R, this.blockSize);
        byte[] byArray4 = GOST3413CipherUtil.copyFromInput(byArray, this.blockSize, n);
        byte[] byArray5 = GOST3413CipherUtil.sum(byArray4, byArray3);
        byte[] byArray6 = new byte[byArray5.length];
        this.cipher.processBlock(byArray5, 0, byArray6, 0);
        System.arraycopy(byArray6, 0, byArray2, n2, byArray6.length);
        if (byArray2.length > n2 + byArray5.length) {
            this.generateR(byArray6);
        }
        return byArray6.length;
    }

    private int decrypt(byte[] byArray, int n, byte[] byArray2, int n2) {
        byte[] byArray3 = GOST3413CipherUtil.MSB(this.R, this.blockSize);
        byte[] byArray4 = GOST3413CipherUtil.copyFromInput(byArray, this.blockSize, n);
        byte[] byArray5 = new byte[byArray4.length];
        this.cipher.processBlock(byArray4, 0, byArray5, 0);
        byte[] byArray6 = GOST3413CipherUtil.sum(byArray5, byArray3);
        System.arraycopy(byArray6, 0, byArray2, n2, byArray6.length);
        if (byArray2.length > n2 + byArray6.length) {
            this.generateR(byArray4);
        }
        return byArray6.length;
    }

    private void generateR(byte[] byArray) {
        byte[] byArray2 = GOST3413CipherUtil.LSB(this.R, this.m - this.blockSize);
        System.arraycopy(byArray2, 0, this.R, 0, byArray2.length);
        System.arraycopy(byArray, 0, this.R, byArray2.length, this.m - byArray2.length);
    }

    public void reset() {
        if (this.initialized) {
            System.arraycopy(this.R_init, 0, this.R, 0, this.R_init.length);
            this.cipher.reset();
        }
    }
}


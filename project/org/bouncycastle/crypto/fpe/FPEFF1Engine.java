/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.fpe;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.fpe.FPEEngine;
import org.bouncycastle.crypto.fpe.SP80038G;
import org.bouncycastle.crypto.params.FPEParameters;
import org.bouncycastle.util.Properties;

public class FPEFF1Engine
extends FPEEngine {
    public FPEFF1Engine() {
        this(new AESEngine());
    }

    public FPEFF1Engine(BlockCipher blockCipher) {
        super(blockCipher);
        if (blockCipher.getBlockSize() != 16) {
            throw new IllegalArgumentException("base cipher needs to be 128 bits");
        }
        if (Properties.isOverrideSet("org.bouncycastle.fpe.disable") || Properties.isOverrideSet("org.bouncycastle.fpe.disable_ff1")) {
            throw new UnsupportedOperationException("FF1 encryption disabled");
        }
    }

    public void init(boolean bl, CipherParameters cipherParameters) {
        this.forEncryption = bl;
        this.fpeParameters = (FPEParameters)cipherParameters;
        this.baseCipher.init(!this.fpeParameters.isUsingInverseFunction(), this.fpeParameters.getKey());
    }

    public String getAlgorithmName() {
        return "FF1";
    }

    protected int encryptBlock(byte[] byArray, int n, int n2, byte[] byArray2, int n3) {
        byte[] byArray3 = this.fpeParameters.getRadix() > 256 ? FPEFF1Engine.toByteArray(SP80038G.encryptFF1w(this.baseCipher, this.fpeParameters.getRadix(), this.fpeParameters.getTweak(), FPEFF1Engine.toShortArray(byArray), n, n2 / 2)) : SP80038G.encryptFF1(this.baseCipher, this.fpeParameters.getRadix(), this.fpeParameters.getTweak(), byArray, n, n2);
        System.arraycopy(byArray3, 0, byArray2, n3, n2);
        return n2;
    }

    protected int decryptBlock(byte[] byArray, int n, int n2, byte[] byArray2, int n3) {
        byte[] byArray3 = this.fpeParameters.getRadix() > 256 ? FPEFF1Engine.toByteArray(SP80038G.decryptFF1w(this.baseCipher, this.fpeParameters.getRadix(), this.fpeParameters.getTweak(), FPEFF1Engine.toShortArray(byArray), n, n2 / 2)) : SP80038G.decryptFF1(this.baseCipher, this.fpeParameters.getRadix(), this.fpeParameters.getTweak(), byArray, n, n2);
        System.arraycopy(byArray3, 0, byArray2, n3, n2);
        return n2;
    }
}


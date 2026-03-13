/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.agreement.DHBasicAgreement;
import org.bouncycastle.crypto.params.DHUPrivateParameters;
import org.bouncycastle.crypto.params.DHUPublicParameters;
import org.bouncycastle.util.BigIntegers;

public class DHUnifiedAgreement {
    private DHUPrivateParameters privParams;

    public void init(CipherParameters cipherParameters) {
        this.privParams = (DHUPrivateParameters)cipherParameters;
    }

    public int getFieldSize() {
        return (this.privParams.getStaticPrivateKey().getParameters().getP().bitLength() + 7) / 8;
    }

    public byte[] calculateAgreement(CipherParameters cipherParameters) {
        DHUPublicParameters dHUPublicParameters = (DHUPublicParameters)cipherParameters;
        DHBasicAgreement dHBasicAgreement = new DHBasicAgreement();
        DHBasicAgreement dHBasicAgreement2 = new DHBasicAgreement();
        dHBasicAgreement.init(this.privParams.getStaticPrivateKey());
        BigInteger bigInteger = dHBasicAgreement.calculateAgreement(dHUPublicParameters.getStaticPublicKey());
        dHBasicAgreement2.init(this.privParams.getEphemeralPrivateKey());
        BigInteger bigInteger2 = dHBasicAgreement2.calculateAgreement(dHUPublicParameters.getEphemeralPublicKey());
        int n = this.getFieldSize();
        byte[] byArray = new byte[n * 2];
        BigIntegers.asUnsignedByteArray(bigInteger2, byArray, 0, n);
        BigIntegers.asUnsignedByteArray(bigInteger, byArray, n, n);
        return byArray;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.LMS;
import org.bouncycastle.pqc.crypto.lms.LMSKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;

public class LMSKeyPairGenerator
implements AsymmetricCipherKeyPairGenerator {
    LMSKeyGenerationParameters param;

    public void init(KeyGenerationParameters keyGenerationParameters) {
        this.param = (LMSKeyGenerationParameters)keyGenerationParameters;
    }

    public AsymmetricCipherKeyPair generateKeyPair() {
        SecureRandom secureRandom = this.param.getRandom();
        byte[] byArray = new byte[16];
        secureRandom.nextBytes(byArray);
        byte[] byArray2 = new byte[32];
        secureRandom.nextBytes(byArray2);
        LMSPrivateKeyParameters lMSPrivateKeyParameters = LMS.generateKeys(this.param.getParameters().getLMSigParam(), this.param.getParameters().getLMOTSParam(), 0, byArray, byArray2);
        return new AsymmetricCipherKeyPair(lMSPrivateKeyParameters.getPublicKey(), lMSPrivateKeyParameters);
    }
}


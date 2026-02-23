/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.HSS;
import org.bouncycastle.pqc.crypto.lms.HSSKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;

public class HSSKeyPairGenerator
implements AsymmetricCipherKeyPairGenerator {
    HSSKeyGenerationParameters param;

    public void init(KeyGenerationParameters keyGenerationParameters) {
        this.param = (HSSKeyGenerationParameters)keyGenerationParameters;
    }

    public AsymmetricCipherKeyPair generateKeyPair() {
        HSSPrivateKeyParameters hSSPrivateKeyParameters = HSS.generateHSSKeyPair(this.param);
        return new AsymmetricCipherKeyPair(hSSPrivateKeyParameters.getPublicKey(), hSSPrivateKeyParameters);
    }
}


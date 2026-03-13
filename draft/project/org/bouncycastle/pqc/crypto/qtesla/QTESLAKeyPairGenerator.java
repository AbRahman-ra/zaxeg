/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.qtesla;

import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPublicKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLASecurityCategory;
import org.bouncycastle.pqc.crypto.qtesla.QTesla1p;
import org.bouncycastle.pqc.crypto.qtesla.QTesla3p;

public final class QTESLAKeyPairGenerator
implements AsymmetricCipherKeyPairGenerator {
    private int securityCategory;
    private SecureRandom secureRandom;

    public void init(KeyGenerationParameters keyGenerationParameters) {
        QTESLAKeyGenerationParameters qTESLAKeyGenerationParameters = (QTESLAKeyGenerationParameters)keyGenerationParameters;
        this.secureRandom = qTESLAKeyGenerationParameters.getRandom();
        this.securityCategory = qTESLAKeyGenerationParameters.getSecurityCategory();
    }

    public AsymmetricCipherKeyPair generateKeyPair() {
        byte[] byArray = this.allocatePrivate(this.securityCategory);
        byte[] byArray2 = this.allocatePublic(this.securityCategory);
        switch (this.securityCategory) {
            case 5: {
                QTesla1p.generateKeyPair(byArray2, byArray, this.secureRandom);
                break;
            }
            case 6: {
                QTesla3p.generateKeyPair(byArray2, byArray, this.secureRandom);
                break;
            }
            default: {
                throw new IllegalArgumentException("unknown security category: " + this.securityCategory);
            }
        }
        return new AsymmetricCipherKeyPair(new QTESLAPublicKeyParameters(this.securityCategory, byArray2), new QTESLAPrivateKeyParameters(this.securityCategory, byArray));
    }

    private byte[] allocatePrivate(int n) {
        return new byte[QTESLASecurityCategory.getPrivateSize(n)];
    }

    private byte[] allocatePublic(int n) {
        return new byte[QTESLASecurityCategory.getPublicSize(n)];
    }
}


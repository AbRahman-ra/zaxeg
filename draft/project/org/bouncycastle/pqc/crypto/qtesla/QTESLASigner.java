/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.qtesla;

import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.MessageSigner;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPublicKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLASecurityCategory;
import org.bouncycastle.pqc.crypto.qtesla.QTesla1p;
import org.bouncycastle.pqc.crypto.qtesla.QTesla3p;

public class QTESLASigner
implements MessageSigner {
    private QTESLAPublicKeyParameters publicKey;
    private QTESLAPrivateKeyParameters privateKey;
    private SecureRandom secureRandom;

    public void init(boolean bl, CipherParameters cipherParameters) {
        if (bl) {
            if (cipherParameters instanceof ParametersWithRandom) {
                this.secureRandom = ((ParametersWithRandom)cipherParameters).getRandom();
                this.privateKey = (QTESLAPrivateKeyParameters)((ParametersWithRandom)cipherParameters).getParameters();
            } else {
                this.secureRandom = CryptoServicesRegistrar.getSecureRandom();
                this.privateKey = (QTESLAPrivateKeyParameters)cipherParameters;
            }
            this.publicKey = null;
            QTESLASecurityCategory.validate(this.privateKey.getSecurityCategory());
        } else {
            this.privateKey = null;
            this.publicKey = (QTESLAPublicKeyParameters)cipherParameters;
            QTESLASecurityCategory.validate(this.publicKey.getSecurityCategory());
        }
    }

    public byte[] generateSignature(byte[] byArray) {
        byte[] byArray2 = new byte[QTESLASecurityCategory.getSignatureSize(this.privateKey.getSecurityCategory())];
        switch (this.privateKey.getSecurityCategory()) {
            case 5: {
                QTesla1p.generateSignature(byArray2, byArray, 0, byArray.length, this.privateKey.getSecret(), this.secureRandom);
                break;
            }
            case 6: {
                QTesla3p.generateSignature(byArray2, byArray, 0, byArray.length, this.privateKey.getSecret(), this.secureRandom);
                break;
            }
            default: {
                throw new IllegalArgumentException("unknown security category: " + this.privateKey.getSecurityCategory());
            }
        }
        return byArray2;
    }

    public boolean verifySignature(byte[] byArray, byte[] byArray2) {
        int n;
        switch (this.publicKey.getSecurityCategory()) {
            case 5: {
                n = QTesla1p.verifying(byArray, byArray2, 0, byArray2.length, this.publicKey.getPublicData());
                break;
            }
            case 6: {
                n = QTesla3p.verifying(byArray, byArray2, 0, byArray2.length, this.publicKey.getPublicData());
                break;
            }
            default: {
                throw new IllegalArgumentException("unknown security category: " + this.publicKey.getSecurityCategory());
            }
        }
        return 0 == n;
    }
}


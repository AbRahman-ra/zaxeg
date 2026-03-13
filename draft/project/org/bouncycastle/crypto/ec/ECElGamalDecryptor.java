/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.ec;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.ec.ECDecryptor;
import org.bouncycastle.crypto.ec.ECPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

public class ECElGamalDecryptor
implements ECDecryptor {
    private ECPrivateKeyParameters key;

    public void init(CipherParameters cipherParameters) {
        if (!(cipherParameters instanceof ECPrivateKeyParameters)) {
            throw new IllegalArgumentException("ECPrivateKeyParameters are required for decryption.");
        }
        this.key = (ECPrivateKeyParameters)cipherParameters;
    }

    public ECPoint decrypt(ECPair eCPair) {
        if (this.key == null) {
            throw new IllegalStateException("ECElGamalDecryptor not initialised");
        }
        ECCurve eCCurve = this.key.getParameters().getCurve();
        ECPoint eCPoint = ECAlgorithms.cleanPoint(eCCurve, eCPair.getX()).multiply(this.key.getD());
        return ECAlgorithms.cleanPoint(eCCurve, eCPair.getY()).subtract(eCPoint).normalize();
    }
}


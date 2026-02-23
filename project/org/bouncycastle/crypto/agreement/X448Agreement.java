/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.RawAgreement;
import org.bouncycastle.crypto.params.X448PrivateKeyParameters;
import org.bouncycastle.crypto.params.X448PublicKeyParameters;

public final class X448Agreement
implements RawAgreement {
    private X448PrivateKeyParameters privateKey;

    public void init(CipherParameters cipherParameters) {
        this.privateKey = (X448PrivateKeyParameters)cipherParameters;
    }

    public int getAgreementSize() {
        return 56;
    }

    public void calculateAgreement(CipherParameters cipherParameters, byte[] byArray, int n) {
        this.privateKey.generateSecret((X448PublicKeyParameters)cipherParameters, byArray, n);
    }
}


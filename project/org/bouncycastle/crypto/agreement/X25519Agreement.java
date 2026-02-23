/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.RawAgreement;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

public final class X25519Agreement
implements RawAgreement {
    private X25519PrivateKeyParameters privateKey;

    public void init(CipherParameters cipherParameters) {
        this.privateKey = (X25519PrivateKeyParameters)cipherParameters;
    }

    public int getAgreementSize() {
        return 32;
    }

    public void calculateAgreement(CipherParameters cipherParameters, byte[] byArray, int n) {
        this.privateKey.generateSecret((X25519PublicKeyParameters)cipherParameters, byArray, n);
    }
}


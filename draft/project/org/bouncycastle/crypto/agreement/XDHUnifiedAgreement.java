/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.RawAgreement;
import org.bouncycastle.crypto.params.XDHUPrivateParameters;
import org.bouncycastle.crypto.params.XDHUPublicParameters;

public class XDHUnifiedAgreement
implements RawAgreement {
    private final RawAgreement xAgreement;
    private XDHUPrivateParameters privParams;

    public XDHUnifiedAgreement(RawAgreement rawAgreement) {
        this.xAgreement = rawAgreement;
    }

    public void init(CipherParameters cipherParameters) {
        this.privParams = (XDHUPrivateParameters)cipherParameters;
    }

    public int getAgreementSize() {
        return this.xAgreement.getAgreementSize() * 2;
    }

    public void calculateAgreement(CipherParameters cipherParameters, byte[] byArray, int n) {
        XDHUPublicParameters xDHUPublicParameters = (XDHUPublicParameters)cipherParameters;
        this.xAgreement.init(this.privParams.getEphemeralPrivateKey());
        this.xAgreement.calculateAgreement(xDHUPublicParameters.getEphemeralPublicKey(), byArray, n);
        this.xAgreement.init(this.privParams.getStaticPrivateKey());
        this.xAgreement.calculateAgreement(xDHUPublicParameters.getStaticPublicKey(), byArray, n + this.xAgreement.getAgreementSize());
    }
}


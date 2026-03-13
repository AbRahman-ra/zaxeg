/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StagedAgreement;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;

public class ECDHCStagedAgreement
implements StagedAgreement {
    ECPrivateKeyParameters key;

    public void init(CipherParameters cipherParameters) {
        this.key = (ECPrivateKeyParameters)cipherParameters;
    }

    public int getFieldSize() {
        return (this.key.getParameters().getCurve().getFieldSize() + 7) / 8;
    }

    public AsymmetricKeyParameter calculateStage(CipherParameters cipherParameters) {
        ECPoint eCPoint = this.calculateNextPoint((ECPublicKeyParameters)cipherParameters);
        return new ECPublicKeyParameters(eCPoint, this.key.getParameters());
    }

    public BigInteger calculateAgreement(CipherParameters cipherParameters) {
        ECPoint eCPoint = this.calculateNextPoint((ECPublicKeyParameters)cipherParameters);
        return eCPoint.getAffineXCoord().toBigInteger();
    }

    private ECPoint calculateNextPoint(ECPublicKeyParameters eCPublicKeyParameters) {
        ECPublicKeyParameters eCPublicKeyParameters2 = eCPublicKeyParameters;
        ECDomainParameters eCDomainParameters = this.key.getParameters();
        if (!eCDomainParameters.equals(eCPublicKeyParameters2.getParameters())) {
            throw new IllegalStateException("ECDHC public key has wrong domain parameters");
        }
        BigInteger bigInteger = eCDomainParameters.getH().multiply(this.key.getD()).mod(eCDomainParameters.getN());
        ECPoint eCPoint = ECAlgorithms.cleanPoint(eCDomainParameters.getCurve(), eCPublicKeyParameters2.getQ());
        if (eCPoint.isInfinity()) {
            throw new IllegalStateException("Infinity is not a valid public key for ECDHC");
        }
        ECPoint eCPoint2 = eCPoint.multiply(bigInteger).normalize();
        if (eCPoint2.isInfinity()) {
            throw new IllegalStateException("Infinity is not a valid agreement value for ECDHC");
        }
        return eCPoint2;
    }
}


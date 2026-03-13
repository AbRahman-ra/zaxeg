/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement;

import java.math.BigInteger;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECPoint;

public class ECDHBasicAgreement
implements BasicAgreement {
    private ECPrivateKeyParameters key;

    public void init(CipherParameters cipherParameters) {
        this.key = (ECPrivateKeyParameters)cipherParameters;
    }

    public int getFieldSize() {
        return (this.key.getParameters().getCurve().getFieldSize() + 7) / 8;
    }

    public BigInteger calculateAgreement(CipherParameters cipherParameters) {
        ECPoint eCPoint;
        ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters)cipherParameters;
        ECDomainParameters eCDomainParameters = this.key.getParameters();
        if (!eCDomainParameters.equals(eCPublicKeyParameters.getParameters())) {
            throw new IllegalStateException("ECDH public key has wrong domain parameters");
        }
        BigInteger bigInteger = this.key.getD();
        ECPoint eCPoint2 = ECAlgorithms.cleanPoint(eCDomainParameters.getCurve(), eCPublicKeyParameters.getQ());
        if (eCPoint2.isInfinity()) {
            throw new IllegalStateException("Infinity is not a valid public key for ECDH");
        }
        BigInteger bigInteger2 = eCDomainParameters.getH();
        if (!bigInteger2.equals(ECConstants.ONE)) {
            bigInteger = eCDomainParameters.getHInv().multiply(bigInteger).mod(eCDomainParameters.getN());
            eCPoint2 = ECAlgorithms.referenceMultiply(eCPoint2, bigInteger2);
        }
        if ((eCPoint = eCPoint2.multiply(bigInteger).normalize()).isInfinity()) {
            throw new IllegalStateException("Infinity is not a valid agreement value for ECDH");
        }
        return eCPoint.getAffineXCoord().toBigInteger();
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement;

import java.math.BigInteger;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.DHMQVPrivateParameters;
import org.bouncycastle.crypto.params.DHMQVPublicParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;

public class MQVBasicAgreement
implements BasicAgreement {
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    DHMQVPrivateParameters privParams;

    public void init(CipherParameters cipherParameters) {
        this.privParams = (DHMQVPrivateParameters)cipherParameters;
    }

    public int getFieldSize() {
        return (this.privParams.getStaticPrivateKey().getParameters().getP().bitLength() + 7) / 8;
    }

    public BigInteger calculateAgreement(CipherParameters cipherParameters) {
        DHMQVPublicParameters dHMQVPublicParameters = (DHMQVPublicParameters)cipherParameters;
        DHPrivateKeyParameters dHPrivateKeyParameters = this.privParams.getStaticPrivateKey();
        if (!this.privParams.getStaticPrivateKey().getParameters().equals(dHMQVPublicParameters.getStaticPublicKey().getParameters())) {
            throw new IllegalStateException("MQV public key components have wrong domain parameters");
        }
        if (this.privParams.getStaticPrivateKey().getParameters().getQ() == null) {
            throw new IllegalStateException("MQV key domain parameters do not have Q set");
        }
        BigInteger bigInteger = this.calculateDHMQVAgreement(dHPrivateKeyParameters.getParameters(), dHPrivateKeyParameters, dHMQVPublicParameters.getStaticPublicKey(), this.privParams.getEphemeralPrivateKey(), this.privParams.getEphemeralPublicKey(), dHMQVPublicParameters.getEphemeralPublicKey());
        if (bigInteger.equals(ONE)) {
            throw new IllegalStateException("1 is not a valid agreement value for MQV");
        }
        return bigInteger;
    }

    private BigInteger calculateDHMQVAgreement(DHParameters dHParameters, DHPrivateKeyParameters dHPrivateKeyParameters, DHPublicKeyParameters dHPublicKeyParameters, DHPrivateKeyParameters dHPrivateKeyParameters2, DHPublicKeyParameters dHPublicKeyParameters2, DHPublicKeyParameters dHPublicKeyParameters3) {
        BigInteger bigInteger = dHParameters.getQ();
        int n = (bigInteger.bitLength() + 1) / 2;
        BigInteger bigInteger2 = BigInteger.valueOf(2L).pow(n);
        BigInteger bigInteger3 = dHPublicKeyParameters2.getY().mod(bigInteger2).add(bigInteger2);
        BigInteger bigInteger4 = dHPrivateKeyParameters2.getX().add(bigInteger3.multiply(dHPrivateKeyParameters.getX())).mod(bigInteger);
        BigInteger bigInteger5 = dHPublicKeyParameters3.getY().mod(bigInteger2).add(bigInteger2);
        BigInteger bigInteger6 = dHPublicKeyParameters3.getY().multiply(dHPublicKeyParameters.getY().modPow(bigInteger5, dHParameters.getP())).modPow(bigInteger4, dHParameters.getP());
        return bigInteger6;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.DSAExt;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

public class ECNRSigner
implements DSAExt {
    private boolean forSigning;
    private ECKeyParameters key;
    private SecureRandom random;

    public void init(boolean bl, CipherParameters cipherParameters) {
        this.forSigning = bl;
        if (bl) {
            if (cipherParameters instanceof ParametersWithRandom) {
                ParametersWithRandom parametersWithRandom = (ParametersWithRandom)cipherParameters;
                this.random = parametersWithRandom.getRandom();
                this.key = (ECPrivateKeyParameters)parametersWithRandom.getParameters();
            } else {
                this.random = CryptoServicesRegistrar.getSecureRandom();
                this.key = (ECPrivateKeyParameters)cipherParameters;
            }
        } else {
            this.key = (ECPublicKeyParameters)cipherParameters;
        }
    }

    public BigInteger getOrder() {
        return this.key.getParameters().getN();
    }

    public BigInteger[] generateSignature(byte[] byArray) {
        Object object;
        AsymmetricCipherKeyPair asymmetricCipherKeyPair;
        Object object2;
        BigInteger[] bigIntegerArray;
        if (!this.forSigning) {
            throw new IllegalStateException("not initialised for signing");
        }
        BigInteger bigInteger = this.getOrder();
        BigInteger bigInteger2 = new BigInteger(1, byArray);
        ECPrivateKeyParameters eCPrivateKeyParameters = (ECPrivateKeyParameters)this.key;
        if (bigInteger2.compareTo(bigInteger) >= 0) {
            throw new DataLengthException("input too large for ECNR key");
        }
        BigInteger bigInteger3 = null;
        BigInteger bigInteger4 = null;
        do {
            object = new ECKeyPairGenerator();
            ((ECKeyPairGenerator)object).init(new ECKeyGenerationParameters(eCPrivateKeyParameters.getParameters(), this.random));
        } while ((bigInteger3 = (bigIntegerArray = ((ECPublicKeyParameters)(object2 = (ECPublicKeyParameters)(asymmetricCipherKeyPair = ((ECKeyPairGenerator)object).generateKeyPair()).getPublic())).getQ().getAffineXCoord().toBigInteger()).add(bigInteger2).mod(bigInteger)).equals(ECConstants.ZERO));
        object = eCPrivateKeyParameters.getD();
        object2 = ((ECPrivateKeyParameters)asymmetricCipherKeyPair.getPrivate()).getD();
        bigInteger4 = ((BigInteger)object2).subtract(bigInteger3.multiply((BigInteger)object)).mod(bigInteger);
        bigIntegerArray = new BigInteger[]{bigInteger3, bigInteger4};
        return bigIntegerArray;
    }

    public boolean verifySignature(byte[] byArray, BigInteger bigInteger, BigInteger bigInteger2) {
        if (this.forSigning) {
            throw new IllegalStateException("not initialised for verifying");
        }
        ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters)this.key;
        BigInteger bigInteger3 = eCPublicKeyParameters.getParameters().getN();
        int n = bigInteger3.bitLength();
        BigInteger bigInteger4 = new BigInteger(1, byArray);
        int n2 = bigInteger4.bitLength();
        if (n2 > n) {
            throw new DataLengthException("input too large for ECNR key.");
        }
        BigInteger bigInteger5 = this.extractT(eCPublicKeyParameters, bigInteger, bigInteger2);
        return bigInteger5 != null && bigInteger5.equals(bigInteger4.mod(bigInteger3));
    }

    public byte[] getRecoveredMessage(BigInteger bigInteger, BigInteger bigInteger2) {
        if (this.forSigning) {
            throw new IllegalStateException("not initialised for verifying/recovery");
        }
        BigInteger bigInteger3 = this.extractT((ECPublicKeyParameters)this.key, bigInteger, bigInteger2);
        if (bigInteger3 != null) {
            return BigIntegers.asUnsignedByteArray(bigInteger3);
        }
        return null;
    }

    private BigInteger extractT(ECPublicKeyParameters eCPublicKeyParameters, BigInteger bigInteger, BigInteger bigInteger2) {
        ECPoint eCPoint;
        BigInteger bigInteger3 = eCPublicKeyParameters.getParameters().getN();
        if (bigInteger.compareTo(ECConstants.ONE) < 0 || bigInteger.compareTo(bigInteger3) >= 0) {
            return null;
        }
        if (bigInteger2.compareTo(ECConstants.ZERO) < 0 || bigInteger2.compareTo(bigInteger3) >= 0) {
            return null;
        }
        ECPoint eCPoint2 = eCPublicKeyParameters.getParameters().getG();
        ECPoint eCPoint3 = ECAlgorithms.sumOfTwoMultiplies(eCPoint2, bigInteger2, eCPoint = eCPublicKeyParameters.getQ(), bigInteger).normalize();
        if (eCPoint3.isInfinity()) {
            return null;
        }
        BigInteger bigInteger4 = eCPoint3.getAffineXCoord().toBigInteger();
        return bigInteger.subtract(bigInteger4).mod(bigInteger3);
    }
}


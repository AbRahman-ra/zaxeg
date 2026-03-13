/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;

public class ECDHUPrivateParameters
implements CipherParameters {
    private ECPrivateKeyParameters staticPrivateKey;
    private ECPrivateKeyParameters ephemeralPrivateKey;
    private ECPublicKeyParameters ephemeralPublicKey;

    public ECDHUPrivateParameters(ECPrivateKeyParameters eCPrivateKeyParameters, ECPrivateKeyParameters eCPrivateKeyParameters2) {
        this(eCPrivateKeyParameters, eCPrivateKeyParameters2, null);
    }

    public ECDHUPrivateParameters(ECPrivateKeyParameters eCPrivateKeyParameters, ECPrivateKeyParameters eCPrivateKeyParameters2, ECPublicKeyParameters eCPublicKeyParameters) {
        if (eCPrivateKeyParameters == null) {
            throw new NullPointerException("staticPrivateKey cannot be null");
        }
        if (eCPrivateKeyParameters2 == null) {
            throw new NullPointerException("ephemeralPrivateKey cannot be null");
        }
        ECDomainParameters eCDomainParameters = eCPrivateKeyParameters.getParameters();
        if (!eCDomainParameters.equals(eCPrivateKeyParameters2.getParameters())) {
            throw new IllegalArgumentException("static and ephemeral private keys have different domain parameters");
        }
        if (eCPublicKeyParameters == null) {
            ECPoint eCPoint = new FixedPointCombMultiplier().multiply(eCDomainParameters.getG(), eCPrivateKeyParameters2.getD());
            eCPublicKeyParameters = new ECPublicKeyParameters(eCPoint, eCDomainParameters);
        } else if (!eCDomainParameters.equals(eCPublicKeyParameters.getParameters())) {
            throw new IllegalArgumentException("ephemeral public key has different domain parameters");
        }
        this.staticPrivateKey = eCPrivateKeyParameters;
        this.ephemeralPrivateKey = eCPrivateKeyParameters2;
        this.ephemeralPublicKey = eCPublicKeyParameters;
    }

    public ECPrivateKeyParameters getStaticPrivateKey() {
        return this.staticPrivateKey;
    }

    public ECPrivateKeyParameters getEphemeralPrivateKey() {
        return this.ephemeralPrivateKey;
    }

    public ECPublicKeyParameters getEphemeralPublicKey() {
        return this.ephemeralPublicKey;
    }
}


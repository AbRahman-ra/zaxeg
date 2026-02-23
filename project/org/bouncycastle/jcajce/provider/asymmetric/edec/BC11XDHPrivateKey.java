/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  java.security.interfaces.XECPrivateKey
 *  java.security.spec.NamedParameterSpec
 *  org.bouncycastle.jcajce.provider.asymmetric.edec.BC11XDHPrivateKey
 *  org.bouncycastle.jcajce.provider.asymmetric.edec.BC11XDHPublicKey
 */
package org.bouncycastle.jcajce.provider.asymmetric.edec;

import java.io.IOException;
import java.security.interfaces.XECPrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.Optional;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X448PrivateKeyParameters;
import org.bouncycastle.jcajce.interfaces.XDHPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BC11XDHPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCXDHPrivateKey;

class BC11XDHPrivateKey
extends BCXDHPrivateKey
implements XECPrivateKey {
    BC11XDHPrivateKey(AsymmetricKeyParameter asymmetricKeyParameter) {
        super(asymmetricKeyParameter);
    }

    BC11XDHPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        super(privateKeyInfo);
    }

    public AlgorithmParameterSpec getParams() {
        if (this.xdhPrivateKey instanceof X448PrivateKeyParameters) {
            return NamedParameterSpec.X448;
        }
        return NamedParameterSpec.X25519;
    }

    @Override
    public XDHPublicKey getPublicKey() {
        if (this.xdhPrivateKey instanceof X448PrivateKeyParameters) {
            return new BC11XDHPublicKey((AsymmetricKeyParameter)((X448PrivateKeyParameters)this.xdhPrivateKey).generatePublicKey());
        }
        return new BC11XDHPublicKey((AsymmetricKeyParameter)((X25519PrivateKeyParameters)this.xdhPrivateKey).generatePublicKey());
    }

    public Optional<byte[]> getScalar() {
        if (this.xdhPrivateKey instanceof X448PrivateKeyParameters) {
            return Optional.of(((X448PrivateKeyParameters)this.xdhPrivateKey).getEncoded());
        }
        return Optional.of(((X25519PrivateKeyParameters)this.xdhPrivateKey).getEncoded());
    }
}


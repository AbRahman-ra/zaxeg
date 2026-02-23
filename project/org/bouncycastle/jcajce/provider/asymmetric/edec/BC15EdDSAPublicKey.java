/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  java.security.interfaces.EdECPublicKey
 *  java.security.spec.EdECPoint
 *  java.security.spec.NamedParameterSpec
 *  org.bouncycastle.jcajce.provider.asymmetric.edec.BC15EdDSAPublicKey
 */
package org.bouncycastle.jcajce.provider.asymmetric.edec;

import java.math.BigInteger;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.EdECPoint;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPublicKey;
import org.bouncycastle.util.Arrays;

class BC15EdDSAPublicKey
extends BCEdDSAPublicKey
implements EdECPublicKey {
    BC15EdDSAPublicKey(AsymmetricKeyParameter asymmetricKeyParameter) {
        super(asymmetricKeyParameter);
    }

    BC15EdDSAPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        super(subjectPublicKeyInfo);
    }

    BC15EdDSAPublicKey(byte[] byArray, byte[] byArray2) throws InvalidKeySpecException {
        super(byArray, byArray2);
    }

    public EdECPoint getPoint() {
        byte[] byArray = this.getPointEncoding();
        Arrays.reverseInPlace(byArray);
        boolean bl = (byArray[0] & 0x80) != 0;
        byArray[0] = (byte)(byArray[0] & 0x7F);
        return new EdECPoint(bl, new BigInteger(1, byArray));
    }

    public NamedParameterSpec getParams() {
        if (this.eddsaPublicKey instanceof Ed448PublicKeyParameters) {
            return NamedParameterSpec.ED448;
        }
        return NamedParameterSpec.ED25519;
    }
}


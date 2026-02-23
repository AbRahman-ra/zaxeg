/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;

public class GOST3410ParameterSpec
implements AlgorithmParameterSpec {
    private final ASN1ObjectIdentifier publicKeyParamSet;
    private final ASN1ObjectIdentifier digestParamSet;
    private final ASN1ObjectIdentifier encryptionParamSet;

    public GOST3410ParameterSpec(String string) {
        this(GOST3410ParameterSpec.getOid(string), GOST3410ParameterSpec.getDigestOid(string), null);
    }

    public GOST3410ParameterSpec(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier2) {
        this(aSN1ObjectIdentifier, aSN1ObjectIdentifier2, null);
    }

    public GOST3410ParameterSpec(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier2, ASN1ObjectIdentifier aSN1ObjectIdentifier3) {
        this.publicKeyParamSet = aSN1ObjectIdentifier;
        this.digestParamSet = aSN1ObjectIdentifier2;
        this.encryptionParamSet = aSN1ObjectIdentifier3;
    }

    public String getPublicKeyParamSetName() {
        return ECGOST3410NamedCurves.getName(this.getPublicKeyParamSet());
    }

    public ASN1ObjectIdentifier getPublicKeyParamSet() {
        return this.publicKeyParamSet;
    }

    public ASN1ObjectIdentifier getDigestParamSet() {
        return this.digestParamSet;
    }

    public ASN1ObjectIdentifier getEncryptionParamSet() {
        return this.encryptionParamSet;
    }

    private static ASN1ObjectIdentifier getOid(String string) {
        return ECGOST3410NamedCurves.getOID(string);
    }

    private static ASN1ObjectIdentifier getDigestOid(String string) {
        if (string.indexOf("12-512") > 0) {
            return RosstandartObjectIdentifiers.id_tc26_gost_3411_12_512;
        }
        if (string.indexOf("12-256") > 0) {
            return RosstandartObjectIdentifiers.id_tc26_gost_3411_12_256;
        }
        return CryptoProObjectIdentifiers.gostR3411_94_CryptoProParamSet;
    }
}


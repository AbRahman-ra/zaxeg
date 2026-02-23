/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.ec;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECCurve;

class ECUtils {
    ECUtils() {
    }

    static AsymmetricKeyParameter generatePublicKeyParameter(PublicKey publicKey) throws InvalidKeyException {
        return publicKey instanceof BCECPublicKey ? ((BCECPublicKey)publicKey).engineGetKeyParameters() : ECUtil.generatePublicKeyParameter(publicKey);
    }

    static X9ECParameters getDomainParametersFromGenSpec(ECGenParameterSpec eCGenParameterSpec, ProviderConfiguration providerConfiguration) {
        return ECUtils.getDomainParametersFromName(eCGenParameterSpec.getName(), providerConfiguration);
    }

    static X9ECParameters getDomainParametersFromName(String string, ProviderConfiguration providerConfiguration) {
        ASN1ObjectIdentifier aSN1ObjectIdentifier;
        if (null == string || string.length() < 1) {
            return null;
        }
        int n = string.indexOf(32);
        if (n > 0) {
            string = string.substring(n + 1);
        }
        if (null == (aSN1ObjectIdentifier = ECUtils.getOID(string))) {
            return ECUtil.getNamedCurveByName(string);
        }
        X9ECParameters x9ECParameters = ECUtil.getNamedCurveByOid(aSN1ObjectIdentifier);
        if (null == x9ECParameters && null != providerConfiguration) {
            Map map = providerConfiguration.getAdditionalECParameters();
            x9ECParameters = (X9ECParameters)map.get(aSN1ObjectIdentifier);
        }
        return x9ECParameters;
    }

    static X962Parameters getDomainParametersFromName(ECParameterSpec eCParameterSpec, boolean bl) {
        X962Parameters x962Parameters;
        if (eCParameterSpec instanceof ECNamedCurveSpec) {
            ASN1ObjectIdentifier aSN1ObjectIdentifier = ECUtil.getNamedCurveOid(((ECNamedCurveSpec)eCParameterSpec).getName());
            if (aSN1ObjectIdentifier == null) {
                aSN1ObjectIdentifier = new ASN1ObjectIdentifier(((ECNamedCurveSpec)eCParameterSpec).getName());
            }
            x962Parameters = new X962Parameters(aSN1ObjectIdentifier);
        } else if (eCParameterSpec == null) {
            x962Parameters = new X962Parameters(DERNull.INSTANCE);
        } else {
            ECCurve eCCurve = EC5Util.convertCurve(eCParameterSpec.getCurve());
            X9ECParameters x9ECParameters = new X9ECParameters(eCCurve, new X9ECPoint(EC5Util.convertPoint(eCCurve, eCParameterSpec.getGenerator()), bl), eCParameterSpec.getOrder(), BigInteger.valueOf(eCParameterSpec.getCofactor()), eCParameterSpec.getCurve().getSeed());
            x962Parameters = new X962Parameters(x9ECParameters);
        }
        return x962Parameters;
    }

    private static ASN1ObjectIdentifier getOID(String string) {
        char c = string.charAt(0);
        if (c >= '0' && c <= '2') {
            try {
                return new ASN1ObjectIdentifier(string);
            } catch (Exception exception) {
                // empty catch block
            }
        }
        return null;
    }
}


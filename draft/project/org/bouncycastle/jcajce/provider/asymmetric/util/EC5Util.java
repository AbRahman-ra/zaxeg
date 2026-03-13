/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.util;

import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import org.bouncycastle.jce.ECGOST3410NamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.field.FiniteField;
import org.bouncycastle.math.field.Polynomial;
import org.bouncycastle.math.field.PolynomialExtensionField;
import org.bouncycastle.util.Arrays;

public class EC5Util {
    private static Map customCurves;

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static ECCurve getCurve(ProviderConfiguration providerConfiguration, X962Parameters x962Parameters) {
        Set set = providerConfiguration.getAcceptableNamedCurves();
        if (x962Parameters.isNamedCurve()) {
            X9ECParameters x9ECParameters;
            ASN1ObjectIdentifier aSN1ObjectIdentifier = ASN1ObjectIdentifier.getInstance(x962Parameters.getParameters());
            if (!set.isEmpty()) {
                if (!set.contains(aSN1ObjectIdentifier)) throw new IllegalStateException("named curve not acceptable");
            }
            if ((x9ECParameters = ECUtil.getNamedCurveByOid(aSN1ObjectIdentifier)) != null) return x9ECParameters.getCurve();
            x9ECParameters = (X9ECParameters)providerConfiguration.getAdditionalECParameters().get(aSN1ObjectIdentifier);
            return x9ECParameters.getCurve();
        }
        if (x962Parameters.isImplicitlyCA()) {
            return providerConfiguration.getEcImplicitlyCa().getCurve();
        }
        ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(x962Parameters.getParameters());
        if (!set.isEmpty()) throw new IllegalStateException("encoded parameters not acceptable");
        if (aSN1Sequence.size() > 3) {
            X9ECParameters x9ECParameters = X9ECParameters.getInstance(aSN1Sequence);
            return x9ECParameters.getCurve();
        }
        ASN1ObjectIdentifier aSN1ObjectIdentifier = ASN1ObjectIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
        return ECGOST3410NamedCurves.getByOIDX9(aSN1ObjectIdentifier).getCurve();
    }

    public static ECDomainParameters getDomainParameters(ProviderConfiguration providerConfiguration, ECParameterSpec eCParameterSpec) {
        ECDomainParameters eCDomainParameters;
        if (eCParameterSpec == null) {
            org.bouncycastle.jce.spec.ECParameterSpec eCParameterSpec2 = providerConfiguration.getEcImplicitlyCa();
            eCDomainParameters = new ECDomainParameters(eCParameterSpec2.getCurve(), eCParameterSpec2.getG(), eCParameterSpec2.getN(), eCParameterSpec2.getH(), eCParameterSpec2.getSeed());
        } else {
            eCDomainParameters = ECUtil.getDomainParameters(providerConfiguration, EC5Util.convertSpec(eCParameterSpec));
        }
        return eCDomainParameters;
    }

    public static ECParameterSpec convertToSpec(X962Parameters x962Parameters, ECCurve eCCurve) {
        ECParameterSpec eCParameterSpec;
        if (x962Parameters.isNamedCurve()) {
            Map map;
            ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier)x962Parameters.getParameters();
            X9ECParameters x9ECParameters = ECUtil.getNamedCurveByOid(aSN1ObjectIdentifier);
            if (x9ECParameters == null && !(map = BouncyCastleProvider.CONFIGURATION.getAdditionalECParameters()).isEmpty()) {
                x9ECParameters = (X9ECParameters)map.get(aSN1ObjectIdentifier);
            }
            EllipticCurve ellipticCurve = EC5Util.convertCurve(eCCurve, x9ECParameters.getSeed());
            eCParameterSpec = new ECNamedCurveSpec(ECUtil.getCurveName(aSN1ObjectIdentifier), ellipticCurve, EC5Util.convertPoint(x9ECParameters.getG()), x9ECParameters.getN(), x9ECParameters.getH());
        } else if (x962Parameters.isImplicitlyCA()) {
            eCParameterSpec = null;
        } else {
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(x962Parameters.getParameters());
            if (aSN1Sequence.size() > 3) {
                X9ECParameters x9ECParameters = X9ECParameters.getInstance(aSN1Sequence);
                EllipticCurve ellipticCurve = EC5Util.convertCurve(eCCurve, x9ECParameters.getSeed());
                eCParameterSpec = x9ECParameters.getH() != null ? new ECParameterSpec(ellipticCurve, EC5Util.convertPoint(x9ECParameters.getG()), x9ECParameters.getN(), x9ECParameters.getH().intValue()) : new ECParameterSpec(ellipticCurve, EC5Util.convertPoint(x9ECParameters.getG()), x9ECParameters.getN(), 1);
            } else {
                GOST3410PublicKeyAlgParameters gOST3410PublicKeyAlgParameters = GOST3410PublicKeyAlgParameters.getInstance(aSN1Sequence);
                ECNamedCurveParameterSpec eCNamedCurveParameterSpec = ECGOST3410NamedCurveTable.getParameterSpec(ECGOST3410NamedCurves.getName(gOST3410PublicKeyAlgParameters.getPublicKeyParamSet()));
                eCCurve = eCNamedCurveParameterSpec.getCurve();
                EllipticCurve ellipticCurve = EC5Util.convertCurve(eCCurve, eCNamedCurveParameterSpec.getSeed());
                eCParameterSpec = new ECNamedCurveSpec(ECGOST3410NamedCurves.getName(gOST3410PublicKeyAlgParameters.getPublicKeyParamSet()), ellipticCurve, EC5Util.convertPoint(eCNamedCurveParameterSpec.getG()), eCNamedCurveParameterSpec.getN(), eCNamedCurveParameterSpec.getH());
            }
        }
        return eCParameterSpec;
    }

    public static ECParameterSpec convertToSpec(X9ECParameters x9ECParameters) {
        return new ECParameterSpec(EC5Util.convertCurve(x9ECParameters.getCurve(), null), EC5Util.convertPoint(x9ECParameters.getG()), x9ECParameters.getN(), x9ECParameters.getH().intValue());
    }

    public static ECParameterSpec convertToSpec(ECDomainParameters eCDomainParameters) {
        return new ECParameterSpec(EC5Util.convertCurve(eCDomainParameters.getCurve(), null), EC5Util.convertPoint(eCDomainParameters.getG()), eCDomainParameters.getN(), eCDomainParameters.getH().intValue());
    }

    public static EllipticCurve convertCurve(ECCurve eCCurve, byte[] byArray) {
        ECField eCField = EC5Util.convertField(eCCurve.getField());
        BigInteger bigInteger = eCCurve.getA().toBigInteger();
        BigInteger bigInteger2 = eCCurve.getB().toBigInteger();
        return new EllipticCurve(eCField, bigInteger, bigInteger2, null);
    }

    public static ECCurve convertCurve(EllipticCurve ellipticCurve) {
        ECField eCField = ellipticCurve.getField();
        BigInteger bigInteger = ellipticCurve.getA();
        BigInteger bigInteger2 = ellipticCurve.getB();
        if (eCField instanceof ECFieldFp) {
            ECCurve.Fp fp = new ECCurve.Fp(((ECFieldFp)eCField).getP(), bigInteger, bigInteger2);
            if (customCurves.containsKey(fp)) {
                return (ECCurve)customCurves.get(fp);
            }
            return fp;
        }
        ECFieldF2m eCFieldF2m = (ECFieldF2m)eCField;
        int n = eCFieldF2m.getM();
        int[] nArray = ECUtil.convertMidTerms(eCFieldF2m.getMidTermsOfReductionPolynomial());
        return new ECCurve.F2m(n, nArray[0], nArray[1], nArray[2], bigInteger, bigInteger2);
    }

    public static ECField convertField(FiniteField finiteField) {
        if (ECAlgorithms.isFpField(finiteField)) {
            return new ECFieldFp(finiteField.getCharacteristic());
        }
        Polynomial polynomial = ((PolynomialExtensionField)finiteField).getMinimalPolynomial();
        int[] nArray = polynomial.getExponentsPresent();
        int[] nArray2 = Arrays.reverseInPlace(Arrays.copyOfRange(nArray, 1, nArray.length - 1));
        return new ECFieldF2m(polynomial.getDegree(), nArray2);
    }

    public static ECParameterSpec convertSpec(EllipticCurve ellipticCurve, org.bouncycastle.jce.spec.ECParameterSpec eCParameterSpec) {
        ECPoint eCPoint = EC5Util.convertPoint(eCParameterSpec.getG());
        if (eCParameterSpec instanceof ECNamedCurveParameterSpec) {
            String string = ((ECNamedCurveParameterSpec)eCParameterSpec).getName();
            return new ECNamedCurveSpec(string, ellipticCurve, eCPoint, eCParameterSpec.getN(), eCParameterSpec.getH());
        }
        return new ECParameterSpec(ellipticCurve, eCPoint, eCParameterSpec.getN(), eCParameterSpec.getH().intValue());
    }

    public static org.bouncycastle.jce.spec.ECParameterSpec convertSpec(ECParameterSpec eCParameterSpec) {
        ECCurve eCCurve = EC5Util.convertCurve(eCParameterSpec.getCurve());
        org.bouncycastle.math.ec.ECPoint eCPoint = EC5Util.convertPoint(eCCurve, eCParameterSpec.getGenerator());
        BigInteger bigInteger = eCParameterSpec.getOrder();
        BigInteger bigInteger2 = BigInteger.valueOf(eCParameterSpec.getCofactor());
        byte[] byArray = eCParameterSpec.getCurve().getSeed();
        if (eCParameterSpec instanceof ECNamedCurveSpec) {
            return new ECNamedCurveParameterSpec(((ECNamedCurveSpec)eCParameterSpec).getName(), eCCurve, eCPoint, bigInteger, bigInteger2, byArray);
        }
        return new org.bouncycastle.jce.spec.ECParameterSpec(eCCurve, eCPoint, bigInteger, bigInteger2, byArray);
    }

    public static org.bouncycastle.math.ec.ECPoint convertPoint(ECParameterSpec eCParameterSpec, ECPoint eCPoint) {
        return EC5Util.convertPoint(EC5Util.convertCurve(eCParameterSpec.getCurve()), eCPoint);
    }

    public static org.bouncycastle.math.ec.ECPoint convertPoint(ECCurve eCCurve, ECPoint eCPoint) {
        return eCCurve.createPoint(eCPoint.getAffineX(), eCPoint.getAffineY());
    }

    public static ECPoint convertPoint(org.bouncycastle.math.ec.ECPoint eCPoint) {
        eCPoint = eCPoint.normalize();
        return new ECPoint(eCPoint.getAffineXCoord().toBigInteger(), eCPoint.getAffineYCoord().toBigInteger());
    }

    static {
        Object object;
        Object object2;
        customCurves = new HashMap();
        Enumeration enumeration = CustomNamedCurves.getNames();
        while (enumeration.hasMoreElements()) {
            object2 = (String)enumeration.nextElement();
            object = ECNamedCurveTable.getByName((String)object2);
            if (object == null) continue;
            customCurves.put(((X9ECParameters)object).getCurve(), CustomNamedCurves.getByName((String)object2).getCurve());
        }
        object2 = CustomNamedCurves.getByName("Curve25519");
        object = ((X9ECParameters)object2).getCurve();
        customCurves.put(new ECCurve.Fp(((ECCurve)object).getField().getCharacteristic(), ((ECCurve)object).getA().toBigInteger(), ((ECCurve)object).getB().toBigInteger(), ((ECCurve)object).getOrder(), ((ECCurve)object).getCofactor()), object);
    }
}


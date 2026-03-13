/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.oiw.ElGamalParameter;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.ua.DSTU4145BinaryField;
import org.bouncycastle.asn1.ua.DSTU4145ECBinary;
import org.bouncycastle.asn1.ua.DSTU4145NamedCurves;
import org.bouncycastle.asn1.ua.DSTU4145Params;
import org.bouncycastle.asn1.ua.DSTU4145PointEncoder;
import org.bouncycastle.asn1.ua.UAObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.DHPublicKey;
import org.bouncycastle.asn1.x9.DomainParameters;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.ValidationParams;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.crypto.params.DHValidationParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECGOST3410Parameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X448PublicKeyParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;

public class PublicKeyFactory {
    private static Map converters = new HashMap();

    public static AsymmetricKeyParameter createKey(byte[] byArray) throws IOException {
        return PublicKeyFactory.createKey(SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(byArray)));
    }

    public static AsymmetricKeyParameter createKey(InputStream inputStream) throws IOException {
        return PublicKeyFactory.createKey(SubjectPublicKeyInfo.getInstance(new ASN1InputStream(inputStream).readObject()));
    }

    public static AsymmetricKeyParameter createKey(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        return PublicKeyFactory.createKey(subjectPublicKeyInfo, null);
    }

    public static AsymmetricKeyParameter createKey(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
        AlgorithmIdentifier algorithmIdentifier = subjectPublicKeyInfo.getAlgorithm();
        SubjectPublicKeyInfoConverter subjectPublicKeyInfoConverter = (SubjectPublicKeyInfoConverter)converters.get(algorithmIdentifier.getAlgorithm());
        if (null == subjectPublicKeyInfoConverter) {
            throw new IOException("algorithm identifier in public key not recognised: " + algorithmIdentifier.getAlgorithm());
        }
        return subjectPublicKeyInfoConverter.getPublicKeyParameters(subjectPublicKeyInfo, object);
    }

    private static byte[] getRawKey(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
        return subjectPublicKeyInfo.getPublicKeyData().getOctets();
    }

    static {
        converters.put(PKCSObjectIdentifiers.rsaEncryption, new RSAConverter());
        converters.put(PKCSObjectIdentifiers.id_RSASSA_PSS, new RSAConverter());
        converters.put(X509ObjectIdentifiers.id_ea_rsa, new RSAConverter());
        converters.put(X9ObjectIdentifiers.dhpublicnumber, new DHPublicNumberConverter());
        converters.put(PKCSObjectIdentifiers.dhKeyAgreement, new DHAgreementConverter());
        converters.put(X9ObjectIdentifiers.id_dsa, new DSAConverter());
        converters.put(OIWObjectIdentifiers.dsaWithSHA1, new DSAConverter());
        converters.put(OIWObjectIdentifiers.elGamalAlgorithm, new ElGamalConverter());
        converters.put(X9ObjectIdentifiers.id_ecPublicKey, new ECConverter());
        converters.put(CryptoProObjectIdentifiers.gostR3410_2001, new GOST3410_2001Converter());
        converters.put(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256, new GOST3410_2012Converter());
        converters.put(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512, new GOST3410_2012Converter());
        converters.put(UAObjectIdentifiers.dstu4145be, new DSTUConverter());
        converters.put(UAObjectIdentifiers.dstu4145le, new DSTUConverter());
        converters.put(EdECObjectIdentifiers.id_X25519, new X25519Converter());
        converters.put(EdECObjectIdentifiers.id_X448, new X448Converter());
        converters.put(EdECObjectIdentifiers.id_Ed25519, new Ed25519Converter());
        converters.put(EdECObjectIdentifiers.id_Ed448, new Ed448Converter());
    }

    private static class DHAgreementConverter
    extends SubjectPublicKeyInfoConverter {
        private DHAgreementConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            DHParameter dHParameter = DHParameter.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            ASN1Integer aSN1Integer = (ASN1Integer)subjectPublicKeyInfo.parsePublicKey();
            BigInteger bigInteger = dHParameter.getL();
            int n = bigInteger == null ? 0 : bigInteger.intValue();
            DHParameters dHParameters = new DHParameters(dHParameter.getP(), dHParameter.getG(), null, n);
            return new DHPublicKeyParameters(aSN1Integer.getValue(), dHParameters);
        }
    }

    private static class DHPublicNumberConverter
    extends SubjectPublicKeyInfoConverter {
        private DHPublicNumberConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            DHPublicKey dHPublicKey = DHPublicKey.getInstance(subjectPublicKeyInfo.parsePublicKey());
            BigInteger bigInteger = dHPublicKey.getY();
            DomainParameters domainParameters = DomainParameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            BigInteger bigInteger2 = domainParameters.getP();
            BigInteger bigInteger3 = domainParameters.getG();
            BigInteger bigInteger4 = domainParameters.getQ();
            BigInteger bigInteger5 = null;
            if (domainParameters.getJ() != null) {
                bigInteger5 = domainParameters.getJ();
            }
            DHValidationParameters dHValidationParameters = null;
            ValidationParams validationParams = domainParameters.getValidationParams();
            if (validationParams != null) {
                byte[] byArray = validationParams.getSeed();
                BigInteger bigInteger6 = validationParams.getPgenCounter();
                dHValidationParameters = new DHValidationParameters(byArray, bigInteger6.intValue());
            }
            return new DHPublicKeyParameters(bigInteger, new DHParameters(bigInteger2, bigInteger3, bigInteger4, bigInteger5, dHValidationParameters));
        }
    }

    private static class DSAConverter
    extends SubjectPublicKeyInfoConverter {
        private DSAConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            ASN1Integer aSN1Integer = (ASN1Integer)subjectPublicKeyInfo.parsePublicKey();
            ASN1Encodable aSN1Encodable = subjectPublicKeyInfo.getAlgorithm().getParameters();
            DSAParameters dSAParameters = null;
            if (aSN1Encodable != null) {
                DSAParameter dSAParameter = DSAParameter.getInstance(aSN1Encodable.toASN1Primitive());
                dSAParameters = new DSAParameters(dSAParameter.getP(), dSAParameter.getQ(), dSAParameter.getG());
            }
            return new DSAPublicKeyParameters(aSN1Integer.getValue(), dSAParameters);
        }
    }

    private static class DSTUConverter
    extends SubjectPublicKeyInfoConverter {
        private DSTUConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            Object object2;
            ECDomainParameters eCDomainParameters;
            ASN1OctetString aSN1OctetString;
            AlgorithmIdentifier algorithmIdentifier = subjectPublicKeyInfo.getAlgorithm();
            ASN1ObjectIdentifier aSN1ObjectIdentifier = algorithmIdentifier.getAlgorithm();
            DSTU4145Params dSTU4145Params = DSTU4145Params.getInstance(algorithmIdentifier.getParameters());
            try {
                aSN1OctetString = (ASN1OctetString)subjectPublicKeyInfo.parsePublicKey();
            } catch (IOException iOException) {
                throw new IllegalArgumentException("error recovering DSTU public key");
            }
            byte[] byArray = Arrays.clone(aSN1OctetString.getOctets());
            if (aSN1ObjectIdentifier.equals(UAObjectIdentifiers.dstu4145le)) {
                this.reverseBytes(byArray);
            }
            if (dSTU4145Params.isNamedCurve()) {
                eCDomainParameters = DSTU4145NamedCurves.getByOID(dSTU4145Params.getNamedCurve());
            } else {
                object2 = dSTU4145Params.getECBinary();
                byte[] byArray2 = ((DSTU4145ECBinary)object2).getB();
                if (aSN1ObjectIdentifier.equals(UAObjectIdentifiers.dstu4145le)) {
                    this.reverseBytes(byArray2);
                }
                BigInteger bigInteger = new BigInteger(1, byArray2);
                DSTU4145BinaryField dSTU4145BinaryField = ((DSTU4145ECBinary)object2).getField();
                ECCurve.F2m f2m = new ECCurve.F2m(dSTU4145BinaryField.getM(), dSTU4145BinaryField.getK1(), dSTU4145BinaryField.getK2(), dSTU4145BinaryField.getK3(), ((DSTU4145ECBinary)object2).getA(), bigInteger);
                byte[] byArray3 = ((DSTU4145ECBinary)object2).getG();
                if (aSN1ObjectIdentifier.equals(UAObjectIdentifiers.dstu4145le)) {
                    this.reverseBytes(byArray3);
                }
                ECPoint eCPoint = DSTU4145PointEncoder.decodePoint(f2m, byArray3);
                eCDomainParameters = new ECDomainParameters(f2m, eCPoint, ((DSTU4145ECBinary)object2).getN());
            }
            object2 = DSTU4145PointEncoder.decodePoint(eCDomainParameters.getCurve(), byArray);
            return new ECPublicKeyParameters((ECPoint)object2, eCDomainParameters);
        }

        private void reverseBytes(byte[] byArray) {
            for (int i = 0; i < byArray.length / 2; ++i) {
                byte by = byArray[i];
                byArray[i] = byArray[byArray.length - 1 - i];
                byArray[byArray.length - 1 - i] = by;
            }
        }
    }

    private static class ECConverter
    extends SubjectPublicKeyInfoConverter {
        private ECConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            int n;
            ECDomainParameters eCDomainParameters;
            Object object2;
            ASN1Object aSN1Object;
            X962Parameters x962Parameters = X962Parameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            if (x962Parameters.isNamedCurve()) {
                aSN1Object = (ASN1ObjectIdentifier)x962Parameters.getParameters();
                object2 = CustomNamedCurves.getByOID((ASN1ObjectIdentifier)aSN1Object);
                if (object2 == null) {
                    object2 = ECNamedCurveTable.getByOID((ASN1ObjectIdentifier)aSN1Object);
                }
                eCDomainParameters = new ECNamedDomainParameters((ASN1ObjectIdentifier)aSN1Object, (X9ECParameters)object2);
            } else if (x962Parameters.isImplicitlyCA()) {
                eCDomainParameters = (ECDomainParameters)object;
            } else {
                aSN1Object = X9ECParameters.getInstance(x962Parameters.getParameters());
                eCDomainParameters = new ECDomainParameters((X9ECParameters)aSN1Object);
            }
            aSN1Object = subjectPublicKeyInfo.getPublicKeyData();
            object2 = ((ASN1BitString)aSN1Object).getBytes();
            ASN1OctetString aSN1OctetString = new DEROctetString((byte[])object2);
            if (object2[0] == 4 && object2[1] == ((Object)object2).length - 2 && (object2[2] == 2 || object2[2] == 3) && (n = new X9IntegerConverter().getByteLength(eCDomainParameters.getCurve())) >= ((Object)object2).length - 3) {
                try {
                    aSN1OctetString = (ASN1OctetString)ASN1Primitive.fromByteArray((byte[])object2);
                } catch (IOException iOException) {
                    throw new IllegalArgumentException("error recovering public key");
                }
            }
            X9ECPoint x9ECPoint = new X9ECPoint(eCDomainParameters.getCurve(), aSN1OctetString);
            return new ECPublicKeyParameters(x9ECPoint.getPoint(), eCDomainParameters);
        }
    }

    private static class Ed25519Converter
    extends SubjectPublicKeyInfoConverter {
        private Ed25519Converter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            return new Ed25519PublicKeyParameters(PublicKeyFactory.getRawKey(subjectPublicKeyInfo, object));
        }
    }

    private static class Ed448Converter
    extends SubjectPublicKeyInfoConverter {
        private Ed448Converter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            return new Ed448PublicKeyParameters(PublicKeyFactory.getRawKey(subjectPublicKeyInfo, object));
        }
    }

    private static class ElGamalConverter
    extends SubjectPublicKeyInfoConverter {
        private ElGamalConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            ElGamalParameter elGamalParameter = ElGamalParameter.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            ASN1Integer aSN1Integer = (ASN1Integer)subjectPublicKeyInfo.parsePublicKey();
            return new ElGamalPublicKeyParameters(aSN1Integer.getValue(), new ElGamalParameters(elGamalParameter.getP(), elGamalParameter.getG()));
        }
    }

    private static class GOST3410_2001Converter
    extends SubjectPublicKeyInfoConverter {
        private GOST3410_2001Converter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            ASN1OctetString aSN1OctetString;
            AlgorithmIdentifier algorithmIdentifier = subjectPublicKeyInfo.getAlgorithm();
            GOST3410PublicKeyAlgParameters gOST3410PublicKeyAlgParameters = GOST3410PublicKeyAlgParameters.getInstance(algorithmIdentifier.getParameters());
            ASN1ObjectIdentifier aSN1ObjectIdentifier = gOST3410PublicKeyAlgParameters.getPublicKeyParamSet();
            ECGOST3410Parameters eCGOST3410Parameters = new ECGOST3410Parameters(new ECNamedDomainParameters(aSN1ObjectIdentifier, ECGOST3410NamedCurves.getByOIDX9(aSN1ObjectIdentifier)), aSN1ObjectIdentifier, gOST3410PublicKeyAlgParameters.getDigestParamSet(), gOST3410PublicKeyAlgParameters.getEncryptionParamSet());
            try {
                aSN1OctetString = (ASN1OctetString)subjectPublicKeyInfo.parsePublicKey();
            } catch (IOException iOException) {
                throw new IllegalArgumentException("error recovering GOST3410_2001 public key");
            }
            int n = 32;
            int n2 = 2 * n;
            byte[] byArray = aSN1OctetString.getOctets();
            if (byArray.length != n2) {
                throw new IllegalArgumentException("invalid length for GOST3410_2001 public key");
            }
            byte[] byArray2 = new byte[1 + n2];
            byArray2[0] = 4;
            for (int i = 1; i <= n; ++i) {
                byArray2[i] = byArray[n - i];
                byArray2[i + n] = byArray[n2 - i];
            }
            ECPoint eCPoint = eCGOST3410Parameters.getCurve().decodePoint(byArray2);
            return new ECPublicKeyParameters(eCPoint, (ECDomainParameters)eCGOST3410Parameters);
        }
    }

    private static class GOST3410_2012Converter
    extends SubjectPublicKeyInfoConverter {
        private GOST3410_2012Converter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            ASN1OctetString aSN1OctetString;
            AlgorithmIdentifier algorithmIdentifier = subjectPublicKeyInfo.getAlgorithm();
            ASN1ObjectIdentifier aSN1ObjectIdentifier = algorithmIdentifier.getAlgorithm();
            GOST3410PublicKeyAlgParameters gOST3410PublicKeyAlgParameters = GOST3410PublicKeyAlgParameters.getInstance(algorithmIdentifier.getParameters());
            ASN1ObjectIdentifier aSN1ObjectIdentifier2 = gOST3410PublicKeyAlgParameters.getPublicKeyParamSet();
            ECGOST3410Parameters eCGOST3410Parameters = new ECGOST3410Parameters(new ECNamedDomainParameters(aSN1ObjectIdentifier2, ECGOST3410NamedCurves.getByOIDX9(aSN1ObjectIdentifier2)), aSN1ObjectIdentifier2, gOST3410PublicKeyAlgParameters.getDigestParamSet(), gOST3410PublicKeyAlgParameters.getEncryptionParamSet());
            try {
                aSN1OctetString = (ASN1OctetString)subjectPublicKeyInfo.parsePublicKey();
            } catch (IOException iOException) {
                throw new IllegalArgumentException("error recovering GOST3410_2012 public key");
            }
            int n = 32;
            if (aSN1ObjectIdentifier.equals(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512)) {
                n = 64;
            }
            int n2 = 2 * n;
            byte[] byArray = aSN1OctetString.getOctets();
            if (byArray.length != n2) {
                throw new IllegalArgumentException("invalid length for GOST3410_2012 public key");
            }
            byte[] byArray2 = new byte[1 + n2];
            byArray2[0] = 4;
            for (int i = 1; i <= n; ++i) {
                byArray2[i] = byArray[n - i];
                byArray2[i + n] = byArray[n2 - i];
            }
            ECPoint eCPoint = eCGOST3410Parameters.getCurve().decodePoint(byArray2);
            return new ECPublicKeyParameters(eCPoint, (ECDomainParameters)eCGOST3410Parameters);
        }
    }

    private static class RSAConverter
    extends SubjectPublicKeyInfoConverter {
        private RSAConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            RSAPublicKey rSAPublicKey = RSAPublicKey.getInstance(subjectPublicKeyInfo.parsePublicKey());
            return new RSAKeyParameters(false, rSAPublicKey.getModulus(), rSAPublicKey.getPublicExponent());
        }
    }

    private static abstract class SubjectPublicKeyInfoConverter {
        private SubjectPublicKeyInfoConverter() {
        }

        abstract AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException;
    }

    private static class X25519Converter
    extends SubjectPublicKeyInfoConverter {
        private X25519Converter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            return new X25519PublicKeyParameters(PublicKeyFactory.getRawKey(subjectPublicKeyInfo, object));
        }
    }

    private static class X448Converter
    extends SubjectPublicKeyInfoConverter {
        private X448Converter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            return new X448PublicKeyParameters(PublicKeyFactory.getRawKey(subjectPublicKeyInfo, object));
        }
    }
}


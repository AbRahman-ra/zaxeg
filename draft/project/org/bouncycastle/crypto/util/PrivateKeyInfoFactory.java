/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECGOST3410Parameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed448PrivateKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X448PrivateKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;

public class PrivateKeyInfoFactory {
    private static Set cryptoProOids = new HashSet(5);

    private PrivateKeyInfoFactory() {
    }

    public static PrivateKeyInfo createPrivateKeyInfo(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        return PrivateKeyInfoFactory.createPrivateKeyInfo(asymmetricKeyParameter, null);
    }

    public static PrivateKeyInfo createPrivateKeyInfo(AsymmetricKeyParameter asymmetricKeyParameter, ASN1Set aSN1Set) throws IOException {
        if (asymmetricKeyParameter instanceof RSAKeyParameters) {
            RSAPrivateCrtKeyParameters rSAPrivateCrtKeyParameters = (RSAPrivateCrtKeyParameters)asymmetricKeyParameter;
            return new PrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), new RSAPrivateKey(rSAPrivateCrtKeyParameters.getModulus(), rSAPrivateCrtKeyParameters.getPublicExponent(), rSAPrivateCrtKeyParameters.getExponent(), rSAPrivateCrtKeyParameters.getP(), rSAPrivateCrtKeyParameters.getQ(), rSAPrivateCrtKeyParameters.getDP(), rSAPrivateCrtKeyParameters.getDQ(), rSAPrivateCrtKeyParameters.getQInv()), aSN1Set);
        }
        if (asymmetricKeyParameter instanceof DSAPrivateKeyParameters) {
            DSAPrivateKeyParameters dSAPrivateKeyParameters = (DSAPrivateKeyParameters)asymmetricKeyParameter;
            DSAParameters dSAParameters = dSAPrivateKeyParameters.getParameters();
            return new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(dSAParameters.getP(), dSAParameters.getQ(), dSAParameters.getG())), new ASN1Integer(dSAPrivateKeyParameters.getX()), aSN1Set);
        }
        if (asymmetricKeyParameter instanceof ECPrivateKeyParameters) {
            Object object;
            int n;
            X962Parameters x962Parameters;
            ECPrivateKeyParameters eCPrivateKeyParameters = (ECPrivateKeyParameters)asymmetricKeyParameter;
            ECDomainParameters eCDomainParameters = eCPrivateKeyParameters.getParameters();
            if (eCDomainParameters == null) {
                x962Parameters = new X962Parameters(DERNull.INSTANCE);
                n = eCPrivateKeyParameters.getD().bitLength();
            } else {
                if (eCDomainParameters instanceof ECGOST3410Parameters) {
                    ASN1ObjectIdentifier aSN1ObjectIdentifier;
                    int n2;
                    GOST3410PublicKeyAlgParameters gOST3410PublicKeyAlgParameters = new GOST3410PublicKeyAlgParameters(((ECGOST3410Parameters)eCDomainParameters).getPublicKeyParamSet(), ((ECGOST3410Parameters)eCDomainParameters).getDigestParamSet(), ((ECGOST3410Parameters)eCDomainParameters).getEncryptionParamSet());
                    if (cryptoProOids.contains(gOST3410PublicKeyAlgParameters.getPublicKeyParamSet())) {
                        n2 = 32;
                        aSN1ObjectIdentifier = CryptoProObjectIdentifiers.gostR3410_2001;
                    } else {
                        boolean bl = eCPrivateKeyParameters.getD().bitLength() > 256;
                        aSN1ObjectIdentifier = bl ? RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512 : RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256;
                        n2 = bl ? 64 : 32;
                    }
                    byte[] byArray = new byte[n2];
                    PrivateKeyInfoFactory.extractBytes(byArray, n2, 0, eCPrivateKeyParameters.getD());
                    return new PrivateKeyInfo(new AlgorithmIdentifier(aSN1ObjectIdentifier, gOST3410PublicKeyAlgParameters), new DEROctetString(byArray));
                }
                if (eCDomainParameters instanceof ECNamedDomainParameters) {
                    x962Parameters = new X962Parameters(((ECNamedDomainParameters)eCDomainParameters).getName());
                    n = eCDomainParameters.getN().bitLength();
                } else {
                    object = new X9ECParameters(eCDomainParameters.getCurve(), new X9ECPoint(eCDomainParameters.getG(), false), eCDomainParameters.getN(), eCDomainParameters.getH(), eCDomainParameters.getSeed());
                    x962Parameters = new X962Parameters((X9ECParameters)object);
                    n = eCDomainParameters.getN().bitLength();
                }
            }
            object = new FixedPointCombMultiplier().multiply(eCDomainParameters.getG(), eCPrivateKeyParameters.getD());
            DERBitString dERBitString = new DERBitString(((ECPoint)object).getEncoded(false));
            return new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, x962Parameters), new ECPrivateKey(n, eCPrivateKeyParameters.getD(), dERBitString, x962Parameters), aSN1Set);
        }
        if (asymmetricKeyParameter instanceof X448PrivateKeyParameters) {
            X448PrivateKeyParameters x448PrivateKeyParameters = (X448PrivateKeyParameters)asymmetricKeyParameter;
            return new PrivateKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X448), new DEROctetString(x448PrivateKeyParameters.getEncoded()), aSN1Set, x448PrivateKeyParameters.generatePublicKey().getEncoded());
        }
        if (asymmetricKeyParameter instanceof X25519PrivateKeyParameters) {
            X25519PrivateKeyParameters x25519PrivateKeyParameters = (X25519PrivateKeyParameters)asymmetricKeyParameter;
            return new PrivateKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X25519), new DEROctetString(x25519PrivateKeyParameters.getEncoded()), aSN1Set, x25519PrivateKeyParameters.generatePublicKey().getEncoded());
        }
        if (asymmetricKeyParameter instanceof Ed448PrivateKeyParameters) {
            Ed448PrivateKeyParameters ed448PrivateKeyParameters = (Ed448PrivateKeyParameters)asymmetricKeyParameter;
            return new PrivateKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed448), new DEROctetString(ed448PrivateKeyParameters.getEncoded()), aSN1Set, ed448PrivateKeyParameters.generatePublicKey().getEncoded());
        }
        if (asymmetricKeyParameter instanceof Ed25519PrivateKeyParameters) {
            Ed25519PrivateKeyParameters ed25519PrivateKeyParameters = (Ed25519PrivateKeyParameters)asymmetricKeyParameter;
            return new PrivateKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), new DEROctetString(ed25519PrivateKeyParameters.getEncoded()), aSN1Set, ed25519PrivateKeyParameters.generatePublicKey().getEncoded());
        }
        throw new IOException("key parameters not recognized");
    }

    private static void extractBytes(byte[] byArray, int n, int n2, BigInteger bigInteger) {
        byte[] byArray2 = bigInteger.toByteArray();
        if (byArray2.length < n) {
            byte[] byArray3 = new byte[n];
            System.arraycopy(byArray2, 0, byArray3, byArray3.length - byArray2.length, byArray2.length);
            byArray2 = byArray3;
        }
        for (int i = 0; i != n; ++i) {
            byArray[n2 + i] = byArray2[byArray2.length - 1 - i];
        }
    }

    static {
        cryptoProOids.add(CryptoProObjectIdentifiers.gostR3410_2001_CryptoPro_A);
        cryptoProOids.add(CryptoProObjectIdentifiers.gostR3410_2001_CryptoPro_B);
        cryptoProOids.add(CryptoProObjectIdentifiers.gostR3410_2001_CryptoPro_C);
        cryptoProOids.add(CryptoProObjectIdentifiers.gostR3410_2001_CryptoPro_XchA);
        cryptoProOids.add(CryptoProObjectIdentifiers.gostR3410_2001_CryptoPro_XchB);
    }
}


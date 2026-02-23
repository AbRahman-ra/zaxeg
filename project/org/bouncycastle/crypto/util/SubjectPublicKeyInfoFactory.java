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
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECGOST3410Parameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X448PublicKeyParameters;

public class SubjectPublicKeyInfoFactory {
    private static Set cryptoProOids = new HashSet(5);

    private SubjectPublicKeyInfoFactory() {
    }

    public static SubjectPublicKeyInfo createSubjectPublicKeyInfo(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        if (asymmetricKeyParameter instanceof RSAKeyParameters) {
            RSAKeyParameters rSAKeyParameters = (RSAKeyParameters)asymmetricKeyParameter;
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), new RSAPublicKey(rSAKeyParameters.getModulus(), rSAKeyParameters.getExponent()));
        }
        if (asymmetricKeyParameter instanceof DSAPublicKeyParameters) {
            DSAPublicKeyParameters dSAPublicKeyParameters = (DSAPublicKeyParameters)asymmetricKeyParameter;
            DSAParameter dSAParameter = null;
            DSAParameters dSAParameters = dSAPublicKeyParameters.getParameters();
            if (dSAParameters != null) {
                dSAParameter = new DSAParameter(dSAParameters.getP(), dSAParameters.getQ(), dSAParameters.getG());
            }
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, dSAParameter), new ASN1Integer(dSAPublicKeyParameters.getY()));
        }
        if (asymmetricKeyParameter instanceof ECPublicKeyParameters) {
            Object object;
            X962Parameters x962Parameters;
            ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters)asymmetricKeyParameter;
            ECDomainParameters eCDomainParameters = eCPublicKeyParameters.getParameters();
            if (eCDomainParameters == null) {
                x962Parameters = new X962Parameters(DERNull.INSTANCE);
            } else {
                if (eCDomainParameters instanceof ECGOST3410Parameters) {
                    ASN1ObjectIdentifier aSN1ObjectIdentifier;
                    int n;
                    int n2;
                    ECGOST3410Parameters eCGOST3410Parameters = (ECGOST3410Parameters)eCDomainParameters;
                    BigInteger bigInteger = eCPublicKeyParameters.getQ().getAffineXCoord().toBigInteger();
                    BigInteger bigInteger2 = eCPublicKeyParameters.getQ().getAffineYCoord().toBigInteger();
                    GOST3410PublicKeyAlgParameters gOST3410PublicKeyAlgParameters = new GOST3410PublicKeyAlgParameters(eCGOST3410Parameters.getPublicKeyParamSet(), eCGOST3410Parameters.getDigestParamSet());
                    if (cryptoProOids.contains(eCGOST3410Parameters.getPublicKeyParamSet())) {
                        n2 = 64;
                        n = 32;
                        aSN1ObjectIdentifier = CryptoProObjectIdentifiers.gostR3410_2001;
                    } else {
                        boolean bl;
                        boolean bl2 = bl = bigInteger.bitLength() > 256;
                        if (bl) {
                            n2 = 128;
                            n = 64;
                            aSN1ObjectIdentifier = RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512;
                        } else {
                            n2 = 64;
                            n = 32;
                            aSN1ObjectIdentifier = RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256;
                        }
                    }
                    byte[] byArray = new byte[n2];
                    SubjectPublicKeyInfoFactory.extractBytes(byArray, n2 / 2, 0, bigInteger);
                    SubjectPublicKeyInfoFactory.extractBytes(byArray, n2 / 2, n, bigInteger2);
                    try {
                        return new SubjectPublicKeyInfo(new AlgorithmIdentifier(aSN1ObjectIdentifier, gOST3410PublicKeyAlgParameters), new DEROctetString(byArray));
                    } catch (IOException iOException) {
                        return null;
                    }
                }
                if (eCDomainParameters instanceof ECNamedDomainParameters) {
                    x962Parameters = new X962Parameters(((ECNamedDomainParameters)eCDomainParameters).getName());
                } else {
                    object = new X9ECParameters(eCDomainParameters.getCurve(), new X9ECPoint(eCDomainParameters.getG(), false), eCDomainParameters.getN(), eCDomainParameters.getH(), eCDomainParameters.getSeed());
                    x962Parameters = new X962Parameters((X9ECParameters)object);
                }
            }
            object = eCPublicKeyParameters.getQ().getEncoded(false);
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, x962Parameters), (byte[])object);
        }
        if (asymmetricKeyParameter instanceof X448PublicKeyParameters) {
            X448PublicKeyParameters x448PublicKeyParameters = (X448PublicKeyParameters)asymmetricKeyParameter;
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X448), x448PublicKeyParameters.getEncoded());
        }
        if (asymmetricKeyParameter instanceof X25519PublicKeyParameters) {
            X25519PublicKeyParameters x25519PublicKeyParameters = (X25519PublicKeyParameters)asymmetricKeyParameter;
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X25519), x25519PublicKeyParameters.getEncoded());
        }
        if (asymmetricKeyParameter instanceof Ed448PublicKeyParameters) {
            Ed448PublicKeyParameters ed448PublicKeyParameters = (Ed448PublicKeyParameters)asymmetricKeyParameter;
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed448), ed448PublicKeyParameters.getEncoded());
        }
        if (asymmetricKeyParameter instanceof Ed25519PublicKeyParameters) {
            Ed25519PublicKeyParameters ed25519PublicKeyParameters = (Ed25519PublicKeyParameters)asymmetricKeyParameter;
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), ed25519PublicKeyParameters.getEncoded());
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


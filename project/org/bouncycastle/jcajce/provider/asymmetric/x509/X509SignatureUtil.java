/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PSSParameterSpec;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jcajce.util.MessageDigestUtils;
import org.bouncycastle.util.encoders.Hex;

class X509SignatureUtil {
    private static final Map<ASN1ObjectIdentifier, String> algNames = new HashMap<ASN1ObjectIdentifier, String>();
    private static final ASN1Null derNull;

    X509SignatureUtil() {
    }

    static boolean isCompositeAlgorithm(AlgorithmIdentifier algorithmIdentifier) {
        return MiscObjectIdentifiers.id_alg_composite.equals(algorithmIdentifier.getAlgorithm());
    }

    static void setSignatureParameters(Signature signature, ASN1Encodable aSN1Encodable) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if (aSN1Encodable != null && !derNull.equals(aSN1Encodable)) {
            AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(signature.getAlgorithm(), signature.getProvider());
            try {
                algorithmParameters.init(aSN1Encodable.toASN1Primitive().getEncoded());
            } catch (IOException iOException) {
                throw new SignatureException("IOException decoding parameters: " + iOException.getMessage());
            }
            if (signature.getAlgorithm().endsWith("MGF1")) {
                try {
                    signature.setParameter(algorithmParameters.getParameterSpec(PSSParameterSpec.class));
                } catch (GeneralSecurityException generalSecurityException) {
                    throw new SignatureException("Exception extracting parameters: " + generalSecurityException.getMessage());
                }
            }
        }
    }

    static String getSignatureName(AlgorithmIdentifier algorithmIdentifier) {
        String string;
        ASN1Encodable aSN1Encodable = algorithmIdentifier.getParameters();
        if (aSN1Encodable != null && !derNull.equals(aSN1Encodable)) {
            if (algorithmIdentifier.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS)) {
                RSASSAPSSparams rSASSAPSSparams = RSASSAPSSparams.getInstance(aSN1Encodable);
                return X509SignatureUtil.getDigestAlgName(rSASSAPSSparams.getHashAlgorithm().getAlgorithm()) + "withRSAandMGF1";
            }
            if (algorithmIdentifier.getAlgorithm().equals(X9ObjectIdentifiers.ecdsa_with_SHA2)) {
                ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(aSN1Encodable);
                return X509SignatureUtil.getDigestAlgName((ASN1ObjectIdentifier)aSN1Sequence.getObjectAt(0)) + "withECDSA";
            }
        }
        if ((string = algNames.get(algorithmIdentifier.getAlgorithm())) != null) {
            return string;
        }
        return X509SignatureUtil.findAlgName(algorithmIdentifier.getAlgorithm());
    }

    private static String getDigestAlgName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        String string = MessageDigestUtils.getDigestName(aSN1ObjectIdentifier);
        int n = string.indexOf(45);
        if (n > 0 && !string.startsWith("SHA3")) {
            return string.substring(0, n) + string.substring(n + 1);
        }
        return string;
    }

    private static String findAlgName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        Object object;
        Provider provider = Security.getProvider("BC");
        if (provider != null && (object = X509SignatureUtil.lookupAlg(provider, aSN1ObjectIdentifier)) != null) {
            return object;
        }
        object = Security.getProviders();
        for (int i = 0; i != ((Provider[])object).length; ++i) {
            String string;
            if (provider == object[i] || (string = X509SignatureUtil.lookupAlg(object[i], aSN1ObjectIdentifier)) == null) continue;
            return string;
        }
        return aSN1ObjectIdentifier.getId();
    }

    private static String lookupAlg(Provider provider, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        String string = provider.getProperty("Alg.Alias.Signature." + aSN1ObjectIdentifier);
        if (string != null) {
            return string;
        }
        string = provider.getProperty("Alg.Alias.Signature.OID." + aSN1ObjectIdentifier);
        if (string != null) {
            return string;
        }
        return null;
    }

    static void prettyPrintSignature(byte[] byArray, StringBuffer stringBuffer, String string) {
        if (byArray.length > 20) {
            stringBuffer.append("            Signature: ").append(Hex.toHexString(byArray, 0, 20)).append(string);
            for (int i = 20; i < byArray.length; i += 20) {
                if (i < byArray.length - 20) {
                    stringBuffer.append("                       ").append(Hex.toHexString(byArray, i, 20)).append(string);
                    continue;
                }
                stringBuffer.append("                       ").append(Hex.toHexString(byArray, i, byArray.length - i)).append(string);
            }
        } else {
            stringBuffer.append("            Signature: ").append(Hex.toHexString(byArray)).append(string);
        }
    }

    static {
        algNames.put(EdECObjectIdentifiers.id_Ed25519, "Ed25519");
        algNames.put(EdECObjectIdentifiers.id_Ed448, "Ed448");
        algNames.put(OIWObjectIdentifiers.dsaWithSHA1, "SHA1withDSA");
        algNames.put(X9ObjectIdentifiers.id_dsa_with_sha1, "SHA1withDSA");
        derNull = DERNull.INSTANCE;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.util;

import com.zatca.sdk.util.HexUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class ECDSAUtil {
    private static final String SIGNALGORITHMS = "SHA256withECDSA";
    private static final String ALGORITHM = "EC";
    private static final String SECP256K1 = "secp256k1";
    private static final Logger LOG = Logger.getLogger(ECDSAUtil.class);

    public static byte[] signECDSA(PrivateKey privateKey, String data) {
        try {
            Signature signature = Signature.getInstance(SIGNALGORITHMS);
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            byte[] sign = signature.sign();
            return sign;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    public static boolean verifyECDSA(PublicKey publicKey, String signed, byte[] data) {
        try {
            Signature signature = Signature.getInstance(SIGNALGORITHMS);
            signature.initVerify(publicKey);
            signature.update(data);
            byte[] hex = Base64.getDecoder().decode(signed);
            boolean bool = signature.verify(hex);
            return bool;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    public static PrivateKey getPrivateKey(String key) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
        byte[] privateKeyDecrypted = Base64.getDecoder().decode(key);
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyDecrypted));
        Security.removeProvider("BC");
        return privateKey;
    }

    public static PrivateKey loadPrivateKey(String key) throws Exception {
        String privateKeyString = "-----BEGIN EC PRIVATE KEY-----\n" + new String(key).replaceAll("\n", "").replaceAll("\t", "") + "\n-----END EC PRIVATE KEY-----";
        InputStreamReader rdr = new InputStreamReader(new ByteArrayInputStream(privateKeyString.getBytes(StandardCharsets.UTF_8)));
        Object parsed = new PEMParser(rdr).readObject();
        KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair)parsed);
        return pair.getPrivate();
    }

    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] bytes = HexUtil.decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    public static KeyPair getKeyPair() throws Exception {
        BouncyCastleProvider bc = new BouncyCastleProvider();
        Security.insertProviderAt(bc, 1);
        BouncyCastleProvider prov = new BouncyCastleProvider();
        Security.addProvider(prov);
        ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(SECP256K1);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDSA", prov.getName());
        generator.initialize(ecSpec, new SecureRandom());
        return generator.generateKeyPair();
    }

    public static byte[] extractR(String digitalSignature) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(Base64.getDecoder().decode(digitalSignature.getBytes(StandardCharsets.UTF_8)));
        return Arrays.copyOfRange(hash, 0, 32);
    }

    public static byte[] extractS(String digitalSignature) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(Base64.getDecoder().decode(digitalSignature.getBytes(StandardCharsets.UTF_8)));
        return Arrays.copyOfRange(hash, 32, 64);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.util;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathValidator;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.crypto.Cipher;
import javax.crypto.ExemptionMechanism;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;

public interface JcaJceHelper {
    public Cipher createCipher(String var1) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException;

    public Mac createMac(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public KeyAgreement createKeyAgreement(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public AlgorithmParameterGenerator createAlgorithmParameterGenerator(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public AlgorithmParameters createAlgorithmParameters(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public KeyGenerator createKeyGenerator(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public KeyFactory createKeyFactory(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public SecretKeyFactory createSecretKeyFactory(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public KeyPairGenerator createKeyPairGenerator(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public MessageDigest createDigest(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public MessageDigest createMessageDigest(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public Signature createSignature(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public CertificateFactory createCertificateFactory(String var1) throws NoSuchProviderException, CertificateException;

    public SecureRandom createSecureRandom(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public CertPathBuilder createCertPathBuilder(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public CertPathValidator createCertPathValidator(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public CertStore createCertStore(String var1, CertStoreParameters var2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException;

    public ExemptionMechanism createExemptionMechanism(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

    public KeyStore createKeyStore(String var1) throws KeyStoreException, NoSuchProviderException;
}


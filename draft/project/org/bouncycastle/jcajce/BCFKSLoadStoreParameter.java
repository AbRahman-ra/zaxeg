/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import org.bouncycastle.crypto.util.PBKDF2Config;
import org.bouncycastle.crypto.util.PBKDFConfig;
import org.bouncycastle.jcajce.BCLoadStoreParameter;

public class BCFKSLoadStoreParameter
extends BCLoadStoreParameter {
    private final PBKDFConfig storeConfig;
    private final EncryptionAlgorithm encAlg;
    private final MacAlgorithm macAlg;
    private final SignatureAlgorithm sigAlg;
    private final Key sigKey;
    private final X509Certificate[] certificates;
    private final CertChainValidator validator;

    private BCFKSLoadStoreParameter(Builder builder) {
        super(builder.in, builder.out, builder.protectionParameter);
        this.storeConfig = builder.storeConfig;
        this.encAlg = builder.encAlg;
        this.macAlg = builder.macAlg;
        this.sigAlg = builder.sigAlg;
        this.sigKey = builder.sigKey;
        this.certificates = builder.certs;
        this.validator = builder.validator;
    }

    public PBKDFConfig getStorePBKDFConfig() {
        return this.storeConfig;
    }

    public EncryptionAlgorithm getStoreEncryptionAlgorithm() {
        return this.encAlg;
    }

    public MacAlgorithm getStoreMacAlgorithm() {
        return this.macAlg;
    }

    public SignatureAlgorithm getStoreSignatureAlgorithm() {
        return this.sigAlg;
    }

    public Key getStoreSignatureKey() {
        return this.sigKey;
    }

    public X509Certificate[] getStoreCertificates() {
        return this.certificates;
    }

    public CertChainValidator getCertChainValidator() {
        return this.validator;
    }

    public static class Builder {
        private final OutputStream out;
        private final InputStream in;
        private final KeyStore.ProtectionParameter protectionParameter;
        private final Key sigKey;
        private PBKDFConfig storeConfig = new PBKDF2Config.Builder().withIterationCount(16384).withSaltLength(64).withPRF(PBKDF2Config.PRF_SHA512).build();
        private EncryptionAlgorithm encAlg = EncryptionAlgorithm.AES256_CCM;
        private MacAlgorithm macAlg = MacAlgorithm.HmacSHA512;
        private SignatureAlgorithm sigAlg = SignatureAlgorithm.SHA512withECDSA;
        private X509Certificate[] certs = null;
        private CertChainValidator validator;

        public Builder() {
            this((OutputStream)null, (KeyStore.ProtectionParameter)null);
        }

        public Builder(OutputStream outputStream, char[] cArray) {
            this(outputStream, (KeyStore.ProtectionParameter)new KeyStore.PasswordProtection(cArray));
        }

        public Builder(OutputStream outputStream, KeyStore.ProtectionParameter protectionParameter) {
            this.in = null;
            this.out = outputStream;
            this.protectionParameter = protectionParameter;
            this.sigKey = null;
        }

        public Builder(OutputStream outputStream, PrivateKey privateKey) {
            this.in = null;
            this.out = outputStream;
            this.protectionParameter = null;
            this.sigKey = privateKey;
        }

        public Builder(InputStream inputStream, PublicKey publicKey) {
            this.in = inputStream;
            this.out = null;
            this.protectionParameter = null;
            this.sigKey = publicKey;
        }

        public Builder(InputStream inputStream, CertChainValidator certChainValidator) {
            this.in = inputStream;
            this.out = null;
            this.protectionParameter = null;
            this.validator = certChainValidator;
            this.sigKey = null;
        }

        public Builder(InputStream inputStream, char[] cArray) {
            this(inputStream, (KeyStore.ProtectionParameter)new KeyStore.PasswordProtection(cArray));
        }

        public Builder(InputStream inputStream, KeyStore.ProtectionParameter protectionParameter) {
            this.in = inputStream;
            this.out = null;
            this.protectionParameter = protectionParameter;
            this.sigKey = null;
        }

        public Builder withStorePBKDFConfig(PBKDFConfig pBKDFConfig) {
            this.storeConfig = pBKDFConfig;
            return this;
        }

        public Builder withStoreEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
            this.encAlg = encryptionAlgorithm;
            return this;
        }

        public Builder withStoreMacAlgorithm(MacAlgorithm macAlgorithm) {
            this.macAlg = macAlgorithm;
            return this;
        }

        public Builder withCertificates(X509Certificate[] x509CertificateArray) {
            X509Certificate[] x509CertificateArray2 = new X509Certificate[x509CertificateArray.length];
            System.arraycopy(x509CertificateArray, 0, x509CertificateArray2, 0, x509CertificateArray2.length);
            this.certs = x509CertificateArray2;
            return this;
        }

        public Builder withStoreSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
            this.sigAlg = signatureAlgorithm;
            return this;
        }

        public BCFKSLoadStoreParameter build() {
            return new BCFKSLoadStoreParameter(this);
        }
    }

    public static interface CertChainValidator {
        public boolean isValid(X509Certificate[] var1);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum EncryptionAlgorithm {
        AES256_CCM,
        AES256_KWP;

    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum MacAlgorithm {
        HmacSHA512,
        HmacSHA3_512;

    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum SignatureAlgorithm {
        SHA512withDSA,
        SHA3_512withDSA,
        SHA512withECDSA,
        SHA3_512withECDSA,
        SHA512withRSA,
        SHA3_512withRSA;

    }
}


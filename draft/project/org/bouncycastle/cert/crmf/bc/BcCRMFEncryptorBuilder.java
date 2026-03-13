/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cert.crmf.bc;

import java.io.OutputStream;
import java.security.SecureRandom;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.crmf.CRMFException;
import org.bouncycastle.cert.crmf.bc.CRMFHelper;
import org.bouncycastle.crypto.CipherKeyGenerator;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.CipherFactory;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.OutputEncryptor;

public class BcCRMFEncryptorBuilder {
    private final ASN1ObjectIdentifier encryptionOID;
    private final int keySize;
    private CRMFHelper helper = new CRMFHelper();
    private SecureRandom random;

    public BcCRMFEncryptorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this(aSN1ObjectIdentifier, -1);
    }

    public BcCRMFEncryptorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier, int n) {
        this.encryptionOID = aSN1ObjectIdentifier;
        this.keySize = n;
    }

    public BcCRMFEncryptorBuilder setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }

    public OutputEncryptor build() throws CRMFException {
        return new CRMFOutputEncryptor(this.encryptionOID, this.keySize, this.random);
    }

    private class CRMFOutputEncryptor
    implements OutputEncryptor {
        private KeyParameter encKey;
        private AlgorithmIdentifier algorithmIdentifier;
        private Object cipher;

        CRMFOutputEncryptor(ASN1ObjectIdentifier aSN1ObjectIdentifier, int n, SecureRandom secureRandom) throws CRMFException {
            secureRandom = CryptoServicesRegistrar.getSecureRandom(secureRandom);
            CipherKeyGenerator cipherKeyGenerator = BcCRMFEncryptorBuilder.this.helper.createKeyGenerator(aSN1ObjectIdentifier, secureRandom);
            this.encKey = new KeyParameter(cipherKeyGenerator.generateKey());
            this.algorithmIdentifier = BcCRMFEncryptorBuilder.this.helper.generateEncryptionAlgID(aSN1ObjectIdentifier, this.encKey, secureRandom);
            BcCRMFEncryptorBuilder.this.helper;
            this.cipher = CRMFHelper.createContentCipher(true, this.encKey, this.algorithmIdentifier);
        }

        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return this.algorithmIdentifier;
        }

        public OutputStream getOutputStream(OutputStream outputStream) {
            return CipherFactory.createOutputStream(outputStream, this.cipher);
        }

        public GenericKey getKey() {
            return new GenericKey(this.algorithmIdentifier, this.encKey.getKey());
        }
    }
}


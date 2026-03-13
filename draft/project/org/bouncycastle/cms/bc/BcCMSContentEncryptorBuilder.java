/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cms.bc;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.bc.EnvelopedDataHelper;
import org.bouncycastle.crypto.CipherKeyGenerator;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.CipherFactory;
import org.bouncycastle.operator.DefaultSecretKeySizeProvider;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.MacCaptureStream;
import org.bouncycastle.operator.OutputAEADEncryptor;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.SecretKeySizeProvider;

public class BcCMSContentEncryptorBuilder {
    private static final SecretKeySizeProvider KEY_SIZE_PROVIDER = DefaultSecretKeySizeProvider.INSTANCE;
    private final ASN1ObjectIdentifier encryptionOID;
    private final int keySize;
    private EnvelopedDataHelper helper = new EnvelopedDataHelper();
    private SecureRandom random;

    public BcCMSContentEncryptorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this(aSN1ObjectIdentifier, KEY_SIZE_PROVIDER.getKeySize(aSN1ObjectIdentifier));
    }

    public BcCMSContentEncryptorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier, int n) {
        this.encryptionOID = aSN1ObjectIdentifier;
        int n2 = KEY_SIZE_PROVIDER.getKeySize(aSN1ObjectIdentifier);
        if (aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.des_EDE3_CBC)) {
            if (n != 168 && n != n2) {
                throw new IllegalArgumentException("incorrect keySize for encryptionOID passed to builder.");
            }
            this.keySize = 168;
        } else if (aSN1ObjectIdentifier.equals(OIWObjectIdentifiers.desCBC)) {
            if (n != 56 && n != n2) {
                throw new IllegalArgumentException("incorrect keySize for encryptionOID passed to builder.");
            }
            this.keySize = 56;
        } else {
            if (n2 > 0 && n2 != n) {
                throw new IllegalArgumentException("incorrect keySize for encryptionOID passed to builder.");
            }
            this.keySize = n;
        }
    }

    public BcCMSContentEncryptorBuilder setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }

    public OutputEncryptor build() throws CMSException {
        if (this.helper.isAuthEnveloped(this.encryptionOID)) {
            return new CMSAuthOutputEncryptor(this.encryptionOID, this.keySize, this.random);
        }
        return new CMSOutputEncryptor(this.encryptionOID, this.keySize, this.random);
    }

    private static class AADStream
    extends OutputStream {
        private AEADBlockCipher cipher;

        public AADStream(AEADBlockCipher aEADBlockCipher) {
            this.cipher = aEADBlockCipher;
        }

        public void write(byte[] byArray, int n, int n2) throws IOException {
            this.cipher.processAADBytes(byArray, n, n2);
        }

        public void write(int n) throws IOException {
            this.cipher.processAADByte((byte)n);
        }
    }

    private class CMSAuthOutputEncryptor
    extends CMSOutputEncryptor
    implements OutputAEADEncryptor {
        private AEADBlockCipher aeadCipher;
        private MacCaptureStream macOut;

        CMSAuthOutputEncryptor(ASN1ObjectIdentifier aSN1ObjectIdentifier, int n, SecureRandom secureRandom) throws CMSException {
            super(aSN1ObjectIdentifier, n, secureRandom);
            this.aeadCipher = this.getCipher();
        }

        private AEADBlockCipher getCipher() {
            if (!(this.cipher instanceof AEADBlockCipher)) {
                throw new IllegalArgumentException("Unable to create Authenticated Output Encryptor without Authenticaed Data cipher!");
            }
            return (AEADBlockCipher)this.cipher;
        }

        public OutputStream getOutputStream(OutputStream outputStream) {
            this.macOut = new MacCaptureStream(outputStream, this.aeadCipher.getMac().length);
            return CipherFactory.createOutputStream(this.macOut, this.cipher);
        }

        public OutputStream getAADStream() {
            return new AADStream(this.aeadCipher);
        }

        public byte[] getMAC() {
            return this.macOut.getMac();
        }
    }

    private class CMSOutputEncryptor
    implements OutputEncryptor {
        private KeyParameter encKey;
        private AlgorithmIdentifier algorithmIdentifier;
        protected Object cipher;

        CMSOutputEncryptor(ASN1ObjectIdentifier aSN1ObjectIdentifier, int n, SecureRandom secureRandom) throws CMSException {
            if (secureRandom == null) {
                secureRandom = new SecureRandom();
            }
            CipherKeyGenerator cipherKeyGenerator = BcCMSContentEncryptorBuilder.this.helper.createKeyGenerator(aSN1ObjectIdentifier, n, secureRandom);
            this.encKey = new KeyParameter(cipherKeyGenerator.generateKey());
            this.algorithmIdentifier = BcCMSContentEncryptorBuilder.this.helper.generateEncryptionAlgID(aSN1ObjectIdentifier, this.encKey, secureRandom);
            this.cipher = EnvelopedDataHelper.createContentCipher(true, this.encKey, this.algorithmIdentifier);
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


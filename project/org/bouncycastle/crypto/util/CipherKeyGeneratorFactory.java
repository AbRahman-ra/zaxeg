/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.security.SecureRandom;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.CipherKeyGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.DESKeyGenerator;
import org.bouncycastle.crypto.generators.DESedeKeyGenerator;
import org.bouncycastle.crypto.util.AlgorithmIdentifierFactory;

public class CipherKeyGeneratorFactory {
    private CipherKeyGeneratorFactory() {
    }

    public static CipherKeyGenerator createKeyGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier, SecureRandom secureRandom) throws IllegalArgumentException {
        if (NISTObjectIdentifiers.id_aes128_CBC.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 128);
        }
        if (NISTObjectIdentifiers.id_aes192_CBC.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 192);
        }
        if (NISTObjectIdentifiers.id_aes256_CBC.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 256);
        }
        if (NISTObjectIdentifiers.id_aes128_GCM.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 128);
        }
        if (NISTObjectIdentifiers.id_aes192_GCM.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 192);
        }
        if (NISTObjectIdentifiers.id_aes256_GCM.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 256);
        }
        if (NISTObjectIdentifiers.id_aes128_CCM.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 128);
        }
        if (NISTObjectIdentifiers.id_aes192_CCM.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 192);
        }
        if (NISTObjectIdentifiers.id_aes256_CCM.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 256);
        }
        if (PKCSObjectIdentifiers.des_EDE3_CBC.equals(aSN1ObjectIdentifier)) {
            DESedeKeyGenerator dESedeKeyGenerator = new DESedeKeyGenerator();
            dESedeKeyGenerator.init(new KeyGenerationParameters(secureRandom, 192));
            return dESedeKeyGenerator;
        }
        if (NTTObjectIdentifiers.id_camellia128_cbc.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 128);
        }
        if (NTTObjectIdentifiers.id_camellia192_cbc.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 192);
        }
        if (NTTObjectIdentifiers.id_camellia256_cbc.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 256);
        }
        if (KISAObjectIdentifiers.id_seedCBC.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 128);
        }
        if (AlgorithmIdentifierFactory.CAST5_CBC.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 128);
        }
        if (OIWObjectIdentifiers.desCBC.equals(aSN1ObjectIdentifier)) {
            DESKeyGenerator dESKeyGenerator = new DESKeyGenerator();
            dESKeyGenerator.init(new KeyGenerationParameters(secureRandom, 64));
            return dESKeyGenerator;
        }
        if (PKCSObjectIdentifiers.rc4.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 128);
        }
        if (PKCSObjectIdentifiers.RC2_CBC.equals(aSN1ObjectIdentifier)) {
            return CipherKeyGeneratorFactory.createCipherKeyGenerator(secureRandom, 128);
        }
        throw new IllegalArgumentException("cannot recognise cipher: " + aSN1ObjectIdentifier);
    }

    private static CipherKeyGenerator createCipherKeyGenerator(SecureRandom secureRandom, int n) {
        CipherKeyGenerator cipherKeyGenerator = new CipherKeyGenerator();
        cipherKeyGenerator.init(new KeyGenerationParameters(secureRandom, n));
        return cipherKeyGenerator;
    }
}


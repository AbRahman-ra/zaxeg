/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cert.crmf.bc;

import java.security.SecureRandom;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.crmf.CRMFException;
import org.bouncycastle.crypto.CipherKeyGenerator;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.AlgorithmIdentifierFactory;
import org.bouncycastle.crypto.util.CipherFactory;
import org.bouncycastle.crypto.util.CipherKeyGeneratorFactory;

class CRMFHelper {
    CRMFHelper() {
    }

    CipherKeyGenerator createKeyGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier, SecureRandom secureRandom) throws CRMFException {
        try {
            return CipherKeyGeneratorFactory.createKeyGenerator(aSN1ObjectIdentifier, secureRandom);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new CRMFException(illegalArgumentException.getMessage(), illegalArgumentException);
        }
    }

    static Object createContentCipher(boolean bl, CipherParameters cipherParameters, AlgorithmIdentifier algorithmIdentifier) throws CRMFException {
        try {
            return CipherFactory.createContentCipher(bl, cipherParameters, algorithmIdentifier);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new CRMFException(illegalArgumentException.getMessage(), illegalArgumentException);
        }
    }

    AlgorithmIdentifier generateEncryptionAlgID(ASN1ObjectIdentifier aSN1ObjectIdentifier, KeyParameter keyParameter, SecureRandom secureRandom) throws CRMFException {
        try {
            return AlgorithmIdentifierFactory.generateEncryptionAlgID(aSN1ObjectIdentifier, keyParameter.getKey().length * 8, secureRandom);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new CRMFException(illegalArgumentException.getMessage(), illegalArgumentException);
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.qtesla;

import org.bouncycastle.crypto.digests.CSHAKEDigest;
import org.bouncycastle.crypto.digests.SHAKEDigest;

class HashUtils {
    static final int SECURE_HASH_ALGORITHM_KECCAK_128_RATE = 168;
    static final int SECURE_HASH_ALGORITHM_KECCAK_256_RATE = 136;

    HashUtils() {
    }

    static void secureHashAlgorithmKECCAK128(byte[] byArray, int n, int n2, byte[] byArray2, int n3, int n4) {
        SHAKEDigest sHAKEDigest = new SHAKEDigest(128);
        sHAKEDigest.update(byArray2, n3, n4);
        sHAKEDigest.doFinal(byArray, n, n2);
    }

    static void secureHashAlgorithmKECCAK256(byte[] byArray, int n, int n2, byte[] byArray2, int n3, int n4) {
        SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
        sHAKEDigest.update(byArray2, n3, n4);
        sHAKEDigest.doFinal(byArray, n, n2);
    }

    static void customizableSecureHashAlgorithmKECCAK128Simple(byte[] byArray, int n, int n2, short s, byte[] byArray2, int n3, int n4) {
        CSHAKEDigest cSHAKEDigest = new CSHAKEDigest(128, null, new byte[]{(byte)s, (byte)(s >> 8)});
        cSHAKEDigest.update(byArray2, n3, n4);
        cSHAKEDigest.doFinal(byArray, n, n2);
    }

    static void customizableSecureHashAlgorithmKECCAK256Simple(byte[] byArray, int n, int n2, short s, byte[] byArray2, int n3, int n4) {
        CSHAKEDigest cSHAKEDigest = new CSHAKEDigest(256, null, new byte[]{(byte)s, (byte)(s >> 8)});
        cSHAKEDigest.update(byArray2, n3, n4);
        cSHAKEDigest.doFinal(byArray, n, n2);
    }
}


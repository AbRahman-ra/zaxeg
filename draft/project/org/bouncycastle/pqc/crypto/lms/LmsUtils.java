/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.lms.LMSParameters;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;

class LmsUtils {
    LmsUtils() {
    }

    static void u32str(int n, Digest digest) {
        digest.update((byte)(n >>> 24));
        digest.update((byte)(n >>> 16));
        digest.update((byte)(n >>> 8));
        digest.update((byte)n);
    }

    static void u16str(short s, Digest digest) {
        digest.update((byte)(s >>> 8));
        digest.update((byte)s);
    }

    static void byteArray(byte[] byArray, Digest digest) {
        digest.update(byArray, 0, byArray.length);
    }

    static void byteArray(byte[] byArray, int n, int n2, Digest digest) {
        digest.update(byArray, n, n2);
    }

    static int calculateStrength(LMSParameters lMSParameters) {
        if (lMSParameters == null) {
            throw new NullPointerException("lmsParameters cannot be null");
        }
        LMSigParameters lMSigParameters = lMSParameters.getLMSigParam();
        return (1 << lMSigParameters.getH()) * lMSigParameters.getM();
    }
}


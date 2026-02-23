/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util;

import org.bouncycastle.crypto.digests.SHA512tDigest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.util.Arrays;

public class Fingerprint {
    private static char[] encodingTable = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private final byte[] fingerprint;

    public Fingerprint(byte[] byArray) {
        this(byArray, 160);
    }

    public Fingerprint(byte[] byArray, int n) {
        this.fingerprint = Fingerprint.calculateFingerprint(byArray, n);
    }

    public Fingerprint(byte[] byArray, boolean bl) {
        this.fingerprint = bl ? Fingerprint.calculateFingerprintSHA512_160(byArray) : Fingerprint.calculateFingerprint(byArray);
    }

    public byte[] getFingerprint() {
        return Arrays.clone(this.fingerprint);
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i != this.fingerprint.length; ++i) {
            if (i > 0) {
                stringBuffer.append(":");
            }
            stringBuffer.append(encodingTable[this.fingerprint[i] >>> 4 & 0xF]);
            stringBuffer.append(encodingTable[this.fingerprint[i] & 0xF]);
        }
        return stringBuffer.toString();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Fingerprint) {
            return Arrays.areEqual(((Fingerprint)object).fingerprint, this.fingerprint);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.fingerprint);
    }

    public static byte[] calculateFingerprint(byte[] byArray) {
        return Fingerprint.calculateFingerprint(byArray, 160);
    }

    public static byte[] calculateFingerprint(byte[] byArray, int n) {
        if (n % 8 != 0) {
            throw new IllegalArgumentException("bitLength must be a multiple of 8");
        }
        SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
        sHAKEDigest.update(byArray, 0, byArray.length);
        byte[] byArray2 = new byte[n / 8];
        sHAKEDigest.doFinal(byArray2, 0, n / 8);
        return byArray2;
    }

    public static byte[] calculateFingerprintSHA512_160(byte[] byArray) {
        SHA512tDigest sHA512tDigest = new SHA512tDigest(160);
        sHA512tDigest.update(byArray, 0, byArray.length);
        byte[] byArray2 = new byte[sHA512tDigest.getDigestSize()];
        sHA512tDigest.doFinal(byArray2, 0);
        return byArray2;
    }
}


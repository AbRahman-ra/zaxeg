/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.util.Arrays;

class Utils {
    Utils() {
    }

    static byte[] octetStringFixed(byte[] byArray, int n) {
        if (byArray.length != n) {
            throw new IllegalArgumentException("octet string out of range");
        }
        return byArray;
    }

    static byte[] octetStringFixed(byte[] byArray) {
        if (byArray.length < 1 || byArray.length > 32) {
            throw new IllegalArgumentException("octet string out of range");
        }
        return Arrays.clone(byArray);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util;

public class Longs {
    public static final int BYTES = 8;
    public static final int SIZE = 64;

    public static int numberOfLeadingZeros(long l) {
        return Long.numberOfLeadingZeros(l);
    }

    public static int numberOfTrailingZeros(long l) {
        return Long.numberOfTrailingZeros(l);
    }

    public static long reverse(long l) {
        return Long.reverse(l);
    }

    public static long reverseBytes(long l) {
        return Long.reverseBytes(l);
    }

    public static long rotateLeft(long l, int n) {
        return Long.rotateLeft(l, n);
    }

    public static long rotateRight(long l, int n) {
        return Long.rotateRight(l, n);
    }

    public static Long valueOf(long l) {
        return l;
    }
}


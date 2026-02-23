/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util;

public class Integers {
    public static final int BYTES = 4;
    public static final int SIZE = 32;

    public static int numberOfLeadingZeros(int n) {
        return Integer.numberOfLeadingZeros(n);
    }

    public static int numberOfTrailingZeros(int n) {
        return Integer.numberOfTrailingZeros(n);
    }

    public static int reverse(int n) {
        return Integer.reverse(n);
    }

    public static int reverseBytes(int n) {
        return Integer.reverseBytes(n);
    }

    public static int rotateLeft(int n, int n2) {
        return Integer.rotateLeft(n, n2);
    }

    public static int rotateRight(int n, int n2) {
        return Integer.rotateRight(n, n2);
    }

    public static Integer valueOf(int n) {
        return n;
    }
}


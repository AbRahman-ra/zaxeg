/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.raw;

public abstract class Bits {
    public static int bitPermuteStep(int n, int n2, int n3) {
        int n4 = (n ^ n >>> n3) & n2;
        return n4 ^ n4 << n3 ^ n;
    }

    public static long bitPermuteStep(long l, long l2, int n) {
        long l3 = (l ^ l >>> n) & l2;
        return l3 ^ l3 << n ^ l;
    }

    public static int bitPermuteStepSimple(int n, int n2, int n3) {
        return (n & n2) << n3 | n >>> n3 & n2;
    }

    public static long bitPermuteStepSimple(long l, long l2, int n) {
        return (l & l2) << n | l >>> n & l2;
    }
}


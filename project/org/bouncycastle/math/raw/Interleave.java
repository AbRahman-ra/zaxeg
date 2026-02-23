/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.raw;

import org.bouncycastle.math.raw.Bits;

public class Interleave {
    private static final long M32 = 0x55555555L;
    private static final long M64 = 0x5555555555555555L;
    private static final long M64R = -6148914691236517206L;

    public static int expand8to16(int n) {
        n &= 0xFF;
        n = (n | n << 4) & 0xF0F;
        n = (n | n << 2) & 0x3333;
        n = (n | n << 1) & 0x5555;
        return n;
    }

    public static int expand16to32(int n) {
        n &= 0xFFFF;
        n = (n | n << 8) & 0xFF00FF;
        n = (n | n << 4) & 0xF0F0F0F;
        n = (n | n << 2) & 0x33333333;
        n = (n | n << 1) & 0x55555555;
        return n;
    }

    public static long expand32to64(int n) {
        n = Bits.bitPermuteStep(n, 65280, 8);
        n = Bits.bitPermuteStep(n, 0xF000F0, 4);
        n = Bits.bitPermuteStep(n, 0xC0C0C0C, 2);
        n = Bits.bitPermuteStep(n, 0x22222222, 1);
        return ((long)(n >>> 1) & 0x55555555L) << 32 | (long)n & 0x55555555L;
    }

    public static void expand64To128(long l, long[] lArray, int n) {
        l = Bits.bitPermuteStep(l, 0xFFFF0000L, 16);
        l = Bits.bitPermuteStep(l, 0xFF000000FF00L, 8);
        l = Bits.bitPermuteStep(l, 0xF000F000F000F0L, 4);
        l = Bits.bitPermuteStep(l, 0xC0C0C0C0C0C0C0CL, 2);
        l = Bits.bitPermuteStep(l, 0x2222222222222222L, 1);
        lArray[n] = l & 0x5555555555555555L;
        lArray[n + 1] = l >>> 1 & 0x5555555555555555L;
    }

    public static void expand64To128(long[] lArray, int n, int n2, long[] lArray2, int n3) {
        for (int i = 0; i < n2; ++i) {
            Interleave.expand64To128(lArray[n + i], lArray2, n3);
            n3 += 2;
        }
    }

    public static void expand64To128Rev(long l, long[] lArray, int n) {
        l = Bits.bitPermuteStep(l, 0xFFFF0000L, 16);
        l = Bits.bitPermuteStep(l, 0xFF000000FF00L, 8);
        l = Bits.bitPermuteStep(l, 0xF000F000F000F0L, 4);
        l = Bits.bitPermuteStep(l, 0xC0C0C0C0C0C0C0CL, 2);
        l = Bits.bitPermuteStep(l, 0x2222222222222222L, 1);
        lArray[n] = l & 0xAAAAAAAAAAAAAAAAL;
        lArray[n + 1] = l << 1 & 0xAAAAAAAAAAAAAAAAL;
    }

    public static int shuffle(int n) {
        n = Bits.bitPermuteStep(n, 65280, 8);
        n = Bits.bitPermuteStep(n, 0xF000F0, 4);
        n = Bits.bitPermuteStep(n, 0xC0C0C0C, 2);
        n = Bits.bitPermuteStep(n, 0x22222222, 1);
        return n;
    }

    public static long shuffle(long l) {
        l = Bits.bitPermuteStep(l, 0xFFFF0000L, 16);
        l = Bits.bitPermuteStep(l, 0xFF000000FF00L, 8);
        l = Bits.bitPermuteStep(l, 0xF000F000F000F0L, 4);
        l = Bits.bitPermuteStep(l, 0xC0C0C0C0C0C0C0CL, 2);
        l = Bits.bitPermuteStep(l, 0x2222222222222222L, 1);
        return l;
    }

    public static int shuffle2(int n) {
        n = Bits.bitPermuteStep(n, 0xAA00AA, 7);
        n = Bits.bitPermuteStep(n, 52428, 14);
        n = Bits.bitPermuteStep(n, 0xF000F0, 4);
        n = Bits.bitPermuteStep(n, 65280, 8);
        return n;
    }

    public static long shuffle2(long l) {
        l = Bits.bitPermuteStep(l, 0xFF00FF00L, 24);
        l = Bits.bitPermuteStep(l, 0xCC00CC00CC00CCL, 6);
        l = Bits.bitPermuteStep(l, 0xF0F00000F0F0L, 12);
        l = Bits.bitPermuteStep(l, 0xA0A0A0A0A0A0A0AL, 3);
        return l;
    }

    public static long shuffle3(long l) {
        l = Bits.bitPermuteStep(l, 0xAA00AA00AA00AAL, 7);
        l = Bits.bitPermuteStep(l, 0xCCCC0000CCCCL, 14);
        l = Bits.bitPermuteStep(l, 0xF0F0F0F0L, 28);
        return l;
    }

    public static int unshuffle(int n) {
        n = Bits.bitPermuteStep(n, 0x22222222, 1);
        n = Bits.bitPermuteStep(n, 0xC0C0C0C, 2);
        n = Bits.bitPermuteStep(n, 0xF000F0, 4);
        n = Bits.bitPermuteStep(n, 65280, 8);
        return n;
    }

    public static long unshuffle(long l) {
        l = Bits.bitPermuteStep(l, 0x2222222222222222L, 1);
        l = Bits.bitPermuteStep(l, 0xC0C0C0C0C0C0C0CL, 2);
        l = Bits.bitPermuteStep(l, 0xF000F000F000F0L, 4);
        l = Bits.bitPermuteStep(l, 0xFF000000FF00L, 8);
        l = Bits.bitPermuteStep(l, 0xFFFF0000L, 16);
        return l;
    }

    public static int unshuffle2(int n) {
        n = Bits.bitPermuteStep(n, 65280, 8);
        n = Bits.bitPermuteStep(n, 0xF000F0, 4);
        n = Bits.bitPermuteStep(n, 52428, 14);
        n = Bits.bitPermuteStep(n, 0xAA00AA, 7);
        return n;
    }

    public static long unshuffle2(long l) {
        l = Bits.bitPermuteStep(l, 0xA0A0A0A0A0A0A0AL, 3);
        l = Bits.bitPermuteStep(l, 0xF0F00000F0F0L, 12);
        l = Bits.bitPermuteStep(l, 0xCC00CC00CC00CCL, 6);
        l = Bits.bitPermuteStep(l, 0xFF00FF00L, 24);
        return l;
    }

    public static long unshuffle3(long l) {
        return Interleave.shuffle3(l);
    }
}


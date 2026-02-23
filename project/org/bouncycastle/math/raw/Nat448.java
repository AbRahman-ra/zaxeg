/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.raw;

import java.math.BigInteger;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat224;
import org.bouncycastle.util.Pack;

public abstract class Nat448 {
    public static void copy64(long[] lArray, long[] lArray2) {
        lArray2[0] = lArray[0];
        lArray2[1] = lArray[1];
        lArray2[2] = lArray[2];
        lArray2[3] = lArray[3];
        lArray2[4] = lArray[4];
        lArray2[5] = lArray[5];
        lArray2[6] = lArray[6];
    }

    public static void copy64(long[] lArray, int n, long[] lArray2, int n2) {
        lArray2[n2 + 0] = lArray[n + 0];
        lArray2[n2 + 1] = lArray[n + 1];
        lArray2[n2 + 2] = lArray[n + 2];
        lArray2[n2 + 3] = lArray[n + 3];
        lArray2[n2 + 4] = lArray[n + 4];
        lArray2[n2 + 5] = lArray[n + 5];
        lArray2[n2 + 6] = lArray[n + 6];
    }

    public static long[] create64() {
        return new long[7];
    }

    public static long[] createExt64() {
        return new long[14];
    }

    public static boolean eq64(long[] lArray, long[] lArray2) {
        for (int i = 6; i >= 0; --i) {
            if (lArray[i] == lArray2[i]) continue;
            return false;
        }
        return true;
    }

    public static long[] fromBigInteger64(BigInteger bigInteger) {
        if (bigInteger.signum() < 0 || bigInteger.bitLength() > 448) {
            throw new IllegalArgumentException();
        }
        long[] lArray = Nat448.create64();
        for (int i = 0; i < 7; ++i) {
            lArray[i] = bigInteger.longValue();
            bigInteger = bigInteger.shiftRight(64);
        }
        return lArray;
    }

    public static boolean isOne64(long[] lArray) {
        if (lArray[0] != 1L) {
            return false;
        }
        for (int i = 1; i < 7; ++i) {
            if (lArray[i] == 0L) continue;
            return false;
        }
        return true;
    }

    public static boolean isZero64(long[] lArray) {
        for (int i = 0; i < 7; ++i) {
            if (lArray[i] == 0L) continue;
            return false;
        }
        return true;
    }

    public static void mul(int[] nArray, int[] nArray2, int[] nArray3) {
        Nat224.mul(nArray, nArray2, nArray3);
        Nat224.mul(nArray, 7, nArray2, 7, nArray3, 14);
        int n = Nat224.addToEachOther(nArray3, 7, nArray3, 14);
        int n2 = n + Nat224.addTo(nArray3, 0, nArray3, 7, 0);
        n += Nat224.addTo(nArray3, 21, nArray3, 14, n2);
        int[] nArray4 = Nat224.create();
        int[] nArray5 = Nat224.create();
        boolean bl = Nat224.diff(nArray, 7, nArray, 0, nArray4, 0) != Nat224.diff(nArray2, 7, nArray2, 0, nArray5, 0);
        int[] nArray6 = Nat224.createExt();
        Nat224.mul(nArray4, nArray5, nArray6);
        Nat.addWordAt(28, n += bl ? Nat.addTo(14, nArray6, 0, nArray3, 7) : Nat.subFrom(14, nArray6, 0, nArray3, 7), nArray3, 21);
    }

    public static void square(int[] nArray, int[] nArray2) {
        Nat224.square(nArray, nArray2);
        Nat224.square(nArray, 7, nArray2, 14);
        int n = Nat224.addToEachOther(nArray2, 7, nArray2, 14);
        int n2 = n + Nat224.addTo(nArray2, 0, nArray2, 7, 0);
        n += Nat224.addTo(nArray2, 21, nArray2, 14, n2);
        int[] nArray3 = Nat224.create();
        Nat224.diff(nArray, 7, nArray, 0, nArray3, 0);
        int[] nArray4 = Nat224.createExt();
        Nat224.square(nArray3, nArray4);
        Nat.addWordAt(28, n += Nat.subFrom(14, nArray4, 0, nArray2, 7), nArray2, 21);
    }

    public static BigInteger toBigInteger64(long[] lArray) {
        byte[] byArray = new byte[56];
        for (int i = 0; i < 7; ++i) {
            long l = lArray[i];
            if (l == 0L) continue;
            Pack.longToBigEndian(l, byArray, 6 - i << 3);
        }
        return new BigInteger(1, byArray);
    }
}


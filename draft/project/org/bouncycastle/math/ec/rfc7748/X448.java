/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec.rfc7748;

import java.security.SecureRandom;
import org.bouncycastle.math.ec.rfc7748.X448Field;
import org.bouncycastle.math.ec.rfc8032.Ed448;
import org.bouncycastle.util.Arrays;

public abstract class X448 {
    public static final int POINT_SIZE = 56;
    public static final int SCALAR_SIZE = 56;
    private static final int C_A = 156326;
    private static final int C_A24 = 39082;

    public static boolean calculateAgreement(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, int n3) {
        X448.scalarMult(byArray, n, byArray2, n2, byArray3, n3);
        return !Arrays.areAllZeroes(byArray3, n3, 56);
    }

    private static int decode32(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        n2 |= (byArray[++n] & 0xFF) << 16;
        return n2 |= byArray[++n] << 24;
    }

    private static void decodeScalar(byte[] byArray, int n, int[] nArray) {
        for (int i = 0; i < 14; ++i) {
            nArray[i] = X448.decode32(byArray, n + i * 4);
        }
        nArray[0] = nArray[0] & 0xFFFFFFFC;
        nArray[13] = nArray[13] | Integer.MIN_VALUE;
    }

    public static void generatePrivateKey(SecureRandom secureRandom, byte[] byArray) {
        secureRandom.nextBytes(byArray);
        byArray[0] = (byte)(byArray[0] & 0xFC);
        byArray[55] = (byte)(byArray[55] | 0x80);
    }

    public static void generatePublicKey(byte[] byArray, int n, byte[] byArray2, int n2) {
        X448.scalarMultBase(byArray, n, byArray2, n2);
    }

    private static void pointDouble(int[] nArray, int[] nArray2) {
        int[] nArray3 = F.create();
        int[] nArray4 = F.create();
        F.add(nArray, nArray2, nArray3);
        F.sub(nArray, nArray2, nArray4);
        F.sqr(nArray3, nArray3);
        F.sqr(nArray4, nArray4);
        F.mul(nArray3, nArray4, nArray);
        F.sub(nArray3, nArray4, nArray3);
        F.mul(nArray3, 39082, nArray2);
        F.add(nArray2, nArray4, nArray2);
        F.mul(nArray2, nArray3, nArray2);
    }

    public static void precompute() {
        Ed448.precompute();
    }

    public static void scalarMult(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, int n3) {
        int n4;
        int[] nArray = new int[14];
        X448.decodeScalar(byArray, n, nArray);
        int[] nArray2 = F.create();
        F.decode(byArray2, n2, nArray2);
        int[] nArray3 = F.create();
        F.copy(nArray2, 0, nArray3, 0);
        int[] nArray4 = F.create();
        nArray4[0] = 1;
        int[] nArray5 = F.create();
        nArray5[0] = 1;
        int[] nArray6 = F.create();
        int[] nArray7 = F.create();
        int[] nArray8 = F.create();
        int n5 = 447;
        int n6 = 1;
        do {
            F.add(nArray5, nArray6, nArray7);
            F.sub(nArray5, nArray6, nArray5);
            F.add(nArray3, nArray4, nArray6);
            F.sub(nArray3, nArray4, nArray3);
            F.mul(nArray7, nArray3, nArray7);
            F.mul(nArray5, nArray6, nArray5);
            F.sqr(nArray6, nArray6);
            F.sqr(nArray3, nArray3);
            F.sub(nArray6, nArray3, nArray8);
            F.mul(nArray8, 39082, nArray4);
            F.add(nArray4, nArray3, nArray4);
            F.mul(nArray4, nArray8, nArray4);
            F.mul(nArray3, nArray6, nArray3);
            F.sub(nArray7, nArray5, nArray6);
            F.add(nArray7, nArray5, nArray5);
            F.sqr(nArray5, nArray5);
            F.sqr(nArray6, nArray6);
            F.mul(nArray6, nArray2, nArray6);
            n4 = --n5 >>> 5;
            int n7 = n5 & 0x1F;
            int n8 = nArray[n4] >>> n7 & 1;
            F.cswap(n6 ^= n8, nArray3, nArray5);
            F.cswap(n6, nArray4, nArray6);
            n6 = n8;
        } while (n5 >= 2);
        for (n4 = 0; n4 < 2; ++n4) {
            X448.pointDouble(nArray3, nArray4);
        }
        F.inv(nArray4, nArray4);
        F.mul(nArray3, nArray4, nArray3);
        F.normalize(nArray3);
        F.encode(nArray3, byArray3, n3);
    }

    public static void scalarMultBase(byte[] byArray, int n, byte[] byArray2, int n2) {
        int[] nArray = F.create();
        int[] nArray2 = F.create();
        Ed448.scalarMultBaseXY(Friend.INSTANCE, byArray, n, nArray, nArray2);
        F.inv(nArray, nArray);
        F.mul(nArray, nArray2, nArray);
        F.sqr(nArray, nArray);
        F.normalize(nArray);
        F.encode(nArray, byArray2, n2);
    }

    private static class F
    extends X448Field {
        private F() {
        }
    }

    public static class Friend {
        private static final Friend INSTANCE = new Friend();

        private Friend() {
        }
    }
}


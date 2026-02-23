/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.qtesla;

import java.security.SecureRandom;
import org.bouncycastle.pqc.crypto.qtesla.HashUtils;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

class QTesla1p {
    private static final int PARAM_N = 1024;
    private static final int PARAM_Q = 343576577;
    private static final int PARAM_Q_LOG = 29;
    private static final long PARAM_QINV = 2205847551L;
    private static final int PARAM_BARR_MULT = 3;
    private static final int PARAM_BARR_DIV = 30;
    private static final int PARAM_B = 524287;
    private static final int PARAM_B_BITS = 19;
    private static final int PARAM_S_BITS = 8;
    private static final int PARAM_K = 4;
    private static final int PARAM_H = 25;
    private static final int PARAM_D = 22;
    private static final int PARAM_GEN_A = 108;
    private static final int PARAM_KEYGEN_BOUND_E = 554;
    private static final int PARAM_E = 554;
    private static final int PARAM_KEYGEN_BOUND_S = 554;
    private static final int PARAM_S = 554;
    private static final int PARAM_R2_INVN = 13632409;
    private static final int CRYPTO_RANDOMBYTES = 32;
    private static final int CRYPTO_SEEDBYTES = 32;
    private static final int CRYPTO_C_BYTES = 32;
    private static final int HM_BYTES = 40;
    private static final int RADIX32 = 32;
    static final int CRYPTO_BYTES = 2592;
    static final int CRYPTO_SECRETKEYBYTES = 5224;
    static final int CRYPTO_PUBLICKEYBYTES = 14880;
    private static final int maskb1 = 1048575;
    private static int NBLOCKS_SHAKE = 56;
    private static int BPLUS1BYTES = 3;

    QTesla1p() {
    }

    static int generateKeyPair(byte[] byArray, byte[] byArray2, SecureRandom secureRandom) {
        int n;
        int n2 = 0;
        byte[] byArray3 = new byte[32];
        byte[] byArray4 = new byte[224];
        int[] nArray = new int[1024];
        int[] nArray2 = new int[4096];
        int[] nArray3 = new int[4096];
        int[] nArray4 = new int[4096];
        int[] nArray5 = new int[1024];
        secureRandom.nextBytes(byArray3);
        HashUtils.secureHashAlgorithmKECCAK128(byArray4, 0, 224, byArray3, 0, 32);
        for (n = 0; n < 4; ++n) {
            do {
                Gaussian.sample_gauss_poly(++n2, byArray4, n * 32, nArray2, n * 1024);
            } while (QTesla1p.checkPolynomial(nArray2, n * 1024, 554));
        }
        do {
            Gaussian.sample_gauss_poly(++n2, byArray4, 128, nArray, 0);
        } while (QTesla1p.checkPolynomial(nArray, 0, 554));
        QTesla1PPolynomial.poly_uniform(nArray3, byArray4, 160);
        QTesla1PPolynomial.poly_ntt(nArray5, nArray);
        for (n = 0; n < 4; ++n) {
            QTesla1PPolynomial.poly_mul(nArray4, n * 1024, nArray3, n * 1024, nArray5);
            QTesla1PPolynomial.poly_add_correct(nArray4, n * 1024, nArray4, n * 1024, nArray2, n * 1024);
        }
        QTesla1p.encodePublicKey(byArray, nArray4, byArray4, 160);
        QTesla1p.encodePrivateKey(byArray2, nArray, nArray2, byArray4, 160, byArray);
        return 0;
    }

    static int generateSignature(byte[] byArray, byte[] byArray2, int n, int n2, byte[] byArray3, SecureRandom secureRandom) {
        byte[] byArray4 = new byte[32];
        byte[] byArray5 = new byte[32];
        byte[] byArray6 = new byte[144];
        int[] nArray = new int[25];
        short[] sArray = new short[25];
        int[] nArray2 = new int[1024];
        int[] nArray3 = new int[1024];
        int[] nArray4 = new int[1024];
        int[] nArray5 = new int[1024];
        int[] nArray6 = new int[4096];
        int[] nArray7 = new int[4096];
        int[] nArray8 = new int[4096];
        int n3 = 0;
        boolean bl = false;
        System.arraycopy(byArray3, 5152, byArray6, 0, 32);
        byte[] byArray7 = new byte[32];
        secureRandom.nextBytes(byArray7);
        System.arraycopy(byArray7, 0, byArray6, 32, 32);
        HashUtils.secureHashAlgorithmKECCAK128(byArray6, 64, 40, byArray2, 0, n2);
        HashUtils.secureHashAlgorithmKECCAK128(byArray5, 0, 32, byArray6, 0, byArray6.length - 40);
        System.arraycopy(byArray3, 5184, byArray6, byArray6.length - 40, 40);
        QTesla1PPolynomial.poly_uniform(nArray8, byArray3, 5120);
        while (true) {
            int n4;
            QTesla1p.sample_y(nArray2, byArray5, 0, ++n3);
            QTesla1PPolynomial.poly_ntt(nArray3, nArray2);
            for (n4 = 0; n4 < 4; ++n4) {
                QTesla1PPolynomial.poly_mul(nArray6, n4 * 1024, nArray8, n4 * 1024, nArray3);
            }
            QTesla1p.hashFunction(byArray4, 0, nArray6, byArray6, 64);
            QTesla1p.encodeC(nArray, sArray, byArray4, 0);
            QTesla1PPolynomial.sparse_mul8(nArray4, 0, byArray3, 0, nArray, sArray);
            QTesla1PPolynomial.poly_add(nArray5, nArray2, nArray4);
            if (QTesla1p.testRejection(nArray5)) continue;
            for (n4 = 0; n4 < 4; ++n4) {
                QTesla1PPolynomial.sparse_mul8(nArray7, n4 * 1024, byArray3, 1024 * (n4 + 1), nArray, sArray);
                QTesla1PPolynomial.poly_sub(nArray6, n4 * 1024, nArray6, n4 * 1024, nArray7, n4 * 1024);
                bl = QTesla1p.test_correctness(nArray6, n4 * 1024);
                if (bl) break;
            }
            if (!bl) break;
        }
        QTesla1p.encodeSignature(byArray, 0, byArray4, 0, nArray5);
        return 0;
    }

    static int verifying(byte[] byArray, byte[] byArray2, int n, int n2, byte[] byArray3) {
        byte[] byArray4 = new byte[32];
        byte[] byArray5 = new byte[32];
        byte[] byArray6 = new byte[32];
        byte[] byArray7 = new byte[80];
        int[] nArray = new int[25];
        short[] sArray = new short[25];
        int[] nArray2 = new int[4096];
        int[] nArray3 = new int[4096];
        int[] nArray4 = new int[4096];
        int[] nArray5 = new int[4096];
        int[] nArray6 = new int[1024];
        int[] nArray7 = new int[1024];
        int n3 = 0;
        if (n2 != 2592) {
            return -1;
        }
        QTesla1p.decodeSignature(byArray4, nArray6, byArray2, n);
        if (QTesla1p.testZ(nArray6)) {
            return -2;
        }
        QTesla1p.decodePublicKey(nArray2, byArray6, 0, byArray3);
        HashUtils.secureHashAlgorithmKECCAK128(byArray7, 0, 40, byArray, 0, byArray.length);
        HashUtils.secureHashAlgorithmKECCAK128(byArray7, 40, 40, byArray3, 0, 14848);
        QTesla1PPolynomial.poly_uniform(nArray4, byArray6, 0);
        QTesla1p.encodeC(nArray, sArray, byArray4, 0);
        QTesla1PPolynomial.poly_ntt(nArray7, nArray6);
        for (n3 = 0; n3 < 4; ++n3) {
            QTesla1PPolynomial.sparse_mul32(nArray5, n3 * 1024, nArray2, n3 * 1024, nArray, sArray);
            QTesla1PPolynomial.poly_mul(nArray3, n3 * 1024, nArray4, n3 * 1024, nArray7);
            QTesla1PPolynomial.poly_sub_reduce(nArray3, n3 * 1024, nArray3, n3 * 1024, nArray5, n3 * 1024);
        }
        QTesla1p.hashFunction(byArray5, 0, nArray3, byArray7, 0);
        if (!QTesla1p.memoryEqual(byArray4, 0, byArray5, 0, 32)) {
            return -3;
        }
        return 0;
    }

    static void encodePrivateKey(byte[] byArray, int[] nArray, int[] nArray2, byte[] byArray2, int n, byte[] byArray3) {
        int n2;
        int n3 = 0;
        int n4 = 0;
        for (n2 = 0; n2 < 1024; ++n2) {
            byArray[n4 + n2] = (byte)nArray[n2];
        }
        n4 += 1024;
        for (n3 = 0; n3 < 4; ++n3) {
            for (n2 = 0; n2 < 1024; ++n2) {
                byArray[n4 + (n3 * 1024 + n2)] = (byte)nArray2[n3 * 1024 + n2];
            }
        }
        System.arraycopy(byArray2, n, byArray, n4 += 4096, 64);
        HashUtils.secureHashAlgorithmKECCAK128(byArray, n4 += 64, 40, byArray3, 0, 14848);
        n4 += 40;
    }

    static void encodePublicKey(byte[] byArray, int[] nArray, byte[] byArray2, int n) {
        int n2 = 0;
        for (int i = 0; i < 3712; i += 29) {
            QTesla1p.at(byArray, i, 0, nArray[n2] | nArray[n2 + 1] << 29);
            QTesla1p.at(byArray, i, 1, nArray[n2 + 1] >> 3 | nArray[n2 + 2] << 26);
            QTesla1p.at(byArray, i, 2, nArray[n2 + 2] >> 6 | nArray[n2 + 3] << 23);
            QTesla1p.at(byArray, i, 3, nArray[n2 + 3] >> 9 | nArray[n2 + 4] << 20);
            QTesla1p.at(byArray, i, 4, nArray[n2 + 4] >> 12 | nArray[n2 + 5] << 17);
            QTesla1p.at(byArray, i, 5, nArray[n2 + 5] >> 15 | nArray[n2 + 6] << 14);
            QTesla1p.at(byArray, i, 6, nArray[n2 + 6] >> 18 | nArray[n2 + 7] << 11);
            QTesla1p.at(byArray, i, 7, nArray[n2 + 7] >> 21 | nArray[n2 + 8] << 8);
            QTesla1p.at(byArray, i, 8, nArray[n2 + 8] >> 24 | nArray[n2 + 9] << 5);
            QTesla1p.at(byArray, i, 9, nArray[n2 + 9] >> 27 | nArray[n2 + 10] << 2 | nArray[n2 + 11] << 31);
            QTesla1p.at(byArray, i, 10, nArray[n2 + 11] >> 1 | nArray[n2 + 12] << 28);
            QTesla1p.at(byArray, i, 11, nArray[n2 + 12] >> 4 | nArray[n2 + 13] << 25);
            QTesla1p.at(byArray, i, 12, nArray[n2 + 13] >> 7 | nArray[n2 + 14] << 22);
            QTesla1p.at(byArray, i, 13, nArray[n2 + 14] >> 10 | nArray[n2 + 15] << 19);
            QTesla1p.at(byArray, i, 14, nArray[n2 + 15] >> 13 | nArray[n2 + 16] << 16);
            QTesla1p.at(byArray, i, 15, nArray[n2 + 16] >> 16 | nArray[n2 + 17] << 13);
            QTesla1p.at(byArray, i, 16, nArray[n2 + 17] >> 19 | nArray[n2 + 18] << 10);
            QTesla1p.at(byArray, i, 17, nArray[n2 + 18] >> 22 | nArray[n2 + 19] << 7);
            QTesla1p.at(byArray, i, 18, nArray[n2 + 19] >> 25 | nArray[n2 + 20] << 4);
            QTesla1p.at(byArray, i, 19, nArray[n2 + 20] >> 28 | nArray[n2 + 21] << 1 | nArray[n2 + 22] << 30);
            QTesla1p.at(byArray, i, 20, nArray[n2 + 22] >> 2 | nArray[n2 + 23] << 27);
            QTesla1p.at(byArray, i, 21, nArray[n2 + 23] >> 5 | nArray[n2 + 24] << 24);
            QTesla1p.at(byArray, i, 22, nArray[n2 + 24] >> 8 | nArray[n2 + 25] << 21);
            QTesla1p.at(byArray, i, 23, nArray[n2 + 25] >> 11 | nArray[n2 + 26] << 18);
            QTesla1p.at(byArray, i, 24, nArray[n2 + 26] >> 14 | nArray[n2 + 27] << 15);
            QTesla1p.at(byArray, i, 25, nArray[n2 + 27] >> 17 | nArray[n2 + 28] << 12);
            QTesla1p.at(byArray, i, 26, nArray[n2 + 28] >> 20 | nArray[n2 + 29] << 9);
            QTesla1p.at(byArray, i, 27, nArray[n2 + 29] >> 23 | nArray[n2 + 30] << 6);
            QTesla1p.at(byArray, i, 28, nArray[n2 + 30] >> 26 | nArray[n2 + 31] << 3);
            n2 += 32;
        }
        System.arraycopy(byArray2, n, byArray, 14848, 32);
    }

    static void decodePublicKey(int[] nArray, byte[] byArray, int n, byte[] byArray2) {
        int n2 = 0;
        byte[] byArray3 = byArray2;
        int n3 = 0x1FFFFFFF;
        for (int i = 0; i < 4096; i += 32) {
            nArray[i] = QTesla1p.at(byArray3, n2, 0) & n3;
            nArray[i + 1] = (QTesla1p.at(byArray3, n2, 0) >>> 29 | QTesla1p.at(byArray3, n2, 1) << 3) & n3;
            nArray[i + 2] = (QTesla1p.at(byArray3, n2, 1) >>> 26 | QTesla1p.at(byArray3, n2, 2) << 6) & n3;
            nArray[i + 3] = (QTesla1p.at(byArray3, n2, 2) >>> 23 | QTesla1p.at(byArray3, n2, 3) << 9) & n3;
            nArray[i + 4] = (QTesla1p.at(byArray3, n2, 3) >>> 20 | QTesla1p.at(byArray3, n2, 4) << 12) & n3;
            nArray[i + 5] = (QTesla1p.at(byArray3, n2, 4) >>> 17 | QTesla1p.at(byArray3, n2, 5) << 15) & n3;
            nArray[i + 6] = (QTesla1p.at(byArray3, n2, 5) >>> 14 | QTesla1p.at(byArray3, n2, 6) << 18) & n3;
            nArray[i + 7] = (QTesla1p.at(byArray3, n2, 6) >>> 11 | QTesla1p.at(byArray3, n2, 7) << 21) & n3;
            nArray[i + 8] = (QTesla1p.at(byArray3, n2, 7) >>> 8 | QTesla1p.at(byArray3, n2, 8) << 24) & n3;
            nArray[i + 9] = (QTesla1p.at(byArray3, n2, 8) >>> 5 | QTesla1p.at(byArray3, n2, 9) << 27) & n3;
            nArray[i + 10] = QTesla1p.at(byArray3, n2, 9) >>> 2 & n3;
            nArray[i + 11] = (QTesla1p.at(byArray3, n2, 9) >>> 31 | QTesla1p.at(byArray3, n2, 10) << 1) & n3;
            nArray[i + 12] = (QTesla1p.at(byArray3, n2, 10) >>> 28 | QTesla1p.at(byArray3, n2, 11) << 4) & n3;
            nArray[i + 13] = (QTesla1p.at(byArray3, n2, 11) >>> 25 | QTesla1p.at(byArray3, n2, 12) << 7) & n3;
            nArray[i + 14] = (QTesla1p.at(byArray3, n2, 12) >>> 22 | QTesla1p.at(byArray3, n2, 13) << 10) & n3;
            nArray[i + 15] = (QTesla1p.at(byArray3, n2, 13) >>> 19 | QTesla1p.at(byArray3, n2, 14) << 13) & n3;
            nArray[i + 16] = (QTesla1p.at(byArray3, n2, 14) >>> 16 | QTesla1p.at(byArray3, n2, 15) << 16) & n3;
            nArray[i + 17] = (QTesla1p.at(byArray3, n2, 15) >>> 13 | QTesla1p.at(byArray3, n2, 16) << 19) & n3;
            nArray[i + 18] = (QTesla1p.at(byArray3, n2, 16) >>> 10 | QTesla1p.at(byArray3, n2, 17) << 22) & n3;
            nArray[i + 19] = (QTesla1p.at(byArray3, n2, 17) >>> 7 | QTesla1p.at(byArray3, n2, 18) << 25) & n3;
            nArray[i + 20] = (QTesla1p.at(byArray3, n2, 18) >>> 4 | QTesla1p.at(byArray3, n2, 19) << 28) & n3;
            nArray[i + 21] = QTesla1p.at(byArray3, n2, 19) >>> 1 & n3;
            nArray[i + 22] = (QTesla1p.at(byArray3, n2, 19) >>> 30 | QTesla1p.at(byArray3, n2, 20) << 2) & n3;
            nArray[i + 23] = (QTesla1p.at(byArray3, n2, 20) >>> 27 | QTesla1p.at(byArray3, n2, 21) << 5) & n3;
            nArray[i + 24] = (QTesla1p.at(byArray3, n2, 21) >>> 24 | QTesla1p.at(byArray3, n2, 22) << 8) & n3;
            nArray[i + 25] = (QTesla1p.at(byArray3, n2, 22) >>> 21 | QTesla1p.at(byArray3, n2, 23) << 11) & n3;
            nArray[i + 26] = (QTesla1p.at(byArray3, n2, 23) >>> 18 | QTesla1p.at(byArray3, n2, 24) << 14) & n3;
            nArray[i + 27] = (QTesla1p.at(byArray3, n2, 24) >>> 15 | QTesla1p.at(byArray3, n2, 25) << 17) & n3;
            nArray[i + 28] = (QTesla1p.at(byArray3, n2, 25) >>> 12 | QTesla1p.at(byArray3, n2, 26) << 20) & n3;
            nArray[i + 29] = (QTesla1p.at(byArray3, n2, 26) >>> 9 | QTesla1p.at(byArray3, n2, 27) << 23) & n3;
            nArray[i + 30] = (QTesla1p.at(byArray3, n2, 27) >>> 6 | QTesla1p.at(byArray3, n2, 28) << 26) & n3;
            nArray[i + 31] = QTesla1p.at(byArray3, n2, 28) >>> 3;
            n2 += 29;
        }
        System.arraycopy(byArray2, 14848, byArray, n, 32);
    }

    private static boolean testZ(int[] nArray) {
        for (int i = 0; i < 1024; ++i) {
            if (nArray[i] >= -523733 && nArray[i] <= 523733) continue;
            return true;
        }
        return false;
    }

    static void encodeSignature(byte[] byArray, int n, byte[] byArray2, int n2, int[] nArray) {
        int n3 = 0;
        for (int i = 0; i < 640; i += 10) {
            QTesla1p.at(byArray, i, 0, nArray[n3] & 0xFFFFF | nArray[n3 + 1] << 20);
            QTesla1p.at(byArray, i, 1, nArray[n3 + 1] >>> 12 & 0xFF | (nArray[n3 + 2] & 0xFFFFF) << 8 | nArray[n3 + 3] << 28);
            QTesla1p.at(byArray, i, 2, nArray[n3 + 3] >>> 4 & 0xFFFF | nArray[n3 + 4] << 16);
            QTesla1p.at(byArray, i, 3, nArray[n3 + 4] >>> 16 & 0xF | (nArray[n3 + 5] & 0xFFFFF) << 4 | nArray[n3 + 6] << 24);
            QTesla1p.at(byArray, i, 4, nArray[n3 + 6] >>> 8 & 0xFFF | nArray[n3 + 7] << 12);
            QTesla1p.at(byArray, i, 5, nArray[n3 + 8] & 0xFFFFF | nArray[n3 + 9] << 20);
            QTesla1p.at(byArray, i, 6, nArray[n3 + 9] >>> 12 & 0xFF | (nArray[n3 + 10] & 0xFFFFF) << 8 | nArray[n3 + 11] << 28);
            QTesla1p.at(byArray, i, 7, nArray[n3 + 11] >>> 4 & 0xFFFF | nArray[n3 + 12] << 16);
            QTesla1p.at(byArray, i, 8, nArray[n3 + 12] >>> 16 & 0xF | (nArray[n3 + 13] & 0xFFFFF) << 4 | nArray[n3 + 14] << 24);
            QTesla1p.at(byArray, i, 9, nArray[n3 + 14] >>> 8 & 0xFFF | nArray[n3 + 15] << 12);
            n3 += 16;
        }
        System.arraycopy(byArray2, n2, byArray, n + 2560, 32);
    }

    static void decodeSignature(byte[] byArray, int[] nArray, byte[] byArray2, int n) {
        int n2 = 0;
        for (int i = 0; i < 1024; i += 16) {
            int n3 = QTesla1p.at(byArray2, n2, 0);
            int n4 = QTesla1p.at(byArray2, n2, 1);
            int n5 = QTesla1p.at(byArray2, n2, 2);
            int n6 = QTesla1p.at(byArray2, n2, 3);
            int n7 = QTesla1p.at(byArray2, n2, 4);
            int n8 = QTesla1p.at(byArray2, n2, 5);
            int n9 = QTesla1p.at(byArray2, n2, 6);
            int n10 = QTesla1p.at(byArray2, n2, 7);
            int n11 = QTesla1p.at(byArray2, n2, 8);
            int n12 = QTesla1p.at(byArray2, n2, 9);
            nArray[i] = n3 << 12 >> 12;
            nArray[i + 1] = n3 >>> 20 | n4 << 24 >> 12;
            nArray[i + 2] = n4 << 4 >> 12;
            nArray[i + 3] = n4 >>> 28 | n5 << 16 >> 12;
            nArray[i + 4] = n5 >>> 16 | n6 << 28 >> 12;
            nArray[i + 5] = n6 << 8 >> 12;
            nArray[i + 6] = n6 >>> 24 | n7 << 20 >> 12;
            nArray[i + 7] = n7 >> 12;
            nArray[i + 8] = n8 << 12 >> 12;
            nArray[i + 9] = n8 >>> 20 | n9 << 24 >> 12;
            nArray[i + 10] = n9 << 4 >> 12;
            nArray[i + 11] = n9 >>> 28 | n10 << 16 >> 12;
            nArray[i + 12] = n10 >>> 16 | n11 << 28 >> 12;
            nArray[i + 13] = n11 << 8 >> 12;
            nArray[i + 14] = n11 >>> 24 | n12 << 20 >> 12;
            nArray[i + 15] = n12 >> 12;
            n2 += 10;
        }
        System.arraycopy(byArray2, n + 2560, byArray, 0, 32);
    }

    static void encodeC(int[] nArray, short[] sArray, byte[] byArray, int n) {
        int n2 = 0;
        short s = 0;
        short[] sArray2 = new short[1024];
        byte[] byArray2 = new byte[168];
        short s2 = s;
        s = (short)(s + 1);
        HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 168, s2, byArray, n, 32);
        Arrays.fill(sArray2, (short)0);
        int n3 = 0;
        while (n3 < 25) {
            if (n2 > 165) {
                short s3 = s;
                s = (short)(s + 1);
                HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 168, s3, byArray, n, 32);
                n2 = 0;
            }
            int n4 = byArray2[n2] << 8 | byArray2[n2 + 1] & 0xFF;
            if (sArray2[n4 &= 0x3FF] == 0) {
                sArray2[n4] = (byArray2[n2 + 2] & 1) == 1 ? -1 : 1;
                nArray[n3] = n4;
                sArray[n3] = sArray2[n4];
                ++n3;
            }
            n2 += 3;
        }
    }

    private static void hashFunction(byte[] byArray, int n, int[] nArray, byte[] byArray2, int n2) {
        byte[] byArray3 = new byte[4176];
        for (int i = 0; i < 4; ++i) {
            int n3 = i * 1024;
            for (int j = 0; j < 1024; ++j) {
                int n4 = nArray[n3];
                int n5 = 171788288 - n4 >> 31;
                n4 = n4 - 343576577 & n5 | n4 & ~n5;
                int n6 = n4 & 0x3FFFFF;
                n5 = 0x200000 - n6 >> 31;
                n6 = n6 - 0x400000 & n5 | n6 & ~n5;
                byArray3[n3++] = (byte)(n4 - n6 >> 22);
            }
        }
        System.arraycopy(byArray2, n2, byArray3, 4096, 80);
        HashUtils.secureHashAlgorithmKECCAK128(byArray, n, 32, byArray3, 0, byArray3.length);
    }

    static int littleEndianToInt24(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        return n2 |= (byArray[++n] & 0xFF) << 16;
    }

    static void sample_y(int[] nArray, byte[] byArray, int n, int n2) {
        short s;
        int n3 = 0;
        int n4 = 0;
        int n5 = 1024;
        byte[] byArray2 = new byte[1024 * BPLUS1BYTES + 1];
        int n6 = BPLUS1BYTES;
        short s2 = s = (short)(n2 << 8);
        s = (short)(s + 1);
        HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 1024 * n6, s2, byArray, n, 32);
        while (n3 < 1024) {
            if (n4 >= n5 * n6) {
                n5 = NBLOCKS_SHAKE;
                short s3 = s;
                s = (short)(s + 1);
                HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 1024 * n6, s3, byArray, n, 32);
                n4 = 0;
            }
            nArray[n3] = QTesla1p.littleEndianToInt24(byArray2, n4) & 0xFFFFF;
            int n7 = n3;
            nArray[n7] = nArray[n7] - 524287;
            if (nArray[n3] != 524288) {
                ++n3;
            }
            n4 += n6;
        }
    }

    private static void at(byte[] byArray, int n, int n2, int n3) {
        Pack.intToLittleEndian(n3, byArray, n + n2 << 2);
    }

    private static int at(byte[] byArray, int n, int n2) {
        return Pack.littleEndianToInt(byArray, n + n2 << 2);
    }

    static boolean test_correctness(int[] nArray, int n) {
        for (int i = 0; i < 1024; ++i) {
            int n2 = nArray[n + i];
            int n3 = 171788288 - n2 >> 31;
            int n4 = n2 - 343576577 & n3 | n2 & ~n3;
            int n5 = ~(QTesla1p.absolute(n4) - 171787734) >>> 31;
            int n6 = n4;
            n4 = n4 + 0x200000 - 1 >> 22;
            int n7 = ~(QTesla1p.absolute(n4 = n6 - (n4 << 22)) - 2096598) >>> 31;
            if ((n5 | n7) != 1) continue;
            return true;
        }
        return false;
    }

    private static boolean testRejection(int[] nArray) {
        int n = 0;
        for (int i = 0; i < 1024; ++i) {
            n |= 523733 - QTesla1p.absolute(nArray[i]);
        }
        return n >>> 31 != 0;
    }

    private static int absolute(int n) {
        int n2 = n >> 31;
        return (n2 ^ n) - n2;
    }

    private static boolean checkPolynomial(int[] nArray, int n, int n2) {
        int n3;
        int n4 = 0;
        int n5 = 1024;
        int[] nArray2 = new int[1024];
        for (n3 = 0; n3 < 1024; ++n3) {
            nArray2[n3] = QTesla1p.absolute(nArray[n + n3]);
        }
        for (n3 = 0; n3 < 25; ++n3) {
            for (int i = 0; i < n5 - 1; ++i) {
                int n6 = nArray2[i];
                int n7 = nArray2[i + 1];
                int n8 = n7 - n6 >> 31;
                int n9 = n7 & n8 | n6 & ~n8;
                nArray2[i + 1] = n6 & n8 | n7 & ~n8;
                nArray2[i] = n9;
            }
            n4 += nArray2[n5 - 1];
            --n5;
        }
        return n4 > n2;
    }

    static boolean memoryEqual(byte[] byArray, int n, byte[] byArray2, int n2, int n3) {
        if (n + n3 <= byArray.length && n2 + n3 <= byArray2.length) {
            for (int i = 0; i < n3; ++i) {
                if (byArray[n + i] == byArray2[n2 + i]) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    static class Gaussian {
        private static final int CDT_ROWS = 78;
        private static final int CDT_COLS = 2;
        private static final int CHUNK_SIZE = 512;
        private static final int[] cdt_v = new int[]{0, 0, 100790826, 671507412, 300982266, 372236861, 497060329, 1131554536, 686469725, 80027618, 866922278, 352172656, 1036478428, 1164298592, 1193606242, 860014474, 1337215220, 1378472045, 1466664345, 1948467327, 1581745882, 839957239, 1682648210, 1125857607, 1769902286, 2009293508, 1844317078, 664324558, 1906909508, 1466301668, 1958834133, 506071440, 2001317010, 234057451, 2035597220, 671584905, 2062878330, 786178128, 2084290940, 306011771, 2100866422, 714310105, 2113521119, 243698855, 2123049658, 417712145, 2130125692, 9470578, 2135308229, 1840927014, 2139051783, 1246948843, 2141718732, 589890969, 2143592579, 1774056149, 2144891082, 1109874008, 2145778525, 1056451611, 2146376698, 1812177762, 2146774350, 829172876, 2147035066, 313414831, 2147203651, 1956430050, 2147311165, 1160031633, 2147378788, 1398244789, 2147420737, 187242113, 2147446401, 321666415, 2147461886, 1304194029, 2147471101, 2048797972, 2147476510, 1282326805, 2147479641, 831849416, 2147481428, 1574767936, 2147482435, 194943011, 2147482993, 1991776993, 2147483299, 2120655340, 2147483465, 653713809, 2147483553, 799217300, 0x7FFFFFCF, 1380433609, 0x7FFFFFE7, 1329670087, 0x7FFFFFF3, 1873439229, 0x7FFFFFFA, 103862387, 0x7FFFFFFD, 254367675, 0x7FFFFFFE, 1339200562, Integer.MAX_VALUE, 754636301, Integer.MAX_VALUE, 1499965744, Integer.MAX_VALUE, 1850514943, Integer.MAX_VALUE, 2013121736, Integer.MAX_VALUE, 2087512222, Integer.MAX_VALUE, 2121077103, Integer.MAX_VALUE, 2136013361, Integer.MAX_VALUE, 2142568585, Integer.MAX_VALUE, 2145405997, Integer.MAX_VALUE, 2146617281, Integer.MAX_VALUE, 2147127267, Integer.MAX_VALUE, 2147339035, Integer.MAX_VALUE, 2147425762, Integer.MAX_VALUE, 2147460791, Integer.MAX_VALUE, 2147474745, Integer.MAX_VALUE, 2147480227, Integer.MAX_VALUE, 2147482351, Integer.MAX_VALUE, 2147483163, Integer.MAX_VALUE, 2147483469, Integer.MAX_VALUE, 0x7FFFFFBF, Integer.MAX_VALUE, 2147483625, Integer.MAX_VALUE, 0x7FFFFFF8, Integer.MAX_VALUE, 0x7FFFFFFD, Integer.MAX_VALUE, Integer.MAX_VALUE};

        Gaussian() {
        }

        static void sample_gauss_poly(int n, byte[] byArray, int n2, int[] nArray, int n3) {
            int n4 = n << 8;
            byte[] byArray2 = new byte[4096];
            int[] nArray2 = new int[2];
            int n5 = Integer.MAX_VALUE;
            for (int i = 0; i < 1024; i += 512) {
                HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 4096, (short)n4++, byArray, n2, 32);
                for (int j = 0; j < 512; ++j) {
                    nArray[n3 + i + j] = 0;
                    for (int k = 1; k < 78; ++k) {
                        int n6 = 0;
                        for (int i2 = 1; i2 >= 0; --i2) {
                            nArray2[i2] = (QTesla1p.at(byArray2, j * 2, i2) & n5) - (cdt_v[k * 2 + i2] + n6);
                            n6 = nArray2[i2] >> 31;
                        }
                        int n7 = n3 + i + j;
                        nArray[n7] = nArray[n7] + (~n6 & 1);
                    }
                    int n8 = byArray2[(j * 2 << 2) + 3] >> 31;
                    nArray[n3 + i + j] = n8 & -nArray[n3 + i + j] | ~n8 & nArray[n3 + i + j];
                }
            }
        }
    }

    static class QTesla1PPolynomial {
        private static final int[] zeta = new int[]{184007114, 341297933, 172127038, 306069179, 260374244, 269720605, 20436325, 2157599, 36206659, 61987110, 112759694, 92762708, 278504038, 139026960, 183642748, 298230187, 37043356, 230730845, 107820937, 97015745, 156688276, 38891102, 170244636, 259345227, 170077366, 141586883, 100118513, 328793523, 289946488, 263574185, 132014089, 14516260, 87424978, 192691578, 190961717, 262687761, 333967048, 12957952, 326574509, 273585413, 151922543, 195893203, 261889302, 120488377, 169571794, 44896463, 128576039, 68257019, 20594664, 44164717, 36060712, 256009818, 172063915, 211967562, 135533785, 104908181, 203788155, 52968398, 123297488, 44711423, 329131026, 245797804, 220629853, 200431766, 92905498, 215466666, 227373088, 120513729, 274875394, 236766448, 84216704, 97363940, 224003799, 167341181, 333540791, 225846253, 290150331, 137934911, 101127339, 95054535, 7072757, 58600117, 264117725, 207480694, 268253444, 292044590, 166300682, 256585624, 133577520, 119707476, 58169614, 188489502, 184778640, 156039906, 286669262, 112658784, 89254003, 266568758, 290599527, 80715937, 180664712, 225980378, 103512701, 304604206, 327443646, 92082345, 296093912, 144843084, 309484036, 329737605, 141656867, 264967053, 227847682, 328674715, 208663554, 309005608, 315790590, 182996330, 333212133, 203436199, 13052895, 23858345, 173478900, 97132319, 57066271, 70747422, 202106993, 309870606, 56390934, 336126437, 189147643, 219236223, 293351741, 305570320, 18378834, 336914091, 59506067, 277923611, 217306643, 129369847, 308113789, 56954705, 190254906, 199465001, 119331054, 143640880, 17590914, 309468163, 172483421, 153376031, 58864560, 70957183, 237697179, 116097341, 62196815, 80692520, 310642530, 328595292, 12121494, 71200620, 200016287, 235006678, 21821056, 102505389, 183332133, 59734849, 283127491, 313646880, 30359439, 163176989, 50717815, 100183661, 322975554, 92821217, 283119421, 34453836, 303758926, 89460722, 147514506, 175603941, 76494101, 220775631, 304963431, 38821441, 217317485, 301302769, 328727631, 101476595, 270750726, 253708871, 176201368, 324059659, 114780906, 304156831, 273708648, 144095014, 263545324, 179240984, 187811389, 244886526, 202581571, 209325648, 117231636, 182195945, 217965216, 252295904, 332003328, 46153749, 334740528, 62618402, 301165510, 283016648, 212224416, 234984074, 107363471, 125430881, 172821269, 270409387, 156316970, 311644197, 50537885, 248376507, 154072039, 331539029, 48454192, 267029920, 225963915, 16753350, 76840946, 226444843, 108106635, 154887261, 326283837, 101291223, 204194230, 54014060, 104099734, 104245071, 260949411, 333985274, 291682234, 328313139, 29607387, 106291750, 162553334, 275058303, 64179189, 263147140, 15599810, 325103190, 137254480, 66787068, 4755224, 308520011, 181897417, 325162685, 221099032, 131741505, 147534370, 131533267, 144073688, 166398146, 155829711, 252509898, 251605008, 323547097, 216038649, 232629333, 95137254, 287931575, 235583527, 32386598, 76722491, 60825791, 138354268, 400761, 51907675, 197369064, 319840588, 98618414, 84343982, 108113946, 314679670, 134518178, 64988900, 4333172, 295712261, 200707216, 147647414, 318013383, 77682006, 92518996, 42154619, 87464521, 285037574, 332936592, 62635246, 5534097, 308862707, 91097989, 269726589, 273280832, 251670430, 95492698, 21676891, 182964692, 177187742, 294825274, 85128609, 273594538, 93115857, 116308166, 312212122, 18665807, 32192823, 313249299, 98777368, 273984239, 312125377, 205655336, 264861277, 178920022, 341054719, 232663249, 173564046, 176591124, 157537342, 305058098, 277279130, 170028356, 228573747, 31628995, 175280663, 37304323, 122111670, 210658936, 175704183, 314649282, 325535066, 266783938, 301319742, 327923297, 279787306, 304633001, 304153402, 292839078, 147442886, 94150133, 40461238, 221384781, 269671052, 265445273, 208370149, 160863546, 287765159, 339146643, 129600429, 96192870, 113146118, 95879915, 216708053, 285201955, 67756451, 79028039, 309141895, 138447809, 212246614, 12641916, 243544995, 33459809, 76979779, 71155723, 152521243, 200750888, 36425947, 339074467, 319204591, 188312744, 266105966, 280016981, 183723313, 238915015, 23277613, 160934729, 200611286, 163282810, 297928823, 226921588, 86839172, 145317111, 202226936, 51887320, 318474782, 282270658, 221219795, 207597867, 132089009, 334627662, 163952597, 67529059, 173759630, 234865017, 255217646, 277806158, 61964704, 216678166, 96126463, 39218331, 70028373, 4899005, 238135514, 242700690, 284680271, 81041980, 332906491, 463527, 299280916, 204600651, 149654879, 222229829, 26825157, 81825189, 127990873, 200962599, 16149163, 108812393, 217708971, 152638110, 28735779, 5272794, 19720409, 231726324, 49854178, 118319174, 185669526, 223407181, 243138094, 259020958, 308825615, 164156486, 341391280, 192526841, 97036052, 279986894, 20263748, 32228956, 43816679, 343421811, 124320208, 3484106, 31711063, 147679160, 195369505, 54243678, 279088595, 149119313, 301997352, 244557309, 19700779, 138872683, 230523717, 113507709, 135291486, 313025300, 254384479, 219815764, 253574481, 220646316, 124744817, 123915741, 325760383, 123516396, 138140410, 154060994, 314730104, 57286356, 222353426, 76630003, 145380041, 52039855, 229881219, 332902036, 152308429, 95071889, 124799350, 270141530, 47897266, 119620601, 133269057, 138561303, 341820265, 66049665, 273409631, 304306012, 212490958, 210388603, 277413768, 280793261, 223131872, 162407285, 44911970, 316685837, 298709373, 252812339, 230786851, 230319350, 56863422, 341141914, 177295413, 248222411, 215148650, 97970603, 291678055, 161911155, 339645428, 206445182, 31895080, 279676698, 78257775, 268845232, 92545841, 336725589, 47384597, 62216335, 82290365, 89893410, 266117967, 791867, 28042243, 110563426, 183316855, 281174508, 166338432, 86326996, 261473803, 164647535, 84749290, 157518777, 214336587, 72257047, 13358702, 229010735, 204196474, 179927635, 21786785, 330554989, 164559635, 144505300, 280425045, 324057501, 268227440, 323362437, 26891539, 228523003, 166709094, 61174973, 13532911, 42168701, 133044957, 158219357, 220115616, 15174468, 281706353, 283813987, 263212325, 289818392, 247170937, 276072317, 197581495, 33713097, 181695825, 96829354, 32991226, 228583784, 4040287, 65188717, 258204083, 96366799, 176298395, 341574369, 306098123, 218746932, 29191888, 311810435, 305844323, 31614267, 28130094, 72716426, 38568041, 197579396, 14876445, 228525674, 294569685, 2451649, 165929882, 112195415, 204786047, 138216235, 3438132, 126150615, 59754608, 158965324, 268160978, 266231264, 244422459, 306155336, 218178824, 301806695, 208837335, 212153467, 209725081, 269355286, 295716530, 13980580, 264284060, 301901789, 275319045, 107139083, 4006959, 143908623, 139848274, 25357089, 21607040, 340818603, 91260932, 198869267, 45119941, 224113252, 269556513, 42857483, 268925602, 188501450, 235382337, 324688793, 113056679, 177232352, 98280013, 117743899, 87369665, 330110286, 310895756, 268425063, 27568325, 266303142, 181405304, 65876631, 246283438, 127636847, 16153922, 210256884, 9257227, 147272724, 235571791, 340876897, 31558760, 224463520, 229909008, 40943950, 263351999, 14865952, 27279162, 51980445, 99553161, 108121152, 145230283, 217402431, 84060866, 190168688, 46894008, 205718237, 296935065, 331646198, 59709076, 265829428, 214503586, 310273189, 86051634, 247210969, 275872780, 55395653, 302717617, 155583500, 207999042, 293597246, 305796948, 139332832, 198434142, 104197059, 320317582, 101819543, 70813687, 43594385, 241913829, 210308279, 298735610, 151599086, 92093482, 24654121, 52528801, 134711941, 324580593, 293101038, 121757877, 323940193, 276114751, 33522997, 218880483, 46953248, 33126382, 294367143, 161595040, 208968904, 129221110, 323693686, 234366848, 50155901, 123936119, 72127416, 34243899, 171824126, 26019236, 93997235, 28452989, 24219933, 188331672, 181161011, 146526219, 186502916, 258266311, 207146754, 206589869, 189836867, 107762500, 129011227, 222324073, 331319091, 36618753, 141615400, 273319528, 246222615, 156139193, 290104141, 154851520, 310226922, 60187406, 73704819, 225899604, 87931539, 142487643, 152682959, 45891249, 212048348, 148547910, 207745063, 4405848, 179269204, 216233362, 230307487, 303352796, 41616117, 47140231, 13452075, 94626849, 48892822, 78453712, 214721933, 300785835, 1512599, 173577933, 163255132, 239883248, 205714288, 306118903, 106953300, 150085654, 77068348, 246390345, 199698311, 280165539, 256497526, 194381508, 78125966, 168327358, 180735395, 145983352, 243342736, 198463602, 83165996, 286431792, 22885329, 271516106, 66137359, 243561376, 324886778, 149497212, 24531379, 32857894, 62778029, 56960216, 224996784, 129315394, 81068505, 277744916, 215817366, 117205172, 195090165, 287841567, 57750901, 162987791, 259309908, 135370005, 194853269, 236792732, 219249166, 42349628, 27805769, 186263338, 310699018, 6491000, 228545163, 315890485, 22219119, 144392189, 15505150, 87848372, 155973124, 20446561, 177725890, 226669021, 205315635, 269580641, 133696452, 189388357, 314652032, 317225560, 304194584, 157633737, 298144493, 185785271, 337434647, 559796, 4438732, 249110619, 184824722, 221490126, 205632858, 172362641, 176702767, 276712118, 296075254, 111221225, 259809961, 15438443, 198021462, 134378223, 162261445, 170746654, 256890644, 125206341, 307078324, 279553989, 170124925, 296845387, 188226544, 295437875, 315053523, 172025817, 279046062, 189967278, 158662482, 192989875, 326540363, 135446089, 98631439, 257379933, 325004289, 26554274, 62190249, 228828648, 274361329, 18518762, 184854759, 210189061, 186836398, 230859454, 206912014, 201250021, 276332768, 119984643, 91358832, 325377399, 69085488, 307352479, 308876137, 208756649, 32865966, 152976045, 207821125, 66426662, 67585526, 118828370, 3107192, 322037257, 146029104, 106553806, 266958791, 89567376, 153815988, 90786397, 271042585, 203781777, 169087756, 315867500, 306916544, 7528726, 327732739, 227901532, 2263402, 14357894, 269740764, 322090105, 59838559, 298337502, 292797139, 337635349, 66476915, 75612762, 328089387, 155232910, 87069405, 36163560, 273715413, 321325749, 218096743, 308178877, 21861281, 180676741, 135208372, 119891712, 122406065, 267537516, 341350322, 87789083, 196340943, 217070591, 83564209, 159382818, 253921239, 184673854, 213569600, 194031064, 35973794, 18071215, 250854127, 115090766, 147707843, 330337973, 266187164, 27853295, 296801215, 254949704, 43331190, 73930201, 35703461, 119780800, 216998106, 12687572, 250863345, 243908221, 330555990, 296216993, 202100577, 111307303, 151049872, 103451600, 237710099, 78658022, 121490075, 134292528, 88277916, 177315676, 186629690, 77848818, 211822377, 145696683, 289190386, 274721999, 328391282, 218772820, 91324151, 321725584, 277577004, 65732866, 275538085, 144429136, 204062923, 177280727, 214204692, 264758257, 169151951, 335535576, 334002493, 281131703, 305997258, 310527888, 136973519, 216764406, 235954329, 254049694, 285174861, 264316834, 11792643, 149333889, 0xCCC0C0A, 261331547, 317320791, 24527858, 118790777, 264146824, 174296812, 332779737, 94199786, 288227027, 172048372};
        private static final int[] zetainv = new int[]{55349550, 249376791, 10796840, 169279765, 79429753, 224785800, 319048719, 26255786, 82245030, 128877559, 194242688, 331783934, 79259743, 58401716, 89526883, 107622248, 126812171, 206603058, 33048689, 37579319, 62444874, 9574084, 8041001, 174424626, 78818320, 129371885, 166295850, 139513654, 199147441, 68038492, 277843711, 65999573, 21850993, 252252426, 124803757, 15185295, 68854578, 54386191, 197879894, 131754200, 265727759, 156946887, 166260901, 255298661, 209284049, 222086502, 264918555, 105866478, 240124977, 192526705, 232269274, 141476000, 47359584, 13020587, 99668356, 92713232, 330889005, 126578471, 223795777, 307873116, 269646376, 300245387, 88626873, 46775362, 315723282, 77389413, 13238604, 195868734, 228485811, 92722450, 325505362, 307602783, 149545513, 130006977, 158902723, 89655338, 184193759, 260012368, 126505986, 147235634, 255787494, 2226255, 76039061, 221170512, 223684865, 208368205, 162899836, 321715296, 35397700, 125479834, 22250828, 69861164, 307413017, 256507172, 188343667, 15487190, 267963815, 277099662, 5941228, 50779438, 45239075, 283738018, 21486472, 73835813, 329218683, 341313175, 115675045, 15843838, 336047851, 36660033, 27709077, 174488821, 139794800, 72533992, 252790180, 189760589, 254009201, 76617786, 237022771, 197547473, 21539320, 340469385, 224748207, 275991051, 277149915, 135755452, 190600532, 310710611, 134819928, 34700440, 36224098, 274491089, 18199178, 252217745, 223591934, 67243809, 142326556, 136664563, 112717123, 156740179, 133387516, 158721818, 325057815, 69215248, 114747929, 281386328, 317022303, 18572288, 86196644, 244945138, 208130488, 17036214, 150586702, 184914095, 153609299, 64530515, 171550760, 28523054, 48138702, 155350033, 46731190, 173451652, 64022588, 36498253, 218370236, 86685933, 172829923, 181315132, 209198354, 145555115, 328138134, 83766616, 232355352, 47501323, 66864459, 166873810, 171213936, 137943719, 122086451, 158751855, 94465958, 339137845, 343016781, 6141930, 157791306, 45432084, 185942840, 39381993, 26351017, 28924545, 154188220, 209880125, 73995936, 138260942, 116907556, 165850687, 323130016, 187603453, 255728205, 328071427, 199184388, 321357458, 27686092, 115031414, 337085577, 32877559, 157313239, 315770808, 301226949, 124327411, 106783845, 148723308, 208206572, 84266669, 180588786, 285825676, 55735010, 148486412, 226371405, 127759211, 65831661, 262508072, 214261183, 118579793, 286616361, 280798548, 310718683, 319045198, 194079365, 18689799, 100015201, 277439218, 72060471, 320691248, 57144785, 260410581, 145112975, 100233841, 197593225, 162841182, 175249219, 265450611, 149195069, 87079051, 63411038, 143878266, 97186232, 266508229, 193490923, 236623277, 37457674, 137862289, 103693329, 180321445, 169998644, 342063978, 42790742, 128854644, 265122865, 294683755, 248949728, 330124502, 296436346, 301960460, 40223781, 113269090, 127343215, 164307373, 339170729, 135831514, 195028667, 131528229, 297685328, 190893618, 201088934, 255645038, 117676973, 269871758, 283389171, 33349655, 188725057, 53472436, 187437384, 97353962, 70257049, 201961177, 306957824, 12257486, 121252504, 214565350, 235814077, 153739710, 136986708, 136429823, 85310266, 157073661, 197050358, 162415566, 155244905, 319356644, 315123588, 249579342, 317557341, 171752451, 309332678, 271449161, 219640458, 293420676, 109209729, 19882891, 214355467, 134607673, 181981537, 49209434, 310450195, 296623329, 124696094, 310053580, 67461826, 19636384, 221818700, 50475539, 18995984, 208864636, 291047776, 318922456, 251483095, 191977491, 44840967, 133268298, 101662748, 299982192, 272762890, 241757034, 23258995, 239379518, 145142435, 204243745, 37779629, 49979331, 135577535, 187993077, 40858960, 288180924, 67703797, 96365608, 257524943, 33303388, 129072991, 77747149, 283867501, 0xB60B0B, 46641512, 137858340, 296682569, 153407889, 259515711, 126174146, 198346294, 235455425, 244023416, 291596132, 316297415, 328710625, 80224578, 302632627, 113667569, 119113057, 312017817, 2699680, 108004786, 196303853, 334319350, 133319693, 327422655, 215939730, 97293139, 277699946, 0x9AA8989, 77273435, 316008252, 75151514, 32680821, 13466291, 256206912, 225832678, 245296564, 166344225, 230519898, 18887784, 108194240, 155075127, 74650975, 300719094, 74020064, 119463325, 298456636, 144707310, 252315645, 2757974, 321969537, 318219488, 203728303, 199667954, 339569618, 236437494, 68257532, 41674788, 79292517, 329595997, 47860047, 74221291, 133851496, 131423110, 134739242, 41769882, 125397753, 37421241, 99154118, 77345313, 75415599, 184611253, 283821969, 217425962, 340138445, 205360342, 138790530, 231381162, 177646695, 341124928, 49006892, 115050903, 328700132, 145997181, 305008536, 270860151, 315446483, 311962310, 37732254, 31766142, 314384689, 124829645, 37478454, 2002208, 167278182, 247209778, 85372494, 278387860, 339536290, 114992793, 310585351, 246747223, 161880752, 309863480, 145995082, 67504260, 96405640, 53758185, 80364252, 59762590, 61870224, 328402109, 123460961, 185357220, 210531620, 301407876, 330043666, 282401604, 176867483, 115053574, 316685038, 20214140, 75349137, 19519076, 63151532, 199071277, 179016942, 13021588, 321789792, 163648942, 139380103, 114565842, 330217875, 271319530, 129239990, 186057800, 258827287, 178929042, 82102774, 257249581, 177238145, 62402069, 160259722, 233013151, 315534334, 342784710, 77458610, 253683167, 261286212, 281360242, 296191980, 6850988, 251030736, 74731345, 265318802, 63899879, 311681497, 137131395, 3931149, 181665422, 51898522, 245605974, 0x7A7A797, 95354166, 166281164, 2434663, 286713155, 113257227, 112789726, 90764238, 44867204, 26890740, 298664607, 181169292, 120444705, 62783316, 66162809, 133187974, 131085619, 39270565, 70166946, 277526912, 1756312, 205015274, 210307520, 223955976, 295679311, 73435047, 218777227, 248504688, 191268148, 10674541, 113695358, 291536722, 198196536, 266946574, 121223151, 286290221, 28846473, 189515583, 205436167, 220060181, 17816194, 219660836, 218831760, 122930261, 90002096, 123760813, 89192098, 30551277, 208285091, 230068868, 113052860, 204703894, 323875798, 99019268, 41579225, 194457264, 64487982, 289332899, 148207072, 195897417, 311865514, 340092471, 219256369, 154766, 299759898, 311347621, 323312829, 63589683, 246540525, 151049736, 2185297, 0xAB1BBBB, 34750962, 84555619, 100438483, 120169396, 157907051, 225257403, 293722399, 111850253, 323856168, 338303783, 314840798, 190938467, 125867606, 234764184, 327427414, 142613978, 215585704, 261751388, 316751420, 121346748, 193921698, 138975926, 44295661, 343113050, 10670086, 262534597, 58896306, 100875887, 105441063, 338677572, 273548204, 304358246, 247450114, 126898411, 281611873, 65770419, 88358931, 108711560, 169816947, 276047518, 179623980, 8948915, 211487568, 135978710, 122356782, 61305919, 25101795, 291689257, 141349641, 198259466, 256737405, 116654989, 45647754, 180293767, 142965291, 182641848, 320298964, 104661562, 159853264, 63559596, 77470611, 155263833, 24371986, 4502110, 307150630, 142825689, 191055334, 272420854, 266596798, 310116768, 100031582, 330934661, 131329963, 205128768, 34434682, 264548538, 275820126, 58374622, 126868524, 247696662, 230430459, 247383707, 213976148, 4429934, 55811418, 182713031, 135206428, 78131304, 73905525, 122191796, 303115339, 249426444, 196133691, 50737499, 39423175, 38943576, 63789271, 15653280, 42256835, 76792639, 18041511, 28927295, 167872394, 132917641, 221464907, 306272254, 168295914, 311947582, 115002830, 173548221, 66297447, 38518479, 186039235, 166985453, 170012531, 110913328, 2521858, 164656555, 78715300, 137921241, 31451200, 69592338, 244799209, 0x1CEC1EE, 311383754, 324910770, 31364455, 227268411, 250460720, 69982039, 258447968, 48751303, 166388835, 160611885, 321899686, 248083879, 91906147, 70295745, 73849988, 252478588, 34713870, 338042480, 280941331, 10639985, 58539003, 256112056, 301421958, 251057581, 265894571, 25563194, 195929163, 142869361, 47864316, 339243405, 278587677, 209058399, 28896907, 235462631, 259232595, 244958163, 23735989, 146207513, 291668902, 343175816, 205222309, 282750786, 266854086, 311189979, 107993050, 55645002, 248439323, 110947244, 127537928, 20029480, 91971569, 91066679, 187746866, 177178431, 199502889, 212043310, 196042207, 211835072, 122477545, 18413892, 161679160, 35056566, 338821353, 276789509, 206322097, 18473387, 327976767, 80429437, 279397388, 68518274, 181023243, 237284827, 313969190, 15263438, 51894343, 9591303, 82627166, 239331506, 239476843, 289562517, 139382347, 242285354, 17292740, 188689316, 235469942, 117131734, 266735631, 326823227, 117612662, 76546657, 295122385, 12037548, 189504538, 95200070, 293038692, 31932380, 187259607, 73167190, 170755308, 218145696, 236213106, 108592503, 131352161, 60559929, 42411067, 280958175, 8836049, 297422828, 11573249, 91280673, 125611361, 161380632, 226344941, 134250929, 140995006, 98690051, 155765188, 164335593, 80031253, 199481563, 69867929, 39419746, 228795671, 19516918, 167375209, 89867706, 72825851, 242099982, 14848946, 42273808, 126259092, 304755136, 38613146, 122800946, 267082476, 167972636, 196062071, 254115855, 39817651, 309122741, 60457156, 250755360, 20601023, 243392916, 292858762, 180399588, 313217138, 29929697, 60449086, 283841728, 160244444, 241071188, 321755521, 108569899, 143560290, 272375957, 331455083, 14981285, 32934047, 262884057, 281379762, 227479236, 105879398, 272619394, 284712017, 190200546, 171093156, 34108414, 325985663, 199935697, 224245523, 144111576, 153321671, 286621872, 35462788, 214206730, 126269934, 65652966, 284070510, 6662486, 325197743, 38006257, 50224836, 124340354, 154428934, 7450140, 287185643, 33705971, 141469584, 272829155, 286510306, 246444258, 170097677, 319718232, 330523682, 140140378, 10364444, 160580247, 27785987, 34570969, 134913023, 14901862, 115728895, 78609524, 201919710, 13838972, 34092541, 198733493, 47482665, 251494232, 16132931, 38972371, 240063876, 117596199, 162911865, 262860640, 52977050, 77007819, 254322574, 230917793, 56907315, 187536671, 158797937, 155087075, 285406963, 223869101, 209999057, 86990953, 177275895, 51531987, 75323133, 136095883, 79458852, 284976460, 336503820, 248522042, 242449238, 205641666, 53426246, 117730324, 10035786, 176235396, 119572778, 246212637, 259359873, 106810129, 68701183, 223062848, 116203489, 128109911, 250671079, 143144811, 122946724, 97778773, 14445551, 298865154, 220279089, 290608179, 139788422, 238668396, 208042792, 131609015, 171512662, 87566759, 307515865, 299411860, 322981913, 275319558, 215000538, 298680114, 174004783, 223088200, 81687275, 147683374, 191654034, 69991164, 17002068, 330618625, 9609529, 80888816, 152614860, 150884999, 256151599, 329060317, 211562488, 80002392, 53630089, 14783054, 243458064, 201989694, 173499211, 84231350, 173331941, 304685475, 186888301, 246560832, 235755640, 112845732, 306533221, 45346390, 159933829, 204549617, 65072539, 250813869, 230816883, 281589467, 307369918, 341418978, 323140252, 73855972, 83202333, 37507398, 171449539, 2278644, 159569463, 171528205};

        QTesla1PPolynomial() {
        }

        static void poly_uniform(int[] nArray, byte[] byArray, int n) {
            short s;
            int n2 = 0;
            int n3 = 0;
            int n4 = 4;
            int n5 = 108;
            int n6 = 0x1FFFFFFF;
            byte[] byArray2 = new byte[18144];
            short s2 = s = 0;
            s = (short)(s + 1);
            HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 18144, s2, byArray, n, 32);
            while (n3 < 4096) {
                if (n2 > 168 * n5 - 4 * n4) {
                    n5 = 1;
                    short s3 = s;
                    s = (short)(s + 1);
                    HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 18144, s3, byArray, n, 32);
                    n2 = 0;
                }
                int n7 = Pack.littleEndianToInt(byArray2, n2) & n6;
                int n8 = Pack.littleEndianToInt(byArray2, n2 += n4) & n6;
                int n9 = Pack.littleEndianToInt(byArray2, n2 += n4) & n6;
                int n10 = Pack.littleEndianToInt(byArray2, n2 += n4) & n6;
                n2 += n4;
                if (n7 < 343576577 && n3 < 4096) {
                    nArray[n3++] = QTesla1PPolynomial.reduce((long)n7 * 13632409L);
                }
                if (n8 < 343576577 && n3 < 4096) {
                    nArray[n3++] = QTesla1PPolynomial.reduce((long)n8 * 13632409L);
                }
                if (n9 < 343576577 && n3 < 4096) {
                    nArray[n3++] = QTesla1PPolynomial.reduce((long)n9 * 13632409L);
                }
                if (n10 >= 343576577 || n3 >= 4096) continue;
                nArray[n3++] = QTesla1PPolynomial.reduce((long)n10 * 13632409L);
            }
        }

        static int reduce(long l) {
            long l2 = l * 2205847551L & 0xFFFFFFFFL;
            return (int)((l += (l2 *= 343576577L)) >> 32);
        }

        static void ntt(int[] nArray, int[] nArray2) {
            int n = 0;
            for (int i = 512; i > 0; i >>= 1) {
                int n2 = 0;
                int n3 = 0;
                while (n3 < 1024) {
                    int n4 = nArray2[n++];
                    for (n2 = n3; n2 < n3 + i; ++n2) {
                        int n5 = nArray[n2];
                        int n6 = nArray[n2 + i];
                        int n7 = QTesla1PPolynomial.reduce((long)n4 * (long)n6);
                        nArray[n2] = QTesla1PPolynomial.correct(n5 + n7 - 343576577);
                        nArray[n2 + i] = QTesla1PPolynomial.correct(n5 - n7);
                    }
                    n3 = n2 + i;
                }
            }
        }

        private static int barr_reduce(int n) {
            int n2 = (int)((long)n * 3L >> 30);
            return n - n2 * 343576577;
        }

        private static int barr_reduce64(long l) {
            long l2 = l * 3L >> 30;
            return (int)(l - l2 * 343576577L);
        }

        private static int correct(int n) {
            return n + (n >> 31 & 0x147A9001);
        }

        static void nttinv(int[] nArray, int n, int[] nArray2) {
            int n2 = 1;
            int n3 = 0;
            for (n2 = 1; n2 < 1024; n2 *= 2) {
                int n4 = 0;
                int n5 = 0;
                while (n5 < 1024) {
                    int n6 = nArray2[n3++];
                    for (n4 = n5; n4 < n5 + n2; ++n4) {
                        int n7 = nArray[n + n4];
                        nArray[n + n4] = QTesla1PPolynomial.barr_reduce(n7 + nArray[n + n4 + n2]);
                        nArray[n + n4 + n2] = QTesla1PPolynomial.reduce((long)n6 * (long)(n7 - nArray[n + n4 + n2]));
                    }
                    n5 = n4 + n2;
                }
            }
        }

        static void poly_ntt(int[] nArray, int[] nArray2) {
            for (int i = 0; i < 1024; ++i) {
                nArray[i] = nArray2[i];
            }
            QTesla1PPolynomial.ntt(nArray, zeta);
        }

        static void poly_pointwise(int[] nArray, int n, int[] nArray2, int n2, int[] nArray3) {
            for (int i = 0; i < 1024; ++i) {
                nArray[i + n] = QTesla1PPolynomial.reduce((long)nArray2[i + n2] * (long)nArray3[i]);
            }
        }

        static void poly_mul(int[] nArray, int n, int[] nArray2, int n2, int[] nArray3) {
            QTesla1PPolynomial.poly_pointwise(nArray, n, nArray2, n2, nArray3);
            QTesla1PPolynomial.nttinv(nArray, n, zetainv);
        }

        static void poly_add(int[] nArray, int[] nArray2, int[] nArray3) {
            for (int i = 0; i < 1024; ++i) {
                nArray[i] = nArray2[i] + nArray3[i];
            }
        }

        static void poly_add_correct(int[] nArray, int n, int[] nArray2, int n2, int[] nArray3, int n3) {
            for (int i = 0; i < 1024; ++i) {
                int n4 = QTesla1PPolynomial.correct(nArray2[n2 + i] + nArray3[n3 + i]);
                nArray[n + i] = QTesla1PPolynomial.correct(n4 - 343576577);
            }
        }

        static void poly_sub(int[] nArray, int n, int[] nArray2, int n2, int[] nArray3, int n3) {
            for (int i = 0; i < 1024; ++i) {
                nArray[n + i] = nArray2[n2 + i] - nArray3[n3 + i];
            }
        }

        static void poly_sub_reduce(int[] nArray, int n, int[] nArray2, int n2, int[] nArray3, int n3) {
            for (int i = 0; i < 1024; ++i) {
                nArray[n + i] = QTesla1PPolynomial.barr_reduce(nArray2[n2 + i] - nArray3[n3 + i]);
            }
        }

        static void sparse_mul8(int[] nArray, int n, byte[] byArray, int n2, int[] nArray2, short[] sArray) {
            int n3;
            for (n3 = 0; n3 < 1024; ++n3) {
                nArray[n + n3] = 0;
            }
            for (n3 = 0; n3 < 25; ++n3) {
                int n4;
                int n5 = nArray2[n3];
                for (n4 = 0; n4 < n5; ++n4) {
                    nArray[n + n4] = nArray[n + n4] - sArray[n3] * byArray[n2 + n4 + 1024 - n5];
                }
                for (n4 = n5; n4 < 1024; ++n4) {
                    nArray[n + n4] = nArray[n + n4] + sArray[n3] * byArray[n2 + n4 - n5];
                }
            }
        }

        static void sparse_mul32(int[] nArray, int n, int[] nArray2, int n2, int[] nArray3, short[] sArray) {
            int n3;
            long[] lArray = new long[1024];
            for (n3 = 0; n3 < 25; ++n3) {
                int n4;
                int n5 = nArray3[n3];
                for (n4 = 0; n4 < n5; ++n4) {
                    lArray[n4] = lArray[n4] - (long)(sArray[n3] * nArray2[n2 + n4 + 1024 - n5]);
                }
                for (n4 = n5; n4 < 1024; ++n4) {
                    lArray[n4] = lArray[n4] + (long)(sArray[n3] * nArray2[n2 + n4 - n5]);
                }
            }
            for (n3 = 0; n3 < 1024; ++n3) {
                nArray[n + n3] = QTesla1PPolynomial.barr_reduce64(lArray[n3]);
            }
        }
    }
}


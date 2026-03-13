/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.qtesla;

import java.security.SecureRandom;
import org.bouncycastle.pqc.crypto.qtesla.HashUtils;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

class QTesla3p {
    private static final int PARAM_N = 2048;
    private static final int PARAM_Q = 856145921;
    private static final int PARAM_Q_LOG = 30;
    private static final long PARAM_QINV = 587710463L;
    private static final long PARAM_BARR_MULT = 5L;
    private static final int PARAM_BARR_DIV = 32;
    private static final int PARAM_B = 0x1FFFFF;
    private static final int PARAM_B_BITS = 21;
    private static final int PARAM_K = 5;
    private static final int PARAM_H = 40;
    private static final int PARAM_D = 24;
    private static final int PARAM_GEN_A = 180;
    private static final int PARAM_KEYGEN_BOUND_E = 901;
    private static final int PARAM_E = 901;
    private static final int PARAM_KEYGEN_BOUND_S = 901;
    private static final int PARAM_S = 901;
    private static final int PARAM_R2_INVN = 513161157;
    private static final int CRYPTO_RANDOMBYTES = 32;
    private static final int CRYPTO_SEEDBYTES = 32;
    private static final int CRYPTO_C_BYTES = 32;
    private static final int HM_BYTES = 40;
    private static final int RADIX32 = 32;
    static final int CRYPTO_BYTES = 5664;
    static final int CRYPTO_SECRETKEYBYTES = 12392;
    static final int CRYPTO_PUBLICKEYBYTES = 38432;
    private static final int maskb1 = 0x3FFFFF;
    private static int NBLOCKS_SHAKE = 56;
    private static int BPLUS1BYTES = 3;

    QTesla3p() {
    }

    static int generateKeyPair(byte[] byArray, byte[] byArray2, SecureRandom secureRandom) {
        int n;
        int n2 = 0;
        byte[] byArray3 = new byte[32];
        byte[] byArray4 = new byte[256];
        long[] lArray = new long[2048];
        long[] lArray2 = new long[10240];
        long[] lArray3 = new long[10240];
        long[] lArray4 = new long[10240];
        long[] lArray5 = new long[2048];
        secureRandom.nextBytes(byArray3);
        HashUtils.secureHashAlgorithmKECCAK256(byArray4, 0, 256, byArray3, 0, 32);
        for (n = 0; n < 5; ++n) {
            do {
                Gaussian.sample_gauss_poly(++n2, byArray4, n * 32, lArray2, n * 2048);
            } while (QTesla3p.checkPolynomial(lArray2, n * 2048, 901));
        }
        do {
            Gaussian.sample_gauss_poly(++n2, byArray4, 160, lArray, 0);
        } while (QTesla3p.checkPolynomial(lArray, 0, 901));
        QTesla3PPolynomial.poly_uniform(lArray3, byArray4, 192);
        QTesla3PPolynomial.poly_ntt(lArray5, lArray);
        for (n = 0; n < 5; ++n) {
            QTesla3PPolynomial.poly_mul(lArray4, n * 2048, lArray3, n * 2048, lArray5);
            QTesla3PPolynomial.poly_add_correct(lArray4, n * 2048, lArray4, n * 2048, lArray2, n * 2048);
        }
        QTesla3p.encodePublicKey(byArray, lArray4, byArray4, 192);
        QTesla3p.encodePrivateKey(byArray2, lArray, lArray2, byArray4, 192, byArray);
        return 0;
    }

    static int generateSignature(byte[] byArray, byte[] byArray2, int n, int n2, byte[] byArray3, SecureRandom secureRandom) {
        byte[] byArray4 = new byte[32];
        byte[] byArray5 = new byte[32];
        byte[] byArray6 = new byte[144];
        int[] nArray = new int[40];
        short[] sArray = new short[40];
        long[] lArray = new long[2048];
        long[] lArray2 = new long[2048];
        long[] lArray3 = new long[2048];
        long[] lArray4 = new long[2048];
        long[] lArray5 = new long[10240];
        long[] lArray6 = new long[10240];
        long[] lArray7 = new long[10240];
        int n3 = 0;
        boolean bl = false;
        System.arraycopy(byArray3, 12320, byArray6, 0, 32);
        byte[] byArray7 = new byte[32];
        secureRandom.nextBytes(byArray7);
        System.arraycopy(byArray7, 0, byArray6, 32, 32);
        HashUtils.secureHashAlgorithmKECCAK256(byArray6, 64, 40, byArray2, 0, n2);
        HashUtils.secureHashAlgorithmKECCAK256(byArray5, 0, 32, byArray6, 0, byArray6.length - 40);
        System.arraycopy(byArray3, 12352, byArray6, byArray6.length - 40, 40);
        QTesla3PPolynomial.poly_uniform(lArray7, byArray3, 12288);
        while (true) {
            int n4;
            QTesla3p.sample_y(lArray, byArray5, 0, ++n3);
            QTesla3PPolynomial.poly_ntt(lArray2, lArray);
            for (n4 = 0; n4 < 5; ++n4) {
                QTesla3PPolynomial.poly_mul(lArray5, n4 * 2048, lArray7, n4 * 2048, lArray2);
            }
            QTesla3p.hashFunction(byArray4, 0, lArray5, byArray6, 64);
            QTesla3p.encodeC(nArray, sArray, byArray4, 0);
            QTesla3PPolynomial.sparse_mul8(lArray3, byArray3, nArray, sArray);
            QTesla3PPolynomial.poly_add(lArray4, lArray, lArray3);
            if (QTesla3p.testRejection(lArray4)) continue;
            for (n4 = 0; n4 < 5; ++n4) {
                QTesla3PPolynomial.sparse_mul8(lArray6, n4 * 2048, byArray3, 2048 * (n4 + 1), nArray, sArray);
                QTesla3PPolynomial.poly_sub(lArray5, n4 * 2048, lArray5, n4 * 2048, lArray6, n4 * 2048);
                bl = QTesla3p.test_correctness(lArray5, n4 * 2048);
                if (bl) break;
            }
            if (!bl) break;
        }
        QTesla3p.encodeSignature(byArray, 0, byArray4, 0, lArray4);
        return 0;
    }

    static int verifying(byte[] byArray, byte[] byArray2, int n, int n2, byte[] byArray3) {
        byte[] byArray4 = new byte[32];
        byte[] byArray5 = new byte[32];
        byte[] byArray6 = new byte[32];
        byte[] byArray7 = new byte[80];
        int[] nArray = new int[40];
        short[] sArray = new short[40];
        int[] nArray2 = new int[10240];
        long[] lArray = new long[10240];
        long[] lArray2 = new long[10240];
        long[] lArray3 = new long[10240];
        long[] lArray4 = new long[2048];
        long[] lArray5 = new long[2048];
        int n3 = 0;
        if (n2 != 5664) {
            return -1;
        }
        QTesla3p.decodeSignature(byArray4, lArray4, byArray2, n);
        if (QTesla3p.testZ(lArray4)) {
            return -2;
        }
        QTesla3p.decodePublicKey(nArray2, byArray6, 0, byArray3);
        HashUtils.secureHashAlgorithmKECCAK256(byArray7, 0, 40, byArray, 0, byArray.length);
        HashUtils.secureHashAlgorithmKECCAK256(byArray7, 40, 40, byArray3, 0, 38400);
        QTesla3PPolynomial.poly_uniform(lArray2, byArray6, 0);
        QTesla3p.encodeC(nArray, sArray, byArray4, 0);
        QTesla3PPolynomial.poly_ntt(lArray5, lArray4);
        for (n3 = 0; n3 < 5; ++n3) {
            QTesla3PPolynomial.sparse_mul32(lArray3, n3 * 2048, nArray2, n3 * 2048, nArray, sArray);
            QTesla3PPolynomial.poly_mul(lArray, n3 * 2048, lArray2, n3 * 2048, lArray5);
            QTesla3PPolynomial.poly_sub(lArray, n3 * 2048, lArray, n3 * 2048, lArray3, n3 * 2048);
        }
        QTesla3p.hashFunction(byArray5, 0, lArray, byArray7, 0);
        if (!QTesla3p.memoryEqual(byArray4, 0, byArray5, 0, 32)) {
            return -3;
        }
        return 0;
    }

    static void encodePrivateKey(byte[] byArray, long[] lArray, long[] lArray2, byte[] byArray2, int n, byte[] byArray3) {
        int n2;
        int n3 = 0;
        int n4 = 0;
        for (n2 = 0; n2 < 2048; ++n2) {
            byArray[n4 + n2] = (byte)lArray[n2];
        }
        n4 += 2048;
        for (n3 = 0; n3 < 5; ++n3) {
            for (n2 = 0; n2 < 2048; ++n2) {
                byArray[n4 + (n3 * 2048 + n2)] = (byte)lArray2[n3 * 2048 + n2];
            }
        }
        System.arraycopy(byArray2, n, byArray, n4 += 10240, 64);
        HashUtils.secureHashAlgorithmKECCAK256(byArray, n4 += 64, 40, byArray3, 0, 38400);
        n4 += 40;
    }

    static void encodePublicKey(byte[] byArray, long[] lArray, byte[] byArray2, int n) {
        int n2 = 0;
        for (int i = 0; i < 9600; i += 15) {
            QTesla3p.at(byArray, i, 0, (int)(lArray[n2] | lArray[n2 + 1] << 30));
            QTesla3p.at(byArray, i, 1, (int)(lArray[n2 + 1] >> 2 | lArray[n2 + 2] << 28));
            QTesla3p.at(byArray, i, 2, (int)(lArray[n2 + 2] >> 4 | lArray[n2 + 3] << 26));
            QTesla3p.at(byArray, i, 3, (int)(lArray[n2 + 3] >> 6 | lArray[n2 + 4] << 24));
            QTesla3p.at(byArray, i, 4, (int)(lArray[n2 + 4] >> 8 | lArray[n2 + 5] << 22));
            QTesla3p.at(byArray, i, 5, (int)(lArray[n2 + 5] >> 10 | lArray[n2 + 6] << 20));
            QTesla3p.at(byArray, i, 6, (int)(lArray[n2 + 6] >> 12 | lArray[n2 + 7] << 18));
            QTesla3p.at(byArray, i, 7, (int)(lArray[n2 + 7] >> 14 | lArray[n2 + 8] << 16));
            QTesla3p.at(byArray, i, 8, (int)(lArray[n2 + 8] >> 16 | lArray[n2 + 9] << 14));
            QTesla3p.at(byArray, i, 9, (int)(lArray[n2 + 9] >> 18 | lArray[n2 + 10] << 12));
            QTesla3p.at(byArray, i, 10, (int)(lArray[n2 + 10] >> 20 | lArray[n2 + 11] << 10));
            QTesla3p.at(byArray, i, 11, (int)(lArray[n2 + 11] >> 22 | lArray[n2 + 12] << 8));
            QTesla3p.at(byArray, i, 12, (int)(lArray[n2 + 12] >> 24 | lArray[n2 + 13] << 6));
            QTesla3p.at(byArray, i, 13, (int)(lArray[n2 + 13] >> 26 | lArray[n2 + 14] << 4));
            QTesla3p.at(byArray, i, 14, (int)(lArray[n2 + 14] >> 28 | lArray[n2 + 15] << 2));
            n2 += 16;
        }
        System.arraycopy(byArray2, n, byArray, 38400, 32);
    }

    static void decodePublicKey(int[] nArray, byte[] byArray, int n, byte[] byArray2) {
        int n2 = 0;
        byte[] byArray3 = byArray2;
        int n3 = 0x3FFFFFFF;
        for (int i = 0; i < 10240; i += 16) {
            nArray[i] = QTesla3p.at(byArray3, n2, 0) & n3;
            nArray[i + 1] = (QTesla3p.at(byArray3, n2, 0) >>> 30 | QTesla3p.at(byArray3, n2, 1) << 2) & n3;
            nArray[i + 2] = (QTesla3p.at(byArray3, n2, 1) >>> 28 | QTesla3p.at(byArray3, n2, 2) << 4) & n3;
            nArray[i + 3] = (QTesla3p.at(byArray3, n2, 2) >>> 26 | QTesla3p.at(byArray3, n2, 3) << 6) & n3;
            nArray[i + 4] = (QTesla3p.at(byArray3, n2, 3) >>> 24 | QTesla3p.at(byArray3, n2, 4) << 8) & n3;
            nArray[i + 5] = (QTesla3p.at(byArray3, n2, 4) >>> 22 | QTesla3p.at(byArray3, n2, 5) << 10) & n3;
            nArray[i + 6] = (QTesla3p.at(byArray3, n2, 5) >>> 20 | QTesla3p.at(byArray3, n2, 6) << 12) & n3;
            nArray[i + 7] = (QTesla3p.at(byArray3, n2, 6) >>> 18 | QTesla3p.at(byArray3, n2, 7) << 14) & n3;
            nArray[i + 8] = (QTesla3p.at(byArray3, n2, 7) >>> 16 | QTesla3p.at(byArray3, n2, 8) << 16) & n3;
            nArray[i + 9] = (QTesla3p.at(byArray3, n2, 8) >>> 14 | QTesla3p.at(byArray3, n2, 9) << 18) & n3;
            nArray[i + 10] = (QTesla3p.at(byArray3, n2, 9) >>> 12 | QTesla3p.at(byArray3, n2, 10) << 20) & n3;
            nArray[i + 11] = (QTesla3p.at(byArray3, n2, 10) >>> 10 | QTesla3p.at(byArray3, n2, 11) << 22) & n3;
            nArray[i + 12] = (QTesla3p.at(byArray3, n2, 11) >>> 8 | QTesla3p.at(byArray3, n2, 12) << 24) & n3;
            nArray[i + 13] = (QTesla3p.at(byArray3, n2, 12) >>> 6 | QTesla3p.at(byArray3, n2, 13) << 26) & n3;
            nArray[i + 14] = (QTesla3p.at(byArray3, n2, 13) >>> 4 | QTesla3p.at(byArray3, n2, 14) << 28) & n3;
            nArray[i + 15] = QTesla3p.at(byArray3, n2, 14) >>> 2 & n3;
            n2 += 15;
        }
        System.arraycopy(byArray2, 38400, byArray, n, 32);
    }

    private static boolean testZ(long[] lArray) {
        for (int i = 0; i < 2048; ++i) {
            if (lArray[i] >= -2096250L && lArray[i] <= 2096250L) continue;
            return true;
        }
        return false;
    }

    static void encodeSignature(byte[] byArray, int n, byte[] byArray2, int n2, long[] lArray) {
        int n3 = 0;
        for (int i = 0; i < 1408; i += 11) {
            QTesla3p.at(byArray, i, 0, (int)(lArray[n3 + 0] & 0x3FFFFFL | lArray[n3 + 1] << 22));
            QTesla3p.at(byArray, i, 1, (int)(lArray[n3 + 1] >>> 10 & 0xFFFL | lArray[n3 + 2] << 12));
            QTesla3p.at(byArray, i, 2, (int)(lArray[n3 + 2] >>> 20 & 3L | (lArray[n3 + 3] & 0x3FFFFFL) << 2 | lArray[n3 + 4] << 24));
            QTesla3p.at(byArray, i, 3, (int)(lArray[n3 + 4] >>> 8 & 0x3FFFL | lArray[n3 + 5] << 14));
            QTesla3p.at(byArray, i, 4, (int)(lArray[n3 + 5] >>> 18 & 0xFL | (lArray[n3 + 6] & 0x3FFFFFL) << 4 | lArray[n3 + 7] << 26));
            QTesla3p.at(byArray, i, 5, (int)(lArray[n3 + 7] >>> 6 & 0xFFFFL | lArray[n3 + 8] << 16));
            QTesla3p.at(byArray, i, 6, (int)(lArray[n3 + 8] >>> 16 & 0x3FL | (lArray[n3 + 9] & 0x3FFFFFL) << 6 | lArray[n3 + 10] << 28));
            QTesla3p.at(byArray, i, 7, (int)(lArray[n3 + 10] >>> 4 & 0x3FFFFL | lArray[n3 + 11] << 18));
            QTesla3p.at(byArray, i, 8, (int)(lArray[n3 + 11] >>> 14 & 0xFFL | (lArray[n3 + 12] & 0x3FFFFFL) << 8 | lArray[n3 + 13] << 30));
            QTesla3p.at(byArray, i, 9, (int)(lArray[n3 + 13] >>> 2 & 0xFFFFFL | lArray[n3 + 14] << 20));
            QTesla3p.at(byArray, i, 10, (int)(lArray[n3 + 14] >>> 12 & 0x3FFL | lArray[n3 + 15] << 10));
            n3 += 16;
        }
        System.arraycopy(byArray2, n2, byArray, n + 5632, 32);
    }

    static void decodeSignature(byte[] byArray, long[] lArray, byte[] byArray2, int n) {
        int n2 = 0;
        for (int i = 0; i < 2048; i += 16) {
            int n3 = QTesla3p.at(byArray2, n2, 0);
            int n4 = QTesla3p.at(byArray2, n2, 1);
            int n5 = QTesla3p.at(byArray2, n2, 2);
            int n6 = QTesla3p.at(byArray2, n2, 3);
            int n7 = QTesla3p.at(byArray2, n2, 4);
            int n8 = QTesla3p.at(byArray2, n2, 5);
            int n9 = QTesla3p.at(byArray2, n2, 6);
            int n10 = QTesla3p.at(byArray2, n2, 7);
            int n11 = QTesla3p.at(byArray2, n2, 8);
            int n12 = QTesla3p.at(byArray2, n2, 9);
            int n13 = QTesla3p.at(byArray2, n2, 10);
            lArray[i] = n3 << 10 >> 10;
            lArray[i + 1] = n3 >>> 22 | n4 << 20 >> 10;
            lArray[i + 2] = n4 >>> 12 | n5 << 30 >> 10;
            lArray[i + 3] = n5 << 8 >> 10;
            lArray[i + 4] = n5 >>> 24 | n6 << 18 >> 10;
            lArray[i + 5] = n6 >>> 14 | n7 << 28 >> 10;
            lArray[i + 6] = n7 << 6 >> 10;
            lArray[i + 7] = n7 >>> 26 | n8 << 16 >> 10;
            lArray[i + 8] = n8 >>> 16 | n9 << 26 >> 10;
            lArray[i + 9] = n9 << 4 >> 10;
            lArray[i + 10] = n9 >>> 28 | n10 << 14 >> 10;
            lArray[i + 11] = n10 >>> 18 | n11 << 24 >> 10;
            lArray[i + 12] = n11 << 2 >> 10;
            lArray[i + 13] = n11 >>> 30 | n12 << 12 >> 10;
            lArray[i + 14] = n12 >>> 20 | n13 << 22 >> 10;
            lArray[i + 15] = n13 >> 10;
            n2 += 11;
        }
        System.arraycopy(byArray2, n + 5632, byArray, 0, 32);
    }

    static void encodeC(int[] nArray, short[] sArray, byte[] byArray, int n) {
        int n2 = 0;
        short s = 0;
        short[] sArray2 = new short[2048];
        byte[] byArray2 = new byte[168];
        short s2 = s;
        s = (short)(s + 1);
        HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 168, s2, byArray, n, 32);
        Arrays.fill(sArray2, (short)0);
        int n3 = 0;
        while (n3 < 40) {
            if (n2 > 165) {
                short s3 = s;
                s = (short)(s + 1);
                HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 168, s3, byArray, n, 32);
                n2 = 0;
            }
            int n4 = byArray2[n2] << 8 | byArray2[n2 + 1] & 0xFF;
            if (sArray2[n4 &= 0x7FF] == 0) {
                sArray2[n4] = (byArray2[n2 + 2] & 1) == 1 ? -1 : 1;
                nArray[n3] = n4;
                sArray[n3] = sArray2[n4];
                ++n3;
            }
            n2 += 3;
        }
    }

    private static void hashFunction(byte[] byArray, int n, long[] lArray, byte[] byArray2, int n2) {
        byte[] byArray3 = new byte[10320];
        for (int i = 0; i < 5; ++i) {
            int n3 = i * 2048;
            for (int j = 0; j < 2048; ++j) {
                int n4 = (int)lArray[n3];
                int n5 = 428072960 - n4 >> 31;
                n4 = n4 - 856145921 & n5 | n4 & ~n5;
                int n6 = n4 & 0xFFFFFF;
                n5 = 0x800000 - n6 >> 31;
                n6 = n6 - 0x1000000 & n5 | n6 & ~n5;
                byArray3[n3++] = (byte)(n4 - n6 >> 24);
            }
        }
        System.arraycopy(byArray2, n2, byArray3, 10240, 80);
        HashUtils.secureHashAlgorithmKECCAK256(byArray, n, 32, byArray3, 0, byArray3.length);
    }

    static int lE24BitToInt(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        return n2 |= (byArray[++n] & 0xFF) << 16;
    }

    static void sample_y(long[] lArray, byte[] byArray, int n, int n2) {
        short s;
        int n3 = 0;
        int n4 = 0;
        int n5 = 2048;
        byte[] byArray2 = new byte[2048 * BPLUS1BYTES + 1];
        int n6 = BPLUS1BYTES;
        short s2 = s = (short)(n2 << 8);
        s = (short)(s + 1);
        HashUtils.customizableSecureHashAlgorithmKECCAK256Simple(byArray2, 0, 2048 * n6, s2, byArray, n, 32);
        while (n3 < 2048) {
            if (n4 >= n5 * n6) {
                n5 = NBLOCKS_SHAKE;
                short s3 = s;
                s = (short)(s + 1);
                HashUtils.customizableSecureHashAlgorithmKECCAK256Simple(byArray2, 0, 2048 * n6, s3, byArray, n, 32);
                n4 = 0;
            }
            lArray[n3] = QTesla3p.lE24BitToInt(byArray2, n4) & 0x3FFFFF;
            int n7 = n3;
            lArray[n7] = lArray[n7] - 0x1FFFFFL;
            if (lArray[n3] != 0x200000L) {
                ++n3;
            }
            n4 += n6;
        }
    }

    private static void at(byte[] byArray, int n, int n2, int n3) {
        Pack.intToLittleEndian(n3, byArray, n * 4 + n2 * 4);
    }

    private static int at(byte[] byArray, int n, int n2) {
        int n3 = n * 4 + n2 * 4;
        int n4 = byArray[n3] & 0xFF;
        n4 |= (byArray[++n3] & 0xFF) << 8;
        n4 |= (byArray[++n3] & 0xFF) << 16;
        return n4 |= byArray[++n3] << 24;
    }

    static boolean test_correctness(long[] lArray, int n) {
        for (int i = 0; i < 2048; ++i) {
            int n2 = (int)(428072960L - lArray[n + i]) >> 31;
            int n3 = (int)(lArray[n + i] - 856145921L & (long)n2 | lArray[n + i] & (long)(~n2));
            int n4 = ~(QTesla3p.absolute(n3) - 428072059) >>> 31;
            int n5 = n3;
            n3 = n3 + 0x800000 - 1 >> 24;
            int n6 = ~(QTesla3p.absolute(n3 = n5 - (n3 << 24)) - 8387707) >>> 31;
            if ((n4 | n6) != 1) continue;
            return true;
        }
        return false;
    }

    private static boolean testRejection(long[] lArray) {
        int n = 0;
        for (int i = 0; i < 2048; ++i) {
            n = (int)((long)n | 2096250L - QTesla3p.absolute(lArray[i]));
        }
        return n >>> 31 > 0;
    }

    private static int absolute(int n) {
        return (n >> 31 ^ n) - (n >> 31);
    }

    private static long absolute(long l) {
        return (l >> 63 ^ l) - (l >> 63);
    }

    private static boolean checkPolynomial(long[] lArray, int n, int n2) {
        int n3;
        int n4 = 0;
        int n5 = 2048;
        long[] lArray2 = new long[2048];
        for (n3 = 0; n3 < 2048; ++n3) {
            lArray2[n3] = QTesla3p.absolute((int)lArray[n + n3]);
        }
        for (n3 = 0; n3 < 40; ++n3) {
            for (int i = 0; i < n5 - 1; ++i) {
                long l = lArray2[i + 1] - lArray2[i] >> 31;
                long l2 = lArray2[i + 1] & l | lArray2[i] & (l ^ 0xFFFFFFFFFFFFFFFFL);
                lArray2[i + 1] = lArray2[i] & l | lArray2[i + 1] & (l ^ 0xFFFFFFFFFFFFFFFFL);
                lArray2[i] = l2;
            }
            n4 += (int)lArray2[n5 - 1];
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
        private static final int CDT_ROWS = 111;
        private static final int CDT_COLS = 4;
        private static final int CHUNK_SIZE = 512;
        private static final long[] cdt_v = new long[]{0L, 0L, 0L, 0L, 100790826L, 671507412L, 773522316L, 511048871L, 300982266L, 372236861L, 1077361076L, 1059759409L, 497060329L, 1131554536L, 291412424L, 1757704870L, 686469725L, 80027618L, 334905296L, 509001536L, 866922278L, 352172656L, 1712883727L, 2032986430L, 1036478428L, 1164298591L, 2125219728L, 373247385L, 1193606242L, 860014474L, 956855218L, 329329358L, 1337215220L, 1378472045L, 161925195L, 487836429L, 1466664345L, 1948467327L, 310898223L, 2073068886L, 1581745882L, 839957238L, 1753315676L, 333401945L, 1682648210L, 1125857606L, 1676773593L, 581940958L, 1769902286L, 2009293507L, 1611293310L, 1722736534L, 1844317078L, 664324558L, 343578873L, 658669348L, 1906909508L, 1466301668L, 267406484L, 1947745304L, 1958834133L, 506071440L, 334189663L, 484025005L, 2001317010L, 234057451L, 1095117285L, 1717635665L, 2035597220L, 671584905L, 701870375L, 699831809L, 2062878330L, 786178128L, 726164822L, 1136608868L, 2084290940L, 306011770L, 1674894630L, 991835989L, 2100866422L, 714310105L, 488689716L, 2009111637L, 2113521119L, 243698855L, 593655431L, 2022065187L, 2123049658L, 417712144L, 2089137196L, 2060028876L, 2130125692L, 9470578L, 1028331278L, 1020621539L, 2135308229L, 1840927013L, 1526892012L, 1181097657L, 2139051783L, 1246948843L, 1357704369L, 1938086978L, 2141718732L, 589890968L, 1884256845L, 139665748L, 2143592579L, 1774056148L, 1941338919L, 987903917L, 2144891082L, 1109874007L, 1865080884L, 1313538965L, 2145778525L, 1056451611L, 627977904L, 165948177L, 2146376698L, 1812177762L, 120802260L, 582398303L, 2146774350L, 829172876L, 477603011L, 473601870L, 2147035066L, 313414830L, 1824672086L, 1000973284L, 2147203651L, 1956430050L, 72036456L, 1645155546L, 2147311165L, 1160031633L, 838355487L, 1294184273L, 2147378788L, 1398244788L, 1748803166L, 735598559L, 2147420737L, 187242113L, 481611375L, 850597049L, 2147446401L, 321666415L, 1283944908L, 1732385397L, 2147461886L, 1304194029L, 1317422290L, 1364763144L, 2147471101L, 2048797972L, 754517418L, 2042604505L, 2147476510L, 1282326805L, 761564954L, 1258404414L, 2147479641L, 831849416L, 750696810L, 1392469358L, 2147481428L, 1574767936L, 164655622L, 1811513013L, 2147482435L, 194943010L, 1532513248L, 1232399747L, 2147482993L, 1991776993L, 1016296796L, 929006971L, 2147483299L, 2120655340L, 1043603591L, 1156388502L, 2147483465L, 653713808L, 2117477881L, 793729097L, 2147483553L, 799217300L, 223606404L, 1502813197L, 0x7FFFFFCFL, 1380433608L, 1556219701L, 964487526L, 0x7FFFFFE7L, 1329670086L, 1687316360L, 30404454L, 0x7FFFFFF3L, 1873439229L, 648445379L, 536487208L, 0x7FFFFFFAL, 103862386L, 2057355703L, 2123410150L, 0x7FFFFFFDL, 254367675L, 987554278L, 1513167912L, 0x7FFFFFFEL, 1339200561L, 1680472557L, 73742693L, Integer.MAX_VALUE, 754636301L, 1391297233L, 642078849L, Integer.MAX_VALUE, 1499965743L, 1552263981L, 411055038L, Integer.MAX_VALUE, 1850514942L, 1987677871L, 230734437L, Integer.MAX_VALUE, 2013121736L, 424671660L, 742369365L, Integer.MAX_VALUE, 2087512222L, 33167986L, 1815987211L, Integer.MAX_VALUE, 2121077102L, 1602401106L, 1471578062L, Integer.MAX_VALUE, 2136013361L, 830825881L, 1469956683L, Integer.MAX_VALUE, 2142568585L, 1139166133L, 2135526978L, Integer.MAX_VALUE, 2145405996L, 1456208623L, 2143928671L, Integer.MAX_VALUE, 2146617280L, 1565562420L, 1104980011L, Integer.MAX_VALUE, 2147127267L, 620102157L, 2069447681L, Integer.MAX_VALUE, 2147339035L, 757681075L, 487993791L, Integer.MAX_VALUE, 2147425761L, 1567995652L, 842732286L, Integer.MAX_VALUE, 2147460790L, 2123906694L, 590947894L, Integer.MAX_VALUE, 2147474745L, 43829804L, 56586263L, Integer.MAX_VALUE, 2147480227L, 543014779L, 389906320L, Integer.MAX_VALUE, 2147482351L, 1064899675L, 1680801885L, Integer.MAX_VALUE, 2147483163L, 598919140L, 1822561999L, Integer.MAX_VALUE, 2147483469L, 513121855L, 1244248835L, Integer.MAX_VALUE, 2147483582L, 2082722777L, 1910280932L, Integer.MAX_VALUE, 2147483624L, 1427178629L, 236458978L, Integer.MAX_VALUE, 0x7FFFFFF7L, 1589410683L, 771680546L, Integer.MAX_VALUE, 0x7FFFFFFDL, 249235829L, 211752505L, Integer.MAX_VALUE, Integer.MAX_VALUE, 0xDDA1A1L, 1843948192L, Integer.MAX_VALUE, Integer.MAX_VALUE, 1422880135L, 37691207L, Integer.MAX_VALUE, Integer.MAX_VALUE, 1904672618L, 1534181260L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2067226300L, 1987742172L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2121317001L, 1156304554L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2139068597L, 1154016330L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2144814274L, 1150215588L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2146648421L, 8934864L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147225872L, 1967808694L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147405175L, 1153330506L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147460084L, 1946199870L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147476669L, 289637131L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147481609L, 439898201L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147483060L, 1103759252L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147483481L, 50312709L, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147483601L, 406195853L, Integer.MAX_VALUE, Integer.MAX_VALUE, 0x7FFFFFF3L, 120196762L, Integer.MAX_VALUE, Integer.MAX_VALUE, 0x7FFFFFFCL, 1008467632L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 107920605L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 1606293274L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2005841951L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2110919166L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2138173553L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2145145487L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2146904460L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147342137L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147449546L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147475542L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147481747L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147483208L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147483548L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2147483625L, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0x7FFFFFFBL, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

        Gaussian() {
        }

        static void sample_gauss_poly(int n, byte[] byArray, int n2, long[] lArray, int n3) {
            int n4 = n << 8;
            byte[] byArray2 = new byte[8192];
            int[] nArray = new int[4];
            int n5 = Integer.MAX_VALUE;
            for (int i = 0; i < 2048; i += 512) {
                HashUtils.customizableSecureHashAlgorithmKECCAK256Simple(byArray2, 0, 8192, (short)n4++, byArray, n2, 32);
                for (int j = 0; j < 512; ++j) {
                    lArray[n3 + i + j] = 0L;
                    for (int k = 1; k < 111; ++k) {
                        int n6 = 0;
                        for (int i2 = 3; i2 >= 0; --i2) {
                            nArray[i2] = (int)((long)(QTesla3p.at(byArray2, 0, j * 4 + i2) & n5) - (cdt_v[k * 4 + i2] + (long)n6));
                            n6 = nArray[i2] >> 31;
                        }
                        int n7 = n3 + i + j;
                        lArray[n7] = lArray[n7] + (long)(~n6 & 1);
                    }
                    int n8 = QTesla3p.at(byArray2, 0, j * 4) >> 31;
                    lArray[n3 + i + j] = (long)n8 & -lArray[n3 + i + j] | (long)(~n8) & lArray[n3 + i + j];
                }
            }
        }
    }

    static class QTesla3PPolynomial {
        private static final long[] zeta = new long[]{147314272L, 762289503L, 284789571L, 461457674L, 723990704L, 123382358L, 685457283L, 458774590L, 644795450L, 723622678L, 441493948L, 676062368L, 648739792L, 214990524L, 261899220L, 138474554L, 205277234L, 788000393L, 541334956L, 769530525L, 786231394L, 812002793L, 251385069L, 152717354L, 674883688L, 458756880L, 323745289L, 823881240L, 686340396L, 716163820L, 107735873L, 144028791L, 586327243L, 71257244L, 739303131L, 487030542L, 313626215L, 396596783L, 664640087L, 728258996L, 854656117L, 567834989L, 2315110L, 210792230L, 795895843L, 433034260L, 432732757L, 480454055L, 750130006L, 47628047L, 2271301L, 98590211L, 729637734L, 683553815L, 476917424L, 121851414L, 296210757L, 820475433L, 403416438L, 605633242L, 804828963L, 435181077L, 781182803L, 276684653L, 329135201L, 697859430L, 248472020L, 396579594L, 109340098L, 97605675L, 755271019L, 565755143L, 534799496L, 378374148L, 85686225L, 298978496L, 650100484L, 712463562L, 818417023L, 283716467L, 269132585L, 153024538L, 223768950L, 331863760L, 761523727L, 586019306L, 805044248L, 810909760L, 77905343L, 401203343L, 162625701L, 616243024L, 659789238L, 385270982L, 720521140L, 545633566L, 688663167L, 740046782L, 257189758L, 115795491L, 101106443L, 409863172L, 622399622L, 405606434L, 498832246L, 730567206L, 350755879L, 41236295L, 561547732L, 525723591L, 18655497L, 3396399L, 289694332L, 221478904L, 738940554L, 769726362L, 32128402L, 693016435L, 275431006L, 65292213L, 601823865L, 469363520L, 480544944L, 607230206L, 473150754L, 267072604L, 463615065L, 412972775L, 197544577L, 770873783L, 189036815L, 407973558L, 110878446L, 442760341L, 667560342L, 756992079L, 663708407L, 585601880L, 763637579L, 660019224L, 424935088L, 249313490L, 844593983L, 664952705L, 274981537L, 40233161L, 655530034L, 742724096L, 8926394L, 67709207L, 616610795L, 539664358L, 306118645L, 741629065L, 283521858L, 621397947L, 369041534L, 162477412L, 258256937L, 269480966L, 75469364L, 815614830L, 724060729L, 510819743L, 489239410L, 265607303L, 103024793L, 434961090L, 474838542L, 234701483L, 505818866L, 450427360L, 188113529L, 650423376L, 599263141L, 720479782L, 755079140L, 469798456L, 745591660L, 432033717L, 530128582L, 94480771L, 722477467L, 169342233L, 35413255L, 89769525L, 424389771L, 240236288L, 360665614L, 66702784L, 76128663L, 565345206L, 605031892L, 393503210L, 249841967L, 485930917L, 45880284L, 746120091L, 684031522L, 537926896L, 408749937L, 608644803L, 692593939L, 515424474L, 748771159L, 155377700L, 347101257L, 393516280L, 708186062L, 809233270L, 562547654L, 768251664L, 651110951L, 574473323L, 588028067L, 352359235L, 646902518L, 410726541L, 134129459L, 460099853L, 829152883L, 819102028L, 7270760L, 562515302L, 419641762L, 347973450L, 161011009L, 401974733L, 619807719L, 559105457L, 276126568L, 165473862L, 380215069L, 356617900L, 347744328L, 615885981L, 824819772L, 811367929L, 6451967L, 515345658L, 648239021L, 56427040L, 709160497L, 71545092L, 390921213L, 17177139L, 194174898L, 825533429L, 497469884L, 88988508L, 64227614L, 641021859L, 159258883L, 529265733L, 823190295L, 567280997L, 414094239L, 238392498L, 695610059L, 416342151L, 90807038L, 206865379L, 568337348L, 168011486L, 844375038L, 777332780L, 147582038L, 199025846L, 396231915L, 151630666L, 466807217L, 12672521L, 570774644L, 764098787L, 283719496L, 779154504L, 383628791L, 851035387L, 395488461L, 291115871L, 52707730L, 776449280L, 479801706L, 73403989L, 402014636L, 255214342L, 56904698L, 446531030L, 639487570L, 848061696L, 202732901L, 739018922L, 653983847L, 453022791L, 391722680L, 584290855L, 270911670L, 390838431L, 653070075L, 535876472L, 83207555L, 131151682L, 505677504L, 778583044L, 472363568L, 734419459L, 768500943L, 321131696L, 371745445L, 751887879L, 51797676L, 157604159L, 838805925L, 358099697L, 763440819L, 776721566L, 719570904L, 304610785L, 656838485L, 239522278L, 796234199L, 659506535L, 825373307L, 674901303L, 250484891L, 54612517L, 410236408L, 111976920L, 728940855L, 720463104L, 559960962L, 514189554L, 637176165L, 436151981L, 485801800L, 802811374L, 549456481L, 808832355L, 112672706L, 199163132L, 807410080L, 645955491L, 365378122L, 222316474L, 381896744L, 693909930L, 402130292L, 199856804L, 277639257L, 6848838L, 648262319L, 601521139L, 108516632L, 392382841L, 563420106L, 475932203L, 249861415L, 99274558L, 152886431L, 744977783L, 269184267L, 562674804L, 760959275L, 733098096L, 771348891L, 674288361L, 631521272L, 513632066L, 476339117L, 621937967L, 206834230L, 507101607L, 420341698L, 528715580L, 853092790L, 580174958L, 278044321L, 432350205L, 603769437L, 144426940L, 733518338L, 365468467L, 848983278L, 385382826L, 846062026L, 593903051L, 216589699L, 219997638L, 350708517L, 733669279L, 624754239L, 499821820L, 772548008L, 199677439L, 287505007L, 144199205L, 215073292L, 825467700L, 101591831L, 571728784L, 841898341L, 420897808L, 61323616L, 823475752L, 72494861L, 89946011L, 236594097L, 379582577L, 539401967L, 221244669L, 479250487L, 100726882L, 263096036L, 647161225L, 491060387L, 419890898L, 816149055L, 546441322L, 690509770L, 215789647L, 5870948L, 821456387L, 294091098L, 783700004L, 278643020L, 520754327L, 813718894L, 123610053L, 157045201L, 265331664L, 807174256L, 258134244L, 703519669L, 300265991L, 41892125L, 662173055L, 439638698L, 494124024L, 700655120L, 535348417L, 37146186L, 379568907L, 644973451L, 554904963L, 594757858L, 477812802L, 266085643L, 46337543L, 454847754L, 496027901L, 701947604L, 5722633L, 790588605L, 233501932L, 728956461L, 462020148L, 214013660L, 155806979L, 159935426L, 423504958L, 638889309L, 602641304L, 277759403L, 71654804L, 710920410L, 108337831L, 641924564L, 252946326L, 463082282L, 23277660L, 142056200L, 263317553L, 9044238L, 367816044L, 349695658L, 291597086L, 230031083L, 385106216L, 281069679L, 644033142L, 134221740L, 212497862L, 686686078L, 787489098L, 781698667L, 748299513L, 774414792L, 380836293L, 114027649L, 766161763L, 10536612L, 707355910L, 100516219L, 637517297L, 21478533L, 769067854L, 668364559L, 410803198L, 64949715L, 643421522L, 525590993L, 585289785L, 423839840L, 554109325L, 450599860L, 295350132L, 435789550L, 306634115L, 611298620L, 777817576L, 553655202L, 804525538L, 794474290L, 138542076L, 780958763L, 62228371L, 738032107L, 684994110L, 661486955L, 67099069L, 68865906L, 32413094L, 358393763L, 205008770L, 849715545L, 289798348L, 384767209L, 787328590L, 823677120L, 47455925L, 706001331L, 612392717L, 487804928L, 731804935L, 520572665L, 442307581L, 351275150L, 726042356L, 667657829L, 254929787L, 459520026L, 625393223L, 319307882L, 77267096L, 815224795L, 335964550L, 408353208L, 604252110L, 574953308L, 563501897L, 515015302L, 313600371L, 178773384L, 417549087L, 510834475L, 167049599L, 488791556L, 664276219L, 82933775L, 822541833L, 17111190L, 409659978L, 96304098L, 500484311L, 269766378L, 327037310L, 584926256L, 538611363L, 404132255L, 170931824L, 744460626L, 154011192L, 322194096L, 215888234L, 258344560L, 702851111L, 192046250L, 738511820L, 530780560L, 57197515L, 335425579L, 410968369L, 830078545L, 448351649L, 208921555L, 356653676L, 718038774L, 424362596L, 158929491L, 420096666L, 387056270L, 797383293L, 381201911L, 466480709L, 373815662L, 84912008L, 4969808L, 524614597L, 93448903L, 559481007L, 400813998L, 665223025L, 601707338L, 466022707L, 192709574L, 615503265L, 822863744L, 639854175L, 158713505L, 0xC2AAA2L, 389196370L, 823105438L, 682974863L, 468401586L, 93508626L, 402414043L, 806357152L, 180544963L, 27876186L, 321527031L, 329857607L, 669501423L, 829809824L, 333202822L, 106923493L, 368991112L, 282317903L, 790323774L, 517381333L, 548329656L, 236147848L, 700119793L, 404187488L, 343578810L, 798813301L, 497964535L, 656188346L, 678161787L, 736817175L, 518031339L, 716647183L, 674797219L, 308643560L, 714308544L, 516103468L, 605229646L, 564549717L, 47650358L, 706404486L, 494887760L, 152496104L, 54954356L, 271435602L, 76951527L, 136123931L, 601823638L, 329273401L, 252710411L, 754980731L, 351648254L, 49239731L, 837833233L, 88830509L, 598216539L, 155534490L, 669603727L, 418388693L, 79322074L, 636251444L, 703683994L, 796989459L, 126497707L, 644863316L, 730359063L, 265213001L, 64483814L, 552208981L, 8135537L, 782474322L, 780853310L, 733976806L, 395661138L, 128188419L, 266691358L, 407092046L, 447349747L, 526245954L, 119272088L, 359659635L, 812410956L, 669835517L, 565139408L, 248981831L, 139910745L, 685462294L, 406991131L, 709944045L, 589819925L, 714299787L, 72923680L, 648836181L, 145321778L, 392775383L, 243093077L, 412955839L, 174619485L, 310936394L, 699727061L, 421087619L, 745421519L, 539546394L, 29471558L, 116471631L, 852650639L, 443777703L, 773131303L, 81618669L, 756719012L, 702785073L, 847088653L, 851830586L, 300908692L, 430974543L, 463215976L, 668971423L, 414271988L, 108350516L, 345933325L, 716417649L, 174980945L, 679092437L, 384030489L, 814050910L, 506580116L, 249434097L, 178438885L, 146797119L, 10369463L, 0x11AA14AAL, 215645133L, 149545847L, 483689845L, 322009569L, 308978588L, 38531178L, 328571637L, 815396967L, 709744233L, 765487128L, 645413104L, 564779557L, 213794315L, 280607549L, 124792697L, 423470554L, 631348430L, 21223627L, 220718413L, 598791979L, 47797633L, 734556299L, 590321944L, 168292920L, 484802055L, 340999812L, 769601438L, 42675060L, 116026587L, 227462622L, 543574607L, 444066479L, 467277895L, 278798674L, 597413704L, 350168725L, 301936652L, 82885511L, 656047519L, 765110538L, 52228202L, 533005731L, 621989298L, 148235931L, 317833915L, 118463894L, 522391939L, 451332724L, 548031654L, 73854149L, 527786213L, 583308898L, 840663438L, 275278054L, 362931963L, 587861579L, 830807449L, 431695707L, 178004048L, 75513216L, 60681147L, 638603143L, 470791469L, 490903319L, 527370962L, 102981857L, 224220555L, 756514239L, 293859807L, 797926303L, 620196520L, 466126507L, 646136763L, 265504163L, 213257337L, 92270416L, 398713724L, 91810366L, 724247342L, 855386762L, 631553083L, 376095634L, 833728623L, 636218061L, 510719408L, 378530670L, 737821436L, 127781731L, 3443282L, 770116208L, 769633348L, 430675947L, 40370755L, 52361322L, 844601468L, 442556599L, 128290354L, 494328514L, 405616679L, 651440882L, 421541290L, 171560170L, 386143493L, 284277254L, 450756213L, 248305939L, 526718005L, 300780198L, 714218239L, 68021827L, 527353904L, 236472015L, 309320156L, 683815803L, 527980097L, 598849444L, 779607597L, 339852811L, 845420163L, 96001931L, 326760873L, 609319751L, 520803868L, 140143851L, 766988701L, 844896794L, 532008178L, 388459130L, 574799295L, 760406065L, 773758517L, 453271555L, 134636434L, 155747417L, 105505251L, 796987277L, 399016325L, 71156680L, 709579308L, 274279004L, 96962867L, 476741915L, 585319990L, 709143538L, 721328791L, 293159344L, 640577897L, 138404614L, 572892015L, 394460832L, 465897068L, 325895331L, 413861636L, 447337182L, 376950267L, 721061932L, 181671909L, 272138750L, 247768905L, 634973622L, 280653872L, 165108426L, 134241779L, 15142090L, 153256717L, 783424845L, 773227607L, 172477802L, 504458250L, 349868083L, 461422806L, 487725644L, 586146740L, 561546455L, 815406759L, 468110471L, 126476456L, 285774551L, 522013234L, 801943660L, 79684345L, 654558548L, 188038414L, 249923934L, 551812615L, 562560206L, 407120348L, 384535446L, 176837117L, 433155458L, 82591339L, 459412819L, 435604627L, 312211805L, 98158590L, 752137480L, 446017293L, 666480139L, 60261988L, 275386848L, 642778031L, 8582401L, 677484160L, 819506256L, 333441964L, 25465219L, 190315429L, 91529631L, 754681170L, 563660271L, 167135649L, 20270015L, 115773732L, 658954441L, 132923202L, 844102455L, 453432758L, 250487209L, 423813160L, 632223296L, 537494486L, 158265753L, 327949044L, 494109748L, 659672289L, 67984726L, 422358258L, 345141182L, 164372996L, 338500924L, 41400311L, 207638305L, 832074651L, 50853458L, 228267776L, 621895888L, 635834787L, 484972544L, 181125024L, 558134871L, 282159878L, 788157855L, 145576343L, 194837894L, 501440949L, 63641414L, 252098681L, 835930645L, 662856247L, 456140980L, 206147937L, 565198503L, 449503819L, 684013129L, 494002381L, 793836418L, 649296754L, 444313288L, 136544068L, 540002286L, 355912945L, 613175147L, 134541429L, 843111781L, 672612536L, 541098995L, 734996181L, 211869705L, 620777828L, 756152791L, 242128346L, 795442420L, 73925532L, 735232214L, 738668090L, 530800757L, 266183732L, 97165934L, 803231879L, 10057267L, 175942047L, 181460965L, 320684297L, 637472526L, 213840116L, 182671953L, 152704513L, 388004388L, 597349323L, 473851493L, 445333546L, 679315863L, 267078568L, 46538491L, 530171754L, 698082287L, 75308587L, 266467406L, 96440883L, 759196579L, 470119952L, 381731475L, 428392158L, 10628712L, 173921356L, 116809433L, 323843928L, 812172630L, 403459283L, 655501128L, 261944441L, 774418023L, 790520709L, 589149480L, 264133112L, 806274256L, 752372117L, 66236193L, 713859568L, 90804933L, 551864345L, 843839891L, 600244073L, 719230074L, 803646506L, 254956426L, 138935723L, 738829647L, 109576220L, 105819621L, 249706947L, 110623114L, 10002331L, 795710911L, 547062229L, 721440199L, 820747461L, 397666160L, 685179945L, 463869301L, 470338753L, 641244231L, 652990696L, 698429485L, 41147155L, 638072709L, 515832968L, 241130026L, 314161759L, 526815813L, 529167244L, 53391331L, 782008115L, 822962086L, 337706389L, 648197286L, 209496506L, 760818531L, 781900302L, 717270807L, 709143641L, 740503641L, 734328409L, 514061476L, 844010670L, 67993787L, 712083588L, 319801387L, 338260400L, 48758556L, 304195768L, 478833380L, 841413917L, 710197685L, 196321647L, 777595184L, 775983866L, 147506314L, 620961439L, 399972264L, 398715644L, 684489092L, 659918078L, 664075287L, 723890579L, 643103903L, 508525962L, 375409248L, 501237729L, 740609783L, 639854810L, 510797913L, 521151016L, 421045341L, 193698327L, 800266392L, 93518128L, 443879633L, 699245445L, 194001794L, 123905867L, 75572337L, 242620749L, 463111940L, 755239011L, 31718790L, 162155292L, 386689240L, 381413538L, 745322913L, 367897558L, 343088005L, 31706107L, 10842029L, 404961623L, 537521191L, 281624684L, 372852160L, 55286017L, 534907560L, 264398082L, 667644310L, 486871690L, 716964533L, 734731419L, 143593638L, 293949413L, 760014789L, 594443755L, 147804127L, 537704286L, 460110740L, 596458323L, 577775570L, 333025386L, 260094086L, 711487611L, 359384182L, 323339045L, 716675075L, 248179763L, 525311626L, 76326208L, 559009987L, 548139736L, 541721430L, 31450329L, 653923741L, 676193285L, 295171241L, 558845563L, 387079118L, 403184480L, 807941436L, 501042343L, 284608894L, 705710380L, 82388415L, 763336555L, 126077422L, 438548854L, 606252517L, 144569238L, 126964439L, 809559381L, 263253751L, 547929033L, 236704198L, 377978058L, 59501955L, 749500335L, 254242336L, 605755194L, 408388953L, 116242711L, 116340056L, 691021496L, 48100285L, 371076069L, 638156108L, 211570763L, 185945242L, 653505761L, 667569173L, 335131755L, 736662207L, 572078378L, 755939949L, 840393623L, 322934679L, 520522390L, 252068808L, 491370519L, 200565770L, 552637112L, 182345569L, 394747039L, 822229467L, 817698102L, 644484388L, 0x9556696L, 729600982L, 695826242L, 509682463L, 785132583L, 746139100L, 188369785L, 628995003L, 406654440L, 650660075L, 676485042L, 540766742L, 493428142L, 753346328L, 82608613L, 670846442L, 145894970L, 770907988L, 621807160L, 14676199L, 793865193L, 36579515L, 619741404L, 303691972L, 794920577L, 134684826L, 190038753L, 538889970L, 836657477L, 643017556L, 316870164L, 464572481L, 305395359L, 446406992L, 587814221L, 423552502L, 122802120L, 146043780L, 173756097L, 130720237L, 445515559L, 109884833L, 133119099L, 804139234L, 834841519L, 458514524L, 74213698L, 490363622L, 119287122L, 165016718L, 351506713L, 433750226L, 439149867L, 348281119L, 319795826L, 320785867L, 446561207L, 705678831L, 714536161L, 172299381L, 552925586L, 635421942L, 851853231L, 208071525L, 142303096L, 93164236L, 207534795L, 655906672L, 558127940L, 98870558L, 388322132L, 87475979L, 835970665L, 61996500L, 298060757L, 256194194L, 563529863L, 249184704L, 451295997L, 73892211L, 559049908L, 44006160L, 832886345L, 720732161L, 255948582L, 827295342L, 629663637L, 323103159L, 155698755L, 598913314L, 586685341L, 761273875L, 135225209L, 324099714L, 391112815L, 493469140L, 796490769L, 667498514L, 148390126L, 721802249L, 781884558L, 309264043L, 603401759L, 503111668L, 563611748L, 363342598L, 383209405L, 108340736L, 758017880L, 145907493L, 312330194L, 608895549L, 45540348L, 143092704L, 772401556L, 806068040L, 853177536L, 662120004L, 463347842L, 495085709L, 560431884L, 274002454L, 76985308L, 519320299L, 253092838L, 727478114L, 593752634L, 490277266L, 206283832L, 701277908L, 504787112L, 816832531L, 730997507L, 27807749L, 58254704L, 584933136L, 515463756L, 0xE5EF55EL, 251881934L, 566567573L, 592887586L, 528932268L, 88111104L, 523103099L, 448331392L, 351083975L, 157811347L, 758866581L, 802151021L, 843579185L, 481417280L, 507414106L, 462708367L, 461501222L, 790988186L, 462220673L, 727683888L, 159759683L, 59757110L, 310746434L, 326369241L, 305829588L, 457718309L, 529317279L, 503631310L, 661769334L, 343160359L, 472216278L, 740498212L, 0xAC9C9CL, 760170115L, 513391009L, 538224236L, 710934956L, 491998229L, 539829044L, 610387964L, 86624968L, 72542777L, 493966272L, 132327984L, 371526334L, 182549152L, 51622114L, 173997077L, 550633787L, 205437301L, 435219235L, 406409162L, 414751325L, 33371226L, 40899348L, 77245052L, 763383124L, 817701136L, 598256078L, 357440859L, 468418959L, 353612800L, 721601331L, 262567156L, 521577430L, 232027892L, 75986872L, 443113391L, 107360999L, 482079354L, 563502258L, 782475535L, 402866161L, 515580626L, 742688144L, 677398836L, 425899303L, 42066550L, 537192943L, 430672016L, 115368023L, 64053241L, 92008456L, 74327791L, 572607165L, 681138002L, 378104858L, 695786430L, 844827190L, 436817825L, 751393351L, 142965259L, 81300919L, 688342617L, 433082724L, 221191094L, 712003270L, 301076404L, 747091407L, 514191589L, 814985450L, 260951422L, 187161058L, 22316970L, 806106670L, 759397054L, 158423624L, 419813636L, 462241316L, 438231460L, 108466764L, 212745115L, 386264342L, 176072326L, 767127195L, 399981627L, 762991681L, 173125691L, 464627163L, 770046798L, 179369718L, 829917528L, 693004603L, 178596003L, 422852852L, 182684967L, 662425026L, 713404098L, 766206683L, 130088738L, 321282752L, 134898541L, 86701214L, 120555423L, 464987852L, 82865891L, 758340585L, 138256323L, 308997895L, 659614345L, 510091933L, 822699180L, 464631718L, 819896232L, 120792059L, 160708255L, 462868879L, 72974246L, 260451492L, 120601343L, 228097712L, 369436704L, 155304088L, 74380537L, 732305166L, 203294189L, 307421597L, 96510570L, 634243454L, 486539430L, 16204477L, 241987531L, 317824421L, 510180366L, 794475492L, 262770124L, 441034891L, 741864347L, 205569410L, 684844547L, 340863522L, 440616421L, 454438375L, 26285496L, 141886125L, 648947081L, 3791510L, 529746935L, 317826713L, 411458050L, 661690316L, 45696331L, 679684665L, 184597094L, 829228068L, 375683582L, 591739456L, 855242340L, 628594662L, 30968619L, 363932244L, 103091463L, 614269714L, 465960778L, 791477766L, 332731888L, 853151007L, 266045534L, 132189407L, 435008168L, 65667470L, 669304246L, 760035868L, 481409581L, 36650645L, 523634336L, 702968013L, 351902214L, 284360680L, 34261165L, 593134528L, 337534074L, 239112910L, 710342799L, 163287447L, 20209506L, 780785984L, 480727309L, 125776519L, 691236193L, 603228570L, 48261672L, 183120677L, 73638683L, 3430616L, 568026489L, 808739797L, 298585898L, 64471573L, 724550960L, 568093636L, 187449517L, 655699449L, 672689645L, 829049456L, 263525899L, 612969883L, 621652807L, 186362075L, 731851539L, 377104257L, 39335761L, 210768226L, 253965025L, 201921517L, 715681274L, 369453531L, 18897741L, 612559390L, 660723864L, 476963596L, 585483298L, 318614839L, 227626072L, 298891387L, 110505944L, 814885802L, 177563961L, 443724544L, 374856237L, 577963338L, 617516835L, 475669105L, 633353115L, 12579943L, 796644307L, 569746680L, 22381253L, 343603333L, 724567543L, 845363898L, 4023795L, 801359177L, 347489967L, 214644600L, 78674056L, 131782857L, 284041623L, 660502381L, 161470286L, 668158595L, 765738294L, 715872268L, 678418089L, 280458288L, 758715787L, 9311288L, 490771912L, 757112000L, 253990619L, 698573830L, 390611635L, 52593584L, 421202448L, 494394112L, 386893540L, 29349323L, 533111491L, 774401558L, 108660117L, 405990553L, 143728136L, 852741683L, 354532633L, 440222591L, 663461253L, 593338391L, 298882952L, 758170600L, 660294062L, 332348846L, 541714172L, 77716403L, 169377728L, 71932929L, 110210904L, 776771173L, 645222398L, 162195941L, 792388932L, 502165627L, 146897021L, 243625970L, 139123400L, 462352793L, 409369440L, 247509680L, 270865496L, 539140627L, 16949766L, 245869282L, 637926655L, 37386603L, 383033875L, 316560876L, 707909555L, 367315004L, 173821041L, 529529257L, 227507318L, 831716891L, 830055847L, 228911074L, 205127100L, 178872273L, 819938491L, 129875615L, 764680417L, 97028082L, 560682982L, 433649390L, 727508847L, 494848582L, 81279272L, 435186566L, 174468080L, 69172161L, 241860102L, 692179355L, 333985572L, 788895276L, 469576414L, 594155471L, 157828532L, 182105752L, 310394758L, 673085082L, 695719789L, 39004854L, 251000641L, 98748282L, 744318650L, 815050298L, 622456803L, 240419561L, 403871914L, 202214044L, 627433637L, 649505808L, 668918393L, 334630440L, 386856024L, 352649543L, 135139523L, 216499252L, 736376783L, 269223150L, 468318208L, 801808348L, 180378366L, 640086372L, 672618369L, 291378195L, 732195369L, 805632553L, 518515631L, 603280165L, 629836417L, 59712833L, 531020081L, 708771168L, 539819295L, 179149444L, 552251927L, 458994127L, 584987693L, 238644928L, 640603619L, 46728500L, 843989005L, 688747457L, 236924093L, 261539965L, 705411056L, 765907765L, 38095657L, 382461698L, 146650814L, 351462947L, 749417520L, 628887925L, 800857475L, 790554154L, 695483946L, 160495923L, 40896482L, 471385785L, 535516195L, 197056285L, 622795937L, 368016917L, 696525353L, 377315918L, 58087122L, 246518254L, 431338589L, 795949654L, 611141265L, 406307405L, 365750089L, 396243561L, 843849531L, 33802729L, 573076974L, 557841126L, 411725124L, 109489622L, 370935707L, 372610558L, 769825999L, 367932152L, 231499145L, 240819898L, 22648665L, 418344529L, 142438794L, 552806180L, 669450690L, 614608056L, 784369586L, 258710636L, 474742428L, 166021530L, 805595815L, 603578176L, 686703780L, 412868426L, 26588048L, 379895115L, 77550061L, 751188758L, 294447541L, 433574579L, 234362222L, 821492181L, 23912038L, 681093196L, 483584545L, 404339808L, 396405029L, 744756742L, 702481685L, 413127074L, 204115019L, 187381271L, 633523978L, 433629465L, 628184183L, 783160918L, 268799033L, 646479372L, 160458176L, 602612912L, 644506365L, 391554011L, 676966578L, 386430153L, 98736426L, 412745127L, 296141927L, 685909285L, 355152260L, 361415843L, 127323093L, 586337666L, 1734791L, 368678692L, 155431915L, 597290023L, 109507713L, 291804866L, 135016081L, 144077689L, 35054937L, 16808265L, 431962815L, 534195521L, 629326143L, 309352001L, 319948849L, 443083246L, 336744161L, 100845182L, 314804947L, 476736581L, 468528479L, 416978018L, 35141019L, 43314058L, 384847955L, 665126798L, 295857628L, 768013680L, 741182796L, 157855570L, 695547618L, 145251639L, 818473396L, 708640763L, 87460130L, 736400748L, 465173936L, 376720282L, 437268868L, 137236663L, 693860377L, 247960644L, 402124416L, 656418852L, 231401654L, 248187016L, 628418583L, 224261112L, 120581342L, 49749199L, 588812480L, 309599954L, 111357387L, 0xDD5D5AL, 754564049L, 513444423L, 816496110L, 509193085L, 361635970L, 190608265L, 697367838L, 230953561L, 140447357L, 27745100L, 163340427L, 607823059L, 325305463L, 383028479L, 269707244L, 475022415L, 708990989L, 738971809L, 797646021L, 126610937L, 589310701L, 191123172L, 819715815L, 337443183L, 432224976L, 337343783L, 257301390L, 172631141L, 560659319L, 646332329L, 55110483L, 467212803L, 442977895L, 311159578L, 569890333L, 669396086L, 536323022L, 542648615L, 366162176L, 88951009L, 408335586L, 276237497L, 384733042L, 525960156L, 74199534L, 338209206L, 676233089L, 264342641L, 241682204L, 226505461L, 165013960L, 129858819L, 664852498L, 432090291L, 165700308L, 382150900L, 537002255L, 368893910L, 61006155L, 238726881L, 92317627L, 632392147L, 404715651L, 802622348L, 126100061L, 306024238L, 397891265L, 214661020L, 211132870L, 783722518L, 149847645L, 665379914L, 624725195L, 85864665L, 496272723L, 304811252L, 29995710L, 410500887L, 756406394L, 31206753L, 647154006L, 596539568L, 783214792L, 286381882L, 24560691L, 681500270L, 774933112L, 506538708L, 850347997L, 611696036L, 512607061L, 251719669L, 367108021L, 456442965L, 636694730L, 399940257L, 73870039L, 85190759L, 264953709L, 238854238L, 395048514L, 612738126L, 27417876L, 652695826L, 188238483L, 324168828L, 736238139L, 789061724L, 529275445L, 382304068L, 176318391L, 709989466L, 14237691L};
        private static final long[] zetainv = new long[]{146156455L, 679827530L, 473841853L, 326870476L, 67084197L, 119907782L, 531977093L, 667907438L, 203450095L, 828728045L, 243407795L, 461097407L, 617291683L, 591192212L, 770955162L, 782275882L, 456205664L, 219451191L, 399702956L, 489037900L, 604426252L, 343538860L, 244449885L, 5797924L, 349607213L, 81212809L, 174645651L, 831585230L, 569764039L, 72931129L, 259606353L, 208991915L, 824939168L, 99739527L, 445645034L, 826150211L, 551334669L, 359873198L, 770281256L, 231420726L, 190766007L, 706298276L, 72423403L, 645013051L, 641484901L, 458254656L, 550121683L, 730045860L, 53523573L, 451430270L, 223753774L, 763828294L, 617419040L, 795139766L, 487252011L, 319143666L, 473995021L, 690445613L, 424055630L, 191293423L, 726287102L, 691131961L, 629640460L, 614463717L, 591803280L, 179912832L, 517936715L, 781946387L, 330185765L, 471412879L, 579908424L, 447810335L, 767194912L, 489983745L, 313497306L, 319822899L, 186749835L, 286255588L, 544986343L, 413168026L, 388933118L, 801035438L, 209813592L, 295486602L, 683514780L, 598844531L, 518802138L, 423920945L, 518702738L, 36430106L, 665022749L, 266835220L, 729534984L, 58499900L, 117174112L, 147154932L, 381123506L, 586438677L, 473117442L, 530840458L, 248322862L, 692805494L, 828400821L, 715698564L, 625192360L, 158778083L, 665537656L, 494509951L, 346952836L, 39649811L, 342701498L, 101581872L, 841638567L, 744788534L, 546545967L, 267333441L, 806396722L, 735564579L, 631884809L, 227727338L, 607958905L, 624744267L, 199727069L, 454021505L, 608185277L, 162285544L, 718909258L, 418877053L, 479425639L, 390971985L, 119745173L, 768685791L, 147505158L, 37672525L, 710894282L, 160598303L, 698290351L, 114963125L, 88132241L, 560288293L, 191019123L, 471297966L, 812831863L, 821004902L, 439167903L, 387617442L, 379409340L, 541340974L, 755300739L, 519401760L, 413062675L, 536197072L, 546793920L, 226819778L, 321950400L, 424183106L, 839337656L, 821090984L, 712068232L, 721129840L, 564341055L, 746638208L, 258855898L, 700714006L, 487467229L, 854411130L, 269808255L, 728822828L, 494730078L, 500993661L, 170236636L, 560003994L, 443400794L, 757409495L, 469715768L, 179179343L, 464591910L, 211639556L, 253533009L, 695687745L, 209666549L, 587346888L, 72985003L, 227961738L, 422516456L, 222621943L, 668764650L, 652030902L, 443018847L, 153664236L, 111389179L, 459740892L, 451806113L, 372561376L, 175052725L, 832233883L, 34653740L, 621783699L, 422571342L, 561698380L, 104957163L, 778595860L, 476250806L, 829557873L, 443277495L, 169442141L, 252567745L, 50550106L, 690124391L, 381403493L, 597435285L, 71776335L, 241537865L, 186695231L, 303339741L, 713707127L, 437801392L, 833497256L, 615326023L, 624646776L, 488213769L, 0x5252332L, 483535363L, 485210214L, 746656299L, 444420797L, 298304795L, 283068947L, 822343192L, 12296390L, 459902360L, 490395832L, 449838516L, 245004656L, 60196267L, 424807332L, 609627667L, 798058799L, 478830003L, 159620568L, 488129004L, 233349984L, 659089636L, 320629726L, 384760136L, 815249439L, 695649998L, 160661975L, 65591767L, 55288446L, 227257996L, 106728401L, 504682974L, 709495107L, 473684223L, 818050264L, 90238156L, 150734865L, 594605956L, 619221828L, 167398464L, 12156916L, 809417421L, 215542302L, 617500993L, 271158228L, 397151794L, 303893994L, 676996477L, 316326626L, 147374753L, 325125840L, 796433088L, 226309504L, 252865756L, 337630290L, 50513368L, 123950552L, 564767726L, 183527552L, 216059549L, 675767555L, 54337573L, 387827713L, 586922771L, 119769138L, 639646669L, 721006398L, 503496378L, 469289897L, 521515481L, 187227528L, 206640113L, 228712284L, 653931877L, 452274007L, 615726360L, 233689118L, 41095623L, 111827271L, 757397639L, 605145280L, 817141067L, 160426132L, 183060839L, 545751163L, 674040169L, 698317389L, 261990450L, 386569507L, 67250645L, 522160349L, 163966566L, 614285819L, 786973760L, 681677841L, 420959355L, 774866649L, 361297339L, 128637074L, 422496531L, 295462939L, 759117839L, 91465504L, 726270306L, 36207430L, 677273648L, 651018821L, 627234847L, 26090074L, 24429030L, 628638603L, 326616664L, 682324880L, 488830917L, 148236366L, 539585045L, 473112046L, 818759318L, 218219266L, 610276639L, 839196155L, 317005294L, 585280425L, 608636241L, 0x1AA144A1L, 393793128L, 717022521L, 612519951L, 709248900L, 353980294L, 63756989L, 693949980L, 210923523L, 79374748L, 745935017L, 784212992L, 686768193L, 778429518L, 314431749L, 523797075L, 195851859L, 97975321L, 557262969L, 262807530L, 192684668L, 415923330L, 501613288L, 3404238L, 712417785L, 450155368L, 747485804L, 81744363L, 323034430L, 826796598L, 469252381L, 361751809L, 434943473L, 803552337L, 465534286L, 157572091L, 602155302L, 99033921L, 365374009L, 846834633L, 97430134L, 575687633L, 177727832L, 140273653L, 90407627L, 187987326L, 694675635L, 195643540L, 572104298L, 724363064L, 777471865L, 641501321L, 508655954L, 54786744L, 852122126L, 10782023L, 131578378L, 512542588L, 833764668L, 286399241L, 59501614L, 843565978L, 222792806L, 380476816L, 238629086L, 278182583L, 481289684L, 412421377L, 678581960L, 41260119L, 745639977L, 557254534L, 628519849L, 537531082L, 270662623L, 379182325L, 195422057L, 243586531L, 837248180L, 486692390L, 140464647L, 654224404L, 602180896L, 645377695L, 816810160L, 479041664L, 124294382L, 669783846L, 234493114L, 243176038L, 592620022L, 27096465L, 183456276L, 200446472L, 668696404L, 288052285L, 131594961L, 791674348L, 557560023L, 47406124L, 288119432L, 852715305L, 782507238L, 673025244L, 807884249L, 252917351L, 164909728L, 730369402L, 375418612L, 75359937L, 835936415L, 692858474L, 145803122L, 617033011L, 518611847L, 263011393L, 821884756L, 571785241L, 504243707L, 153177908L, 332511585L, 819495276L, 374736340L, 96110053L, 186841675L, 790478451L, 421137753L, 723956514L, 590100387L, 2994914L, 523414033L, 64668155L, 390185143L, 241876207L, 753054458L, 492213677L, 825177302L, 227551259L, 903581L, 264406465L, 480462339L, 26917853L, 671548827L, 176461256L, 810449590L, 194455605L, 444687871L, 538319208L, 326398986L, 852354411L, 207198840L, 714259796L, 829860425L, 401707546L, 415529500L, 515282399L, 171301374L, 650576511L, 0x6CFCC66L, 415111030L, 593375797L, 61670429L, 345965555L, 538321500L, 614158390L, 839941444L, 369606491L, 221902467L, 759635351L, 548724324L, 652851732L, 123840755L, 781765384L, 700841833L, 486709217L, 628048209L, 735544578L, 595694429L, 783171675L, 393277042L, 695437666L, 735353862L, 36249689L, 391514203L, 33446741L, 346053988L, 196531576L, 547148026L, 717889598L, 97805336L, 773280030L, 391158069L, 735590498L, 769444707L, 721247380L, 534863169L, 726057183L, 89939238L, 142741823L, 193720895L, 673460954L, 433293069L, 677549918L, 163141318L, 26228393L, 676776203L, 86099123L, 391518758L, 683020230L, 93154240L, 456164294L, 89018726L, 680073595L, 469881579L, 643400806L, 747679157L, 417914461L, 393904605L, 436332285L, 697722297L, 96748867L, 50039251L, 833828951L, 668984863L, 595194499L, 41160471L, 341954332L, 109054514L, 555069517L, 144142651L, 634954827L, 423063197L, 167803304L, 774845002L, 713180662L, 104752570L, 419328096L, 11318731L, 160359491L, 478041063L, 175007919L, 283538756L, 781818130L, 764137465L, 792092680L, 740777898L, 425473905L, 318952978L, 814079371L, 430246618L, 178747085L, 113457777L, 340565295L, 453279760L, 73670386L, 292643663L, 374066567L, 748784922L, 413032530L, 780159049L, 624118029L, 334568491L, 593578765L, 134544590L, 502533121L, 387726962L, 498705062L, 257889843L, 38444785L, 92762797L, 778900869L, 815246573L, 822774695L, 441394596L, 449736759L, 420926686L, 650708620L, 305512134L, 682148844L, 804523807L, 673596769L, 484619587L, 723817937L, 362179649L, 783603144L, 769520953L, 245757957L, 316316877L, 364147692L, 145210965L, 317921685L, 342754912L, 95975806L, 844833637L, 115647709L, 383929643L, 512985562L, 194376587L, 352514611L, 326828642L, 398427612L, 550316333L, 529776680L, 545399487L, 796388811L, 696386238L, 128462033L, 393925248L, 65157735L, 394644699L, 393437554L, 348731815L, 374728641L, 12566736L, 53994900L, 97279340L, 698334574L, 505061946L, 407814529L, 333042822L, 768034817L, 327213653L, 263258335L, 289578348L, 604263987L, 615041699L, 340682165L, 271212785L, 797891217L, 828338172L, 125148414L, 39313390L, 351358809L, 154868013L, 649862089L, 365868655L, 262393287L, 128667807L, 603053083L, 336825622L, 779160613L, 582143467L, 295714037L, 361060212L, 392798079L, 194025917L, 2968385L, 50077881L, 83744365L, 713053217L, 810605573L, 247250372L, 543815727L, 710238428L, 98128041L, 747805185L, 472936516L, 492803323L, 292534173L, 353034253L, 252744162L, 546881878L, 74261363L, 134343672L, 707755795L, 188647407L, 59655152L, 362676781L, 465033106L, 532046207L, 720920712L, 94872046L, 269460580L, 257232607L, 700447166L, 533042762L, 226482284L, 28850579L, 600197339L, 135413760L, 23259576L, 812139761L, 297096013L, 782253710L, 404849924L, 606961217L, 292616058L, 599951727L, 558085164L, 794149421L, 20175256L, 768669942L, 467823789L, 757275363L, 298017981L, 200239249L, 648611126L, 762981685L, 713842825L, 648074396L, 4292690L, 220723979L, 303220335L, 683846540L, 141609760L, 150467090L, 409584714L, 535360054L, 536350095L, 507864802L, 416996054L, 422395695L, 504639208L, 691129203L, 736858799L, 365782299L, 781932223L, 397631397L, 21304402L, 52006687L, 723026822L, 746261088L, 410630362L, 725425684L, 682389824L, 710102141L, 733343801L, 432593419L, 268331700L, 409738929L, 550750562L, 391573440L, 539275757L, 213128365L, 19488444L, 317255951L, 666107168L, 721461095L, 61225344L, 552453949L, 236404517L, 819566406L, 62280728L, 841469722L, 234338761L, 85237933L, 710250951L, 185299479L, 773537308L, 102799593L, 362717779L, 315379179L, 179660879L, 205485846L, 449491481L, 227150918L, 667776136L, 110006821L, 71013338L, 346463458L, 160319679L, 126544939L, 699554155L, 211661533L, 38447819L, 33916454L, 461398882L, 673800352L, 303508809L, 655580151L, 364775402L, 604077113L, 335623531L, 533211242L, 15752298L, 100205972L, 284067543L, 119483714L, 521014166L, 188576748L, 202640160L, 670200679L, 644575158L, 217989813L, 485069852L, 808045636L, 165124425L, 739805865L, 739903210L, 447756968L, 250390727L, 601903585L, 106645586L, 796643966L, 478167863L, 619441723L, 308216888L, 592892170L, 46586540L, 729181482L, 711576683L, 249893404L, 417597067L, 730068499L, 92809366L, 773757506L, 150435541L, 571537027L, 355103578L, 48204485L, 452961441L, 469066803L, 297300358L, 560974680L, 179952636L, 202222180L, 824695592L, 314424491L, 308006185L, 297135934L, 779819713L, 330834295L, 607966158L, 139470846L, 532806876L, 496761739L, 144658310L, 596051835L, 523120535L, 278370351L, 259687598L, 396035181L, 318441635L, 708341794L, 261702166L, 96131132L, 562196508L, 712552283L, 121414502L, 139181388L, 369274231L, 188501611L, 591747839L, 321238361L, 800859904L, 483293761L, 574521237L, 318624730L, 451184298L, 845303892L, 824439814L, 513057916L, 488248363L, 110823008L, 474732383L, 469456681L, 693990629L, 824427131L, 100906910L, 393033981L, 613525172L, 780573584L, 732240054L, 662144127L, 156900476L, 412266288L, 762627793L, 55879529L, 662447594L, 435100580L, 334994905L, 345348008L, 216291111L, 115536138L, 354908192L, 480736673L, 347619959L, 213042018L, 132255342L, 192070634L, 196227843L, 171656829L, 457430277L, 456173657L, 235184482L, 708639607L, 80162055L, 78550737L, 659824274L, 145948236L, 14732004L, 377312541L, 551950153L, 807387365L, 517885521L, 536344534L, 144062333L, 788152134L, 12135251L, 342084445L, 121817512L, 115642280L, 147002280L, 138875114L, 74245619L, 95327390L, 646649415L, 207948635L, 518439532L, 33183835L, 74137806L, 802754590L, 326978677L, 329330108L, 541984162L, 615015895L, 340312953L, 218073212L, 814998766L, 157716436L, 203155225L, 214901690L, 385807168L, 392276620L, 170965976L, 458479761L, 35398460L, 134705722L, 309083692L, 60435010L, 846143590L, 745522807L, 606438974L, 750326300L, 746569701L, 117316274L, 717210198L, 601189495L, 52499415L, 136915847L, 255901848L, 12306030L, 304281576L, 765340988L, 142286353L, 789909728L, 103773804L, 49871665L, 592012809L, 266996441L, 65625212L, 81727898L, 594201480L, 200644793L, 452686638L, 43973291L, 532301993L, 739336488L, 682224565L, 845517209L, 427753763L, 474414446L, 386025969L, 96949342L, 759705038L, 589678515L, 780837334L, 158063634L, 325974167L, 809607430L, 589067353L, 176830058L, 410812375L, 382294428L, 258796598L, 468141533L, 703441408L, 673473968L, 642305805L, 218673395L, 535461624L, 674684956L, 680203874L, 846088654L, 52914042L, 758979987L, 589962189L, 325345164L, 117477831L, 120913707L, 782220389L, 60703501L, 614017575L, 99993130L, 235368093L, 644276216L, 121149740L, 315046926L, 183533385L, 13034140L, 721604492L, 242970774L, 500232976L, 316143635L, 719601853L, 411832633L, 206849167L, 62309503L, 362143540L, 172132792L, 406642102L, 290947418L, 649997984L, 400004941L, 193289674L, 20215276L, 604047240L, 792504507L, 354704972L, 661308027L, 710569578L, 67988066L, 573986043L, 298011050L, 675020897L, 371173377L, 220311134L, 234250033L, 627878145L, 805292463L, 24071270L, 648507616L, 814745610L, 517644997L, 691772925L, 511004739L, 433787663L, 788161195L, 196473632L, 362036173L, 528196877L, 697880168L, 318651435L, 223922625L, 432332761L, 605658712L, 402713163L, 12043466L, 723222719L, 197191480L, 740372189L, 835875906L, 689010272L, 292485650L, 101464751L, 764616290L, 665830492L, 830680702L, 522703957L, 36639665L, 178661761L, 847563520L, 213367890L, 580759073L, 795883933L, 189665782L, 410128628L, 104008441L, 757987331L, 543934116L, 420541294L, 396733102L, 773554582L, 422990463L, 679308804L, 471610475L, 449025573L, 293585715L, 304333306L, 606221987L, 668107507L, 201587373L, 776461576L, 54202261L, 334132687L, 570371370L, 729669465L, 388035450L, 40739162L, 294599466L, 269999181L, 368420277L, 394723115L, 506277838L, 351687671L, 683668119L, 82918314L, 72721076L, 702889204L, 841003831L, 721904142L, 691037495L, 575492049L, 221172299L, 608377016L, 584007171L, 674474012L, 135083989L, 479195654L, 408808739L, 442284285L, 530250590L, 390248853L, 461685089L, 283253906L, 717741307L, 215568024L, 562986577L, 134817130L, 147002383L, 270825931L, 379404006L, 759183054L, 581866917L, 146566613L, 784989241L, 457129596L, 59158644L, 750640670L, 700398504L, 721509487L, 402874366L, 82387404L, 95739856L, 281346626L, 467686791L, 324137743L, 11249127L, 89157220L, 716002070L, 335342053L, 246826170L, 529385048L, 760143990L, 10725758L, 516293110L, 76538324L, 257296477L, 328165824L, 172330118L, 546825765L, 619673906L, 328792017L, 788124094L, 141927682L, 555365723L, 329427916L, 607839982L, 405389708L, 571868667L, 470002428L, 684585751L, 434604631L, 204705039L, 450529242L, 361817407L, 727855567L, 413589322L, 11544453L, 803784599L, 815775166L, 425469974L, 86512573L, 86029713L, 852702639L, 728364190L, 118324485L, 477615251L, 345426513L, 219927860L, 22417298L, 480050287L, 224592838L, 759159L, 131898579L, 764335555L, 457432197L, 763875505L, 642888584L, 590641758L, 210009158L, 390019414L, 235949401L, 58219618L, 562286114L, 99631682L, 631925366L, 753164064L, 328774959L, 365242602L, 385354452L, 217542778L, 795464774L, 780632705L, 678141873L, 424450214L, 25338472L, 268284342L, 493213958L, 580867867L, 15482483L, 272837023L, 328359708L, 782291772L, 308114267L, 404813197L, 333753982L, 737682027L, 538312006L, 707909990L, 234156623L, 323140190L, 803917719L, 91035383L, 200098402L, 773260410L, 554209269L, 505977196L, 258732217L, 577347247L, 388868026L, 412079442L, 312571314L, 628683299L, 740119334L, 813470861L, 86544483L, 515146109L, 371343866L, 687853001L, 265823977L, 121589622L, 808348288L, 257353942L, 635427508L, 834922294L, 224797491L, 432675367L, 731353224L, 575538372L, 642351606L, 291366364L, 210732817L, 90658793L, 146401688L, 40748954L, 527574284L, 817614743L, 547167333L, 534136352L, 372456076L, 706600074L, 640500788L, 559786839L, 845776458L, 709348802L, 677707036L, 606711824L, 349565805L, 42095011L, 472115432L, 177053484L, 681164976L, 139728272L, 510212596L, 747795405L, 441873933L, 187174498L, 392929945L, 425171378L, 555237229L, 4315335L, 9057268L, 153360848L, 99426909L, 774527252L, 83014618L, 412368218L, 3495282L, 739674290L, 826674363L, 316599527L, 110724402L, 435058302L, 156418860L, 545209527L, 681526436L, 443190082L, 613052844L, 463370538L, 710824143L, 207309740L, 783222241L, 141846134L, 266325996L, 146201876L, 449154790L, 170683627L, 716235176L, 607164090L, 291006513L, 186310404L, 43734965L, 496486286L, 736873833L, 329899967L, 408796174L, 449053875L, 589454563L, 727957502L, 460484783L, 122169115L, 75292611L, 73671599L, 848010384L, 303936940L, 791662107L, 590932920L, 125786858L, 211282605L, 729648214L, 59156462L, 152461927L, 219894477L, 776823847L, 437757228L, 186542194L, 700611431L, 257929382L, 767315412L, 18312688L, 806906190L, 504497667L, 101165190L, 603435510L, 526872520L, 254322283L, 720021990L, 779194394L, 584710319L, 801191565L, 703649817L, 361258161L, 149741435L, 808495563L, 291596204L, 250916275L, 340042453L, 141837377L, 547502361L, 181348702L, 139498738L, 338114582L, 119328746L, 177984134L, 199957575L, 358181386L, 57332620L, 512567111L, 451958433L, 156026128L, 619998073L, 307816265L, 338764588L, 65822147L, 573828018L, 487154809L, 749222428L, 522943099L, 26336097L, 186644498L, 526288314L, 534618890L, 828269735L, 675600958L, 49788769L, 453731878L, 762637295L, 387744335L, 173171058L, 33040483L, 466949551L, 843388255L, 697432416L, 216291746L, 33282177L, 240642656L, 663436347L, 390123214L, 254438583L, 190922896L, 455331923L, 296664914L, 762697018L, 331531324L, 851176113L, 771233913L, 482330259L, 389665212L, 474944010L, 58762628L, 469089651L, 436049255L, 697216430L, 431783325L, 138107147L, 499492245L, 647224366L, 407794272L, 26067376L, 445177552L, 520720342L, 798948406L, 325365361L, 117634101L, 664099671L, 153294810L, 597801361L, 640257687L, 533951825L, 702134729L, 111685295L, 685214097L, 452013666L, 317534558L, 271219665L, 529108611L, 586379543L, 355661610L, 759841823L, 446485943L, 839034731L, 33604088L, 773212146L, 191869702L, 367354365L, 689096322L, 345311446L, 438596834L, 677372537L, 542545550L, 341130619L, 292644024L, 281192613L, 251893811L, 447792713L, 520181371L, 40921126L, 778878825L, 536838039L, 230752698L, 396625895L, 601216134L, 188488092L, 130103565L, 504870771L, 413838340L, 335573256L, 124340986L, 368340993L, 243753204L, 150144590L, 808689996L, 32468801L, 68817331L, 471378712L, 566347573L, 6430376L, 651137151L, 497752158L, 823732827L, 787280015L, 789046852L, 194658966L, 171151811L, 118113814L, 793917550L, 75187158L, 717603845L, 61671631L, 51620383L, 302490719L, 78328345L, 244847301L, 549511806L, 420356371L, 560795789L, 405546061L, 302036596L, 432306081L, 270856136L, 330554928L, 212724399L, 791196206L, 445342723L, 187781362L, 87078067L, 834667388L, 218628624L, 755629702L, 148790011L, 845609309L, 89984158L, 742118272L, 475309628L, 81731129L, 107846408L, 74447254L, 68656823L, 169459843L, 643648059L, 721924181L, 212112779L, 575076242L, 471039705L, 626114838L, 564548835L, 506450263L, 488329877L, 847101683L, 592828368L, 714089721L, 832868261L, 393063639L, 603199595L, 214221357L, 747808090L, 145225511L, 784491117L, 578386518L, 253504617L, 217256612L, 432640963L, 696210495L, 700338942L, 642132261L, 394125773L, 127189460L, 622643989L, 65557316L, 850423288L, 154198317L, 360118020L, 401298167L, 809808378L, 590060278L, 378333119L, 261388063L, 301240958L, 211172470L, 476577014L, 818999735L, 320797504L, 155490801L, 362021897L, 416507223L, 193972866L, 814253796L, 555879930L, 152626252L, 598011677L, 48971665L, 590814257L, 699100720L, 732535868L, 42427027L, 335391594L, 577502901L, 72445917L, 562054823L, 34689534L, 850274973L, 640356274L, 165636151L, 309704599L, 39996866L, 436255023L, 365085534L, 208984696L, 593049885L, 755419039L, 376895434L, 634901252L, 0x12E12112L, 476563344L, 619551824L, 766199910L, 783651060L, 32670169L, 794822305L, 435248113L, 14247580L, 284417137L, 754554090L, 30678221L, 641072629L, 711946716L, 568640914L, 656468482L, 83597913L, 356324101L, 231391682L, 122476642L, 505437404L, 636148283L, 639556222L, 262242870L, 10083895L, 470763095L, 7162643L, 490677454L, 122627583L, 711718981L, 252376484L, 423795716L, 578101600L, 275970963L, 3053131L, 327430341L, 435804223L, 349044314L, 649311691L, 234207954L, 379806804L, 342513855L, 224624649L, 181857560L, 84797030L, 123047825L, 95186646L, 293471117L, 586961654L, 111168138L, 703259490L, 756871363L, 606284506L, 380213718L, 292725815L, 463763080L, 747629289L, 254624782L, 207883602L, 849297083L, 578506664L, 656289117L, 454015629L, 162235991L, 474249177L, 633829447L, 490767799L, 210190430L, 48735841L, 656982789L, 743473215L, 47313566L, 306689440L, 53334547L, 370344121L, 419993940L, 218969756L, 341956367L, 296184959L, 135682817L, 127205066L, 744169001L, 445909513L, 801533404L, 605661030L, 181244618L, 30772614L, 196639386L, 59911722L, 616623643L, 199307436L, 551535136L, 136575017L, 79424355L, 92705102L, 498046224L, 17339996L, 698541762L, 804348245L, 104258042L, 484400476L, 535014225L, 87644978L, 121726462L, 383782353L, 77562877L, 350468417L, 724994239L, 772938366L, 320269449L, 203075846L, 465307490L, 585234251L, 271855066L, 464423241L, 403123130L, 202162074L, 117126999L, 653413020L, 8084225L, 216658351L, 409614891L, 799241223L, 600931579L, 454131285L, 782741932L, 376344215L, 79696641L, 803438191L, 565030050L, 460657460L, 5110534L, 472517130L, 76991417L, 572426425L, 92047134L, 285371277L, 843473400L, 389338704L, 704515255L, 459914006L, 657120075L, 708563883L, 78813141L, 11770883L, 688134435L, 287808573L, 649280542L, 765338883L, 439803770L, 160535862L, 617753423L, 442051682L, 288864924L, 32955626L, 326880188L, 696887038L, 215124062L, 791918307L, 767157413L, 358676037L, 30612492L, 661971023L, 838968782L, 465224708L, 784600829L, 146985424L, 799718881L, 207906900L, 340800263L, 849693954L, 44777992L, 31326149L, 240259940L, 508401593L, 499528021L, 475930852L, 690672059L, 580019353L, 297040464L, 236338202L, 454171188L, 695134912L, 508172471L, 436504159L, 293630619L, 848875161L, 37043893L, 26993038L, 396046068L, 722016462L, 445419380L, 209243403L, 503786686L, 268117854L, 281672598L, 205034970L, 87894257L, 293598267L, 46912651L, 147959859L, 462629641L, 509044664L, 700768221L, 0x66668AAL, 340721447L, 163551982L, 247501118L, 447395984L, 318219025L, 172114399L, 110025830L, 810265637L, 370215004L, 606303954L, 462642711L, 251114029L, 290800715L, 780017258L, 789443137L, 495480307L, 615909633L, 431756150L, 766376396L, 820732666L, 686803688L, 133668454L, 761665150L, 326017339L, 424112204L, 110554261L, 386347465L, 101066781L, 135666139L, 256882780L, 205722545L, 668032392L, 405718561L, 350327055L, 621444438L, 381307379L, 421184831L, 753121128L, 590538618L, 366906511L, 345326178L, 132085192L, 40531091L, 780676557L, 586664955L, 597888984L, 693668509L, 487104387L, 234747974L, 572624063L, 114516856L, 550027276L, 316481563L, 239535126L, 788436714L, 847219527L, 113421825L, 200615887L, 815912760L, 581164384L, 191193216L, 11551938L, 606832431L, 431210833L, 196126697L, 92508342L, 270544041L, 192437514L, 99153842L, 188585579L, 413385580L, 745267475L, 448172363L, 667109106L, 85272138L, 658601344L, 443173146L, 392530856L, 589073317L, 382995167L, 248915715L, 375600977L, 386782401L, 254322056L, 790853708L, 580714915L, 163129486L, 824017519L, 86419559L, 117205367L, 634667017L, 566451589L, 852749522L, 837490424L, 330422330L, 294598189L, 814909626L, 505390042L, 125578715L, 357313675L, 450539487L, 233746299L, 446282749L, 755039478L, 740350430L, 598956163L, 116099139L, 167482754L, 310512355L, 135624781L, 470874939L, 196356683L, 239902897L, 693520220L, 454942578L, 778240578L, 45236161L, 51101673L, 270126615L, 94622194L, 524282161L, 632376971L, 703121383L, 587013336L, 572429454L, 37728898L, 143682359L, 206045437L, 557167425L, 770459696L, 477771773L, 321346425L, 290390778L, 100874902L, 758540246L, 746805823L, 459566327L, 607673901L, 158286491L, 527010720L, 579461268L, 74963118L, 420964844L, 51316958L, 250512679L, 452729483L, 35670488L, 559935164L, 734294507L, 379228497L, 172592106L, 126508187L, 757555710L, 853874620L, 808517874L, 106015915L, 375691866L, 423413164L, 423111661L, 60250078L, 645353691L, 853830811L, 288310932L, 1489804L, 127886925L, 191505834L, 459549138L, 542519706L, 369115379L, 116842790L, 784888677L, 269818678L, 712117130L, 748410048L, 139982101L, 169805525L, 32264681L, 532400632L, 397389041L, 181262233L, 703428567L, 604760852L, 44143128L, 69914527L, 86615396L, 314810965L, 68145528L, 650868687L, 717671367L, 594246701L, 641155397L, 207406129L, 180083553L, 414651973L, 132523243L, 211350471L, 397371331L, 170688638L, 732763563L, 132155217L, 394688247L, 571356350L, 93856418L, 708831649L, 841908230L};

        QTesla3PPolynomial() {
        }

        static void poly_uniform(long[] lArray, byte[] byArray, int n) {
            short s;
            int n2 = 0;
            int n3 = 0;
            int n4 = 4;
            int n5 = 180;
            int n6 = 0x3FFFFFFF;
            byte[] byArray2 = new byte[30240];
            short s2 = s = 0;
            s = (short)(s + 1);
            HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 30240, s2, byArray, n, 32);
            while (n3 < 10240) {
                if (n2 > 168 * n5 - 4 * n4) {
                    n5 = 1;
                    short s3 = s;
                    s = (short)(s + 1);
                    HashUtils.customizableSecureHashAlgorithmKECCAK128Simple(byArray2, 0, 30240, s3, byArray, n, 32);
                    n2 = 0;
                }
                int n7 = Pack.littleEndianToInt(byArray2, n2) & n6;
                int n8 = Pack.littleEndianToInt(byArray2, n2 += n4) & n6;
                int n9 = Pack.littleEndianToInt(byArray2, n2 += n4) & n6;
                int n10 = Pack.littleEndianToInt(byArray2, n2 += n4) & n6;
                n2 += n4;
                if (n7 < 856145921 && n3 < 10240) {
                    lArray[n3++] = QTesla3PPolynomial.reduce((long)n7 * 513161157L);
                }
                if (n8 < 856145921 && n3 < 10240) {
                    lArray[n3++] = QTesla3PPolynomial.reduce((long)n8 * 513161157L);
                }
                if (n9 < 856145921 && n3 < 10240) {
                    lArray[n3++] = QTesla3PPolynomial.reduce((long)n9 * 513161157L);
                }
                if (n10 >= 856145921 || n3 >= 10240) continue;
                lArray[n3++] = QTesla3PPolynomial.reduce((long)n10 * 513161157L);
            }
        }

        static long reduce(long l) {
            long l2 = l * 587710463L & 0xFFFFFFFFL;
            return (l += (l2 *= 856145921L)) >> 32;
        }

        static void ntt(long[] lArray, long[] lArray2) {
            int n = 0;
            for (int i = 1024; i > 0; i >>= 1) {
                int n2 = 0;
                int n3 = 0;
                while (n3 < 2048) {
                    int n4 = (int)lArray2[n++];
                    for (n2 = n3; n2 < n3 + i; ++n2) {
                        long l = QTesla3PPolynomial.barr_reduce(QTesla3PPolynomial.reduce((long)n4 * lArray[n2 + i]));
                        lArray[n2 + i] = QTesla3PPolynomial.barr_reduce(lArray[n2] + (1712291842L - l));
                        lArray[n2] = QTesla3PPolynomial.barr_reduce(l + lArray[n2]);
                    }
                    n3 = n2 + i;
                }
            }
        }

        static long barr_reduce(long l) {
            long l2 = l * 5L >> 32;
            return l - l2 * 856145921L;
        }

        static void nttinv(long[] lArray, long[] lArray2) {
            int n = 1;
            int n2 = 0;
            for (n = 1; n < 2048; n *= 2) {
                int n3 = 0;
                int n4 = 0;
                while (n4 < 2048) {
                    int n5 = (int)lArray2[n2++];
                    for (n3 = n4; n3 < n4 + n; ++n3) {
                        long l = lArray[n3];
                        lArray[n3] = QTesla3PPolynomial.barr_reduce(l + lArray[n3 + n]);
                        lArray[n3 + n] = QTesla3PPolynomial.barr_reduce(QTesla3PPolynomial.reduce((long)n5 * (l + (1712291842L - lArray[n3 + n]))));
                    }
                    n4 = n3 + n;
                }
            }
        }

        static void nttinv(long[] lArray, int n, long[] lArray2) {
            int n2 = 1;
            int n3 = 0;
            for (n2 = 1; n2 < 2048; n2 *= 2) {
                int n4 = 0;
                int n5 = 0;
                while (n5 < 2048) {
                    int n6 = (int)lArray2[n3++];
                    for (n4 = n5; n4 < n5 + n2; ++n4) {
                        long l = lArray[n + n4];
                        lArray[n + n4] = QTesla3PPolynomial.barr_reduce(l + lArray[n + n4 + n2]);
                        lArray[n + n4 + n2] = QTesla3PPolynomial.barr_reduce(QTesla3PPolynomial.reduce((long)n6 * (l + (1712291842L - lArray[n + n4 + n2]))));
                    }
                    n5 = n4 + n2;
                }
            }
        }

        static void poly_ntt(long[] lArray, long[] lArray2) {
            for (int i = 0; i < 2048; ++i) {
                lArray[i] = lArray2[i];
            }
            QTesla3PPolynomial.ntt(lArray, zeta);
        }

        static void poly_pointwise(long[] lArray, long[] lArray2, long[] lArray3) {
            for (int i = 0; i < 2048; ++i) {
                lArray[i] = QTesla3PPolynomial.reduce(lArray2[i] * lArray3[i]);
            }
        }

        static void poly_pointwise(long[] lArray, int n, long[] lArray2, int n2, long[] lArray3) {
            for (int i = 0; i < 2048; ++i) {
                lArray[i + n] = QTesla3PPolynomial.reduce(lArray2[i + n2] * lArray3[i]);
            }
        }

        static void poly_mul(long[] lArray, long[] lArray2, long[] lArray3) {
            QTesla3PPolynomial.poly_pointwise(lArray, lArray2, lArray3);
            QTesla3PPolynomial.nttinv(lArray, zetainv);
        }

        static void poly_mul(long[] lArray, int n, long[] lArray2, int n2, long[] lArray3) {
            QTesla3PPolynomial.poly_pointwise(lArray, n, lArray2, n2, lArray3);
            QTesla3PPolynomial.nttinv(lArray, n, zetainv);
        }

        static void poly_add(long[] lArray, long[] lArray2, long[] lArray3) {
            for (int i = 0; i < 2048; ++i) {
                lArray[i] = lArray2[i] + lArray3[i];
            }
        }

        static void poly_sub(long[] lArray, int n, long[] lArray2, int n2, long[] lArray3, int n3) {
            for (int i = 0; i < 2048; ++i) {
                lArray[n + i] = QTesla3PPolynomial.barr_reduce(lArray2[n2 + i] - lArray3[n3 + i]);
            }
        }

        static void poly_add_correct(long[] lArray, int n, long[] lArray2, int n2, long[] lArray3, int n3) {
            for (int i = 0; i < 2048; ++i) {
                lArray[n + i] = lArray2[n2 + i] + lArray3[n3 + i];
                int n4 = n + i;
                lArray[n4] = lArray[n4] - 856145921L;
                int n5 = n + i;
                lArray[n5] = lArray[n5] + (lArray[n + i] >> 31 & 0x3307C001L);
            }
        }

        static void poly_sub_correct(int[] nArray, int[] nArray2, int[] nArray3) {
            for (int i = 0; i < 2048; ++i) {
                nArray[i] = nArray2[i] - nArray3[i];
                int n = i;
                nArray[n] = nArray[n] + (nArray[i] >> 31 & 0x3307C001);
            }
        }

        static void sparse_mul8(long[] lArray, int n, byte[] byArray, int n2, int[] nArray, short[] sArray) {
            int n3;
            for (n3 = 0; n3 < 2048; ++n3) {
                lArray[n + n3] = 0L;
            }
            for (n3 = 0; n3 < 40; ++n3) {
                int n4;
                int n5 = nArray[n3];
                for (n4 = 0; n4 < n5; ++n4) {
                    lArray[n + n4] = lArray[n + n4] - (long)(sArray[n3] * byArray[n2 + n4 + 2048 - n5]);
                }
                for (n4 = n5; n4 < 2048; ++n4) {
                    lArray[n + n4] = lArray[n + n4] + (long)(sArray[n3] * byArray[n2 + n4 - n5]);
                }
            }
        }

        static void sparse_mul8(long[] lArray, byte[] byArray, int[] nArray, short[] sArray) {
            int n;
            byte[] byArray2 = byArray;
            for (n = 0; n < 2048; ++n) {
                lArray[n] = 0L;
            }
            for (n = 0; n < 40; ++n) {
                int n2;
                int n3 = nArray[n];
                for (n2 = 0; n2 < n3; ++n2) {
                    lArray[n2] = lArray[n2] - (long)(sArray[n] * byArray2[n2 + 2048 - n3]);
                }
                for (n2 = n3; n2 < 2048; ++n2) {
                    lArray[n2] = lArray[n2] + (long)(sArray[n] * byArray2[n2 - n3]);
                }
            }
        }

        static void sparse_mul16(int[] nArray, int[] nArray2, int[] nArray3, short[] sArray) {
            int n;
            for (n = 0; n < 2048; ++n) {
                nArray[n] = 0;
            }
            for (n = 0; n < 40; ++n) {
                int n2;
                int n3 = nArray3[n];
                for (n2 = 0; n2 < n3; ++n2) {
                    nArray[n2] = nArray[n2] - sArray[n] * nArray2[n2 + 2048 - n3];
                }
                for (n2 = n3; n2 < 2048; ++n2) {
                    nArray[n2] = nArray[n2] + sArray[n] * nArray2[n2 - n3];
                }
            }
        }

        static void sparse_mul32(int[] nArray, int[] nArray2, int[] nArray3, short[] sArray) {
            int n;
            for (n = 0; n < 2048; ++n) {
                nArray[n] = 0;
            }
            for (n = 0; n < 40; ++n) {
                int n2;
                int n3 = nArray3[n];
                for (n2 = 0; n2 < n3; ++n2) {
                    nArray[n2] = nArray[n2] - sArray[n] * nArray2[n2 + 2048 - n3];
                }
                for (n2 = n3; n2 < 2048; ++n2) {
                    nArray[n2] = nArray[n2] + sArray[n] * nArray2[n2 - n3];
                }
            }
        }

        static void sparse_mul32(long[] lArray, int n, int[] nArray, int n2, int[] nArray2, short[] sArray) {
            int n3;
            for (n3 = 0; n3 < 2048; ++n3) {
                lArray[n + n3] = 0L;
            }
            for (n3 = 0; n3 < 40; ++n3) {
                int n4;
                int n5 = nArray2[n3];
                for (n4 = 0; n4 < n5; ++n4) {
                    lArray[n + n4] = lArray[n + n4] - (long)(sArray[n3] * nArray[n2 + n4 + 2048 - n5]);
                }
                for (n4 = n5; n4 < 2048; ++n4) {
                    lArray[n + n4] = lArray[n + n4] + (long)(sArray[n3] * nArray[n2 + n4 - n5]);
                }
            }
        }
    }
}


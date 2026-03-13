/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.fpe;

import java.math.BigInteger;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Pack;

class SP80038G {
    static final String FPE_DISABLED = "org.bouncycastle.fpe.disable";
    static final String FF1_DISABLED = "org.bouncycastle.fpe.disable_ff1";
    protected static final int BLOCK_SIZE = 16;
    protected static final double LOG2 = Math.log(2.0);
    protected static final double TWO_TO_96 = Math.pow(2.0, 96.0);

    SP80038G() {
    }

    static byte[] decryptFF1(BlockCipher blockCipher, int n, byte[] byArray, byte[] byArray2, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, true, n, byArray2, n2, n3);
        int n4 = n3;
        int n5 = n4 / 2;
        int n6 = n4 - n5;
        short[] sArray = SP80038G.toShort(byArray2, n2, n5);
        short[] sArray2 = SP80038G.toShort(byArray2, n2 + n5, n6);
        short[] sArray3 = SP80038G.decFF1(blockCipher, n, byArray, n4, n5, n6, sArray, sArray2);
        return SP80038G.toByte(sArray3);
    }

    static short[] decryptFF1w(BlockCipher blockCipher, int n, byte[] byArray, short[] sArray, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, true, n, sArray, n2, n3);
        int n4 = n3;
        int n5 = n4 / 2;
        int n6 = n4 - n5;
        short[] sArray2 = new short[n5];
        short[] sArray3 = new short[n6];
        System.arraycopy(sArray, n2, sArray2, 0, n5);
        System.arraycopy(sArray, n2 + n5, sArray3, 0, n6);
        return SP80038G.decFF1(blockCipher, n, byArray, n4, n5, n6, sArray2, sArray3);
    }

    static short[] decFF1(BlockCipher blockCipher, int n, byte[] byArray, int n2, int n3, int n4, short[] sArray, short[] sArray2) {
        int n5 = byArray.length;
        int n6 = ((int)Math.ceil(Math.log(n) * (double)n4 / LOG2) + 7) / 8;
        int n7 = (n6 + 3) / 4 * 4 + 4;
        byte[] byArray2 = SP80038G.calculateP_FF1(n, (byte)n3, n2, n5);
        BigInteger bigInteger = BigInteger.valueOf(n);
        BigInteger[] bigIntegerArray = SP80038G.calculateModUV(bigInteger, n3, n4);
        int n8 = n3;
        for (int i = 9; i >= 0; --i) {
            BigInteger bigInteger2 = SP80038G.calculateY_FF1(blockCipher, bigInteger, byArray, n6, n7, i, byArray2, sArray);
            n8 = n2 - n8;
            BigInteger bigInteger3 = bigIntegerArray[i & 1];
            BigInteger bigInteger4 = SP80038G.num(bigInteger, sArray2).subtract(bigInteger2).mod(bigInteger3);
            short[] sArray3 = sArray2;
            sArray2 = sArray;
            sArray = sArray3;
            SP80038G.str(bigInteger, bigInteger4, n8, sArray3, 0);
        }
        return Arrays.concatenate(sArray, sArray2);
    }

    static byte[] decryptFF3(BlockCipher blockCipher, int n, byte[] byArray, byte[] byArray2, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, false, n, byArray2, n2, n3);
        if (byArray.length != 8) {
            throw new IllegalArgumentException();
        }
        return SP80038G.implDecryptFF3(blockCipher, n, byArray, byArray2, n2, n3);
    }

    static byte[] decryptFF3_1(BlockCipher blockCipher, int n, byte[] byArray, byte[] byArray2, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, false, n, byArray2, n2, n3);
        if (byArray.length != 7) {
            throw new IllegalArgumentException("tweak should be 56 bits");
        }
        byte[] byArray3 = SP80038G.calculateTweak64_FF3_1(byArray);
        return SP80038G.implDecryptFF3(blockCipher, n, byArray3, byArray2, n2, n3);
    }

    static short[] decryptFF3_1w(BlockCipher blockCipher, int n, byte[] byArray, short[] sArray, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, false, n, sArray, n2, n3);
        if (byArray.length != 7) {
            throw new IllegalArgumentException("tweak should be 56 bits");
        }
        byte[] byArray2 = SP80038G.calculateTweak64_FF3_1(byArray);
        return SP80038G.implDecryptFF3w(blockCipher, n, byArray2, sArray, n2, n3);
    }

    static byte[] encryptFF1(BlockCipher blockCipher, int n, byte[] byArray, byte[] byArray2, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, true, n, byArray2, n2, n3);
        int n4 = n3;
        int n5 = n4 / 2;
        int n6 = n4 - n5;
        short[] sArray = SP80038G.toShort(byArray2, n2, n5);
        short[] sArray2 = SP80038G.toShort(byArray2, n2 + n5, n6);
        return SP80038G.toByte(SP80038G.encFF1(blockCipher, n, byArray, n4, n5, n6, sArray, sArray2));
    }

    static short[] encryptFF1w(BlockCipher blockCipher, int n, byte[] byArray, short[] sArray, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, true, n, sArray, n2, n3);
        int n4 = n3;
        int n5 = n4 / 2;
        int n6 = n4 - n5;
        short[] sArray2 = new short[n5];
        short[] sArray3 = new short[n6];
        System.arraycopy(sArray, n2, sArray2, 0, n5);
        System.arraycopy(sArray, n2 + n5, sArray3, 0, n6);
        return SP80038G.encFF1(blockCipher, n, byArray, n4, n5, n6, sArray2, sArray3);
    }

    private static short[] encFF1(BlockCipher blockCipher, int n, byte[] byArray, int n2, int n3, int n4, short[] sArray, short[] sArray2) {
        int n5 = byArray.length;
        int n6 = ((int)Math.ceil(Math.log(n) * (double)n4 / LOG2) + 7) / 8;
        int n7 = (n6 + 3) / 4 * 4 + 4;
        byte[] byArray2 = SP80038G.calculateP_FF1(n, (byte)n3, n2, n5);
        BigInteger bigInteger = BigInteger.valueOf(n);
        BigInteger[] bigIntegerArray = SP80038G.calculateModUV(bigInteger, n3, n4);
        int n8 = n4;
        for (int i = 0; i < 10; ++i) {
            BigInteger bigInteger2 = SP80038G.calculateY_FF1(blockCipher, bigInteger, byArray, n6, n7, i, byArray2, sArray2);
            n8 = n2 - n8;
            BigInteger bigInteger3 = bigIntegerArray[i & 1];
            BigInteger bigInteger4 = SP80038G.num(bigInteger, sArray).add(bigInteger2).mod(bigInteger3);
            short[] sArray3 = sArray;
            sArray = sArray2;
            sArray2 = sArray3;
            SP80038G.str(bigInteger, bigInteger4, n8, sArray3, 0);
        }
        return Arrays.concatenate(sArray, sArray2);
    }

    static byte[] encryptFF3(BlockCipher blockCipher, int n, byte[] byArray, byte[] byArray2, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, false, n, byArray2, n2, n3);
        if (byArray.length != 8) {
            throw new IllegalArgumentException();
        }
        return SP80038G.implEncryptFF3(blockCipher, n, byArray, byArray2, n2, n3);
    }

    static short[] encryptFF3w(BlockCipher blockCipher, int n, byte[] byArray, short[] sArray, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, false, n, sArray, n2, n3);
        if (byArray.length != 8) {
            throw new IllegalArgumentException();
        }
        return SP80038G.implEncryptFF3w(blockCipher, n, byArray, sArray, n2, n3);
    }

    static short[] encryptFF3_1w(BlockCipher blockCipher, int n, byte[] byArray, short[] sArray, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, false, n, sArray, n2, n3);
        if (byArray.length != 7) {
            throw new IllegalArgumentException("tweak should be 56 bits");
        }
        byte[] byArray2 = SP80038G.calculateTweak64_FF3_1(byArray);
        return SP80038G.encryptFF3w(blockCipher, n, byArray2, sArray, n2, n3);
    }

    static byte[] encryptFF3_1(BlockCipher blockCipher, int n, byte[] byArray, byte[] byArray2, int n2, int n3) {
        SP80038G.checkArgs(blockCipher, false, n, byArray2, n2, n3);
        if (byArray.length != 7) {
            throw new IllegalArgumentException("tweak should be 56 bits");
        }
        byte[] byArray3 = SP80038G.calculateTweak64_FF3_1(byArray);
        return SP80038G.encryptFF3(blockCipher, n, byArray3, byArray2, n2, n3);
    }

    protected static BigInteger[] calculateModUV(BigInteger bigInteger, int n, int n2) {
        BigInteger[] bigIntegerArray;
        bigIntegerArray = new BigInteger[]{bigInteger.pow(n), bigIntegerArray[0]};
        if (n2 != n) {
            bigIntegerArray[1] = bigIntegerArray[1].multiply(bigInteger);
        }
        return bigIntegerArray;
    }

    protected static byte[] calculateP_FF1(int n, byte by, int n2, int n3) {
        byte[] byArray = new byte[16];
        byArray[0] = 1;
        byArray[1] = 2;
        byArray[2] = 1;
        byArray[3] = 0;
        byArray[4] = (byte)(n >> 8);
        byArray[5] = (byte)n;
        byArray[6] = 10;
        byArray[7] = by;
        Pack.intToBigEndian(n2, byArray, 8);
        Pack.intToBigEndian(n3, byArray, 12);
        return byArray;
    }

    protected static byte[] calculateTweak64_FF3_1(byte[] byArray) {
        byte[] byArray2 = new byte[]{byArray[0], byArray[1], byArray[2], (byte)(byArray[3] & 0xF0), byArray[4], byArray[5], byArray[6], (byte)(byArray[3] << 4)};
        return byArray2;
    }

    protected static BigInteger calculateY_FF1(BlockCipher blockCipher, BigInteger bigInteger, byte[] byArray, int n, int n2, int n3, byte[] byArray2, short[] sArray) {
        byte[] byArray3;
        int n4 = byArray.length;
        BigInteger bigInteger2 = SP80038G.num(bigInteger, sArray);
        byte[] byArray4 = BigIntegers.asUnsignedByteArray(bigInteger2);
        int n5 = -(n4 + n + 1) & 0xF;
        byte[] byArray5 = new byte[n4 + n5 + 1 + n];
        System.arraycopy(byArray, 0, byArray5, 0, n4);
        byArray5[n4 + n5] = (byte)n3;
        System.arraycopy(byArray4, 0, byArray5, byArray5.length - byArray4.length, byArray4.length);
        byte[] byArray6 = byArray3 = SP80038G.prf(blockCipher, Arrays.concatenate(byArray2, byArray5));
        if (n2 > 16) {
            int n6 = (n2 + 16 - 1) / 16;
            byArray6 = new byte[n6 * 16];
            System.arraycopy(byArray3, 0, byArray6, 0, 16);
            byte[] byArray7 = new byte[4];
            for (int i = 1; i < n6; ++i) {
                int n7 = i * 16;
                System.arraycopy(byArray3, 0, byArray6, n7, 16);
                Pack.intToBigEndian(i, byArray7, 0);
                SP80038G.xor(byArray7, 0, byArray6, n7 + 16 - 4, 4);
                blockCipher.processBlock(byArray6, n7, byArray6, n7);
            }
        }
        return SP80038G.num(byArray6, 0, n2);
    }

    protected static BigInteger calculateY_FF3(BlockCipher blockCipher, BigInteger bigInteger, byte[] byArray, int n, int n2, short[] sArray) {
        byte[] byArray2 = new byte[16];
        Pack.intToBigEndian(n2, byArray2, 0);
        SP80038G.xor(byArray, n, byArray2, 0, 4);
        BigInteger bigInteger2 = SP80038G.num(bigInteger, sArray);
        byte[] byArray3 = BigIntegers.asUnsignedByteArray(bigInteger2);
        if (byArray2.length - byArray3.length < 4) {
            throw new IllegalStateException("input out of range");
        }
        System.arraycopy(byArray3, 0, byArray2, byArray2.length - byArray3.length, byArray3.length);
        SP80038G.rev(byArray2);
        blockCipher.processBlock(byArray2, 0, byArray2, 0);
        SP80038G.rev(byArray2);
        byte[] byArray4 = byArray2;
        return SP80038G.num(byArray4, 0, byArray4.length);
    }

    protected static void checkArgs(BlockCipher blockCipher, boolean bl, int n, short[] sArray, int n2, int n3) {
        SP80038G.checkCipher(blockCipher);
        if (n < 2 || n > 65536) {
            throw new IllegalArgumentException();
        }
        SP80038G.checkData(bl, n, sArray, n2, n3);
    }

    protected static void checkArgs(BlockCipher blockCipher, boolean bl, int n, byte[] byArray, int n2, int n3) {
        SP80038G.checkCipher(blockCipher);
        if (n < 2 || n > 256) {
            throw new IllegalArgumentException();
        }
        SP80038G.checkData(bl, n, byArray, n2, n3);
    }

    protected static void checkCipher(BlockCipher blockCipher) {
        if (16 != blockCipher.getBlockSize()) {
            throw new IllegalArgumentException();
        }
    }

    protected static void checkData(boolean bl, int n, short[] sArray, int n2, int n3) {
        SP80038G.checkLength(bl, n, n3);
        for (int i = 0; i < n3; ++i) {
            int n4 = sArray[n2 + i] & 0xFFFF;
            if (n4 < n) continue;
            throw new IllegalArgumentException("input data outside of radix");
        }
    }

    protected static void checkData(boolean bl, int n, byte[] byArray, int n2, int n3) {
        SP80038G.checkLength(bl, n, n3);
        for (int i = 0; i < n3; ++i) {
            int n4 = byArray[n2 + i] & 0xFF;
            if (n4 < n) continue;
            throw new IllegalArgumentException("input data outside of radix");
        }
    }

    private static void checkLength(boolean bl, int n, int n2) {
        int n3;
        if (n2 < 2 || Math.pow(n, n2) < 1000000.0) {
            throw new IllegalArgumentException("input too short");
        }
        if (!bl && n2 > (n3 = 2 * (int)Math.floor(Math.log(TWO_TO_96) / Math.log(n)))) {
            throw new IllegalArgumentException("maximum input length is " + n3);
        }
    }

    protected static byte[] implDecryptFF3(BlockCipher blockCipher, int n, byte[] byArray, byte[] byArray2, int n2, int n3) {
        byte[] byArray3 = byArray;
        int n4 = n3;
        int n5 = n4 / 2;
        int n6 = n4 - n5;
        short[] sArray = SP80038G.toShort(byArray2, n2, n6);
        short[] sArray2 = SP80038G.toShort(byArray2, n2 + n6, n5);
        short[] sArray3 = SP80038G.decFF3_1(blockCipher, n, byArray3, n4, n5, n6, sArray, sArray2);
        return SP80038G.toByte(sArray3);
    }

    protected static short[] implDecryptFF3w(BlockCipher blockCipher, int n, byte[] byArray, short[] sArray, int n2, int n3) {
        byte[] byArray2 = byArray;
        int n4 = n3;
        int n5 = n4 / 2;
        int n6 = n4 - n5;
        short[] sArray2 = new short[n6];
        short[] sArray3 = new short[n5];
        System.arraycopy(sArray, n2, sArray2, 0, n6);
        System.arraycopy(sArray, n2 + n6, sArray3, 0, n5);
        return SP80038G.decFF3_1(blockCipher, n, byArray2, n4, n5, n6, sArray2, sArray3);
    }

    private static short[] decFF3_1(BlockCipher blockCipher, int n, byte[] byArray, int n2, int n3, int n4, short[] sArray, short[] sArray2) {
        BigInteger bigInteger = BigInteger.valueOf(n);
        BigInteger[] bigIntegerArray = SP80038G.calculateModUV(bigInteger, n3, n4);
        int n5 = n4;
        SP80038G.rev(sArray);
        SP80038G.rev(sArray2);
        for (int i = 7; i >= 0; --i) {
            n5 = n2 - n5;
            BigInteger bigInteger2 = bigIntegerArray[1 - (i & 1)];
            int n6 = 4 - (i & 1) * 4;
            BigInteger bigInteger3 = SP80038G.calculateY_FF3(blockCipher, bigInteger, byArray, n6, i, sArray);
            BigInteger bigInteger4 = SP80038G.num(bigInteger, sArray2).subtract(bigInteger3).mod(bigInteger2);
            short[] sArray3 = sArray2;
            sArray2 = sArray;
            sArray = sArray3;
            SP80038G.str(bigInteger, bigInteger4, n5, sArray3, 0);
        }
        SP80038G.rev(sArray);
        SP80038G.rev(sArray2);
        return Arrays.concatenate(sArray, sArray2);
    }

    protected static byte[] implEncryptFF3(BlockCipher blockCipher, int n, byte[] byArray, byte[] byArray2, int n2, int n3) {
        byte[] byArray3 = byArray;
        int n4 = n3;
        int n5 = n4 / 2;
        int n6 = n4 - n5;
        short[] sArray = SP80038G.toShort(byArray2, n2, n6);
        short[] sArray2 = SP80038G.toShort(byArray2, n2 + n6, n5);
        short[] sArray3 = SP80038G.encFF3_1(blockCipher, n, byArray3, n4, n5, n6, sArray, sArray2);
        return SP80038G.toByte(sArray3);
    }

    protected static short[] implEncryptFF3w(BlockCipher blockCipher, int n, byte[] byArray, short[] sArray, int n2, int n3) {
        byte[] byArray2 = byArray;
        int n4 = n3;
        int n5 = n4 / 2;
        int n6 = n4 - n5;
        short[] sArray2 = new short[n6];
        short[] sArray3 = new short[n5];
        System.arraycopy(sArray, n2, sArray2, 0, n6);
        System.arraycopy(sArray, n2 + n6, sArray3, 0, n5);
        return SP80038G.encFF3_1(blockCipher, n, byArray2, n4, n5, n6, sArray2, sArray3);
    }

    private static short[] encFF3_1(BlockCipher blockCipher, int n, byte[] byArray, int n2, int n3, int n4, short[] sArray, short[] sArray2) {
        BigInteger bigInteger = BigInteger.valueOf(n);
        BigInteger[] bigIntegerArray = SP80038G.calculateModUV(bigInteger, n3, n4);
        int n5 = n3;
        SP80038G.rev(sArray);
        SP80038G.rev(sArray2);
        for (int i = 0; i < 8; ++i) {
            n5 = n2 - n5;
            BigInteger bigInteger2 = bigIntegerArray[1 - (i & 1)];
            int n6 = 4 - (i & 1) * 4;
            BigInteger bigInteger3 = SP80038G.calculateY_FF3(blockCipher, bigInteger, byArray, n6, i, sArray2);
            BigInteger bigInteger4 = SP80038G.num(bigInteger, sArray).add(bigInteger3).mod(bigInteger2);
            short[] sArray3 = sArray;
            sArray = sArray2;
            sArray2 = sArray3;
            SP80038G.str(bigInteger, bigInteger4, n5, sArray3, 0);
        }
        SP80038G.rev(sArray);
        SP80038G.rev(sArray2);
        return Arrays.concatenate(sArray, sArray2);
    }

    protected static BigInteger num(byte[] byArray, int n, int n2) {
        return new BigInteger(1, Arrays.copyOfRange(byArray, n, n + n2));
    }

    protected static BigInteger num(BigInteger bigInteger, short[] sArray) {
        BigInteger bigInteger2 = BigIntegers.ZERO;
        for (int i = 0; i < sArray.length; ++i) {
            bigInteger2 = bigInteger2.multiply(bigInteger).add(BigInteger.valueOf(sArray[i] & 0xFFFF));
        }
        return bigInteger2;
    }

    protected static byte[] prf(BlockCipher blockCipher, byte[] byArray) {
        if (byArray.length % 16 != 0) {
            throw new IllegalArgumentException();
        }
        int n = byArray.length / 16;
        byte[] byArray2 = new byte[16];
        for (int i = 0; i < n; ++i) {
            SP80038G.xor(byArray, i * 16, byArray2, 0, 16);
            blockCipher.processBlock(byArray2, 0, byArray2, 0);
        }
        return byArray2;
    }

    protected static void rev(byte[] byArray) {
        int n = byArray.length / 2;
        int n2 = byArray.length - 1;
        for (int i = 0; i < n; ++i) {
            byte by = byArray[i];
            byArray[i] = byArray[n2 - i];
            byArray[n2 - i] = by;
        }
    }

    protected static void rev(short[] sArray) {
        int n = sArray.length / 2;
        int n2 = sArray.length - 1;
        for (int i = 0; i < n; ++i) {
            short s = sArray[i];
            sArray[i] = sArray[n2 - i];
            sArray[n2 - i] = s;
        }
    }

    protected static void str(BigInteger bigInteger, BigInteger bigInteger2, int n, short[] sArray, int n2) {
        if (bigInteger2.signum() < 0) {
            throw new IllegalArgumentException();
        }
        for (int i = 1; i <= n; ++i) {
            BigInteger[] bigIntegerArray = bigInteger2.divideAndRemainder(bigInteger);
            sArray[n2 + n - i] = (short)bigIntegerArray[1].intValue();
            bigInteger2 = bigIntegerArray[0];
        }
        if (bigInteger2.signum() != 0) {
            throw new IllegalArgumentException();
        }
    }

    protected static void xor(byte[] byArray, int n, byte[] byArray2, int n2, int n3) {
        for (int i = 0; i < n3; ++i) {
            int n4 = n2 + i;
            byArray2[n4] = (byte)(byArray2[n4] ^ byArray[n + i]);
        }
    }

    private static byte[] toByte(short[] sArray) {
        byte[] byArray = new byte[sArray.length];
        for (int i = 0; i != byArray.length; ++i) {
            byArray[i] = (byte)sArray[i];
        }
        return byArray;
    }

    private static short[] toShort(byte[] byArray, int n, int n2) {
        short[] sArray = new short[n2];
        for (int i = 0; i != sArray.length; ++i) {
            sArray[i] = (short)(byArray[n + i] & 0xFF);
        }
        return sArray;
    }
}


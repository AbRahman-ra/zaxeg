/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.math.raw.Mod;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.util.Arrays;

public final class BigIntegers {
    public static final BigInteger ZERO = BigInteger.valueOf(0L);
    public static final BigInteger ONE = BigInteger.valueOf(1L);
    public static final BigInteger TWO = BigInteger.valueOf(2L);
    private static final BigInteger THREE = BigInteger.valueOf(3L);
    private static final int MAX_ITERATIONS = 1000;
    private static final BigInteger SMALL_PRIMES_PRODUCT = new BigInteger("8138e8a0fcf3a4e84a771d40fd305d7f4aa59306d7251de54d98af8fe95729a1f73d893fa424cd2edc8636a6c3285e022b0e3866a565ae8108eed8591cd4fe8d2ce86165a978d719ebf647f362d33fca29cd179fb42401cbaf3df0c614056f9c8f3cfd51e474afb6bc6974f78db8aba8e9e517fded658591ab7502bd41849462f", 16);
    private static final int MAX_SMALL = BigInteger.valueOf(743L).bitLength();

    public static byte[] asUnsignedByteArray(BigInteger bigInteger) {
        byte[] byArray = bigInteger.toByteArray();
        if (byArray[0] == 0 && byArray.length != 1) {
            byte[] byArray2 = new byte[byArray.length - 1];
            System.arraycopy(byArray, 1, byArray2, 0, byArray2.length);
            return byArray2;
        }
        return byArray;
    }

    public static byte[] asUnsignedByteArray(int n, BigInteger bigInteger) {
        byte[] byArray = bigInteger.toByteArray();
        if (byArray.length == n) {
            return byArray;
        }
        int n2 = byArray[0] == 0 && byArray.length != 1 ? 1 : 0;
        int n3 = byArray.length - n2;
        if (n3 > n) {
            throw new IllegalArgumentException("standard length exceeded for value");
        }
        byte[] byArray2 = new byte[n];
        System.arraycopy(byArray, n2, byArray2, byArray2.length - n3, n3);
        return byArray2;
    }

    public static void asUnsignedByteArray(BigInteger bigInteger, byte[] byArray, int n, int n2) {
        byte[] byArray2 = bigInteger.toByteArray();
        if (byArray2.length == n2) {
            System.arraycopy(byArray2, 0, byArray, n, n2);
            return;
        }
        int n3 = byArray2[0] == 0 && byArray2.length != 1 ? 1 : 0;
        int n4 = byArray2.length - n3;
        if (n4 > n2) {
            throw new IllegalArgumentException("standard length exceeded for value");
        }
        int n5 = n2 - n4;
        Arrays.fill(byArray, n, n + n5, (byte)0);
        System.arraycopy(byArray2, n3, byArray, n + n5, n4);
    }

    public static BigInteger createRandomInRange(BigInteger bigInteger, BigInteger bigInteger2, SecureRandom secureRandom) {
        int n = bigInteger.compareTo(bigInteger2);
        if (n >= 0) {
            if (n > 0) {
                throw new IllegalArgumentException("'min' may not be greater than 'max'");
            }
            return bigInteger;
        }
        if (bigInteger.bitLength() > bigInteger2.bitLength() / 2) {
            return BigIntegers.createRandomInRange(ZERO, bigInteger2.subtract(bigInteger), secureRandom).add(bigInteger);
        }
        for (int i = 0; i < 1000; ++i) {
            BigInteger bigInteger3 = BigIntegers.createRandomBigInteger(bigInteger2.bitLength(), secureRandom);
            if (bigInteger3.compareTo(bigInteger) < 0 || bigInteger3.compareTo(bigInteger2) > 0) continue;
            return bigInteger3;
        }
        return BigIntegers.createRandomBigInteger(bigInteger2.subtract(bigInteger).bitLength() - 1, secureRandom).add(bigInteger);
    }

    public static BigInteger fromUnsignedByteArray(byte[] byArray) {
        return new BigInteger(1, byArray);
    }

    public static BigInteger fromUnsignedByteArray(byte[] byArray, int n, int n2) {
        byte[] byArray2 = byArray;
        if (n != 0 || n2 != byArray.length) {
            byArray2 = new byte[n2];
            System.arraycopy(byArray, n, byArray2, 0, n2);
        }
        return new BigInteger(1, byArray2);
    }

    public static int intValueExact(BigInteger bigInteger) {
        if (bigInteger.bitLength() > 31) {
            throw new ArithmeticException("BigInteger out of int range");
        }
        return bigInteger.intValue();
    }

    public static long longValueExact(BigInteger bigInteger) {
        if (bigInteger.bitLength() > 63) {
            throw new ArithmeticException("BigInteger out of long range");
        }
        return bigInteger.longValue();
    }

    public static BigInteger modOddInverse(BigInteger bigInteger, BigInteger bigInteger2) {
        int n;
        int[] nArray;
        int[] nArray2;
        int n2;
        int[] nArray3;
        if (!bigInteger.testBit(0)) {
            throw new IllegalArgumentException("'M' must be odd");
        }
        if (bigInteger.signum() != 1) {
            throw new ArithmeticException("BigInteger: modulus not positive");
        }
        if (bigInteger2.signum() < 0 || bigInteger2.compareTo(bigInteger) >= 0) {
            bigInteger2 = bigInteger2.mod(bigInteger);
        }
        if (0 == Mod.modOddInverse(nArray3 = Nat.fromBigInteger(n2 = bigInteger.bitLength(), bigInteger), nArray2 = Nat.fromBigInteger(n2, bigInteger2), nArray = Nat.create(n = nArray3.length))) {
            throw new ArithmeticException("BigInteger not invertible.");
        }
        return Nat.toBigInteger(n, nArray);
    }

    public static BigInteger modOddInverseVar(BigInteger bigInteger, BigInteger bigInteger2) {
        int n;
        int[] nArray;
        int[] nArray2;
        if (!bigInteger.testBit(0)) {
            throw new IllegalArgumentException("'M' must be odd");
        }
        if (bigInteger.signum() != 1) {
            throw new ArithmeticException("BigInteger: modulus not positive");
        }
        if (bigInteger.equals(ONE)) {
            return ZERO;
        }
        if (bigInteger2.signum() < 0 || bigInteger2.compareTo(bigInteger) >= 0) {
            bigInteger2 = bigInteger2.mod(bigInteger);
        }
        if (bigInteger2.equals(ONE)) {
            return ONE;
        }
        int n2 = bigInteger.bitLength();
        int[] nArray3 = Nat.fromBigInteger(n2, bigInteger);
        if (!Mod.modOddInverseVar(nArray3, nArray2 = Nat.fromBigInteger(n2, bigInteger2), nArray = Nat.create(n = nArray3.length))) {
            throw new ArithmeticException("BigInteger not invertible.");
        }
        return Nat.toBigInteger(n, nArray);
    }

    public static int getUnsignedByteLength(BigInteger bigInteger) {
        if (bigInteger.equals(ZERO)) {
            return 1;
        }
        return (bigInteger.bitLength() + 7) / 8;
    }

    public static BigInteger createRandomBigInteger(int n, SecureRandom secureRandom) {
        return new BigInteger(1, BigIntegers.createRandom(n, secureRandom));
    }

    public static BigInteger createRandomPrime(int n, int n2, SecureRandom secureRandom) {
        BigInteger bigInteger;
        if (n < 2) {
            throw new IllegalArgumentException("bitLength < 2");
        }
        if (n == 2) {
            return secureRandom.nextInt() < 0 ? TWO : THREE;
        }
        do {
            byte[] byArray = BigIntegers.createRandom(n, secureRandom);
            int n3 = 8 * byArray.length - n;
            byte by = (byte)(1 << 7 - n3);
            byArray[0] = (byte)(byArray[0] | by);
            int n4 = byArray.length - 1;
            byArray[n4] = (byte)(byArray[n4] | 1);
            bigInteger = new BigInteger(1, byArray);
            if (n <= MAX_SMALL) continue;
            while (!bigInteger.gcd(SMALL_PRIMES_PRODUCT).equals(ONE)) {
                bigInteger = bigInteger.add(TWO);
            }
        } while (!bigInteger.isProbablePrime(n2));
        return bigInteger;
    }

    private static byte[] createRandom(int n, SecureRandom secureRandom) throws IllegalArgumentException {
        if (n < 1) {
            throw new IllegalArgumentException("bitLength must be at least 1");
        }
        int n2 = (n + 7) / 8;
        byte[] byArray = new byte[n2];
        secureRandom.nextBytes(byArray);
        int n3 = 8 * n2 - n;
        byArray[0] = (byte)(byArray[0] & (byte)(255 >>> n3));
        return byArray;
    }
}


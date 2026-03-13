/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util;

import java.math.BigInteger;
import java.util.NoSuchElementException;
import org.bouncycastle.util.Objects;

public final class Arrays {
    private Arrays() {
    }

    public static boolean areAllZeroes(byte[] byArray, int n, int n2) {
        int n3 = 0;
        for (int i = 0; i < n2; ++i) {
            n3 |= byArray[n + i];
        }
        return n3 == 0;
    }

    public static boolean areEqual(boolean[] blArray, boolean[] blArray2) {
        return java.util.Arrays.equals(blArray, blArray2);
    }

    public static boolean areEqual(byte[] byArray, byte[] byArray2) {
        return java.util.Arrays.equals(byArray, byArray2);
    }

    public static boolean areEqual(byte[] byArray, int n, int n2, byte[] byArray2, int n3, int n4) {
        int n5 = n2 - n;
        int n6 = n4 - n3;
        if (n5 != n6) {
            return false;
        }
        for (int i = 0; i < n5; ++i) {
            if (byArray[n + i] == byArray2[n3 + i]) continue;
            return false;
        }
        return true;
    }

    public static boolean areEqual(char[] cArray, char[] cArray2) {
        return java.util.Arrays.equals(cArray, cArray2);
    }

    public static boolean areEqual(int[] nArray, int[] nArray2) {
        return java.util.Arrays.equals(nArray, nArray2);
    }

    public static boolean areEqual(long[] lArray, long[] lArray2) {
        return java.util.Arrays.equals(lArray, lArray2);
    }

    public static boolean areEqual(Object[] objectArray, Object[] objectArray2) {
        return java.util.Arrays.equals(objectArray, objectArray2);
    }

    public static boolean areEqual(short[] sArray, short[] sArray2) {
        return java.util.Arrays.equals(sArray, sArray2);
    }

    public static boolean constantTimeAreEqual(byte[] byArray, byte[] byArray2) {
        int n;
        if (byArray == null || byArray2 == null) {
            return false;
        }
        if (byArray == byArray2) {
            return true;
        }
        int n2 = byArray.length < byArray2.length ? byArray.length : byArray2.length;
        int n3 = byArray.length ^ byArray2.length;
        for (n = 0; n != n2; ++n) {
            n3 |= byArray[n] ^ byArray2[n];
        }
        for (n = n2; n < byArray2.length; ++n) {
            n3 |= byArray2[n] ^ ~byArray2[n];
        }
        return n3 == 0;
    }

    public static boolean constantTimeAreEqual(int n, byte[] byArray, int n2, byte[] byArray2, int n3) {
        if (null == byArray) {
            throw new NullPointerException("'a' cannot be null");
        }
        if (null == byArray2) {
            throw new NullPointerException("'b' cannot be null");
        }
        if (n < 0) {
            throw new IllegalArgumentException("'len' cannot be negative");
        }
        if (n2 > byArray.length - n) {
            throw new IndexOutOfBoundsException("'aOff' value invalid for specified length");
        }
        if (n3 > byArray2.length - n) {
            throw new IndexOutOfBoundsException("'bOff' value invalid for specified length");
        }
        int n4 = 0;
        for (int i = 0; i < n; ++i) {
            n4 |= byArray[n2 + i] ^ byArray2[n3 + i];
        }
        return 0 == n4;
    }

    public static int compareUnsigned(byte[] byArray, byte[] byArray2) {
        if (byArray == byArray2) {
            return 0;
        }
        if (byArray == null) {
            return -1;
        }
        if (byArray2 == null) {
            return 1;
        }
        int n = Math.min(byArray.length, byArray2.length);
        for (int i = 0; i < n; ++i) {
            int n2 = byArray[i] & 0xFF;
            int n3 = byArray2[i] & 0xFF;
            if (n2 < n3) {
                return -1;
            }
            if (n2 <= n3) continue;
            return 1;
        }
        if (byArray.length < byArray2.length) {
            return -1;
        }
        if (byArray.length > byArray2.length) {
            return 1;
        }
        return 0;
    }

    public static boolean contains(boolean[] blArray, boolean bl) {
        for (int i = 0; i < blArray.length; ++i) {
            if (blArray[i] != bl) continue;
            return true;
        }
        return false;
    }

    public static boolean contains(byte[] byArray, byte by) {
        for (int i = 0; i < byArray.length; ++i) {
            if (byArray[i] != by) continue;
            return true;
        }
        return false;
    }

    public static boolean contains(char[] cArray, char c) {
        for (int i = 0; i < cArray.length; ++i) {
            if (cArray[i] != c) continue;
            return true;
        }
        return false;
    }

    public static boolean contains(int[] nArray, int n) {
        for (int i = 0; i < nArray.length; ++i) {
            if (nArray[i] != n) continue;
            return true;
        }
        return false;
    }

    public static boolean contains(long[] lArray, long l) {
        for (int i = 0; i < lArray.length; ++i) {
            if (lArray[i] != l) continue;
            return true;
        }
        return false;
    }

    public static boolean contains(short[] sArray, short s) {
        for (int i = 0; i < sArray.length; ++i) {
            if (sArray[i] != s) continue;
            return true;
        }
        return false;
    }

    public static void fill(boolean[] blArray, boolean bl) {
        java.util.Arrays.fill(blArray, bl);
    }

    public static void fill(boolean[] blArray, int n, int n2, boolean bl) {
        java.util.Arrays.fill(blArray, n, n2, bl);
    }

    public static void fill(byte[] byArray, byte by) {
        java.util.Arrays.fill(byArray, by);
    }

    public static void fill(byte[] byArray, int n, byte by) {
        Arrays.fill(byArray, n, byArray.length, by);
    }

    public static void fill(byte[] byArray, int n, int n2, byte by) {
        java.util.Arrays.fill(byArray, n, n2, by);
    }

    public static void fill(char[] cArray, char c) {
        java.util.Arrays.fill(cArray, c);
    }

    public static void fill(char[] cArray, int n, int n2, char c) {
        java.util.Arrays.fill(cArray, n, n2, c);
    }

    public static void fill(int[] nArray, int n) {
        java.util.Arrays.fill(nArray, n);
    }

    public static void fill(int[] nArray, int n, int n2) {
        java.util.Arrays.fill(nArray, n, nArray.length, n2);
    }

    public static void fill(int[] nArray, int n, int n2, int n3) {
        java.util.Arrays.fill(nArray, n, n2, n3);
    }

    public static void fill(long[] lArray, long l) {
        java.util.Arrays.fill(lArray, l);
    }

    public static void fill(long[] lArray, int n, long l) {
        java.util.Arrays.fill(lArray, n, lArray.length, l);
    }

    public static void fill(long[] lArray, int n, int n2, long l) {
        java.util.Arrays.fill(lArray, n, n2, l);
    }

    public static void fill(Object[] objectArray, Object object) {
        java.util.Arrays.fill(objectArray, object);
    }

    public static void fill(Object[] objectArray, int n, int n2, Object object) {
        java.util.Arrays.fill(objectArray, n, n2, object);
    }

    public static void fill(short[] sArray, short s) {
        java.util.Arrays.fill(sArray, s);
    }

    public static void fill(short[] sArray, int n, short s) {
        java.util.Arrays.fill(sArray, n, sArray.length, s);
    }

    public static void fill(short[] sArray, int n, int n2, short s) {
        java.util.Arrays.fill(sArray, n, n2, s);
    }

    public static int hashCode(byte[] byArray) {
        if (byArray == null) {
            return 0;
        }
        int n = byArray.length;
        int n2 = n + 1;
        while (--n >= 0) {
            n2 *= 257;
            n2 ^= byArray[n];
        }
        return n2;
    }

    public static int hashCode(byte[] byArray, int n, int n2) {
        if (byArray == null) {
            return 0;
        }
        int n3 = n2;
        int n4 = n3 + 1;
        while (--n3 >= 0) {
            n4 *= 257;
            n4 ^= byArray[n + n3];
        }
        return n4;
    }

    public static int hashCode(char[] cArray) {
        if (cArray == null) {
            return 0;
        }
        int n = cArray.length;
        int n2 = n + 1;
        while (--n >= 0) {
            n2 *= 257;
            n2 ^= cArray[n];
        }
        return n2;
    }

    public static int hashCode(int[][] nArray) {
        int n = 0;
        for (int i = 0; i != nArray.length; ++i) {
            n = n * 257 + Arrays.hashCode(nArray[i]);
        }
        return n;
    }

    public static int hashCode(int[] nArray) {
        if (nArray == null) {
            return 0;
        }
        int n = nArray.length;
        int n2 = n + 1;
        while (--n >= 0) {
            n2 *= 257;
            n2 ^= nArray[n];
        }
        return n2;
    }

    public static int hashCode(int[] nArray, int n, int n2) {
        if (nArray == null) {
            return 0;
        }
        int n3 = n2;
        int n4 = n3 + 1;
        while (--n3 >= 0) {
            n4 *= 257;
            n4 ^= nArray[n + n3];
        }
        return n4;
    }

    public static int hashCode(long[] lArray) {
        if (lArray == null) {
            return 0;
        }
        int n = lArray.length;
        int n2 = n + 1;
        while (--n >= 0) {
            long l = lArray[n];
            n2 *= 257;
            n2 ^= (int)l;
            n2 *= 257;
            n2 ^= (int)(l >>> 32);
        }
        return n2;
    }

    public static int hashCode(long[] lArray, int n, int n2) {
        if (lArray == null) {
            return 0;
        }
        int n3 = n2;
        int n4 = n3 + 1;
        while (--n3 >= 0) {
            long l = lArray[n + n3];
            n4 *= 257;
            n4 ^= (int)l;
            n4 *= 257;
            n4 ^= (int)(l >>> 32);
        }
        return n4;
    }

    public static int hashCode(short[][][] sArray) {
        int n = 0;
        for (int i = 0; i != sArray.length; ++i) {
            n = n * 257 + Arrays.hashCode(sArray[i]);
        }
        return n;
    }

    public static int hashCode(short[][] sArray) {
        int n = 0;
        for (int i = 0; i != sArray.length; ++i) {
            n = n * 257 + Arrays.hashCode(sArray[i]);
        }
        return n;
    }

    public static int hashCode(short[] sArray) {
        if (sArray == null) {
            return 0;
        }
        int n = sArray.length;
        int n2 = n + 1;
        while (--n >= 0) {
            n2 *= 257;
            n2 ^= sArray[n] & 0xFF;
        }
        return n2;
    }

    public static int hashCode(Object[] objectArray) {
        if (objectArray == null) {
            return 0;
        }
        int n = objectArray.length;
        int n2 = n + 1;
        while (--n >= 0) {
            n2 *= 257;
            n2 ^= Objects.hashCode(objectArray[n]);
        }
        return n2;
    }

    public static boolean[] clone(boolean[] blArray) {
        return null == blArray ? null : (boolean[])blArray.clone();
    }

    public static byte[] clone(byte[] byArray) {
        return null == byArray ? null : (byte[])byArray.clone();
    }

    public static char[] clone(char[] cArray) {
        return null == cArray ? null : (char[])cArray.clone();
    }

    public static int[] clone(int[] nArray) {
        return null == nArray ? null : (int[])nArray.clone();
    }

    public static long[] clone(long[] lArray) {
        return null == lArray ? null : (long[])lArray.clone();
    }

    public static short[] clone(short[] sArray) {
        return null == sArray ? null : (short[])sArray.clone();
    }

    public static BigInteger[] clone(BigInteger[] bigIntegerArray) {
        return null == bigIntegerArray ? null : (BigInteger[])bigIntegerArray.clone();
    }

    public static byte[] clone(byte[] byArray, byte[] byArray2) {
        if (byArray == null) {
            return null;
        }
        if (byArray2 == null || byArray2.length != byArray.length) {
            return Arrays.clone(byArray);
        }
        System.arraycopy(byArray, 0, byArray2, 0, byArray2.length);
        return byArray2;
    }

    public static long[] clone(long[] lArray, long[] lArray2) {
        if (lArray == null) {
            return null;
        }
        if (lArray2 == null || lArray2.length != lArray.length) {
            return Arrays.clone(lArray);
        }
        System.arraycopy(lArray, 0, lArray2, 0, lArray2.length);
        return lArray2;
    }

    public static byte[][] clone(byte[][] byArray) {
        if (byArray == null) {
            return null;
        }
        byte[][] byArrayArray = new byte[byArray.length][];
        for (int i = 0; i != byArrayArray.length; ++i) {
            byArrayArray[i] = Arrays.clone(byArray[i]);
        }
        return byArrayArray;
    }

    public static byte[][][] clone(byte[][][] byArray) {
        if (byArray == null) {
            return null;
        }
        byte[][][] byArrayArray = new byte[byArray.length][][];
        for (int i = 0; i != byArrayArray.length; ++i) {
            byArrayArray[i] = Arrays.clone(byArray[i]);
        }
        return byArrayArray;
    }

    public static boolean[] copyOf(boolean[] blArray, int n) {
        boolean[] blArray2 = new boolean[n];
        System.arraycopy(blArray, 0, blArray2, 0, Math.min(blArray.length, n));
        return blArray2;
    }

    public static byte[] copyOf(byte[] byArray, int n) {
        byte[] byArray2 = new byte[n];
        System.arraycopy(byArray, 0, byArray2, 0, Math.min(byArray.length, n));
        return byArray2;
    }

    public static char[] copyOf(char[] cArray, int n) {
        char[] cArray2 = new char[n];
        System.arraycopy(cArray, 0, cArray2, 0, Math.min(cArray.length, n));
        return cArray2;
    }

    public static int[] copyOf(int[] nArray, int n) {
        int[] nArray2 = new int[n];
        System.arraycopy(nArray, 0, nArray2, 0, Math.min(nArray.length, n));
        return nArray2;
    }

    public static long[] copyOf(long[] lArray, int n) {
        long[] lArray2 = new long[n];
        System.arraycopy(lArray, 0, lArray2, 0, Math.min(lArray.length, n));
        return lArray2;
    }

    public static short[] copyOf(short[] sArray, int n) {
        short[] sArray2 = new short[n];
        System.arraycopy(sArray, 0, sArray2, 0, Math.min(sArray.length, n));
        return sArray2;
    }

    public static BigInteger[] copyOf(BigInteger[] bigIntegerArray, int n) {
        BigInteger[] bigIntegerArray2 = new BigInteger[n];
        System.arraycopy(bigIntegerArray, 0, bigIntegerArray2, 0, Math.min(bigIntegerArray.length, n));
        return bigIntegerArray2;
    }

    public static boolean[] copyOfRange(boolean[] blArray, int n, int n2) {
        int n3 = Arrays.getLength(n, n2);
        boolean[] blArray2 = new boolean[n3];
        System.arraycopy(blArray, n, blArray2, 0, Math.min(blArray.length - n, n3));
        return blArray2;
    }

    public static byte[] copyOfRange(byte[] byArray, int n, int n2) {
        int n3 = Arrays.getLength(n, n2);
        byte[] byArray2 = new byte[n3];
        System.arraycopy(byArray, n, byArray2, 0, Math.min(byArray.length - n, n3));
        return byArray2;
    }

    public static char[] copyOfRange(char[] cArray, int n, int n2) {
        int n3 = Arrays.getLength(n, n2);
        char[] cArray2 = new char[n3];
        System.arraycopy(cArray, n, cArray2, 0, Math.min(cArray.length - n, n3));
        return cArray2;
    }

    public static int[] copyOfRange(int[] nArray, int n, int n2) {
        int n3 = Arrays.getLength(n, n2);
        int[] nArray2 = new int[n3];
        System.arraycopy(nArray, n, nArray2, 0, Math.min(nArray.length - n, n3));
        return nArray2;
    }

    public static long[] copyOfRange(long[] lArray, int n, int n2) {
        int n3 = Arrays.getLength(n, n2);
        long[] lArray2 = new long[n3];
        System.arraycopy(lArray, n, lArray2, 0, Math.min(lArray.length - n, n3));
        return lArray2;
    }

    public static short[] copyOfRange(short[] sArray, int n, int n2) {
        int n3 = Arrays.getLength(n, n2);
        short[] sArray2 = new short[n3];
        System.arraycopy(sArray, n, sArray2, 0, Math.min(sArray.length - n, n3));
        return sArray2;
    }

    public static BigInteger[] copyOfRange(BigInteger[] bigIntegerArray, int n, int n2) {
        int n3 = Arrays.getLength(n, n2);
        BigInteger[] bigIntegerArray2 = new BigInteger[n3];
        System.arraycopy(bigIntegerArray, n, bigIntegerArray2, 0, Math.min(bigIntegerArray.length - n, n3));
        return bigIntegerArray2;
    }

    private static int getLength(int n, int n2) {
        int n3 = n2 - n;
        if (n3 < 0) {
            StringBuffer stringBuffer = new StringBuffer(n);
            stringBuffer.append(" > ").append(n2);
            throw new IllegalArgumentException(stringBuffer.toString());
        }
        return n3;
    }

    public static byte[] append(byte[] byArray, byte by) {
        if (byArray == null) {
            return new byte[]{by};
        }
        int n = byArray.length;
        byte[] byArray2 = new byte[n + 1];
        System.arraycopy(byArray, 0, byArray2, 0, n);
        byArray2[n] = by;
        return byArray2;
    }

    public static short[] append(short[] sArray, short s) {
        if (sArray == null) {
            return new short[]{s};
        }
        int n = sArray.length;
        short[] sArray2 = new short[n + 1];
        System.arraycopy(sArray, 0, sArray2, 0, n);
        sArray2[n] = s;
        return sArray2;
    }

    public static int[] append(int[] nArray, int n) {
        if (nArray == null) {
            return new int[]{n};
        }
        int n2 = nArray.length;
        int[] nArray2 = new int[n2 + 1];
        System.arraycopy(nArray, 0, nArray2, 0, n2);
        nArray2[n2] = n;
        return nArray2;
    }

    public static String[] append(String[] stringArray, String string) {
        if (stringArray == null) {
            return new String[]{string};
        }
        int n = stringArray.length;
        String[] stringArray2 = new String[n + 1];
        System.arraycopy(stringArray, 0, stringArray2, 0, n);
        stringArray2[n] = string;
        return stringArray2;
    }

    public static byte[] concatenate(byte[] byArray, byte[] byArray2) {
        if (null == byArray) {
            return Arrays.clone(byArray2);
        }
        if (null == byArray2) {
            return Arrays.clone(byArray);
        }
        byte[] byArray3 = new byte[byArray.length + byArray2.length];
        System.arraycopy(byArray, 0, byArray3, 0, byArray.length);
        System.arraycopy(byArray2, 0, byArray3, byArray.length, byArray2.length);
        return byArray3;
    }

    public static short[] concatenate(short[] sArray, short[] sArray2) {
        if (null == sArray) {
            return Arrays.clone(sArray2);
        }
        if (null == sArray2) {
            return Arrays.clone(sArray);
        }
        short[] sArray3 = new short[sArray.length + sArray2.length];
        System.arraycopy(sArray, 0, sArray3, 0, sArray.length);
        System.arraycopy(sArray2, 0, sArray3, sArray.length, sArray2.length);
        return sArray3;
    }

    public static byte[] concatenate(byte[] byArray, byte[] byArray2, byte[] byArray3) {
        if (null == byArray) {
            return Arrays.concatenate(byArray2, byArray3);
        }
        if (null == byArray2) {
            return Arrays.concatenate(byArray, byArray3);
        }
        if (null == byArray3) {
            return Arrays.concatenate(byArray, byArray2);
        }
        byte[] byArray4 = new byte[byArray.length + byArray2.length + byArray3.length];
        int n = 0;
        System.arraycopy(byArray, 0, byArray4, n, byArray.length);
        System.arraycopy(byArray2, 0, byArray4, n += byArray.length, byArray2.length);
        System.arraycopy(byArray3, 0, byArray4, n += byArray2.length, byArray3.length);
        return byArray4;
    }

    public static byte[] concatenate(byte[] byArray, byte[] byArray2, byte[] byArray3, byte[] byArray4) {
        if (null == byArray) {
            return Arrays.concatenate(byArray2, byArray3, byArray4);
        }
        if (null == byArray2) {
            return Arrays.concatenate(byArray, byArray3, byArray4);
        }
        if (null == byArray3) {
            return Arrays.concatenate(byArray, byArray2, byArray4);
        }
        if (null == byArray4) {
            return Arrays.concatenate(byArray, byArray2, byArray3);
        }
        byte[] byArray5 = new byte[byArray.length + byArray2.length + byArray3.length + byArray4.length];
        int n = 0;
        System.arraycopy(byArray, 0, byArray5, n, byArray.length);
        System.arraycopy(byArray2, 0, byArray5, n += byArray.length, byArray2.length);
        System.arraycopy(byArray3, 0, byArray5, n += byArray2.length, byArray3.length);
        System.arraycopy(byArray4, 0, byArray5, n += byArray3.length, byArray4.length);
        return byArray5;
    }

    public static byte[] concatenate(byte[][] byArray) {
        int n = 0;
        for (int i = 0; i != byArray.length; ++i) {
            n += byArray[i].length;
        }
        byte[] byArray2 = new byte[n];
        int n2 = 0;
        for (int i = 0; i != byArray.length; ++i) {
            System.arraycopy(byArray[i], 0, byArray2, n2, byArray[i].length);
            n2 += byArray[i].length;
        }
        return byArray2;
    }

    public static int[] concatenate(int[] nArray, int[] nArray2) {
        if (null == nArray) {
            return Arrays.clone(nArray2);
        }
        if (null == nArray2) {
            return Arrays.clone(nArray);
        }
        int[] nArray3 = new int[nArray.length + nArray2.length];
        System.arraycopy(nArray, 0, nArray3, 0, nArray.length);
        System.arraycopy(nArray2, 0, nArray3, nArray.length, nArray2.length);
        return nArray3;
    }

    public static byte[] prepend(byte[] byArray, byte by) {
        if (byArray == null) {
            return new byte[]{by};
        }
        int n = byArray.length;
        byte[] byArray2 = new byte[n + 1];
        System.arraycopy(byArray, 0, byArray2, 1, n);
        byArray2[0] = by;
        return byArray2;
    }

    public static short[] prepend(short[] sArray, short s) {
        if (sArray == null) {
            return new short[]{s};
        }
        int n = sArray.length;
        short[] sArray2 = new short[n + 1];
        System.arraycopy(sArray, 0, sArray2, 1, n);
        sArray2[0] = s;
        return sArray2;
    }

    public static int[] prepend(int[] nArray, int n) {
        if (nArray == null) {
            return new int[]{n};
        }
        int n2 = nArray.length;
        int[] nArray2 = new int[n2 + 1];
        System.arraycopy(nArray, 0, nArray2, 1, n2);
        nArray2[0] = n;
        return nArray2;
    }

    public static byte[] reverse(byte[] byArray) {
        if (byArray == null) {
            return null;
        }
        int n = 0;
        int n2 = byArray.length;
        byte[] byArray2 = new byte[n2];
        while (--n2 >= 0) {
            byArray2[n2] = byArray[n++];
        }
        return byArray2;
    }

    public static int[] reverse(int[] nArray) {
        if (nArray == null) {
            return null;
        }
        int n = 0;
        int n2 = nArray.length;
        int[] nArray2 = new int[n2];
        while (--n2 >= 0) {
            nArray2[n2] = nArray[n++];
        }
        return nArray2;
    }

    public static byte[] reverseInPlace(byte[] byArray) {
        if (null == byArray) {
            return null;
        }
        int n = 0;
        int n2 = byArray.length - 1;
        while (n < n2) {
            byte by = byArray[n];
            byte by2 = byArray[n2];
            byArray[n++] = by2;
            byArray[n2--] = by;
        }
        return byArray;
    }

    public static int[] reverseInPlace(int[] nArray) {
        if (null == nArray) {
            return null;
        }
        int n = 0;
        int n2 = nArray.length - 1;
        while (n < n2) {
            int n3 = nArray[n];
            int n4 = nArray[n2];
            nArray[n++] = n4;
            nArray[n2--] = n3;
        }
        return nArray;
    }

    public static void clear(byte[] byArray) {
        if (null != byArray) {
            java.util.Arrays.fill(byArray, (byte)0);
        }
    }

    public static void clear(int[] nArray) {
        if (null != nArray) {
            java.util.Arrays.fill(nArray, 0);
        }
    }

    public static boolean isNullOrContainsNull(Object[] objectArray) {
        if (null == objectArray) {
            return true;
        }
        int n = objectArray.length;
        for (int i = 0; i < n; ++i) {
            if (null != objectArray[i]) continue;
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(byte[] byArray) {
        return null == byArray || byArray.length < 1;
    }

    public static boolean isNullOrEmpty(int[] nArray) {
        return null == nArray || nArray.length < 1;
    }

    public static boolean isNullOrEmpty(Object[] objectArray) {
        return null == objectArray || objectArray.length < 1;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static class Iterator<T>
    implements java.util.Iterator<T> {
        private final T[] dataArray;
        private int position = 0;

        public Iterator(T[] TArray) {
            this.dataArray = TArray;
        }

        @Override
        public boolean hasNext() {
            return this.position < this.dataArray.length;
        }

        @Override
        public T next() {
            if (this.position == this.dataArray.length) {
                throw new NoSuchElementException("Out of elements: " + this.position);
            }
            return this.dataArray[this.position++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove element from an Array.");
        }
    }
}


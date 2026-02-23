/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.starkbank.ellipticcurve.utils;

import com.starkbank.ellipticcurve.utils.ByteString;
import java.math.BigInteger;
import java.util.Arrays;

public final class BinaryAscii {
    public static String hexFromBinary(ByteString string) {
        return BinaryAscii.hexFromBinary(string.getBytes());
    }

    public static String hexFromBinary(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static byte[] binaryFromHex(String string) {
        int i;
        byte[] bytes = new BigInteger(string, 16).toByteArray();
        for (i = 0; i < bytes.length && bytes[i] == 0; ++i) {
        }
        return Arrays.copyOfRange(bytes, i, bytes.length);
    }

    public static byte[] toBytes(int c) {
        return new byte[]{(byte)c};
    }

    public static BigInteger numberFromString(byte[] string) {
        return new BigInteger(BinaryAscii.hexFromBinary(string), 16);
    }

    public static ByteString stringFromNumber(BigInteger number, int length) {
        String fmtStr = "%0" + String.valueOf(2 * length) + "x";
        String hexString = String.format(fmtStr, number);
        return new ByteString(BinaryAscii.binaryFromHex(hexString));
    }
}


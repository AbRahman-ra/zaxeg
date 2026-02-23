/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.payneteasy.tlv;

public class HexUtil {
    private static final char[] CHARS_TABLES = "0123456789ABCDEF".toCharArray();
    static final byte[] BYTES = new byte[128];

    public static String toHexString(byte[] aBytes) {
        return HexUtil.toHexString(aBytes, 0, aBytes.length);
    }

    public static String toFormattedHexString(byte[] aBytes) {
        return HexUtil.toFormattedHexString(aBytes, 0, aBytes.length);
    }

    public static String toHexString(byte[] aBytes, int aLength) {
        return HexUtil.toHexString(aBytes, 0, aLength);
    }

    public static byte[] parseHex(String aHexString) {
        char[] src = aHexString.replace("\n", "").replace(" ", "").toUpperCase().toCharArray();
        byte[] dst = new byte[src.length / 2];
        int si = 0;
        for (int di = 0; di < dst.length; ++di) {
            byte high = BYTES[src[si++] & 0x7F];
            byte low = BYTES[src[si++] & 0x7F];
            dst[di] = (byte)((high << 4) + low);
        }
        return dst;
    }

    public static String toFormattedHexString(byte[] aBytes, int aOffset, int aLength) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(aLength);
        sb.append("] :");
        int si = aOffset;
        int di = 0;
        while (si < aOffset + aLength) {
            byte b = aBytes[si];
            if (di % 4 == 0) {
                sb.append("  ");
            } else {
                sb.append(' ');
            }
            sb.append(CHARS_TABLES[(b & 0xF0) >>> 4]);
            sb.append(CHARS_TABLES[b & 0xF]);
            ++si;
            ++di;
        }
        return sb.toString();
    }

    public static String toHexString(byte[] aBytes, int aOffset, int aLength) {
        char[] dst = new char[aLength * 2];
        int di = 0;
        for (int si = aOffset; si < aOffset + aLength; ++si) {
            byte b = aBytes[si];
            dst[di++] = CHARS_TABLES[(b & 0xF0) >>> 4];
            dst[di++] = CHARS_TABLES[b & 0xF];
        }
        return new String(dst);
    }

    static {
        for (int i = 0; i < 10; ++i) {
            HexUtil.BYTES[48 + i] = (byte)i;
            HexUtil.BYTES[65 + i] = (byte)(10 + i);
            HexUtil.BYTES[97 + i] = (byte)(10 + i);
        }
    }
}


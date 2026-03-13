/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util.encoders;

public class UTF8 {
    private static final byte C_ILL = 0;
    private static final byte C_CR1 = 1;
    private static final byte C_CR2 = 2;
    private static final byte C_CR3 = 3;
    private static final byte C_L2A = 4;
    private static final byte C_L3A = 5;
    private static final byte C_L3B = 6;
    private static final byte C_L3C = 7;
    private static final byte C_L4A = 8;
    private static final byte C_L4B = 9;
    private static final byte C_L4C = 10;
    private static final byte S_ERR = -2;
    private static final byte S_END = -1;
    private static final byte S_CS1 = 0;
    private static final byte S_CS2 = 16;
    private static final byte S_CS3 = 32;
    private static final byte S_P3A = 48;
    private static final byte S_P3B = 64;
    private static final byte S_P4A = 80;
    private static final byte S_P4B = 96;
    private static final short[] firstUnitTable = new short[128];
    private static final byte[] transitionTable = new byte[112];

    private static void fill(byte[] byArray, int n, int n2, byte by) {
        for (int i = n; i <= n2; ++i) {
            byArray[i] = by;
        }
    }

    public static int transcodeToUTF16(byte[] byArray, char[] cArray) {
        int n = 0;
        int n2 = 0;
        while (n < byArray.length) {
            byte by;
            if ((by = byArray[n++]) >= 0) {
                if (n2 >= cArray.length) {
                    return -1;
                }
                cArray[n2++] = (char)by;
                continue;
            }
            short s = firstUnitTable[by & 0x7F];
            int n3 = s >>> 8;
            byte by2 = (byte)s;
            while (by2 >= 0) {
                if (n >= byArray.length) {
                    return -1;
                }
                by = byArray[n++];
                n3 = n3 << 6 | by & 0x3F;
                by2 = transitionTable[by2 + ((by & 0xFF) >>> 4)];
            }
            if (by2 == -2) {
                return -1;
            }
            if (n3 <= 65535) {
                if (n2 >= cArray.length) {
                    return -1;
                }
                cArray[n2++] = (char)n3;
                continue;
            }
            if (n2 >= cArray.length - 1) {
                return -1;
            }
            cArray[n2++] = (char)(55232 + (n3 >>> 10));
            cArray[n2++] = (char)(0xDC00 | n3 & 0x3FF);
        }
        return n2;
    }

    static {
        byte[] byArray = new byte[128];
        UTF8.fill(byArray, 0, 15, (byte)1);
        UTF8.fill(byArray, 16, 31, (byte)2);
        UTF8.fill(byArray, 32, 63, (byte)3);
        UTF8.fill(byArray, 64, 65, (byte)0);
        UTF8.fill(byArray, 66, 95, (byte)4);
        UTF8.fill(byArray, 96, 96, (byte)5);
        UTF8.fill(byArray, 97, 108, (byte)6);
        UTF8.fill(byArray, 109, 109, (byte)7);
        UTF8.fill(byArray, 110, 111, (byte)6);
        UTF8.fill(byArray, 112, 112, (byte)8);
        UTF8.fill(byArray, 113, 115, (byte)9);
        UTF8.fill(byArray, 116, 116, (byte)10);
        UTF8.fill(byArray, 117, 127, (byte)0);
        UTF8.fill(transitionTable, 0, transitionTable.length - 1, (byte)-2);
        UTF8.fill(transitionTable, 8, 11, (byte)-1);
        UTF8.fill(transitionTable, 24, 27, (byte)0);
        UTF8.fill(transitionTable, 40, 43, (byte)16);
        UTF8.fill(transitionTable, 58, 59, (byte)0);
        UTF8.fill(transitionTable, 72, 73, (byte)0);
        UTF8.fill(transitionTable, 89, 91, (byte)16);
        UTF8.fill(transitionTable, 104, 104, (byte)16);
        byte[] byArray2 = new byte[]{0, 0, 0, 0, 31, 15, 15, 15, 7, 7, 7};
        byte[] byArray3 = new byte[]{-2, -2, -2, -2, 0, 48, 16, 64, 80, 32, 96};
        for (int i = 0; i < 128; ++i) {
            byte by = byArray[i];
            int n = i & byArray2[by];
            byte by2 = byArray3[by];
            UTF8.firstUnitTable[i] = (short)(n << 8 | by2);
        }
    }
}


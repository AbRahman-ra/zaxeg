/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util.encoders;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.util.encoders.Encoder;

public class HexEncoder
implements Encoder {
    protected final byte[] encodingTable = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
    protected final byte[] decodingTable = new byte[128];

    protected void initialiseDecodingTable() {
        int n;
        for (n = 0; n < this.decodingTable.length; ++n) {
            this.decodingTable[n] = -1;
        }
        for (n = 0; n < this.encodingTable.length; ++n) {
            this.decodingTable[this.encodingTable[n]] = (byte)n;
        }
        this.decodingTable[65] = this.decodingTable[97];
        this.decodingTable[66] = this.decodingTable[98];
        this.decodingTable[67] = this.decodingTable[99];
        this.decodingTable[68] = this.decodingTable[100];
        this.decodingTable[69] = this.decodingTable[101];
        this.decodingTable[70] = this.decodingTable[102];
    }

    public HexEncoder() {
        this.initialiseDecodingTable();
    }

    public int encode(byte[] byArray, int n, int n2, byte[] byArray2, int n3) throws IOException {
        int n4 = n;
        int n5 = n + n2;
        int n6 = n3;
        while (n4 < n5) {
            int n7 = byArray[n4++] & 0xFF;
            byArray2[n6++] = this.encodingTable[n7 >>> 4];
            byArray2[n6++] = this.encodingTable[n7 & 0xF];
        }
        return n6 - n3;
    }

    public int getEncodedLength(int n) {
        return n * 2;
    }

    public int getMaxDecodedLength(int n) {
        return n / 2;
    }

    public int encode(byte[] byArray, int n, int n2, OutputStream outputStream) throws IOException {
        byte[] byArray2 = new byte[72];
        while (n2 > 0) {
            int n3 = Math.min(36, n2);
            int n4 = this.encode(byArray, n, n3, byArray2, 0);
            outputStream.write(byArray2, 0, n4);
            n += n3;
            n2 -= n3;
        }
        return n2 * 2;
    }

    private static boolean ignore(char c) {
        return c == '\n' || c == '\r' || c == '\t' || c == ' ';
    }

    public int decode(byte[] byArray, int n, int n2, OutputStream outputStream) throws IOException {
        int n3;
        int n4 = 0;
        byte[] byArray2 = new byte[36];
        int n5 = 0;
        for (n3 = n + n2; n3 > n && HexEncoder.ignore((char)byArray[n3 - 1]); --n3) {
        }
        int n6 = n;
        while (n6 < n3) {
            byte by;
            while (n6 < n3 && HexEncoder.ignore((char)byArray[n6])) {
                ++n6;
            }
            byte by2 = this.decodingTable[byArray[n6++]];
            while (n6 < n3 && HexEncoder.ignore((char)byArray[n6])) {
                ++n6;
            }
            if ((by2 | (by = this.decodingTable[byArray[n6++]])) < 0) {
                throw new IOException("invalid characters encountered in Hex data");
            }
            byArray2[n5++] = (byte)(by2 << 4 | by);
            if (n5 == byArray2.length) {
                outputStream.write(byArray2);
                n5 = 0;
            }
            ++n4;
        }
        if (n5 > 0) {
            outputStream.write(byArray2, 0, n5);
        }
        return n4;
    }

    public int decode(String string, OutputStream outputStream) throws IOException {
        int n;
        int n2 = 0;
        byte[] byArray = new byte[36];
        int n3 = 0;
        for (n = string.length(); n > 0 && HexEncoder.ignore(string.charAt(n - 1)); --n) {
        }
        int n4 = 0;
        while (n4 < n) {
            byte by;
            while (n4 < n && HexEncoder.ignore(string.charAt(n4))) {
                ++n4;
            }
            byte by2 = this.decodingTable[string.charAt(n4++)];
            while (n4 < n && HexEncoder.ignore(string.charAt(n4))) {
                ++n4;
            }
            if ((by2 | (by = this.decodingTable[string.charAt(n4++)])) < 0) {
                throw new IOException("invalid characters encountered in Hex string");
            }
            byArray[n3++] = (byte)(by2 << 4 | by);
            if (n3 == byArray.length) {
                outputStream.write(byArray);
                n3 = 0;
            }
            ++n2;
        }
        if (n3 > 0) {
            outputStream.write(byArray, 0, n3);
        }
        return n2;
    }

    byte[] decodeStrict(String string, int n, int n2) throws IOException {
        if (null == string) {
            throw new NullPointerException("'str' cannot be null");
        }
        if (n < 0 || n2 < 0 || n > string.length() - n2) {
            throw new IndexOutOfBoundsException("invalid offset and/or length specified");
        }
        if (0 != (n2 & 1)) {
            throw new IOException("a hexadecimal encoding must have an even number of characters");
        }
        int n3 = n2 >>> 1;
        byte[] byArray = new byte[n3];
        int n4 = n;
        for (int i = 0; i < n3; ++i) {
            byte by;
            byte by2;
            int n5;
            if ((n5 = (by2 = this.decodingTable[string.charAt(n4++)]) << 4 | (by = this.decodingTable[string.charAt(n4++)])) < 0) {
                throw new IOException("invalid characters encountered in Hex string");
            }
            byArray[i] = (byte)n5;
        }
        return byArray;
    }
}


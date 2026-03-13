/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util.encoders;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.util.encoders.Encoder;

public class Base64Encoder
implements Encoder {
    protected final byte[] encodingTable = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    protected byte padding = (byte)61;
    protected final byte[] decodingTable = new byte[128];

    protected void initialiseDecodingTable() {
        int n;
        for (n = 0; n < this.decodingTable.length; ++n) {
            this.decodingTable[n] = -1;
        }
        for (n = 0; n < this.encodingTable.length; ++n) {
            this.decodingTable[this.encodingTable[n]] = (byte)n;
        }
    }

    public Base64Encoder() {
        this.initialiseDecodingTable();
    }

    public int encode(byte[] byArray, int n, int n2, byte[] byArray2, int n3) throws IOException {
        int n4;
        int n5;
        int n6 = n;
        int n7 = n + n2 - 2;
        int n8 = n3;
        while (n6 < n7) {
            n5 = byArray[n6++];
            n4 = byArray[n6++] & 0xFF;
            int n9 = byArray[n6++] & 0xFF;
            byArray2[n8++] = this.encodingTable[n5 >>> 2 & 0x3F];
            byArray2[n8++] = this.encodingTable[(n5 << 4 | n4 >>> 4) & 0x3F];
            byArray2[n8++] = this.encodingTable[(n4 << 2 | n9 >>> 6) & 0x3F];
            byArray2[n8++] = this.encodingTable[n9 & 0x3F];
        }
        switch (n2 - (n6 - n)) {
            case 1: {
                n5 = byArray[n6++] & 0xFF;
                byArray2[n8++] = this.encodingTable[n5 >>> 2 & 0x3F];
                byArray2[n8++] = this.encodingTable[n5 << 4 & 0x3F];
                byArray2[n8++] = this.padding;
                byArray2[n8++] = this.padding;
                break;
            }
            case 2: {
                n5 = byArray[n6++] & 0xFF;
                n4 = byArray[n6++] & 0xFF;
                byArray2[n8++] = this.encodingTable[n5 >>> 2 & 0x3F];
                byArray2[n8++] = this.encodingTable[(n5 << 4 | n4 >>> 4) & 0x3F];
                byArray2[n8++] = this.encodingTable[n4 << 2 & 0x3F];
                byArray2[n8++] = this.padding;
                break;
            }
        }
        return n8 - n3;
    }

    public int getEncodedLength(int n) {
        return (n + 2) / 3 * 4;
    }

    public int getMaxDecodedLength(int n) {
        return n / 4 * 3;
    }

    public int encode(byte[] byArray, int n, int n2, OutputStream outputStream) throws IOException {
        byte[] byArray2 = new byte[72];
        while (n2 > 0) {
            int n3 = Math.min(54, n2);
            int n4 = this.encode(byArray, n, n3, byArray2, 0);
            outputStream.write(byArray2, 0, n4);
            n += n3;
            n2 -= n3;
        }
        return (n2 + 2) / 3 * 4;
    }

    private boolean ignore(char c) {
        return c == '\n' || c == '\r' || c == '\t' || c == ' ';
    }

    public int decode(byte[] byArray, int n, int n2, OutputStream outputStream) throws IOException {
        int n3;
        int n4;
        byte[] byArray2 = new byte[54];
        int n5 = 0;
        int n6 = 0;
        for (n4 = n + n2; n4 > n && this.ignore((char)byArray[n4 - 1]); --n4) {
        }
        if (n4 == 0) {
            return 0;
        }
        int n7 = 0;
        for (n3 = n4; n3 > n && n7 != 4; --n3) {
            if (this.ignore((char)byArray[n3 - 1])) continue;
            ++n7;
        }
        n7 = this.nextI(byArray, n, n3);
        while (n7 < n3) {
            byte by;
            byte by2 = this.decodingTable[byArray[n7++]];
            n7 = this.nextI(byArray, n7, n3);
            byte by3 = this.decodingTable[byArray[n7++]];
            n7 = this.nextI(byArray, n7, n3);
            byte by4 = this.decodingTable[byArray[n7++]];
            n7 = this.nextI(byArray, n7, n3);
            if ((by2 | by3 | by4 | (by = this.decodingTable[byArray[n7++]])) < 0) {
                throw new IOException("invalid characters encountered in base64 data");
            }
            byArray2[n5++] = (byte)(by2 << 2 | by3 >> 4);
            byArray2[n5++] = (byte)(by3 << 4 | by4 >> 2);
            byArray2[n5++] = (byte)(by4 << 6 | by);
            if (n5 == byArray2.length) {
                outputStream.write(byArray2);
                n5 = 0;
            }
            n6 += 3;
            n7 = this.nextI(byArray, n7, n3);
        }
        if (n5 > 0) {
            outputStream.write(byArray2, 0, n5);
        }
        int n8 = this.nextI(byArray, n7, n4);
        int n9 = this.nextI(byArray, n8 + 1, n4);
        int n10 = this.nextI(byArray, n9 + 1, n4);
        int n11 = this.nextI(byArray, n10 + 1, n4);
        return n6 += this.decodeLastBlock(outputStream, (char)byArray[n8], (char)byArray[n9], (char)byArray[n10], (char)byArray[n11]);
    }

    private int nextI(byte[] byArray, int n, int n2) {
        while (n < n2 && this.ignore((char)byArray[n])) {
            ++n;
        }
        return n;
    }

    public int decode(String string, OutputStream outputStream) throws IOException {
        int n;
        int n2;
        byte[] byArray = new byte[54];
        int n3 = 0;
        int n4 = 0;
        for (n2 = string.length(); n2 > 0 && this.ignore(string.charAt(n2 - 1)); --n2) {
        }
        if (n2 == 0) {
            return 0;
        }
        int n5 = 0;
        for (n = n2; n > 0 && n5 != 4; --n) {
            if (this.ignore(string.charAt(n - 1))) continue;
            ++n5;
        }
        n5 = this.nextI(string, 0, n);
        while (n5 < n) {
            byte by;
            byte by2 = this.decodingTable[string.charAt(n5++)];
            n5 = this.nextI(string, n5, n);
            byte by3 = this.decodingTable[string.charAt(n5++)];
            n5 = this.nextI(string, n5, n);
            byte by4 = this.decodingTable[string.charAt(n5++)];
            n5 = this.nextI(string, n5, n);
            if ((by2 | by3 | by4 | (by = this.decodingTable[string.charAt(n5++)])) < 0) {
                throw new IOException("invalid characters encountered in base64 data");
            }
            byArray[n3++] = (byte)(by2 << 2 | by3 >> 4);
            byArray[n3++] = (byte)(by3 << 4 | by4 >> 2);
            byArray[n3++] = (byte)(by4 << 6 | by);
            n4 += 3;
            if (n3 == byArray.length) {
                outputStream.write(byArray);
                n3 = 0;
            }
            n5 = this.nextI(string, n5, n);
        }
        if (n3 > 0) {
            outputStream.write(byArray, 0, n3);
        }
        int n6 = this.nextI(string, n5, n2);
        int n7 = this.nextI(string, n6 + 1, n2);
        int n8 = this.nextI(string, n7 + 1, n2);
        int n9 = this.nextI(string, n8 + 1, n2);
        return n4 += this.decodeLastBlock(outputStream, string.charAt(n6), string.charAt(n7), string.charAt(n8), string.charAt(n9));
    }

    private int decodeLastBlock(OutputStream outputStream, char c, char c2, char c3, char c4) throws IOException {
        if (c3 == this.padding) {
            if (c4 != this.padding) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            byte by = this.decodingTable[c];
            byte by2 = this.decodingTable[c2];
            if ((by | by2) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            outputStream.write(by << 2 | by2 >> 4);
            return 1;
        }
        if (c4 == this.padding) {
            byte by = this.decodingTable[c];
            byte by3 = this.decodingTable[c2];
            byte by4 = this.decodingTable[c3];
            if ((by | by3 | by4) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            outputStream.write(by << 2 | by3 >> 4);
            outputStream.write(by3 << 4 | by4 >> 2);
            return 2;
        }
        byte by = this.decodingTable[c];
        byte by5 = this.decodingTable[c2];
        byte by6 = this.decodingTable[c3];
        byte by7 = this.decodingTable[c4];
        if ((by | by5 | by6 | by7) < 0) {
            throw new IOException("invalid characters encountered at end of base64 data");
        }
        outputStream.write(by << 2 | by5 >> 4);
        outputStream.write(by5 << 4 | by6 >> 2);
        outputStream.write(by6 << 6 | by7);
        return 3;
    }

    private int nextI(String string, int n, int n2) {
        while (n < n2 && this.ignore(string.charAt(n))) {
            ++n;
        }
        return n;
    }
}


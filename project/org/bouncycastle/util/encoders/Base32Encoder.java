/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util.encoders;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Encoder;

public class Base32Encoder
implements Encoder {
    private static final byte[] DEAULT_ENCODING_TABLE = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 50, 51, 52, 53, 54, 55};
    private static final byte DEFAULT_PADDING = 61;
    private final byte[] encodingTable;
    private final byte padding;
    private final byte[] decodingTable = new byte[128];

    protected void initialiseDecodingTable() {
        int n;
        for (n = 0; n < this.decodingTable.length; ++n) {
            this.decodingTable[n] = -1;
        }
        for (n = 0; n < this.encodingTable.length; ++n) {
            this.decodingTable[this.encodingTable[n]] = (byte)n;
        }
    }

    public Base32Encoder() {
        this.encodingTable = DEAULT_ENCODING_TABLE;
        this.padding = (byte)61;
        this.initialiseDecodingTable();
    }

    public Base32Encoder(byte[] byArray, byte by) {
        if (byArray.length != 32) {
            throw new IllegalArgumentException("encoding table needs to be length 32");
        }
        this.encodingTable = Arrays.clone(byArray);
        this.padding = by;
        this.initialiseDecodingTable();
    }

    public int encode(byte[] byArray, int n, int n2, byte[] byArray2, int n3) throws IOException {
        int n4 = n;
        int n5 = n + n2 - 4;
        int n6 = n3;
        while (n4 < n5) {
            this.encodeBlock(byArray, n4, byArray2, n6);
            n4 += 5;
            n6 += 8;
        }
        int n7 = n2 - (n4 - n);
        if (n7 > 0) {
            byte[] byArray3 = new byte[5];
            System.arraycopy(byArray, n4, byArray3, 0, n7);
            this.encodeBlock(byArray3, 0, byArray2, n6);
            switch (n7) {
                case 1: {
                    byArray2[n6 + 2] = this.padding;
                    byArray2[n6 + 3] = this.padding;
                    byArray2[n6 + 4] = this.padding;
                    byArray2[n6 + 5] = this.padding;
                    byArray2[n6 + 6] = this.padding;
                    byArray2[n6 + 7] = this.padding;
                    break;
                }
                case 2: {
                    byArray2[n6 + 4] = this.padding;
                    byArray2[n6 + 5] = this.padding;
                    byArray2[n6 + 6] = this.padding;
                    byArray2[n6 + 7] = this.padding;
                    break;
                }
                case 3: {
                    byArray2[n6 + 5] = this.padding;
                    byArray2[n6 + 6] = this.padding;
                    byArray2[n6 + 7] = this.padding;
                    break;
                }
                case 4: {
                    byArray2[n6 + 7] = this.padding;
                }
            }
            n6 += 8;
        }
        return n6 - n3;
    }

    private void encodeBlock(byte[] byArray, int n, byte[] byArray2, int n2) {
        byte by = byArray[n++];
        int n3 = byArray[n++] & 0xFF;
        int n4 = byArray[n++] & 0xFF;
        int n5 = byArray[n++] & 0xFF;
        int n6 = byArray[n] & 0xFF;
        byArray2[n2++] = this.encodingTable[by >>> 3 & 0x1F];
        byArray2[n2++] = this.encodingTable[(by << 2 | n3 >>> 6) & 0x1F];
        byArray2[n2++] = this.encodingTable[n3 >>> 1 & 0x1F];
        byArray2[n2++] = this.encodingTable[(n3 << 4 | n4 >>> 4) & 0x1F];
        byArray2[n2++] = this.encodingTable[(n4 << 1 | n5 >>> 7) & 0x1F];
        byArray2[n2++] = this.encodingTable[n5 >>> 2 & 0x1F];
        byArray2[n2++] = this.encodingTable[(n5 << 3 | n6 >>> 5) & 0x1F];
        byArray2[n2] = this.encodingTable[n6 & 0x1F];
    }

    public int getEncodedLength(int n) {
        return (n + 4) / 5 * 8;
    }

    public int getMaxDecodedLength(int n) {
        return n / 8 * 5;
    }

    public int encode(byte[] byArray, int n, int n2, OutputStream outputStream) throws IOException {
        byte[] byArray2 = new byte[72];
        while (n2 > 0) {
            int n3 = Math.min(45, n2);
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
        byte[] byArray2 = new byte[55];
        int n5 = 0;
        int n6 = 0;
        for (n4 = n + n2; n4 > n && this.ignore((char)byArray[n4 - 1]); --n4) {
        }
        if (n4 == 0) {
            return 0;
        }
        int n7 = 0;
        for (n3 = n4; n3 > n && n7 != 8; --n3) {
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
            byte by5 = this.decodingTable[byArray[n7++]];
            n7 = this.nextI(byArray, n7, n3);
            byte by6 = this.decodingTable[byArray[n7++]];
            n7 = this.nextI(byArray, n7, n3);
            byte by7 = this.decodingTable[byArray[n7++]];
            n7 = this.nextI(byArray, n7, n3);
            byte by8 = this.decodingTable[byArray[n7++]];
            n7 = this.nextI(byArray, n7, n3);
            if ((by2 | by3 | by4 | by5 | by6 | by7 | by8 | (by = this.decodingTable[byArray[n7++]])) < 0) {
                throw new IOException("invalid characters encountered in base32 data");
            }
            byArray2[n5++] = (byte)(by2 << 3 | by3 >> 2);
            byArray2[n5++] = (byte)(by3 << 6 | by4 << 1 | by5 >> 4);
            byArray2[n5++] = (byte)(by5 << 4 | by6 >> 1);
            byArray2[n5++] = (byte)(by6 << 7 | by7 << 2 | by8 >> 3);
            byArray2[n5++] = (byte)(by8 << 5 | by);
            if (n5 == byArray2.length) {
                outputStream.write(byArray2);
                n5 = 0;
            }
            n6 += 5;
            n7 = this.nextI(byArray, n7, n3);
        }
        if (n5 > 0) {
            outputStream.write(byArray2, 0, n5);
        }
        int n8 = this.nextI(byArray, n7, n4);
        int n9 = this.nextI(byArray, n8 + 1, n4);
        int n10 = this.nextI(byArray, n9 + 1, n4);
        int n11 = this.nextI(byArray, n10 + 1, n4);
        int n12 = this.nextI(byArray, n11 + 1, n4);
        int n13 = this.nextI(byArray, n12 + 1, n4);
        int n14 = this.nextI(byArray, n13 + 1, n4);
        int n15 = this.nextI(byArray, n14 + 1, n4);
        return n6 += this.decodeLastBlock(outputStream, (char)byArray[n8], (char)byArray[n9], (char)byArray[n10], (char)byArray[n11], (char)byArray[n12], (char)byArray[n13], (char)byArray[n14], (char)byArray[n15]);
    }

    private int nextI(byte[] byArray, int n, int n2) {
        while (n < n2 && this.ignore((char)byArray[n])) {
            ++n;
        }
        return n;
    }

    public int decode(String string, OutputStream outputStream) throws IOException {
        byte[] byArray = Strings.toByteArray(string);
        return this.decode(byArray, 0, byArray.length, outputStream);
    }

    private int decodeLastBlock(OutputStream outputStream, char c, char c2, char c3, char c4, char c5, char c6, char c7, char c8) throws IOException {
        if (c8 == this.padding) {
            if (c7 != this.padding) {
                byte by = this.decodingTable[c];
                byte by2 = this.decodingTable[c2];
                byte by3 = this.decodingTable[c3];
                byte by4 = this.decodingTable[c4];
                byte by5 = this.decodingTable[c5];
                byte by6 = this.decodingTable[c6];
                byte by7 = this.decodingTable[c7];
                if ((by | by2 | by3 | by4 | by5 | by6 | by7) < 0) {
                    throw new IOException("invalid characters encountered at end of base32 data");
                }
                outputStream.write(by << 3 | by2 >> 2);
                outputStream.write(by2 << 6 | by3 << 1 | by4 >> 4);
                outputStream.write(by4 << 4 | by5 >> 1);
                outputStream.write(by5 << 7 | by6 << 2 | by7 >> 3);
                return 4;
            }
            if (c6 != this.padding) {
                throw new IOException("invalid characters encountered at end of base32 data");
            }
            if (c5 != this.padding) {
                byte by = this.decodingTable[c];
                byte by8 = this.decodingTable[c2];
                byte by9 = this.decodingTable[c3];
                byte by10 = this.decodingTable[c4];
                byte by11 = this.decodingTable[c5];
                if ((by | by8 | by9 | by10 | by11) < 0) {
                    throw new IOException("invalid characters encountered at end of base32 data");
                }
                outputStream.write(by << 3 | by8 >> 2);
                outputStream.write(by8 << 6 | by9 << 1 | by10 >> 4);
                outputStream.write(by10 << 4 | by11 >> 1);
                return 3;
            }
            if (c4 != this.padding) {
                byte by = this.decodingTable[c];
                byte by12 = this.decodingTable[c2];
                byte by13 = this.decodingTable[c3];
                byte by14 = this.decodingTable[c4];
                if ((by | by12 | by13 | by14) < 0) {
                    throw new IOException("invalid characters encountered at end of base32 data");
                }
                outputStream.write(by << 3 | by12 >> 2);
                outputStream.write(by12 << 6 | by13 << 1 | by14 >> 4);
                return 2;
            }
            if (c3 != this.padding) {
                throw new IOException("invalid characters encountered at end of base32 data");
            }
            byte by = this.decodingTable[c];
            byte by15 = this.decodingTable[c2];
            if ((by | by15) < 0) {
                throw new IOException("invalid characters encountered at end of base32 data");
            }
            outputStream.write(by << 3 | by15 >> 2);
            return 1;
        }
        byte by = this.decodingTable[c];
        byte by16 = this.decodingTable[c2];
        byte by17 = this.decodingTable[c3];
        byte by18 = this.decodingTable[c4];
        byte by19 = this.decodingTable[c5];
        byte by20 = this.decodingTable[c6];
        byte by21 = this.decodingTable[c7];
        byte by22 = this.decodingTable[c8];
        if ((by | by16 | by17 | by18 | by19 | by20 | by21 | by22) < 0) {
            throw new IOException("invalid characters encountered at end of base32 data");
        }
        outputStream.write(by << 3 | by16 >> 2);
        outputStream.write(by16 << 6 | by17 << 1 | by18 >> 4);
        outputStream.write(by18 << 4 | by19 >> 1);
        outputStream.write(by19 << 7 | by20 << 2 | by21 >> 3);
        outputStream.write(by21 << 5 | by22);
        return 5;
    }
}


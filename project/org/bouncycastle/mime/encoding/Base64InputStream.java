/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.encoding;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class Base64InputStream
extends InputStream {
    private static final byte[] decodingTable;
    InputStream in;
    int[] outBuf = new int[3];
    int bufPtr = 3;

    private int decode(int n, int n2, int n3, int n4, int[] nArray) throws EOFException {
        if (n4 < 0) {
            throw new EOFException("unexpected end of file in armored stream.");
        }
        if (n3 == 61) {
            int n5 = decodingTable[n] & 0xFF;
            int n6 = decodingTable[n2] & 0xFF;
            nArray[2] = (n5 << 2 | n6 >> 4) & 0xFF;
            return 2;
        }
        if (n4 == 61) {
            byte by = decodingTable[n];
            byte by2 = decodingTable[n2];
            byte by3 = decodingTable[n3];
            nArray[1] = (by << 2 | by2 >> 4) & 0xFF;
            nArray[2] = (by2 << 4 | by3 >> 2) & 0xFF;
            return 1;
        }
        byte by = decodingTable[n];
        byte by4 = decodingTable[n2];
        byte by5 = decodingTable[n3];
        byte by6 = decodingTable[n4];
        nArray[0] = (by << 2 | by4 >> 4) & 0xFF;
        nArray[1] = (by4 << 4 | by5 >> 2) & 0xFF;
        nArray[2] = (by5 << 6 | by6) & 0xFF;
        return 0;
    }

    public Base64InputStream(InputStream inputStream) {
        this.in = inputStream;
    }

    public int available() throws IOException {
        return 0;
    }

    public int read() throws IOException {
        if (this.bufPtr > 2) {
            int n = this.readIgnoreSpaceFirst();
            if (n < 0) {
                return -1;
            }
            int n2 = this.readIgnoreSpace();
            int n3 = this.readIgnoreSpace();
            int n4 = this.readIgnoreSpace();
            this.bufPtr = this.decode(n, n2, n3, n4, this.outBuf);
        }
        return this.outBuf[this.bufPtr++];
    }

    public void close() throws IOException {
        this.in.close();
    }

    private int readIgnoreSpace() throws IOException {
        int n;
        block3: while (true) {
            n = this.in.read();
            switch (n) {
                case 9: 
                case 32: {
                    continue block3;
                }
            }
            break;
        }
        return n;
    }

    private int readIgnoreSpaceFirst() throws IOException {
        int n;
        block3: while (true) {
            n = this.in.read();
            switch (n) {
                case 9: 
                case 10: 
                case 13: 
                case 32: {
                    continue block3;
                }
            }
            break;
        }
        return n;
    }

    static {
        int n;
        decodingTable = new byte[128];
        for (n = 65; n <= 90; ++n) {
            Base64InputStream.decodingTable[n] = (byte)(n - 65);
        }
        for (n = 97; n <= 122; ++n) {
            Base64InputStream.decodingTable[n] = (byte)(n - 97 + 26);
        }
        for (n = 48; n <= 57; ++n) {
            Base64InputStream.decodingTable[n] = (byte)(n - 48 + 52);
        }
        Base64InputStream.decodingTable[43] = 62;
        Base64InputStream.decodingTable[47] = 63;
    }
}


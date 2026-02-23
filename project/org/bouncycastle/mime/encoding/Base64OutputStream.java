/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.encoding;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.util.encoders.Base64Encoder;

public class Base64OutputStream
extends FilterOutputStream {
    private static final Base64Encoder ENCODER = new Base64Encoder();
    private static final int INBUF_SIZE = 54;
    private static final int OUTBUF_SIZE = 74;
    private final byte[] inBuf = new byte[54];
    private final byte[] outBuf = new byte[74];
    private int inPos = 0;

    public Base64OutputStream(OutputStream outputStream) {
        super(outputStream);
        this.outBuf[72] = 13;
        this.outBuf[73] = 10;
    }

    public void write(int n) throws IOException {
        this.inBuf[this.inPos++] = (byte)n;
        if (this.inPos == 54) {
            this.encodeBlock(this.inBuf, 0);
            this.inPos = 0;
        }
    }

    public void write(byte[] byArray, int n, int n2) throws IOException {
        int n3;
        int n4 = 54 - this.inPos;
        if (n2 < n4) {
            System.arraycopy(byArray, n, this.inBuf, this.inPos, n2);
            this.inPos += n2;
            return;
        }
        int n5 = 0;
        if (this.inPos > 0) {
            System.arraycopy(byArray, n, this.inBuf, this.inPos, n4);
            n5 += n4;
            this.encodeBlock(this.inBuf, 0);
        }
        while ((n3 = n2 - n5) >= 54) {
            this.encodeBlock(byArray, n + n5);
            n5 += 54;
        }
        System.arraycopy(byArray, n + n5, this.inBuf, 0, n3);
        this.inPos = n3;
    }

    public void write(byte[] byArray) throws IOException {
        this.write(byArray, 0, byArray.length);
    }

    public void close() throws IOException {
        if (this.inPos > 0) {
            int n = ENCODER.encode(this.inBuf, 0, this.inPos, this.outBuf, 0);
            this.inPos = 0;
            this.outBuf[n++] = 13;
            this.outBuf[n++] = 10;
            this.out.write(this.outBuf, 0, n);
        }
        this.out.close();
    }

    private void encodeBlock(byte[] byArray, int n) throws IOException {
        ENCODER.encode(byArray, n, 54, this.outBuf, 0);
        this.out.write(this.outBuf, 0, 74);
    }
}


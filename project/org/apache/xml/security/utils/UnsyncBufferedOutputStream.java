/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.io.IOException;
import java.io.OutputStream;

public class UnsyncBufferedOutputStream
extends OutputStream {
    static final int size = 8192;
    private int pointer = 0;
    private final OutputStream out;
    private final byte[] buf = new byte[8192];

    public UnsyncBufferedOutputStream(OutputStream out) {
        this.out = out;
    }

    public void write(byte[] arg0) throws IOException {
        this.write(arg0, 0, arg0.length);
    }

    public void write(byte[] arg0, int arg1, int len) throws IOException {
        int newLen = this.pointer + len;
        if (newLen > 8192) {
            this.flushBuffer();
            if (len > 8192) {
                this.out.write(arg0, arg1, len);
                return;
            }
            newLen = len;
        }
        System.arraycopy(arg0, arg1, this.buf, this.pointer, len);
        this.pointer = newLen;
    }

    private void flushBuffer() throws IOException {
        if (this.pointer > 0) {
            this.out.write(this.buf, 0, this.pointer);
        }
        this.pointer = 0;
    }

    public void write(int arg0) throws IOException {
        if (this.pointer >= 8192) {
            this.flushBuffer();
        }
        this.buf[this.pointer++] = (byte)arg0;
    }

    public void flush() throws IOException {
        this.flushBuffer();
        this.out.flush();
    }

    public void close() throws IOException {
        this.flush();
        this.out.close();
    }
}


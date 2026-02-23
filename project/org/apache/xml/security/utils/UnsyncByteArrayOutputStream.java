/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.io.OutputStream;

public class UnsyncByteArrayOutputStream
extends OutputStream {
    private static final int INITIAL_SIZE = 8192;
    private byte[] buf = new byte[8192];
    private int size = 8192;
    private int pos = 0;

    public void write(byte[] arg0) {
        if (Integer.MAX_VALUE - this.pos < arg0.length) {
            throw new OutOfMemoryError();
        }
        int newPos = this.pos + arg0.length;
        if (newPos > this.size) {
            this.expandSize(newPos);
        }
        System.arraycopy(arg0, 0, this.buf, this.pos, arg0.length);
        this.pos = newPos;
    }

    public void write(byte[] arg0, int arg1, int arg2) {
        if (Integer.MAX_VALUE - this.pos < arg2) {
            throw new OutOfMemoryError();
        }
        int newPos = this.pos + arg2;
        if (newPos > this.size) {
            this.expandSize(newPos);
        }
        System.arraycopy(arg0, arg1, this.buf, this.pos, arg2);
        this.pos = newPos;
    }

    public void write(int arg0) {
        if (Integer.MAX_VALUE - this.pos == 0) {
            throw new OutOfMemoryError();
        }
        int newPos = this.pos + 1;
        if (newPos > this.size) {
            this.expandSize(newPos);
        }
        this.buf[this.pos++] = (byte)arg0;
    }

    public byte[] toByteArray() {
        byte[] result = new byte[this.pos];
        System.arraycopy(this.buf, 0, result, 0, this.pos);
        return result;
    }

    public void reset() {
        this.pos = 0;
    }

    private void expandSize(int newPos) {
        int newSize = this.size;
        while (newPos > newSize) {
            if ((newSize <<= 1) >= 0) continue;
            newSize = Integer.MAX_VALUE;
        }
        byte[] newBuf = new byte[newSize];
        System.arraycopy(this.buf, 0, newBuf, 0, this.pos);
        this.buf = newBuf;
        this.size = newSize;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.Strings;

public class BoundaryLimitedInputStream
extends InputStream {
    private final InputStream src;
    private final byte[] boundary;
    private final byte[] buf;
    private int bufOff = 0;
    private int index = 0;
    private boolean ended = false;
    private int lastI;

    public BoundaryLimitedInputStream(InputStream inputStream, String string) {
        this.src = inputStream;
        this.boundary = Strings.toByteArray(string);
        this.buf = new byte[string.length() + 3];
        this.bufOff = 0;
    }

    public int read() throws IOException {
        int n;
        if (this.ended) {
            return -1;
        }
        if (this.index < this.bufOff) {
            n = this.buf[this.index++] & 0xFF;
            if (this.index < this.bufOff) {
                return n;
            }
            this.bufOff = 0;
            this.index = 0;
        } else {
            n = this.src.read();
        }
        this.lastI = n;
        if (n < 0) {
            return -1;
        }
        if (n == 13 || n == 10) {
            int n2;
            this.index = 0;
            if (n == 13) {
                n2 = this.src.read();
                if (n2 == 10) {
                    this.buf[this.bufOff++] = 10;
                    n2 = this.src.read();
                }
            } else {
                n2 = this.src.read();
            }
            if (n2 == 45) {
                this.buf[this.bufOff++] = 45;
                n2 = this.src.read();
            }
            if (n2 == 45) {
                int n3;
                this.buf[this.bufOff++] = 45;
                int n4 = this.bufOff;
                while (this.bufOff - n4 != this.boundary.length && (n3 = this.src.read()) >= 0) {
                    this.buf[this.bufOff] = (byte)n3;
                    if (this.buf[this.bufOff] != this.boundary[this.bufOff - n4]) {
                        ++this.bufOff;
                        break;
                    }
                    ++this.bufOff;
                }
                if (this.bufOff - n4 == this.boundary.length) {
                    this.ended = true;
                    return -1;
                }
            } else if (n2 >= 0) {
                this.buf[this.bufOff++] = (byte)n2;
            }
        }
        return n;
    }
}


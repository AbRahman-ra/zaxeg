/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.encoding;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class QuotedPrintableInputStream
extends FilterInputStream {
    public QuotedPrintableInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public int read(byte[] byArray, int n, int n2) throws IOException {
        int n3;
        int n4;
        for (n4 = 0; n4 != n2 && (n3 = this.read()) >= 0; ++n4) {
            byArray[n4 + n] = (byte)n3;
        }
        if (n4 == 0) {
            return -1;
        }
        return n4;
    }

    public int read() throws IOException {
        int n = this.in.read();
        if (n == -1) {
            return -1;
        }
        while (n == 61) {
            int n2 = this.in.read();
            if (n2 == -1) {
                throw new IllegalStateException("Quoted '=' at end of stream");
            }
            if (n2 == 13) {
                n2 = this.in.read();
                if (n2 == 10) {
                    n2 = this.in.read();
                }
                n = n2;
                continue;
            }
            if (n2 == 10) {
                n = this.in.read();
                continue;
            }
            int n3 = 0;
            if (n2 >= 48 && n2 <= 57) {
                n3 = n2 - 48;
            } else if (n2 >= 65 && n2 <= 70) {
                n3 = 10 + (n2 - 65);
            } else {
                throw new IllegalStateException("Expecting '0123456789ABCDEF after quote that was not immediately followed by LF or CRLF");
            }
            n3 <<= 4;
            n2 = this.in.read();
            if (n2 >= 48 && n2 <= 57) {
                n3 |= n2 - 48;
            } else if (n2 >= 65 && n2 <= 70) {
                n3 |= 10 + (n2 - 65);
            } else {
                throw new IllegalStateException("Expecting second '0123456789ABCDEF after quote that was not immediately followed by LF or CRLF");
            }
            return n3;
        }
        return n;
    }
}


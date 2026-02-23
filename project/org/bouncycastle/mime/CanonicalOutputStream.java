/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.smime.SMimeParserContext;

public class CanonicalOutputStream
extends FilterOutputStream {
    protected int lastb = -1;
    protected static byte[] newline = new byte[2];
    private final boolean is7Bit;

    public CanonicalOutputStream(SMimeParserContext sMimeParserContext, Headers headers, OutputStream outputStream) {
        super(outputStream);
        this.is7Bit = headers.getContentType() != null ? headers.getContentType() != null && !headers.getContentType().equals("binary") : sMimeParserContext.getDefaultContentTransferEncoding().equals("7bit");
    }

    public void write(int n) throws IOException {
        if (this.is7Bit) {
            if (n == 13) {
                this.out.write(newline);
            } else if (n == 10) {
                if (this.lastb != 13) {
                    this.out.write(newline);
                }
            } else {
                this.out.write(n);
            }
        } else {
            this.out.write(n);
        }
        this.lastb = n;
    }

    public void write(byte[] byArray) throws IOException {
        this.write(byArray, 0, byArray.length);
    }

    public void write(byte[] byArray, int n, int n2) throws IOException {
        for (int i = n; i != n + n2; ++i) {
            this.write(byArray[i]);
        }
    }

    public void writeln() throws IOException {
        this.out.write(newline);
    }

    static {
        CanonicalOutputStream.newline[0] = 13;
        CanonicalOutputStream.newline[1] = 10;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.Strings;

class LineReader {
    private final InputStream src;
    private int lastC = -1;

    LineReader(InputStream inputStream) {
        this.src = inputStream;
    }

    String readLine() throws IOException {
        int n;
        int n2;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (this.lastC != -1) {
            if (this.lastC == 13) {
                return "";
            }
            n2 = this.lastC;
            this.lastC = -1;
        } else {
            n2 = this.src.read();
        }
        while (n2 >= 0 && n2 != 13 && n2 != 10) {
            byteArrayOutputStream.write(n2);
            n2 = this.src.read();
        }
        if (n2 == 13 && (n = this.src.read()) != 10 && n >= 0) {
            this.lastC = n;
        }
        if (n2 < 0) {
            return null;
        }
        return Strings.fromUTF8ByteArray(byteArrayOutputStream.toByteArray());
    }
}


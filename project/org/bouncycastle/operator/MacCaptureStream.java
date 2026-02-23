/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.operator;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.util.Arrays;

public class MacCaptureStream
extends OutputStream {
    private final OutputStream cOut;
    private final byte[] mac;
    int macIndex = 0;

    public MacCaptureStream(OutputStream outputStream, int n) {
        this.cOut = outputStream;
        this.mac = new byte[n];
    }

    public void write(byte[] byArray, int n, int n2) throws IOException {
        if (n2 >= this.mac.length) {
            this.cOut.write(this.mac, 0, this.macIndex);
            this.macIndex = this.mac.length;
            System.arraycopy(byArray, n + n2 - this.mac.length, this.mac, 0, this.mac.length);
            this.cOut.write(byArray, n, n2 - this.mac.length);
        } else {
            for (int i = 0; i != n2; ++i) {
                this.write(byArray[n + i]);
            }
        }
    }

    public void write(int n) throws IOException {
        if (this.macIndex == this.mac.length) {
            byte by = this.mac[0];
            System.arraycopy(this.mac, 1, this.mac, 0, this.mac.length - 1);
            this.mac[this.mac.length - 1] = (byte)n;
            this.cOut.write(by);
        } else {
            this.mac[this.macIndex++] = (byte)n;
        }
    }

    public byte[] getMac() {
        return Arrays.clone(this.mac);
    }
}


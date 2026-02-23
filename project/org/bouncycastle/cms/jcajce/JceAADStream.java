/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cms.jcajce;

import java.io.IOException;
import java.io.OutputStream;
import javax.crypto.Cipher;

class JceAADStream
extends OutputStream {
    private static final byte[] SINGLE_BYTE = new byte[1];
    private Cipher cipher;

    JceAADStream(Cipher cipher) {
        this.cipher = cipher;
    }

    public void write(byte[] byArray, int n, int n2) throws IOException {
        this.cipher.updateAAD(byArray, n, n2);
    }

    public void write(int n) throws IOException {
        JceAADStream.SINGLE_BYTE[0] = (byte)n;
        this.cipher.updateAAD(SINGLE_BYTE, 0, 1);
    }
}


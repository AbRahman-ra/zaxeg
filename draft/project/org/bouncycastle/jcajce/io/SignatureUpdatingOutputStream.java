/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.io;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;

class SignatureUpdatingOutputStream
extends OutputStream {
    private Signature sig;

    SignatureUpdatingOutputStream(Signature signature) {
        this.sig = signature;
    }

    public void write(byte[] byArray, int n, int n2) throws IOException {
        try {
            this.sig.update(byArray, n, n2);
        } catch (SignatureException signatureException) {
            throw new IOException(signatureException.getMessage());
        }
    }

    public void write(byte[] byArray) throws IOException {
        try {
            this.sig.update(byArray);
        } catch (SignatureException signatureException) {
            throw new IOException(signatureException.getMessage());
        }
    }

    public void write(int n) throws IOException {
        try {
            this.sig.update((byte)n);
        } catch (SignatureException signatureException) {
            throw new IOException(signatureException.getMessage());
        }
    }
}


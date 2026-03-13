/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.signers;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.Ed448PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.math.ec.rfc8032.Ed448;
import org.bouncycastle.util.Arrays;

public class Ed448Signer
implements Signer {
    private final Buffer buffer = new Buffer();
    private final byte[] context;
    private boolean forSigning;
    private Ed448PrivateKeyParameters privateKey;
    private Ed448PublicKeyParameters publicKey;

    public Ed448Signer(byte[] byArray) {
        this.context = Arrays.clone(byArray);
    }

    public void init(boolean bl, CipherParameters cipherParameters) {
        this.forSigning = bl;
        if (bl) {
            this.privateKey = (Ed448PrivateKeyParameters)cipherParameters;
            this.publicKey = null;
        } else {
            this.privateKey = null;
            this.publicKey = (Ed448PublicKeyParameters)cipherParameters;
        }
        this.reset();
    }

    public void update(byte by) {
        this.buffer.write(by);
    }

    public void update(byte[] byArray, int n, int n2) {
        this.buffer.write(byArray, n, n2);
    }

    public byte[] generateSignature() {
        if (!this.forSigning || null == this.privateKey) {
            throw new IllegalStateException("Ed448Signer not initialised for signature generation.");
        }
        return this.buffer.generateSignature(this.privateKey, this.context);
    }

    public boolean verifySignature(byte[] byArray) {
        if (this.forSigning || null == this.publicKey) {
            throw new IllegalStateException("Ed448Signer not initialised for verification");
        }
        return this.buffer.verifySignature(this.publicKey, this.context, byArray);
    }

    public void reset() {
        this.buffer.reset();
    }

    private static class Buffer
    extends ByteArrayOutputStream {
        private Buffer() {
        }

        synchronized byte[] generateSignature(Ed448PrivateKeyParameters ed448PrivateKeyParameters, byte[] byArray) {
            byte[] byArray2 = new byte[114];
            ed448PrivateKeyParameters.sign(0, byArray, this.buf, 0, this.count, byArray2, 0);
            this.reset();
            return byArray2;
        }

        synchronized boolean verifySignature(Ed448PublicKeyParameters ed448PublicKeyParameters, byte[] byArray, byte[] byArray2) {
            if (114 != byArray2.length) {
                this.reset();
                return false;
            }
            byte[] byArray3 = ed448PublicKeyParameters.getEncoded();
            boolean bl = Ed448.verify(byArray2, 0, byArray3, 0, byArray, this.buf, 0, this.count);
            this.reset();
            return bl;
        }

        public synchronized void reset() {
            Arrays.fill(this.buf, 0, this.count, (byte)0);
            this.count = 0;
        }
    }
}


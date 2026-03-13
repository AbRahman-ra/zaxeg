/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.signers;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.Xof;
import org.bouncycastle.crypto.params.Ed448PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.math.ec.rfc8032.Ed448;
import org.bouncycastle.util.Arrays;

public class Ed448phSigner
implements Signer {
    private final Xof prehash = Ed448.createPrehash();
    private final byte[] context;
    private boolean forSigning;
    private Ed448PrivateKeyParameters privateKey;
    private Ed448PublicKeyParameters publicKey;

    public Ed448phSigner(byte[] byArray) {
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
        this.prehash.update(by);
    }

    public void update(byte[] byArray, int n, int n2) {
        this.prehash.update(byArray, n, n2);
    }

    public byte[] generateSignature() {
        if (!this.forSigning || null == this.privateKey) {
            throw new IllegalStateException("Ed448phSigner not initialised for signature generation.");
        }
        byte[] byArray = new byte[64];
        if (64 != this.prehash.doFinal(byArray, 0, 64)) {
            throw new IllegalStateException("Prehash digest failed");
        }
        byte[] byArray2 = new byte[114];
        this.privateKey.sign(1, this.context, byArray, 0, 64, byArray2, 0);
        return byArray2;
    }

    public boolean verifySignature(byte[] byArray) {
        if (this.forSigning || null == this.publicKey) {
            throw new IllegalStateException("Ed448phSigner not initialised for verification");
        }
        if (114 != byArray.length) {
            this.prehash.reset();
            return false;
        }
        byte[] byArray2 = this.publicKey.getEncoded();
        return Ed448.verifyPrehash(byArray, 0, byArray2, 0, this.context, this.prehash);
    }

    public void reset() {
        this.prehash.reset();
    }
}


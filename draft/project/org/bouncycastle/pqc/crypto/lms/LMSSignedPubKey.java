/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.IOException;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;
import org.bouncycastle.util.Encodable;

class LMSSignedPubKey
implements Encodable {
    private final LMSSignature signature;
    private final LMSPublicKeyParameters publicKey;

    public LMSSignedPubKey(LMSSignature lMSSignature, LMSPublicKeyParameters lMSPublicKeyParameters) {
        this.signature = lMSSignature;
        this.publicKey = lMSPublicKeyParameters;
    }

    public LMSSignature getSignature() {
        return this.signature;
    }

    public LMSPublicKeyParameters getPublicKey() {
        return this.publicKey;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        LMSSignedPubKey lMSSignedPubKey = (LMSSignedPubKey)object;
        if (this.signature != null ? !this.signature.equals(lMSSignedPubKey.signature) : lMSSignedPubKey.signature != null) {
            return false;
        }
        return this.publicKey != null ? this.publicKey.equals(lMSSignedPubKey.publicKey) : lMSSignedPubKey.publicKey == null;
    }

    public int hashCode() {
        int n = this.signature != null ? this.signature.hashCode() : 0;
        n = 31 * n + (this.publicKey != null ? this.publicKey.hashCode() : 0);
        return n;
    }

    public byte[] getEncoded() throws IOException {
        return Composer.compose().bytes(this.signature.getEncoded()).bytes(this.publicKey.getEncoded()).build();
    }
}


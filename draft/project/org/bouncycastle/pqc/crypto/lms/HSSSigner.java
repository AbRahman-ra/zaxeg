/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.IOException;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.crypto.MessageSigner;
import org.bouncycastle.pqc.crypto.lms.HSS;
import org.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.HSSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.HSSSignature;

public class HSSSigner
implements MessageSigner {
    private HSSPrivateKeyParameters privKey;
    private HSSPublicKeyParameters pubKey;

    public void init(boolean bl, CipherParameters cipherParameters) {
        if (bl) {
            this.privKey = (HSSPrivateKeyParameters)cipherParameters;
        } else {
            this.pubKey = (HSSPublicKeyParameters)cipherParameters;
        }
    }

    public byte[] generateSignature(byte[] byArray) {
        try {
            return HSS.generateSignature(this.privKey, byArray).getEncoded();
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to encode signature: " + iOException.getMessage());
        }
    }

    public boolean verifySignature(byte[] byArray, byte[] byArray2) {
        try {
            return HSS.verifySignature(this.pubKey, HSSSignature.getInstance(byArray2, this.pubKey.getL()), byArray);
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to decode signature: " + iOException.getMessage());
        }
    }
}


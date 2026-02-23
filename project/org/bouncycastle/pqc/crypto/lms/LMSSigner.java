/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.IOException;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.crypto.MessageSigner;
import org.bouncycastle.pqc.crypto.lms.LMS;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;

public class LMSSigner
implements MessageSigner {
    private LMSPrivateKeyParameters privKey;
    private LMSPublicKeyParameters pubKey;

    public void init(boolean bl, CipherParameters cipherParameters) {
        if (bl) {
            this.privKey = (LMSPrivateKeyParameters)cipherParameters;
        } else {
            this.pubKey = (LMSPublicKeyParameters)cipherParameters;
        }
    }

    public byte[] generateSignature(byte[] byArray) {
        try {
            return LMS.generateSign(this.privKey, byArray).getEncoded();
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to encode signature: " + iOException.getMessage());
        }
    }

    public boolean verifySignature(byte[] byArray, byte[] byArray2) {
        try {
            return LMS.verifySignature(this.pubKey, LMSSignature.getInstance(byArray2), byArray);
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to decode signature: " + iOException.getMessage());
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.HSSSignature;
import org.bouncycastle.pqc.crypto.lms.LMS;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSContextBasedVerifier;
import org.bouncycastle.pqc.crypto.lms.LMSKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;
import org.bouncycastle.pqc.crypto.lms.LMSSignedPubKey;
import org.bouncycastle.util.io.Streams;

public class HSSPublicKeyParameters
extends LMSKeyParameters
implements LMSContextBasedVerifier {
    private final int l;
    private final LMSPublicKeyParameters lmsPublicKey;

    public HSSPublicKeyParameters(int n, LMSPublicKeyParameters lMSPublicKeyParameters) {
        super(false);
        this.l = n;
        this.lmsPublicKey = lMSPublicKeyParameters;
    }

    public static HSSPublicKeyParameters getInstance(Object object) throws IOException {
        if (object instanceof HSSPublicKeyParameters) {
            return (HSSPublicKeyParameters)object;
        }
        if (object instanceof DataInputStream) {
            int n = ((DataInputStream)object).readInt();
            LMSPublicKeyParameters lMSPublicKeyParameters = LMSPublicKeyParameters.getInstance(object);
            return new HSSPublicKeyParameters(n, lMSPublicKeyParameters);
        }
        if (object instanceof byte[]) {
            InputStream inputStream = null;
            try {
                inputStream = new DataInputStream(new ByteArrayInputStream((byte[])object));
                HSSPublicKeyParameters hSSPublicKeyParameters = HSSPublicKeyParameters.getInstance(inputStream);
                return hSSPublicKeyParameters;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        if (object instanceof InputStream) {
            return HSSPublicKeyParameters.getInstance(Streams.readAll((InputStream)object));
        }
        throw new IllegalArgumentException("cannot parse " + object);
    }

    public int getL() {
        return this.l;
    }

    public LMSPublicKeyParameters getLMSPublicKey() {
        return this.lmsPublicKey;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        HSSPublicKeyParameters hSSPublicKeyParameters = (HSSPublicKeyParameters)object;
        if (this.l != hSSPublicKeyParameters.l) {
            return false;
        }
        return this.lmsPublicKey.equals(hSSPublicKeyParameters.lmsPublicKey);
    }

    public int hashCode() {
        int n = this.l;
        n = 31 * n + this.lmsPublicKey.hashCode();
        return n;
    }

    public byte[] getEncoded() throws IOException {
        return Composer.compose().u32str(this.l).bytes(this.lmsPublicKey.getEncoded()).build();
    }

    public LMSContext generateLMSContext(byte[] byArray) {
        HSSSignature hSSSignature;
        try {
            hSSSignature = HSSSignature.getInstance(byArray, this.getL());
        } catch (IOException iOException) {
            throw new IllegalStateException("cannot parse signature: " + iOException.getMessage());
        }
        LMSSignedPubKey[] lMSSignedPubKeyArray = hSSSignature.getSignedPubKey();
        LMSPublicKeyParameters lMSPublicKeyParameters = lMSSignedPubKeyArray[lMSSignedPubKeyArray.length - 1].getPublicKey();
        return lMSPublicKeyParameters.generateOtsContext(hSSSignature.getSignature()).withSignedPublicKeys(lMSSignedPubKeyArray);
    }

    public boolean verify(LMSContext lMSContext) {
        boolean bl = false;
        LMSSignedPubKey[] lMSSignedPubKeyArray = lMSContext.getSignedPubKeys();
        if (lMSSignedPubKeyArray.length != this.getL() - 1) {
            return false;
        }
        LMSPublicKeyParameters lMSPublicKeyParameters = this.getLMSPublicKey();
        for (int i = 0; i < lMSSignedPubKeyArray.length; ++i) {
            byte[] byArray;
            LMSSignature lMSSignature = lMSSignedPubKeyArray[i].getSignature();
            if (!LMS.verifySignature(lMSPublicKeyParameters, lMSSignature, byArray = lMSSignedPubKeyArray[i].getPublicKey().toByteArray())) {
                bl = true;
            }
            lMSPublicKeyParameters = lMSSignedPubKeyArray[i].getPublicKey();
        }
        return !bl & lMSPublicKeyParameters.verify(lMSContext);
    }
}


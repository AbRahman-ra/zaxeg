/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;
import org.bouncycastle.pqc.crypto.lms.LMSSignedPubKey;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.io.Streams;

public class HSSSignature
implements Encodable {
    private final int lMinus1;
    private final LMSSignedPubKey[] signedPubKey;
    private final LMSSignature signature;

    public HSSSignature(int n, LMSSignedPubKey[] lMSSignedPubKeyArray, LMSSignature lMSSignature) {
        this.lMinus1 = n;
        this.signedPubKey = lMSSignedPubKeyArray;
        this.signature = lMSSignature;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static HSSSignature getInstance(Object object, int n) throws IOException {
        if (object instanceof HSSSignature) {
            return (HSSSignature)object;
        }
        if (object instanceof DataInputStream) {
            int n2 = ((DataInputStream)object).readInt();
            if (n2 != n - 1) {
                throw new IllegalStateException("nspk exceeded maxNspk");
            }
            LMSSignedPubKey[] lMSSignedPubKeyArray = new LMSSignedPubKey[n2];
            if (n2 != 0) {
                for (int i = 0; i < lMSSignedPubKeyArray.length; ++i) {
                    lMSSignedPubKeyArray[i] = new LMSSignedPubKey(LMSSignature.getInstance(object), LMSPublicKeyParameters.getInstance(object));
                }
            }
            LMSSignature lMSSignature = LMSSignature.getInstance(object);
            return new HSSSignature(n2, lMSSignedPubKeyArray, lMSSignature);
        }
        if (object instanceof byte[]) {
            InputStream inputStream = null;
            try {
                inputStream = new DataInputStream(new ByteArrayInputStream((byte[])object));
                HSSSignature hSSSignature = HSSSignature.getInstance(inputStream, n);
                return hSSSignature;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        if (object instanceof InputStream) {
            return HSSSignature.getInstance(Streams.readAll((InputStream)object), n);
        }
        throw new IllegalArgumentException("cannot parse " + object);
    }

    public int getlMinus1() {
        return this.lMinus1;
    }

    public LMSSignedPubKey[] getSignedPubKey() {
        return this.signedPubKey;
    }

    public LMSSignature getSignature() {
        return this.signature;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        HSSSignature hSSSignature = (HSSSignature)object;
        if (this.lMinus1 != hSSSignature.lMinus1) {
            return false;
        }
        if (this.signedPubKey.length != hSSSignature.signedPubKey.length) {
            return false;
        }
        for (int i = 0; i < this.signedPubKey.length; ++i) {
            if (this.signedPubKey[i].equals(hSSSignature.signedPubKey[i])) continue;
            return false;
        }
        return this.signature != null ? this.signature.equals(hSSSignature.signature) : hSSSignature.signature == null;
    }

    public int hashCode() {
        int n = this.lMinus1;
        n = 31 * n + Arrays.hashCode(this.signedPubKey);
        n = 31 * n + (this.signature != null ? this.signature.hashCode() : 0);
        return n;
    }

    public byte[] getEncoded() throws IOException {
        Composer composer = Composer.compose();
        composer.u32str(this.lMinus1);
        if (this.signedPubKey != null) {
            for (LMSSignedPubKey lMSSignedPubKey : this.signedPubKey) {
                composer.bytes(lMSSignedPubKey);
            }
        }
        composer.bytes(this.signature);
        return composer.build();
    }
}


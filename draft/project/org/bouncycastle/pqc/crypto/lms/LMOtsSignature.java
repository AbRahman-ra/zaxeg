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
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.io.Streams;

class LMOtsSignature
implements Encodable {
    private final LMOtsParameters type;
    private final byte[] C;
    private final byte[] y;

    public LMOtsSignature(LMOtsParameters lMOtsParameters, byte[] byArray, byte[] byArray2) {
        this.type = lMOtsParameters;
        this.C = byArray;
        this.y = byArray2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static LMOtsSignature getInstance(Object object) throws IOException {
        if (object instanceof LMOtsSignature) {
            return (LMOtsSignature)object;
        }
        if (object instanceof DataInputStream) {
            LMOtsParameters lMOtsParameters = LMOtsParameters.getParametersForType(((DataInputStream)object).readInt());
            byte[] byArray = new byte[lMOtsParameters.getN()];
            ((DataInputStream)object).readFully(byArray);
            byte[] byArray2 = new byte[lMOtsParameters.getP() * lMOtsParameters.getN()];
            ((DataInputStream)object).readFully(byArray2);
            return new LMOtsSignature(lMOtsParameters, byArray, byArray2);
        }
        if (object instanceof byte[]) {
            InputStream inputStream = null;
            try {
                inputStream = new DataInputStream(new ByteArrayInputStream((byte[])object));
                LMOtsSignature lMOtsSignature = LMOtsSignature.getInstance(inputStream);
                return lMOtsSignature;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        if (object instanceof InputStream) {
            return LMOtsSignature.getInstance(Streams.readAll((InputStream)object));
        }
        throw new IllegalArgumentException("cannot parse " + object);
    }

    public LMOtsParameters getType() {
        return this.type;
    }

    public byte[] getC() {
        return this.C;
    }

    public byte[] getY() {
        return this.y;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        LMOtsSignature lMOtsSignature = (LMOtsSignature)object;
        if (this.type != null ? !this.type.equals(lMOtsSignature.type) : lMOtsSignature.type != null) {
            return false;
        }
        if (!Arrays.equals(this.C, lMOtsSignature.C)) {
            return false;
        }
        return Arrays.equals(this.y, lMOtsSignature.y);
    }

    public int hashCode() {
        int n = this.type != null ? this.type.hashCode() : 0;
        n = 31 * n + Arrays.hashCode(this.C);
        n = 31 * n + Arrays.hashCode(this.y);
        return n;
    }

    public byte[] getEncoded() throws IOException {
        return Composer.compose().u32str(this.type.getType()).bytes(this.C).bytes(this.y).build();
    }
}


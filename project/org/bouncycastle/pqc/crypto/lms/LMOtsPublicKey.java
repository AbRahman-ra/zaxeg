/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.DigestUtil;
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMOtsSignature;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;
import org.bouncycastle.pqc.crypto.lms.LmsUtils;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.io.Streams;

class LMOtsPublicKey
implements Encodable {
    private final LMOtsParameters parameter;
    private final byte[] I;
    private final int q;
    private final byte[] K;

    public LMOtsPublicKey(LMOtsParameters lMOtsParameters, byte[] byArray, int n, byte[] byArray2) {
        this.parameter = lMOtsParameters;
        this.I = byArray;
        this.q = n;
        this.K = byArray2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static LMOtsPublicKey getInstance(Object object) throws Exception {
        if (object instanceof LMOtsPublicKey) {
            return (LMOtsPublicKey)object;
        }
        if (object instanceof DataInputStream) {
            LMOtsParameters lMOtsParameters = LMOtsParameters.getParametersForType(((DataInputStream)object).readInt());
            byte[] byArray = new byte[16];
            ((DataInputStream)object).readFully(byArray);
            int n = ((DataInputStream)object).readInt();
            byte[] byArray2 = new byte[lMOtsParameters.getN()];
            ((DataInputStream)object).readFully(byArray2);
            return new LMOtsPublicKey(lMOtsParameters, byArray, n, byArray2);
        }
        if (object instanceof byte[]) {
            InputStream inputStream = null;
            try {
                inputStream = new DataInputStream(new ByteArrayInputStream((byte[])object));
                LMOtsPublicKey lMOtsPublicKey = LMOtsPublicKey.getInstance(inputStream);
                return lMOtsPublicKey;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        if (object instanceof InputStream) {
            return LMOtsPublicKey.getInstance(Streams.readAll((InputStream)object));
        }
        throw new IllegalArgumentException("cannot parse " + object);
    }

    public LMOtsParameters getParameter() {
        return this.parameter;
    }

    public byte[] getI() {
        return this.I;
    }

    public int getQ() {
        return this.q;
    }

    public byte[] getK() {
        return this.K;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        LMOtsPublicKey lMOtsPublicKey = (LMOtsPublicKey)object;
        if (this.q != lMOtsPublicKey.q) {
            return false;
        }
        if (this.parameter != null ? !this.parameter.equals(lMOtsPublicKey.parameter) : lMOtsPublicKey.parameter != null) {
            return false;
        }
        if (!Arrays.equals(this.I, lMOtsPublicKey.I)) {
            return false;
        }
        return Arrays.equals(this.K, lMOtsPublicKey.K);
    }

    public int hashCode() {
        int n = this.parameter != null ? this.parameter.hashCode() : 0;
        n = 31 * n + Arrays.hashCode(this.I);
        n = 31 * n + this.q;
        n = 31 * n + Arrays.hashCode(this.K);
        return n;
    }

    public byte[] getEncoded() throws IOException {
        return Composer.compose().u32str(this.parameter.getType()).bytes(this.I).u32str(this.q).bytes(this.K).build();
    }

    LMSContext createOtsContext(LMOtsSignature lMOtsSignature) {
        Digest digest = DigestUtil.getDigest(this.parameter.getDigestOID());
        LmsUtils.byteArray(this.I, digest);
        LmsUtils.u32str(this.q, digest);
        LmsUtils.u16str((short)-32383, digest);
        LmsUtils.byteArray(lMOtsSignature.getC(), digest);
        return new LMSContext(this, lMOtsSignature, digest);
    }

    LMSContext createOtsContext(LMSSignature lMSSignature) {
        Digest digest = DigestUtil.getDigest(this.parameter.getDigestOID());
        LmsUtils.byteArray(this.I, digest);
        LmsUtils.u32str(this.q, digest);
        LmsUtils.u16str((short)-32383, digest);
        LmsUtils.byteArray(lMSSignature.getOtsSignature().getC(), digest);
        return new LMSContext(this, lMSSignature, digest);
    }
}


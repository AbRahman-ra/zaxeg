/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.lms.DigestUtil;
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;
import org.bouncycastle.pqc.crypto.lms.LmsUtils;
import org.bouncycastle.pqc.crypto.lms.SeedDerive;

class LMOtsPrivateKey {
    private final LMOtsParameters parameter;
    private final byte[] I;
    private final int q;
    private final byte[] masterSecret;

    public LMOtsPrivateKey(LMOtsParameters lMOtsParameters, byte[] byArray, int n, byte[] byArray2) {
        this.parameter = lMOtsParameters;
        this.I = byArray;
        this.q = n;
        this.masterSecret = byArray2;
    }

    LMSContext getSignatureContext(LMSigParameters lMSigParameters, byte[][] byArray) {
        byte[] byArray2 = new byte[32];
        SeedDerive seedDerive = this.getDerivationFunction();
        seedDerive.setJ(-3);
        seedDerive.deriveSeed(byArray2, false);
        Digest digest = DigestUtil.getDigest(this.parameter.getDigestOID());
        LmsUtils.byteArray(this.getI(), digest);
        LmsUtils.u32str(this.getQ(), digest);
        LmsUtils.u16str((short)-32383, digest);
        LmsUtils.byteArray(byArray2, digest);
        return new LMSContext(this, lMSigParameters, digest, byArray2, byArray);
    }

    SeedDerive getDerivationFunction() {
        SeedDerive seedDerive = new SeedDerive(this.I, this.masterSecret, DigestUtil.getDigest(this.parameter.getDigestOID()));
        seedDerive.setQ(this.q);
        return seedDerive;
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

    public byte[] getMasterSecret() {
        return this.masterSecret;
    }
}


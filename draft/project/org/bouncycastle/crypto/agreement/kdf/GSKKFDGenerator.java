/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement.kdf;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.DigestDerivationFunction;
import org.bouncycastle.crypto.agreement.kdf.GSKKDFParameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class GSKKFDGenerator
implements DigestDerivationFunction {
    private final Digest digest;
    private byte[] z;
    private int counter;
    private byte[] r;
    private byte[] buf;

    public GSKKFDGenerator(Digest digest) {
        this.digest = digest;
        this.buf = new byte[digest.getDigestSize()];
    }

    public Digest getDigest() {
        return this.digest;
    }

    public void init(DerivationParameters derivationParameters) {
        if (!(derivationParameters instanceof GSKKDFParameters)) {
            throw new IllegalArgumentException("unkown parameters type");
        }
        this.z = ((GSKKDFParameters)derivationParameters).getZ();
        this.counter = ((GSKKDFParameters)derivationParameters).getStartCounter();
        this.r = ((GSKKDFParameters)derivationParameters).getNonce();
    }

    public int generateBytes(byte[] byArray, int n, int n2) throws DataLengthException, IllegalArgumentException {
        if (n + n2 > byArray.length) {
            throw new DataLengthException("output buffer too small");
        }
        this.digest.update(this.z, 0, this.z.length);
        byte[] byArray2 = Pack.intToBigEndian(this.counter++);
        this.digest.update(byArray2, 0, byArray2.length);
        if (this.r != null) {
            this.digest.update(this.r, 0, this.r.length);
        }
        this.digest.doFinal(this.buf, 0);
        System.arraycopy(this.buf, 0, byArray, n, n2);
        Arrays.clear(this.buf);
        return n2;
    }
}


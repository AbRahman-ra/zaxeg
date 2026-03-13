/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.crypto.digests.XofUtils;
import org.bouncycastle.util.Arrays;

public class CSHAKEDigest
extends SHAKEDigest {
    private static final byte[] padding = new byte[100];
    private final byte[] diff;

    public CSHAKEDigest(int n, byte[] byArray, byte[] byArray2) {
        super(n);
        if (!(byArray != null && byArray.length != 0 || byArray2 != null && byArray2.length != 0)) {
            this.diff = null;
        } else {
            this.diff = Arrays.concatenate(XofUtils.leftEncode(this.rate / 8), this.encodeString(byArray), this.encodeString(byArray2));
            this.diffPadAndAbsorb();
        }
    }

    CSHAKEDigest(CSHAKEDigest cSHAKEDigest) {
        super(cSHAKEDigest);
        this.diff = Arrays.clone(cSHAKEDigest.diff);
    }

    private void diffPadAndAbsorb() {
        int n = this.rate / 8;
        this.absorb(this.diff, 0, this.diff.length);
        int n2 = this.diff.length % n;
        if (n2 != 0) {
            int n3;
            for (n3 = n - n2; n3 > padding.length; n3 -= padding.length) {
                this.absorb(padding, 0, padding.length);
            }
            this.absorb(padding, 0, n3);
        }
    }

    private byte[] encodeString(byte[] byArray) {
        if (byArray == null || byArray.length == 0) {
            return XofUtils.leftEncode(0L);
        }
        return Arrays.concatenate(XofUtils.leftEncode((long)byArray.length * 8L), byArray);
    }

    public String getAlgorithmName() {
        return "CSHAKE" + this.fixedOutputLength;
    }

    public int doOutput(byte[] byArray, int n, int n2) {
        if (this.diff != null) {
            if (!this.squeezing) {
                this.absorbBits(0, 2);
            }
            this.squeeze(byArray, n, (long)n2 * 8L);
            return n2;
        }
        return super.doOutput(byArray, n, n2);
    }

    public void reset() {
        super.reset();
        if (this.diff != null) {
            this.diffPadAndAbsorb();
        }
    }
}


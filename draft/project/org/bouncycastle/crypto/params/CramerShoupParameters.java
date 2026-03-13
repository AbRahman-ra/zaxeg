/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.util.Memoable;

public class CramerShoupParameters
implements CipherParameters {
    private BigInteger p;
    private BigInteger g1;
    private BigInteger g2;
    private Digest H;

    public CramerShoupParameters(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, Digest digest) {
        this.p = bigInteger;
        this.g1 = bigInteger2;
        this.g2 = bigInteger3;
        this.H = (Digest)((Object)((Memoable)((Object)digest)).copy());
        this.H.reset();
    }

    public boolean equals(Object object) {
        if (!(object instanceof CramerShoupParameters)) {
            return false;
        }
        CramerShoupParameters cramerShoupParameters = (CramerShoupParameters)object;
        return cramerShoupParameters.getP().equals(this.p) && cramerShoupParameters.getG1().equals(this.g1) && cramerShoupParameters.getG2().equals(this.g2);
    }

    public int hashCode() {
        return this.getP().hashCode() ^ this.getG1().hashCode() ^ this.getG2().hashCode();
    }

    public BigInteger getG1() {
        return this.g1;
    }

    public BigInteger getG2() {
        return this.g2;
    }

    public BigInteger getP() {
        return this.p;
    }

    public Digest getH() {
        return (Digest)((Object)((Memoable)((Object)this.H)).copy());
    }
}


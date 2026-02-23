/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.Digest;

class SeedDerive {
    private final byte[] I;
    private final byte[] masterSeed;
    private final Digest digest;
    private int q;
    private int j;

    public SeedDerive(byte[] byArray, byte[] byArray2, Digest digest) {
        this.I = byArray;
        this.masterSeed = byArray2;
        this.digest = digest;
    }

    public int getQ() {
        return this.q;
    }

    public void setQ(int n) {
        this.q = n;
    }

    public int getJ() {
        return this.j;
    }

    public void setJ(int n) {
        this.j = n;
    }

    public byte[] getI() {
        return this.I;
    }

    public byte[] getMasterSeed() {
        return this.masterSeed;
    }

    public byte[] deriveSeed(byte[] byArray, int n) {
        if (byArray.length < this.digest.getDigestSize()) {
            throw new IllegalArgumentException("target length is less than digest size.");
        }
        this.digest.update(this.I, 0, this.I.length);
        this.digest.update((byte)(this.q >>> 24));
        this.digest.update((byte)(this.q >>> 16));
        this.digest.update((byte)(this.q >>> 8));
        this.digest.update((byte)this.q);
        this.digest.update((byte)(this.j >>> 8));
        this.digest.update((byte)this.j);
        this.digest.update((byte)-1);
        this.digest.update(this.masterSeed, 0, this.masterSeed.length);
        this.digest.doFinal(byArray, n);
        return byArray;
    }

    public void deriveSeed(byte[] byArray, boolean bl) {
        this.deriveSeed(byArray, bl, 0);
    }

    public void deriveSeed(byte[] byArray, boolean bl, int n) {
        this.deriveSeed(byArray, n);
        if (bl) {
            ++this.j;
        }
    }
}


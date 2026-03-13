/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.engines.Zuc128CoreEngine;

public final class Zuc128Mac
implements Mac {
    private static final int TOPBIT = 128;
    private final InternalZuc128Engine theEngine = new InternalZuc128Engine();
    private int theMac;
    private final int[] theKeyStream = new int[2];
    private Zuc128CoreEngine theState;
    private int theWordIndex;
    private int theByteIndex;

    public String getAlgorithmName() {
        return "Zuc128Mac";
    }

    public int getMacSize() {
        return 4;
    }

    public void init(CipherParameters cipherParameters) {
        this.theEngine.init(true, cipherParameters);
        this.theState = (Zuc128CoreEngine)this.theEngine.copy();
        this.initKeyStream();
    }

    private void initKeyStream() {
        this.theMac = 0;
        for (int i = 0; i < this.theKeyStream.length - 1; ++i) {
            this.theKeyStream[i] = this.theEngine.createKeyStreamWord();
        }
        this.theWordIndex = this.theKeyStream.length - 1;
        this.theByteIndex = 3;
    }

    public void update(byte by) {
        this.shift4NextByte();
        int n = this.theByteIndex * 8;
        int n2 = 128;
        int n3 = 0;
        while (n2 > 0) {
            if ((by & n2) != 0) {
                this.updateMac(n + n3);
            }
            n2 >>= 1;
            ++n3;
        }
    }

    private void shift4NextByte() {
        this.theByteIndex = (this.theByteIndex + 1) % 4;
        if (this.theByteIndex == 0) {
            this.theKeyStream[this.theWordIndex] = this.theEngine.createKeyStreamWord();
            this.theWordIndex = (this.theWordIndex + 1) % this.theKeyStream.length;
        }
    }

    private void updateMac(int n) {
        this.theMac ^= this.getKeyStreamWord(n);
    }

    private int getKeyStreamWord(int n) {
        int n2 = this.theKeyStream[this.theWordIndex];
        if (n == 0) {
            return n2;
        }
        int n3 = this.theKeyStream[(this.theWordIndex + 1) % this.theKeyStream.length];
        return n2 << n | n3 >>> 32 - n;
    }

    public void update(byte[] byArray, int n, int n2) {
        for (int i = 0; i < n2; ++i) {
            this.update(byArray[n + i]);
        }
    }

    private int getFinalWord() {
        if (this.theByteIndex != 0) {
            return this.theEngine.createKeyStreamWord();
        }
        this.theWordIndex = (this.theWordIndex + 1) % this.theKeyStream.length;
        return this.theKeyStream[this.theWordIndex];
    }

    public int doFinal(byte[] byArray, int n) {
        this.shift4NextByte();
        this.theMac ^= this.getKeyStreamWord(this.theByteIndex * 8);
        this.theMac ^= this.getFinalWord();
        Zuc128CoreEngine.encode32be(this.theMac, byArray, n);
        this.reset();
        return this.getMacSize();
    }

    public void reset() {
        if (this.theState != null) {
            this.theEngine.reset(this.theState);
        }
        this.initKeyStream();
    }

    private static class InternalZuc128Engine
    extends Zuc128CoreEngine {
        private InternalZuc128Engine() {
        }

        int createKeyStreamWord() {
            return super.makeKeyStreamWord();
        }
    }
}


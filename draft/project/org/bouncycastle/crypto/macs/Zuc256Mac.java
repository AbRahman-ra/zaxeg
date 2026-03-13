/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.engines.Zuc256CoreEngine;

public final class Zuc256Mac
implements Mac {
    private static final int TOPBIT = 128;
    private final InternalZuc256Engine theEngine;
    private final int theMacLength;
    private final int[] theMac;
    private final int[] theKeyStream;
    private Zuc256CoreEngine theState;
    private int theWordIndex;
    private int theByteIndex;

    public Zuc256Mac(int n) {
        this.theEngine = new InternalZuc256Engine(n);
        this.theMacLength = n;
        int n2 = n / 32;
        this.theMac = new int[n2];
        this.theKeyStream = new int[n2 + 1];
    }

    public String getAlgorithmName() {
        return "Zuc256Mac-" + this.theMacLength;
    }

    public int getMacSize() {
        return this.theMacLength / 8;
    }

    public void init(CipherParameters cipherParameters) {
        this.theEngine.init(true, cipherParameters);
        this.theState = (Zuc256CoreEngine)this.theEngine.copy();
        this.initKeyStream();
    }

    private void initKeyStream() {
        int n;
        for (n = 0; n < this.theMac.length; ++n) {
            this.theMac[n] = this.theEngine.createKeyStreamWord();
        }
        for (n = 0; n < this.theKeyStream.length - 1; ++n) {
            this.theKeyStream[n] = this.theEngine.createKeyStreamWord();
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

    private void shift4Final() {
        this.theByteIndex = (this.theByteIndex + 1) % 4;
        if (this.theByteIndex == 0) {
            this.theWordIndex = (this.theWordIndex + 1) % this.theKeyStream.length;
        }
    }

    private void updateMac(int n) {
        for (int i = 0; i < this.theMac.length; ++i) {
            int n2 = i;
            this.theMac[n2] = this.theMac[n2] ^ this.getKeyStreamWord(i, n);
        }
    }

    private int getKeyStreamWord(int n, int n2) {
        int n3 = this.theKeyStream[(this.theWordIndex + n) % this.theKeyStream.length];
        if (n2 == 0) {
            return n3;
        }
        int n4 = this.theKeyStream[(this.theWordIndex + n + 1) % this.theKeyStream.length];
        return n3 << n2 | n4 >>> 32 - n2;
    }

    public void update(byte[] byArray, int n, int n2) {
        for (int i = 0; i < n2; ++i) {
            this.update(byArray[n + i]);
        }
    }

    public int doFinal(byte[] byArray, int n) {
        this.shift4Final();
        this.updateMac(this.theByteIndex * 8);
        for (int i = 0; i < this.theMac.length; ++i) {
            Zuc256CoreEngine.encode32be(this.theMac[i], byArray, n + i * 4);
        }
        this.reset();
        return this.getMacSize();
    }

    public void reset() {
        if (this.theState != null) {
            this.theEngine.reset(this.theState);
        }
        this.initKeyStream();
    }

    private static class InternalZuc256Engine
    extends Zuc256CoreEngine {
        public InternalZuc256Engine(int n) {
            super(n);
        }

        int createKeyStreamWord() {
            return super.makeKeyStreamWord();
        }
    }
}


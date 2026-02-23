/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.engines.Zuc128CoreEngine;
import org.bouncycastle.util.Memoable;

public class Zuc256CoreEngine
extends Zuc128CoreEngine {
    private static final byte[] EK_d = new byte[]{34, 47, 36, 42, 109, 64, 64, 64, 64, 64, 64, 64, 64, 82, 16, 48};
    private static final byte[] EK_d32 = new byte[]{34, 47, 37, 42, 109, 64, 64, 64, 64, 64, 64, 64, 64, 82, 16, 48};
    private static final byte[] EK_d64 = new byte[]{35, 47, 36, 42, 109, 64, 64, 64, 64, 64, 64, 64, 64, 82, 16, 48};
    private static final byte[] EK_d128 = new byte[]{35, 47, 37, 42, 109, 64, 64, 64, 64, 64, 64, 64, 64, 82, 16, 48};
    private byte[] theD;

    protected Zuc256CoreEngine() {
        this.theD = EK_d;
    }

    protected Zuc256CoreEngine(int n) {
        switch (n) {
            case 32: {
                this.theD = EK_d32;
                break;
            }
            case 64: {
                this.theD = EK_d64;
                break;
            }
            case 128: {
                this.theD = EK_d128;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported length: " + n);
            }
        }
    }

    protected Zuc256CoreEngine(Zuc256CoreEngine zuc256CoreEngine) {
        super(zuc256CoreEngine);
    }

    protected int getMaxIterations() {
        return 625;
    }

    public String getAlgorithmName() {
        return "Zuc-256";
    }

    private static int MAKEU31(byte by, byte by2, byte by3, byte by4) {
        return (by & 0xFF) << 23 | (by2 & 0xFF) << 16 | (by3 & 0xFF) << 8 | by4 & 0xFF;
    }

    protected void setKeyAndIV(int[] nArray, byte[] byArray, byte[] byArray2) {
        if (byArray == null || byArray.length != 32) {
            throw new IllegalArgumentException("A key of 32 bytes is needed");
        }
        if (byArray2 == null || byArray2.length != 25) {
            throw new IllegalArgumentException("An IV of 25 bytes is needed");
        }
        nArray[0] = Zuc256CoreEngine.MAKEU31(byArray[0], this.theD[0], byArray[21], byArray[16]);
        nArray[1] = Zuc256CoreEngine.MAKEU31(byArray[1], this.theD[1], byArray[22], byArray[17]);
        nArray[2] = Zuc256CoreEngine.MAKEU31(byArray[2], this.theD[2], byArray[23], byArray[18]);
        nArray[3] = Zuc256CoreEngine.MAKEU31(byArray[3], this.theD[3], byArray[24], byArray[19]);
        nArray[4] = Zuc256CoreEngine.MAKEU31(byArray[4], this.theD[4], byArray[25], byArray[20]);
        nArray[5] = Zuc256CoreEngine.MAKEU31(byArray2[0], (byte)(this.theD[5] | byArray2[17] & 0x3F), byArray[5], byArray[26]);
        nArray[6] = Zuc256CoreEngine.MAKEU31(byArray2[1], (byte)(this.theD[6] | byArray2[18] & 0x3F), byArray[6], byArray[27]);
        nArray[7] = Zuc256CoreEngine.MAKEU31(byArray2[10], (byte)(this.theD[7] | byArray2[19] & 0x3F), byArray[7], byArray2[2]);
        nArray[8] = Zuc256CoreEngine.MAKEU31(byArray[8], (byte)(this.theD[8] | byArray2[20] & 0x3F), byArray2[3], byArray2[11]);
        nArray[9] = Zuc256CoreEngine.MAKEU31(byArray[9], (byte)(this.theD[9] | byArray2[21] & 0x3F), byArray2[12], byArray2[4]);
        nArray[10] = Zuc256CoreEngine.MAKEU31(byArray2[5], (byte)(this.theD[10] | byArray2[22] & 0x3F), byArray[10], byArray[28]);
        nArray[11] = Zuc256CoreEngine.MAKEU31(byArray[11], (byte)(this.theD[11] | byArray2[23] & 0x3F), byArray2[6], byArray2[13]);
        nArray[12] = Zuc256CoreEngine.MAKEU31(byArray[12], (byte)(this.theD[12] | byArray2[24] & 0x3F), byArray2[7], byArray2[14]);
        nArray[13] = Zuc256CoreEngine.MAKEU31(byArray[13], this.theD[13], byArray2[15], byArray2[8]);
        nArray[14] = Zuc256CoreEngine.MAKEU31(byArray[14], (byte)(this.theD[14] | byArray[31] >>> 4 & 0xF), byArray2[16], byArray2[9]);
        nArray[15] = Zuc256CoreEngine.MAKEU31(byArray[15], (byte)(this.theD[15] | byArray[31] & 0xF), byArray[30], byArray[29]);
    }

    public Memoable copy() {
        return new Zuc256CoreEngine(this);
    }

    public void reset(Memoable memoable) {
        Zuc256CoreEngine zuc256CoreEngine = (Zuc256CoreEngine)memoable;
        super.reset(memoable);
        this.theD = zuc256CoreEngine.theD;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;

public class GOST3412_2015Engine
implements BlockCipher {
    private static final byte[] PI = new byte[]{-4, -18, -35, 17, -49, 110, 49, 22, -5, -60, -6, -38, 35, -59, 4, 77, -23, 119, -16, -37, -109, 46, -103, -70, 23, 54, -15, -69, 20, -51, 95, -63, -7, 24, 101, 90, -30, 92, -17, 33, -127, 28, 60, 66, -117, 1, -114, 79, 5, -124, 2, -82, -29, 106, -113, -96, 6, 11, -19, -104, 127, -44, -45, 31, -21, 52, 44, 81, -22, -56, 72, -85, -14, 42, 104, -94, -3, 58, -50, -52, -75, 112, 14, 86, 8, 12, 118, 18, -65, 114, 19, 71, -100, -73, 93, -121, 21, -95, -106, 41, 16, 123, -102, -57, -13, -111, 120, 111, -99, -98, -78, -79, 50, 117, 25, 61, -1, 53, -118, 126, 109, 84, -58, -128, -61, -67, 13, 87, -33, -11, 36, -87, 62, -88, 67, -55, -41, 121, -42, -10, 124, 34, -71, 3, -32, 15, -20, -34, 122, -108, -80, -68, -36, -24, 40, 80, 78, 51, 10, 74, -89, -105, 96, 115, 30, 0, 98, 68, 26, -72, 56, -126, 100, -97, 38, 65, -83, 69, 70, -110, 39, 94, 85, 47, -116, -93, -91, 125, 105, -43, -107, 59, 7, 88, -77, 64, -122, -84, 29, -9, 48, 55, 107, -28, -120, -39, -25, -119, -31, 27, -125, 73, 76, 63, -8, -2, -115, 83, -86, -112, -54, -40, -123, 97, 32, 113, 103, -92, 45, 43, 9, 91, -53, -101, 37, -48, -66, -27, 108, 82, 89, -90, 116, -46, -26, -12, -76, -64, -47, 102, -81, -62, 57, 75, 99, -74};
    private static final byte[] inversePI = new byte[]{-91, 45, 50, -113, 14, 48, 56, -64, 84, -26, -98, 57, 85, 126, 82, -111, 100, 3, 87, 90, 28, 96, 7, 24, 33, 114, -88, -47, 41, -58, -92, 63, -32, 39, -115, 12, -126, -22, -82, -76, -102, 99, 73, -27, 66, -28, 21, -73, -56, 6, 112, -99, 65, 117, 25, -55, -86, -4, 77, -65, 42, 115, -124, -43, -61, -81, 43, -122, -89, -79, -78, 91, 70, -45, -97, -3, -44, 15, -100, 47, -101, 67, -17, -39, 121, -74, 83, 127, -63, -16, 35, -25, 37, 94, -75, 30, -94, -33, -90, -2, -84, 34, -7, -30, 74, -68, 53, -54, -18, 120, 5, 107, 81, -31, 89, -93, -14, 113, 86, 17, 106, -119, -108, 101, -116, -69, 119, 60, 123, 40, -85, -46, 49, -34, -60, 95, -52, -49, 118, 44, -72, -40, 46, 54, -37, 105, -77, 20, -107, -66, 98, -95, 59, 22, 102, -23, 92, 108, 109, -83, 55, 97, 75, -71, -29, -70, -15, -96, -123, -125, -38, 71, -59, -80, 51, -6, -106, 111, 110, -62, -10, 80, -1, 93, -87, -114, 23, 27, -105, 125, -20, 88, -9, 31, -5, 124, 9, 13, 122, 103, 69, -121, -36, -24, 79, 29, 78, 4, -21, -8, -13, 62, 61, -67, -118, -120, -35, -51, 11, 19, -104, 2, -109, -128, -112, -48, 36, 52, -53, -19, -12, -50, -103, 16, 68, 64, -110, 58, 1, 38, 18, 26, 72, 104, -11, -127, -117, -57, -42, 32, 10, 8, 0, 76, -41, 116};
    private final byte[] lFactors = new byte[]{-108, 32, -123, 16, -62, -64, 1, -5, 1, -64, -62, 16, -123, 32, -108, 1};
    protected static final int BLOCK_SIZE = 16;
    private int KEY_LENGTH = 32;
    private int SUB_LENGTH = this.KEY_LENGTH / 2;
    private byte[][] subKeys = null;
    private boolean forEncryption;
    private byte[][] _gf_mul = GOST3412_2015Engine.init_gf256_mul_table();

    private static byte[][] init_gf256_mul_table() {
        byte[][] byArrayArray = new byte[256][];
        for (int i = 0; i < 256; ++i) {
            byArrayArray[i] = new byte[256];
            for (int j = 0; j < 256; ++j) {
                byArrayArray[i][j] = GOST3412_2015Engine.kuz_mul_gf256_slow((byte)i, (byte)j);
            }
        }
        return byArrayArray;
    }

    private static byte kuz_mul_gf256_slow(byte by, byte by2) {
        byte by3 = 0;
        for (int n = 0; n < 8 && by != 0 && by2 != 0; by2 = (byte)(by2 >> 1), n = (int)((byte)(n + 1))) {
            if ((by2 & 1) != 0) {
                by3 = (byte)(by3 ^ by);
            }
            byte by4 = (byte)(by & 0x80);
            by = (byte)(by << 1);
            if (by4 == 0) continue;
            by = (byte)(by ^ 0xC3);
        }
        return by3;
    }

    public String getAlgorithmName() {
        return "GOST3412_2015";
    }

    public int getBlockSize() {
        return 16;
    }

    public void init(boolean bl, CipherParameters cipherParameters) throws IllegalArgumentException {
        if (cipherParameters instanceof KeyParameter) {
            this.forEncryption = bl;
            this.generateSubKeys(((KeyParameter)cipherParameters).getKey());
        } else if (cipherParameters != null) {
            throw new IllegalArgumentException("invalid parameter passed to GOST3412_2015 init - " + cipherParameters.getClass().getName());
        }
    }

    private void generateSubKeys(byte[] byArray) {
        if (byArray.length != this.KEY_LENGTH) {
            throw new IllegalArgumentException("Key length invalid. Key needs to be 32 byte - 256 bit!!!");
        }
        this.subKeys = new byte[10][];
        for (int i = 0; i < 10; ++i) {
            this.subKeys[i] = new byte[this.SUB_LENGTH];
        }
        byte[] byArray2 = new byte[this.SUB_LENGTH];
        byte[] byArray3 = new byte[this.SUB_LENGTH];
        for (int i = 0; i < this.SUB_LENGTH; ++i) {
            this.subKeys[0][i] = byArray2[i] = byArray[i];
            this.subKeys[1][i] = byArray3[i] = byArray[i + this.SUB_LENGTH];
        }
        byte[] byArray4 = new byte[this.SUB_LENGTH];
        for (int i = 1; i < 5; ++i) {
            for (int j = 1; j <= 8; ++j) {
                this.C(byArray4, 8 * (i - 1) + j);
                this.F(byArray4, byArray2, byArray3);
            }
            System.arraycopy(byArray2, 0, this.subKeys[2 * i], 0, this.SUB_LENGTH);
            System.arraycopy(byArray3, 0, this.subKeys[2 * i + 1], 0, this.SUB_LENGTH);
        }
    }

    private void C(byte[] byArray, int n) {
        Arrays.clear(byArray);
        byArray[15] = (byte)n;
        this.L(byArray);
    }

    private void F(byte[] byArray, byte[] byArray2, byte[] byArray3) {
        byte[] byArray4 = this.LSX(byArray, byArray2);
        this.X(byArray4, byArray3);
        System.arraycopy(byArray2, 0, byArray3, 0, this.SUB_LENGTH);
        System.arraycopy(byArray4, 0, byArray2, 0, this.SUB_LENGTH);
    }

    public int processBlock(byte[] byArray, int n, byte[] byArray2, int n2) throws DataLengthException, IllegalStateException {
        if (this.subKeys == null) {
            throw new IllegalStateException("GOST3412_2015 engine not initialised");
        }
        if (n + 16 > byArray.length) {
            throw new DataLengthException("input buffer too short");
        }
        if (n2 + 16 > byArray2.length) {
            throw new OutputLengthException("output buffer too short");
        }
        this.GOST3412_2015Func(byArray, n, byArray2, n2);
        return 16;
    }

    private void GOST3412_2015Func(byte[] byArray, int n, byte[] byArray2, int n2) {
        byte[] byArray3 = new byte[16];
        System.arraycopy(byArray, n, byArray3, 0, 16);
        if (this.forEncryption) {
            for (int i = 0; i < 9; ++i) {
                byte[] byArray4 = this.LSX(this.subKeys[i], byArray3);
                byArray3 = Arrays.copyOf(byArray4, 16);
            }
            this.X(byArray3, this.subKeys[9]);
        } else {
            for (int i = 9; i > 0; --i) {
                byte[] byArray5 = this.XSL(this.subKeys[i], byArray3);
                byArray3 = Arrays.copyOf(byArray5, 16);
            }
            this.X(byArray3, this.subKeys[0]);
        }
        System.arraycopy(byArray3, 0, byArray2, n2, 16);
    }

    private byte[] LSX(byte[] byArray, byte[] byArray2) {
        byte[] byArray3 = Arrays.copyOf(byArray, byArray.length);
        this.X(byArray3, byArray2);
        this.S(byArray3);
        this.L(byArray3);
        return byArray3;
    }

    private byte[] XSL(byte[] byArray, byte[] byArray2) {
        byte[] byArray3 = Arrays.copyOf(byArray, byArray.length);
        this.X(byArray3, byArray2);
        this.inverseL(byArray3);
        this.inverseS(byArray3);
        return byArray3;
    }

    private void X(byte[] byArray, byte[] byArray2) {
        for (int i = 0; i < byArray.length; ++i) {
            int n = i;
            byArray[n] = (byte)(byArray[n] ^ byArray2[i]);
        }
    }

    private void S(byte[] byArray) {
        for (int i = 0; i < byArray.length; ++i) {
            byArray[i] = PI[this.unsignedByte(byArray[i])];
        }
    }

    private void inverseS(byte[] byArray) {
        for (int i = 0; i < byArray.length; ++i) {
            byArray[i] = inversePI[this.unsignedByte(byArray[i])];
        }
    }

    private int unsignedByte(byte by) {
        return by & 0xFF;
    }

    private void L(byte[] byArray) {
        for (int i = 0; i < 16; ++i) {
            this.R(byArray);
        }
    }

    private void inverseL(byte[] byArray) {
        for (int i = 0; i < 16; ++i) {
            this.inverseR(byArray);
        }
    }

    private void R(byte[] byArray) {
        byte by = this.l(byArray);
        System.arraycopy(byArray, 0, byArray, 1, 15);
        byArray[0] = by;
    }

    private void inverseR(byte[] byArray) {
        byte[] byArray2 = new byte[16];
        System.arraycopy(byArray, 1, byArray2, 0, 15);
        byArray2[15] = byArray[0];
        byte by = this.l(byArray2);
        System.arraycopy(byArray, 1, byArray, 0, 15);
        byArray[15] = by;
    }

    private byte l(byte[] byArray) {
        byte by = byArray[15];
        for (int i = 14; i >= 0; --i) {
            by = (byte)(by ^ this._gf_mul[this.unsignedByte(byArray[i])][this.unsignedByte(this.lFactors[i])]);
        }
        return by;
    }

    public void reset() {
    }
}


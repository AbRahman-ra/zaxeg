/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Pack;

public class NoekeonEngine
implements BlockCipher {
    private static final int SIZE = 16;
    private static final byte[] roundConstants = new byte[]{-128, 27, 54, 108, -40, -85, 77, -102, 47, 94, -68, 99, -58, -105, 53, 106, -44};
    private final int[] k = new int[4];
    private boolean _initialised = false;
    private boolean _forEncryption;

    public String getAlgorithmName() {
        return "Noekeon";
    }

    public int getBlockSize() {
        return 16;
    }

    public void init(boolean bl, CipherParameters cipherParameters) {
        if (!(cipherParameters instanceof KeyParameter)) {
            throw new IllegalArgumentException("invalid parameter passed to Noekeon init - " + cipherParameters.getClass().getName());
        }
        KeyParameter keyParameter = (KeyParameter)cipherParameters;
        byte[] byArray = keyParameter.getKey();
        if (byArray.length != 16) {
            throw new IllegalArgumentException("Key length not 128 bits.");
        }
        Pack.bigEndianToInt(byArray, 0, this.k, 0, 4);
        if (!bl) {
            int n = this.k[0];
            int n2 = this.k[1];
            int n3 = this.k[2];
            int n4 = this.k[3];
            int n5 = n ^ n3;
            n5 ^= Integers.rotateLeft(n5, 8) ^ Integers.rotateLeft(n5, 24);
            int n6 = n2 ^ n4;
            n6 ^= Integers.rotateLeft(n6, 8) ^ Integers.rotateLeft(n6, 24);
            this.k[0] = n ^= n6;
            this.k[1] = n2 ^= n5;
            this.k[2] = n3 ^= n6;
            this.k[3] = n4 ^= n5;
        }
        this._forEncryption = bl;
        this._initialised = true;
    }

    public int processBlock(byte[] byArray, int n, byte[] byArray2, int n2) {
        if (!this._initialised) {
            throw new IllegalStateException(this.getAlgorithmName() + " not initialised");
        }
        if (n > byArray.length - 16) {
            throw new DataLengthException("input buffer too short");
        }
        if (n2 > byArray2.length - 16) {
            throw new OutputLengthException("output buffer too short");
        }
        return this._forEncryption ? this.encryptBlock(byArray, n, byArray2, n2) : this.decryptBlock(byArray, n, byArray2, n2);
    }

    public void reset() {
    }

    private int encryptBlock(byte[] byArray, int n, byte[] byArray2, int n2) {
        int n3 = Pack.bigEndianToInt(byArray, n);
        int n4 = Pack.bigEndianToInt(byArray, n + 4);
        int n5 = Pack.bigEndianToInt(byArray, n + 8);
        int n6 = Pack.bigEndianToInt(byArray, n + 12);
        int n7 = this.k[0];
        int n8 = this.k[1];
        int n9 = this.k[2];
        int n10 = this.k[3];
        int n11 = 0;
        while (true) {
            int n12 = (n3 ^= roundConstants[n11] & 0xFF) ^ n5;
            n12 ^= Integers.rotateLeft(n12, 8) ^ Integers.rotateLeft(n12, 24);
            n3 ^= n7;
            n5 ^= n9;
            int n13 = (n4 ^= n8) ^ (n6 ^= n10);
            n13 ^= Integers.rotateLeft(n13, 8) ^ Integers.rotateLeft(n13, 24);
            n3 ^= n13;
            n4 ^= n12;
            n5 ^= n13;
            n6 ^= n12;
            if (++n11 > 16) break;
            n4 = Integers.rotateLeft(n4, 1);
            n5 = Integers.rotateLeft(n5, 5);
            n12 = n6 = Integers.rotateLeft(n6, 2);
            n4 ^= n6 | n5;
            n6 = n3 ^ n5 & ~n4;
            n5 = n12 ^ ~n4 ^ n5 ^ n6;
            n3 = n12 ^ n5 & (n4 ^= n6 | n5);
            n4 = Integers.rotateLeft(n4, 31);
            n5 = Integers.rotateLeft(n5, 27);
            n6 = Integers.rotateLeft(n6, 30);
        }
        Pack.intToBigEndian(n3, byArray2, n2);
        Pack.intToBigEndian(n4, byArray2, n2 + 4);
        Pack.intToBigEndian(n5, byArray2, n2 + 8);
        Pack.intToBigEndian(n6, byArray2, n2 + 12);
        return 16;
    }

    private int decryptBlock(byte[] byArray, int n, byte[] byArray2, int n2) {
        int n3 = Pack.bigEndianToInt(byArray, n);
        int n4 = Pack.bigEndianToInt(byArray, n + 4);
        int n5 = Pack.bigEndianToInt(byArray, n + 8);
        int n6 = Pack.bigEndianToInt(byArray, n + 12);
        int n7 = this.k[0];
        int n8 = this.k[1];
        int n9 = this.k[2];
        int n10 = this.k[3];
        int n11 = 16;
        while (true) {
            int n12 = n3 ^ n5;
            n12 ^= Integers.rotateLeft(n12, 8) ^ Integers.rotateLeft(n12, 24);
            n3 ^= n7;
            n5 ^= n9;
            int n13 = (n4 ^= n8) ^ (n6 ^= n10);
            n13 ^= Integers.rotateLeft(n13, 8) ^ Integers.rotateLeft(n13, 24);
            n3 ^= n13;
            n4 ^= n12;
            n5 ^= n13;
            n6 ^= n12;
            n3 ^= roundConstants[n11] & 0xFF;
            if (--n11 < 0) break;
            n4 = Integers.rotateLeft(n4, 1);
            n5 = Integers.rotateLeft(n5, 5);
            n12 = n6 = Integers.rotateLeft(n6, 2);
            n4 ^= n6 | n5;
            n6 = n3 ^ n5 & ~n4;
            n5 = n12 ^ ~n4 ^ n5 ^ n6;
            n3 = n12 ^ n5 & (n4 ^= n6 | n5);
            n4 = Integers.rotateLeft(n4, 31);
            n5 = Integers.rotateLeft(n5, 27);
            n6 = Integers.rotateLeft(n6, 30);
        }
        Pack.intToBigEndian(n3, byArray2, n2);
        Pack.intToBigEndian(n4, byArray2, n2 + 4);
        Pack.intToBigEndian(n5, byArray2, n2 + 8);
        Pack.intToBigEndian(n6, byArray2, n2 + 12);
        return 16;
    }
}


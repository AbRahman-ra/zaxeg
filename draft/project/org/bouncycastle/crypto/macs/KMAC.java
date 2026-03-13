/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.Xof;
import org.bouncycastle.crypto.digests.CSHAKEDigest;
import org.bouncycastle.crypto.digests.XofUtils;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public class KMAC
implements Mac,
Xof {
    private static final byte[] padding = new byte[100];
    private final CSHAKEDigest cshake;
    private final int bitLength;
    private final int outputLength;
    private byte[] key;
    private boolean initialised;
    private boolean firstOutput;

    public KMAC(int n, byte[] byArray) {
        this.cshake = new CSHAKEDigest(n, Strings.toByteArray("KMAC"), byArray);
        this.bitLength = n;
        this.outputLength = n * 2 / 8;
    }

    public void init(CipherParameters cipherParameters) throws IllegalArgumentException {
        KeyParameter keyParameter = (KeyParameter)cipherParameters;
        this.key = Arrays.clone(keyParameter.getKey());
        this.initialised = true;
        this.reset();
    }

    public String getAlgorithmName() {
        return "KMAC" + this.cshake.getAlgorithmName().substring(6);
    }

    public int getByteLength() {
        return this.cshake.getByteLength();
    }

    public int getMacSize() {
        return this.outputLength;
    }

    public int getDigestSize() {
        return this.outputLength;
    }

    public void update(byte by) throws IllegalStateException {
        if (!this.initialised) {
            throw new IllegalStateException("KMAC not initialized");
        }
        this.cshake.update(by);
    }

    public void update(byte[] byArray, int n, int n2) throws DataLengthException, IllegalStateException {
        if (!this.initialised) {
            throw new IllegalStateException("KMAC not initialized");
        }
        this.cshake.update(byArray, n, n2);
    }

    public int doFinal(byte[] byArray, int n) throws DataLengthException, IllegalStateException {
        if (this.firstOutput) {
            if (!this.initialised) {
                throw new IllegalStateException("KMAC not initialized");
            }
            byte[] byArray2 = XofUtils.rightEncode(this.getMacSize() * 8);
            this.cshake.update(byArray2, 0, byArray2.length);
        }
        int n2 = this.cshake.doFinal(byArray, n, this.getMacSize());
        this.reset();
        return n2;
    }

    public int doFinal(byte[] byArray, int n, int n2) {
        if (this.firstOutput) {
            if (!this.initialised) {
                throw new IllegalStateException("KMAC not initialized");
            }
            byte[] byArray2 = XofUtils.rightEncode(n2 * 8);
            this.cshake.update(byArray2, 0, byArray2.length);
        }
        int n3 = this.cshake.doFinal(byArray, n, n2);
        this.reset();
        return n3;
    }

    public int doOutput(byte[] byArray, int n, int n2) {
        if (this.firstOutput) {
            if (!this.initialised) {
                throw new IllegalStateException("KMAC not initialized");
            }
            byte[] byArray2 = XofUtils.rightEncode(0L);
            this.cshake.update(byArray2, 0, byArray2.length);
            this.firstOutput = false;
        }
        return this.cshake.doOutput(byArray, n, n2);
    }

    public void reset() {
        this.cshake.reset();
        if (this.key != null) {
            if (this.bitLength == 128) {
                this.bytePad(this.key, 168);
            } else {
                this.bytePad(this.key, 136);
            }
        }
        this.firstOutput = true;
    }

    private void bytePad(byte[] byArray, int n) {
        int n2;
        byte[] byArray2 = XofUtils.leftEncode(n);
        this.update(byArray2, 0, byArray2.length);
        byte[] byArray3 = KMAC.encode(byArray);
        this.update(byArray3, 0, byArray3.length);
        if (n2 > 0 && n2 != n) {
            for (n2 = n - (byArray2.length + byArray3.length) % n; n2 > padding.length; n2 -= padding.length) {
                this.update(padding, 0, padding.length);
            }
            this.update(padding, 0, n2);
        }
    }

    private static byte[] encode(byte[] byArray) {
        return Arrays.concatenate(XofUtils.leftEncode(byArray.length * 8), byArray);
    }
}


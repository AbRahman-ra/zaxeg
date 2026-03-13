/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Xof;
import org.bouncycastle.crypto.digests.CSHAKEDigest;
import org.bouncycastle.crypto.digests.XofUtils;
import org.bouncycastle.util.Strings;

public class TupleHash
implements Xof,
Digest {
    private static final byte[] N_TUPLE_HASH = Strings.toByteArray("TupleHash");
    private final CSHAKEDigest cshake;
    private final int bitLength;
    private final int outputLength;
    private boolean firstOutput;

    public TupleHash(int n, byte[] byArray) {
        this(n, byArray, n * 2);
    }

    public TupleHash(int n, byte[] byArray, int n2) {
        this.cshake = new CSHAKEDigest(n, N_TUPLE_HASH, byArray);
        this.bitLength = n;
        this.outputLength = (n2 + 7) / 8;
        this.reset();
    }

    public TupleHash(TupleHash tupleHash) {
        this.cshake = new CSHAKEDigest(tupleHash.cshake);
        this.bitLength = this.cshake.fixedOutputLength;
        this.outputLength = this.bitLength * 2 / 8;
        this.firstOutput = tupleHash.firstOutput;
    }

    public String getAlgorithmName() {
        return "TupleHash" + this.cshake.getAlgorithmName().substring(6);
    }

    public int getByteLength() {
        return this.cshake.getByteLength();
    }

    public int getDigestSize() {
        return this.outputLength;
    }

    public void update(byte by) throws IllegalStateException {
        byte[] byArray = XofUtils.encode(by);
        this.cshake.update(byArray, 0, byArray.length);
    }

    public void update(byte[] byArray, int n, int n2) throws DataLengthException, IllegalStateException {
        byte[] byArray2 = XofUtils.encode(byArray, n, n2);
        this.cshake.update(byArray2, 0, byArray2.length);
    }

    private void wrapUp(int n) {
        byte[] byArray = XofUtils.rightEncode((long)n * 8L);
        this.cshake.update(byArray, 0, byArray.length);
        this.firstOutput = false;
    }

    public int doFinal(byte[] byArray, int n) throws DataLengthException, IllegalStateException {
        if (this.firstOutput) {
            this.wrapUp(this.getDigestSize());
        }
        int n2 = this.cshake.doFinal(byArray, n, this.getDigestSize());
        this.reset();
        return n2;
    }

    public int doFinal(byte[] byArray, int n, int n2) {
        if (this.firstOutput) {
            this.wrapUp(this.getDigestSize());
        }
        int n3 = this.cshake.doFinal(byArray, n, n2);
        this.reset();
        return n3;
    }

    public int doOutput(byte[] byArray, int n, int n2) {
        if (this.firstOutput) {
            this.wrapUp(0);
        }
        return this.cshake.doOutput(byArray, n, n2);
    }

    public void reset() {
        this.cshake.reset();
        this.firstOutput = true;
    }
}


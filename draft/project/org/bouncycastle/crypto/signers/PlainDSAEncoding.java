/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import org.bouncycastle.crypto.signers.DSAEncoding;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;

public class PlainDSAEncoding
implements DSAEncoding {
    public static final PlainDSAEncoding INSTANCE = new PlainDSAEncoding();

    public byte[] encode(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) {
        int n = BigIntegers.getUnsignedByteLength(bigInteger);
        byte[] byArray = new byte[n * 2];
        this.encodeValue(bigInteger, bigInteger2, byArray, 0, n);
        this.encodeValue(bigInteger, bigInteger3, byArray, n, n);
        return byArray;
    }

    public BigInteger[] decode(BigInteger bigInteger, byte[] byArray) {
        int n = BigIntegers.getUnsignedByteLength(bigInteger);
        if (byArray.length != n * 2) {
            throw new IllegalArgumentException("Encoding has incorrect length");
        }
        return new BigInteger[]{this.decodeValue(bigInteger, byArray, 0, n), this.decodeValue(bigInteger, byArray, n, n)};
    }

    protected BigInteger checkValue(BigInteger bigInteger, BigInteger bigInteger2) {
        if (bigInteger2.signum() < 0 || bigInteger2.compareTo(bigInteger) >= 0) {
            throw new IllegalArgumentException("Value out of range");
        }
        return bigInteger2;
    }

    protected BigInteger decodeValue(BigInteger bigInteger, byte[] byArray, int n, int n2) {
        byte[] byArray2 = Arrays.copyOfRange(byArray, n, n + n2);
        return this.checkValue(bigInteger, new BigInteger(1, byArray2));
    }

    private void encodeValue(BigInteger bigInteger, BigInteger bigInteger2, byte[] byArray, int n, int n2) {
        byte[] byArray2 = this.checkValue(bigInteger, bigInteger2).toByteArray();
        int n3 = Math.max(0, byArray2.length - n2);
        int n4 = byArray2.length - n3;
        int n5 = n2 - n4;
        Arrays.fill(byArray, n, n + n5, (byte)0);
        System.arraycopy(byArray2, n3, byArray, n + n5, n4);
    }
}


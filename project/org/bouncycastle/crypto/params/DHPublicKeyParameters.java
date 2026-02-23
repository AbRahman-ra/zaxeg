/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import java.math.BigInteger;
import org.bouncycastle.crypto.params.DHKeyParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.util.Integers;

public class DHPublicKeyParameters
extends DHKeyParameters {
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private static final BigInteger TWO = BigInteger.valueOf(2L);
    private BigInteger y;

    public DHPublicKeyParameters(BigInteger bigInteger, DHParameters dHParameters) {
        super(false, dHParameters);
        this.y = this.validate(bigInteger, dHParameters);
    }

    private BigInteger validate(BigInteger bigInteger, DHParameters dHParameters) {
        if (bigInteger == null) {
            throw new NullPointerException("y value cannot be null");
        }
        BigInteger bigInteger2 = dHParameters.getP();
        if (bigInteger.compareTo(TWO) < 0 || bigInteger.compareTo(bigInteger2.subtract(TWO)) > 0) {
            throw new IllegalArgumentException("invalid DH public key");
        }
        BigInteger bigInteger3 = dHParameters.getQ();
        if (bigInteger3 == null) {
            return bigInteger;
        }
        if (bigInteger2.testBit(0) && bigInteger2.bitLength() - 1 == bigInteger3.bitLength() && bigInteger2.shiftRight(1).equals(bigInteger3) ? 1 == DHPublicKeyParameters.legendre(bigInteger, bigInteger2) : ONE.equals(bigInteger.modPow(bigInteger3, bigInteger2))) {
            return bigInteger;
        }
        throw new IllegalArgumentException("Y value does not appear to be in correct group");
    }

    public BigInteger getY() {
        return this.y;
    }

    public int hashCode() {
        return this.y.hashCode() ^ super.hashCode();
    }

    public boolean equals(Object object) {
        if (!(object instanceof DHPublicKeyParameters)) {
            return false;
        }
        DHPublicKeyParameters dHPublicKeyParameters = (DHPublicKeyParameters)object;
        return dHPublicKeyParameters.getY().equals(this.y) && super.equals(object);
    }

    private static int legendre(BigInteger bigInteger, BigInteger bigInteger2) {
        int n = bigInteger2.bitLength();
        int[] nArray = Nat.fromBigInteger(n, bigInteger);
        int[] nArray2 = Nat.fromBigInteger(n, bigInteger2);
        int n2 = 0;
        int n3 = nArray2.length;
        while (true) {
            int n4;
            if (nArray[0] == 0) {
                Nat.shiftDownWord(n3, nArray, 0);
                continue;
            }
            int n5 = Integers.numberOfTrailingZeros(nArray[0]);
            if (n5 > 0) {
                Nat.shiftDownBits(n3, nArray, n5, 0);
                n4 = nArray2[0];
                n2 ^= (n4 ^ n4 >>> 1) & n5 << 1;
            }
            if ((n4 = Nat.compare(n3, nArray, nArray2)) == 0) break;
            if (n4 < 0) {
                n2 ^= nArray[0] & nArray2[0];
                int[] nArray3 = nArray;
                nArray = nArray2;
                nArray2 = nArray3;
            }
            while (nArray[n3 - 1] == 0) {
                --n3;
            }
            Nat.sub(n3, nArray, nArray2, nArray);
        }
        return Nat.isOne(n3, nArray2) ? 1 - (n2 & 2) : 0;
    }
}


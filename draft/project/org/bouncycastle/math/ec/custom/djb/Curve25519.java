/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec.custom.djb;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.math.ec.AbstractECLookupTable;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.djb.Curve25519Field;
import org.bouncycastle.math.ec.custom.djb.Curve25519FieldElement;
import org.bouncycastle.math.ec.custom.djb.Curve25519Point;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.encoders.Hex;

public class Curve25519
extends ECCurve.AbstractFp {
    public static final BigInteger q = Curve25519FieldElement.Q;
    private static final BigInteger C_a = new BigInteger(1, Hex.decodeStrict("2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA984914A144"));
    private static final BigInteger C_b = new BigInteger(1, Hex.decodeStrict("7B425ED097B425ED097B425ED097B425ED097B425ED097B4260B5E9C7710C864"));
    private static final int CURVE25519_DEFAULT_COORDS = 4;
    private static final ECFieldElement[] CURVE25519_AFFINE_ZS = new ECFieldElement[]{new Curve25519FieldElement(ECConstants.ONE), new Curve25519FieldElement(C_a)};
    protected Curve25519Point infinity = new Curve25519Point(this, null, null);

    public Curve25519() {
        super(q);
        this.a = this.fromBigInteger(C_a);
        this.b = this.fromBigInteger(C_b);
        this.order = new BigInteger(1, Hex.decodeStrict("1000000000000000000000000000000014DEF9DEA2F79CD65812631A5CF5D3ED"));
        this.cofactor = BigInteger.valueOf(8L);
        this.coord = 4;
    }

    protected ECCurve cloneCurve() {
        return new Curve25519();
    }

    public boolean supportsCoordinateSystem(int n) {
        switch (n) {
            case 4: {
                return true;
            }
        }
        return false;
    }

    public BigInteger getQ() {
        return q;
    }

    public int getFieldSize() {
        return q.bitLength();
    }

    public ECFieldElement fromBigInteger(BigInteger bigInteger) {
        return new Curve25519FieldElement(bigInteger);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return new Curve25519Point(this, eCFieldElement, eCFieldElement2);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArray) {
        return new Curve25519Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArray);
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArray, int n, int n2) {
        int[] nArray = new int[n2 * 8 * 2];
        int n3 = 0;
        for (int i = 0; i < n2; ++i) {
            ECPoint eCPoint = eCPointArray[n + i];
            Nat256.copy(((Curve25519FieldElement)eCPoint.getRawXCoord()).x, 0, nArray, n3);
            Nat256.copy(((Curve25519FieldElement)eCPoint.getRawYCoord()).x, 0, nArray, n3 += 8);
            n3 += 8;
        }
        return new AbstractECLookupTable(){

            public int getSize() {
                return n2;
            }

            public ECPoint lookup(int n) {
                int[] nArray3 = Nat256.create();
                int[] nArray2 = Nat256.create();
                int n22 = 0;
                for (int i = 0; i < n2; ++i) {
                    int n3 = (i ^ n) - 1 >> 31;
                    for (int j = 0; j < 8; ++j) {
                        int n4 = j;
                        nArray3[n4] = nArray3[n4] ^ nArray[n22 + j] & n3;
                        int n5 = j;
                        nArray2[n5] = nArray2[n5] ^ nArray[n22 + 8 + j] & n3;
                    }
                    n22 += 16;
                }
                return this.createPoint(nArray3, nArray2);
            }

            public ECPoint lookupVar(int n) {
                int[] nArray3 = Nat256.create();
                int[] nArray2 = Nat256.create();
                int n22 = n * 8 * 2;
                for (int i = 0; i < 8; ++i) {
                    nArray3[i] = nArray[n22 + i];
                    nArray2[i] = nArray[n22 + 8 + i];
                }
                return this.createPoint(nArray3, nArray2);
            }

            private ECPoint createPoint(int[] nArray3, int[] nArray2) {
                return Curve25519.this.createRawPoint(new Curve25519FieldElement(nArray3), new Curve25519FieldElement(nArray2), CURVE25519_AFFINE_ZS);
            }
        };
    }

    public ECFieldElement randomFieldElement(SecureRandom secureRandom) {
        int[] nArray = Nat256.create();
        Curve25519Field.random(secureRandom, nArray);
        return new Curve25519FieldElement(nArray);
    }

    public ECFieldElement randomFieldElementMult(SecureRandom secureRandom) {
        int[] nArray = Nat256.create();
        Curve25519Field.randomMult(secureRandom, nArray);
        return new Curve25519FieldElement(nArray);
    }
}


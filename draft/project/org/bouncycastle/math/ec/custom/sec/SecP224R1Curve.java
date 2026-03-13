/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.math.ec.AbstractECLookupTable;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP224R1Field;
import org.bouncycastle.math.ec.custom.sec.SecP224R1FieldElement;
import org.bouncycastle.math.ec.custom.sec.SecP224R1Point;
import org.bouncycastle.math.raw.Nat224;
import org.bouncycastle.util.encoders.Hex;

public class SecP224R1Curve
extends ECCurve.AbstractFp {
    public static final BigInteger q = SecP224R1FieldElement.Q;
    private static final int SECP224R1_DEFAULT_COORDS = 2;
    private static final ECFieldElement[] SECP224R1_AFFINE_ZS = new ECFieldElement[]{new SecP224R1FieldElement(ECConstants.ONE)};
    protected SecP224R1Point infinity = new SecP224R1Point(this, null, null);

    public SecP224R1Curve() {
        super(q);
        this.a = this.fromBigInteger(new BigInteger(1, Hex.decodeStrict("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFE")));
        this.b = this.fromBigInteger(new BigInteger(1, Hex.decodeStrict("B4050A850C04B3ABF54132565044B0B7D7BFD8BA270B39432355FFB4")));
        this.order = new BigInteger(1, Hex.decodeStrict("FFFFFFFFFFFFFFFFFFFFFFFFFFFF16A2E0B8F03E13DD29455C5C2A3D"));
        this.cofactor = BigInteger.valueOf(1L);
        this.coord = 2;
    }

    protected ECCurve cloneCurve() {
        return new SecP224R1Curve();
    }

    public boolean supportsCoordinateSystem(int n) {
        switch (n) {
            case 2: {
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
        return new SecP224R1FieldElement(bigInteger);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return new SecP224R1Point(this, eCFieldElement, eCFieldElement2);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArray) {
        return new SecP224R1Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArray);
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArray, int n, int n2) {
        int[] nArray = new int[n2 * 7 * 2];
        int n3 = 0;
        for (int i = 0; i < n2; ++i) {
            ECPoint eCPoint = eCPointArray[n + i];
            Nat224.copy(((SecP224R1FieldElement)eCPoint.getRawXCoord()).x, 0, nArray, n3);
            Nat224.copy(((SecP224R1FieldElement)eCPoint.getRawYCoord()).x, 0, nArray, n3 += 7);
            n3 += 7;
        }
        return new AbstractECLookupTable(){

            public int getSize() {
                return n2;
            }

            public ECPoint lookup(int n) {
                int[] nArray3 = Nat224.create();
                int[] nArray2 = Nat224.create();
                int n22 = 0;
                for (int i = 0; i < n2; ++i) {
                    int n3 = (i ^ n) - 1 >> 31;
                    for (int j = 0; j < 7; ++j) {
                        int n4 = j;
                        nArray3[n4] = nArray3[n4] ^ nArray[n22 + j] & n3;
                        int n5 = j;
                        nArray2[n5] = nArray2[n5] ^ nArray[n22 + 7 + j] & n3;
                    }
                    n22 += 14;
                }
                return this.createPoint(nArray3, nArray2);
            }

            public ECPoint lookupVar(int n) {
                int[] nArray3 = Nat224.create();
                int[] nArray2 = Nat224.create();
                int n22 = n * 7 * 2;
                for (int i = 0; i < 7; ++i) {
                    nArray3[i] = nArray[n22 + i];
                    nArray2[i] = nArray[n22 + 7 + i];
                }
                return this.createPoint(nArray3, nArray2);
            }

            private ECPoint createPoint(int[] nArray3, int[] nArray2) {
                return SecP224R1Curve.this.createRawPoint(new SecP224R1FieldElement(nArray3), new SecP224R1FieldElement(nArray2), SECP224R1_AFFINE_ZS);
            }
        };
    }

    public ECFieldElement randomFieldElement(SecureRandom secureRandom) {
        int[] nArray = Nat224.create();
        SecP224R1Field.random(secureRandom, nArray);
        return new SecP224R1FieldElement(nArray);
    }

    public ECFieldElement randomFieldElementMult(SecureRandom secureRandom) {
        int[] nArray = Nat224.create();
        SecP224R1Field.randomMult(secureRandom, nArray);
        return new SecP224R1FieldElement(nArray);
    }
}


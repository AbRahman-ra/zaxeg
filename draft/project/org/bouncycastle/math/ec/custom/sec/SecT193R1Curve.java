/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.AbstractECLookupTable;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecT193FieldElement;
import org.bouncycastle.math.ec.custom.sec.SecT193R1Point;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.encoders.Hex;

public class SecT193R1Curve
extends ECCurve.AbstractF2m {
    private static final int SECT193R1_DEFAULT_COORDS = 6;
    private static final ECFieldElement[] SECT193R1_AFFINE_ZS = new ECFieldElement[]{new SecT193FieldElement(ECConstants.ONE)};
    protected SecT193R1Point infinity = new SecT193R1Point(this, null, null);

    public SecT193R1Curve() {
        super(193, 15, 0, 0);
        this.a = this.fromBigInteger(new BigInteger(1, Hex.decodeStrict("0017858FEB7A98975169E171F77B4087DE098AC8A911DF7B01")));
        this.b = this.fromBigInteger(new BigInteger(1, Hex.decodeStrict("00FDFB49BFE6C3A89FACADAA7A1E5BBC7CC1C2E5D831478814")));
        this.order = new BigInteger(1, Hex.decodeStrict("01000000000000000000000000C7F34A778F443ACC920EBA49"));
        this.cofactor = BigInteger.valueOf(2L);
        this.coord = 6;
    }

    protected ECCurve cloneCurve() {
        return new SecT193R1Curve();
    }

    public boolean supportsCoordinateSystem(int n) {
        switch (n) {
            case 6: {
                return true;
            }
        }
        return false;
    }

    public int getFieldSize() {
        return 193;
    }

    public ECFieldElement fromBigInteger(BigInteger bigInteger) {
        return new SecT193FieldElement(bigInteger);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return new SecT193R1Point(this, eCFieldElement, eCFieldElement2);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArray) {
        return new SecT193R1Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArray);
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }

    public boolean isKoblitz() {
        return false;
    }

    public int getM() {
        return 193;
    }

    public boolean isTrinomial() {
        return true;
    }

    public int getK1() {
        return 15;
    }

    public int getK2() {
        return 0;
    }

    public int getK3() {
        return 0;
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArray, int n, int n2) {
        long[] lArray = new long[n2 * 4 * 2];
        int n3 = 0;
        for (int i = 0; i < n2; ++i) {
            ECPoint eCPoint = eCPointArray[n + i];
            Nat256.copy64(((SecT193FieldElement)eCPoint.getRawXCoord()).x, 0, lArray, n3);
            Nat256.copy64(((SecT193FieldElement)eCPoint.getRawYCoord()).x, 0, lArray, n3 += 4);
            n3 += 4;
        }
        return new AbstractECLookupTable(){

            public int getSize() {
                return n2;
            }

            public ECPoint lookup(int n) {
                long[] lArray3 = Nat256.create64();
                long[] lArray2 = Nat256.create64();
                int n22 = 0;
                for (int i = 0; i < n2; ++i) {
                    long l = (i ^ n) - 1 >> 31;
                    for (int j = 0; j < 4; ++j) {
                        int n3 = j;
                        lArray3[n3] = lArray3[n3] ^ lArray[n22 + j] & l;
                        int n4 = j;
                        lArray2[n4] = lArray2[n4] ^ lArray[n22 + 4 + j] & l;
                    }
                    n22 += 8;
                }
                return this.createPoint(lArray3, lArray2);
            }

            public ECPoint lookupVar(int n) {
                long[] lArray3 = Nat256.create64();
                long[] lArray2 = Nat256.create64();
                int n22 = n * 4 * 2;
                for (int i = 0; i < 4; ++i) {
                    lArray3[i] = lArray[n22 + i];
                    lArray2[i] = lArray[n22 + 4 + i];
                }
                return this.createPoint(lArray3, lArray2);
            }

            private ECPoint createPoint(long[] lArray3, long[] lArray2) {
                return SecT193R1Curve.this.createRawPoint(new SecT193FieldElement(lArray3), new SecT193FieldElement(lArray2), SECT193R1_AFFINE_ZS);
            }
        };
    }
}


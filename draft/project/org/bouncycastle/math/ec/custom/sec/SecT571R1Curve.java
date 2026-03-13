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
import org.bouncycastle.math.ec.custom.sec.SecT571FieldElement;
import org.bouncycastle.math.ec.custom.sec.SecT571R1Point;
import org.bouncycastle.math.raw.Nat576;
import org.bouncycastle.util.encoders.Hex;

public class SecT571R1Curve
extends ECCurve.AbstractF2m {
    private static final int SECT571R1_DEFAULT_COORDS = 6;
    private static final ECFieldElement[] SECT571R1_AFFINE_ZS = new ECFieldElement[]{new SecT571FieldElement(ECConstants.ONE)};
    protected SecT571R1Point infinity = new SecT571R1Point(this, null, null);
    static final SecT571FieldElement SecT571R1_B = new SecT571FieldElement(new BigInteger(1, Hex.decodeStrict("02F40E7E2221F295DE297117B7F3D62F5C6A97FFCB8CEFF1CD6BA8CE4A9A18AD84FFABBD8EFA59332BE7AD6756A66E294AFD185A78FF12AA520E4DE739BACA0C7FFEFF7F2955727A")));
    static final SecT571FieldElement SecT571R1_B_SQRT = (SecT571FieldElement)SecT571R1_B.sqrt();

    public SecT571R1Curve() {
        super(571, 2, 5, 10);
        this.a = this.fromBigInteger(BigInteger.valueOf(1L));
        this.b = SecT571R1_B;
        this.order = new BigInteger(1, Hex.decodeStrict("03FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE661CE18FF55987308059B186823851EC7DD9CA1161DE93D5174D66E8382E9BB2FE84E47"));
        this.cofactor = BigInteger.valueOf(2L);
        this.coord = 6;
    }

    protected ECCurve cloneCurve() {
        return new SecT571R1Curve();
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
        return 571;
    }

    public ECFieldElement fromBigInteger(BigInteger bigInteger) {
        return new SecT571FieldElement(bigInteger);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return new SecT571R1Point(this, eCFieldElement, eCFieldElement2);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArray) {
        return new SecT571R1Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArray);
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }

    public boolean isKoblitz() {
        return false;
    }

    public int getM() {
        return 571;
    }

    public boolean isTrinomial() {
        return false;
    }

    public int getK1() {
        return 2;
    }

    public int getK2() {
        return 5;
    }

    public int getK3() {
        return 10;
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArray, int n, int n2) {
        long[] lArray = new long[n2 * 9 * 2];
        int n3 = 0;
        for (int i = 0; i < n2; ++i) {
            ECPoint eCPoint = eCPointArray[n + i];
            Nat576.copy64(((SecT571FieldElement)eCPoint.getRawXCoord()).x, 0, lArray, n3);
            Nat576.copy64(((SecT571FieldElement)eCPoint.getRawYCoord()).x, 0, lArray, n3 += 9);
            n3 += 9;
        }
        return new AbstractECLookupTable(){

            public int getSize() {
                return n2;
            }

            public ECPoint lookup(int n) {
                long[] lArray3 = Nat576.create64();
                long[] lArray2 = Nat576.create64();
                int n22 = 0;
                for (int i = 0; i < n2; ++i) {
                    long l = (i ^ n) - 1 >> 31;
                    for (int j = 0; j < 9; ++j) {
                        int n3 = j;
                        lArray3[n3] = lArray3[n3] ^ lArray[n22 + j] & l;
                        int n4 = j;
                        lArray2[n4] = lArray2[n4] ^ lArray[n22 + 9 + j] & l;
                    }
                    n22 += 18;
                }
                return this.createPoint(lArray3, lArray2);
            }

            public ECPoint lookupVar(int n) {
                long[] lArray3 = Nat576.create64();
                long[] lArray2 = Nat576.create64();
                int n22 = n * 9 * 2;
                for (int i = 0; i < 9; ++i) {
                    lArray3[i] = lArray[n22 + i];
                    lArray2[i] = lArray[n22 + 9 + i];
                }
                return this.createPoint(lArray3, lArray2);
            }

            private ECPoint createPoint(long[] lArray3, long[] lArray2) {
                return SecT571R1Curve.this.createRawPoint(new SecT571FieldElement(lArray3), new SecT571FieldElement(lArray2), SECT571R1_AFFINE_ZS);
            }
        };
    }
}


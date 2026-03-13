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
import org.bouncycastle.math.ec.custom.sec.SecT131FieldElement;
import org.bouncycastle.math.ec.custom.sec.SecT131R1Point;
import org.bouncycastle.math.raw.Nat192;
import org.bouncycastle.util.encoders.Hex;

public class SecT131R1Curve
extends ECCurve.AbstractF2m {
    private static final int SECT131R1_DEFAULT_COORDS = 6;
    private static final ECFieldElement[] SECT131R1_AFFINE_ZS = new ECFieldElement[]{new SecT131FieldElement(ECConstants.ONE)};
    protected SecT131R1Point infinity = new SecT131R1Point(this, null, null);

    public SecT131R1Curve() {
        super(131, 2, 3, 8);
        this.a = this.fromBigInteger(new BigInteger(1, Hex.decodeStrict("07A11B09A76B562144418FF3FF8C2570B8")));
        this.b = this.fromBigInteger(new BigInteger(1, Hex.decodeStrict("0217C05610884B63B9C6C7291678F9D341")));
        this.order = new BigInteger(1, Hex.decodeStrict("0400000000000000023123953A9464B54D"));
        this.cofactor = BigInteger.valueOf(2L);
        this.coord = 6;
    }

    protected ECCurve cloneCurve() {
        return new SecT131R1Curve();
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
        return 131;
    }

    public ECFieldElement fromBigInteger(BigInteger bigInteger) {
        return new SecT131FieldElement(bigInteger);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return new SecT131R1Point(this, eCFieldElement, eCFieldElement2);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArray) {
        return new SecT131R1Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArray);
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }

    public boolean isKoblitz() {
        return false;
    }

    public int getM() {
        return 131;
    }

    public boolean isTrinomial() {
        return false;
    }

    public int getK1() {
        return 2;
    }

    public int getK2() {
        return 3;
    }

    public int getK3() {
        return 8;
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArray, int n, int n2) {
        long[] lArray = new long[n2 * 3 * 2];
        int n3 = 0;
        for (int i = 0; i < n2; ++i) {
            ECPoint eCPoint = eCPointArray[n + i];
            Nat192.copy64(((SecT131FieldElement)eCPoint.getRawXCoord()).x, 0, lArray, n3);
            Nat192.copy64(((SecT131FieldElement)eCPoint.getRawYCoord()).x, 0, lArray, n3 += 3);
            n3 += 3;
        }
        return new AbstractECLookupTable(){

            public int getSize() {
                return n2;
            }

            public ECPoint lookup(int n) {
                long[] lArray3 = Nat192.create64();
                long[] lArray2 = Nat192.create64();
                int n22 = 0;
                for (int i = 0; i < n2; ++i) {
                    long l = (i ^ n) - 1 >> 31;
                    for (int j = 0; j < 3; ++j) {
                        int n3 = j;
                        lArray3[n3] = lArray3[n3] ^ lArray[n22 + j] & l;
                        int n4 = j;
                        lArray2[n4] = lArray2[n4] ^ lArray[n22 + 3 + j] & l;
                    }
                    n22 += 6;
                }
                return this.createPoint(lArray3, lArray2);
            }

            public ECPoint lookupVar(int n) {
                long[] lArray3 = Nat192.create64();
                long[] lArray2 = Nat192.create64();
                int n22 = n * 3 * 2;
                for (int i = 0; i < 3; ++i) {
                    lArray3[i] = lArray[n22 + i];
                    lArray2[i] = lArray[n22 + 3 + i];
                }
                return this.createPoint(lArray3, lArray2);
            }

            private ECPoint createPoint(long[] lArray3, long[] lArray2) {
                return SecT131R1Curve.this.createRawPoint(new SecT131FieldElement(lArray3), new SecT131FieldElement(lArray2), SECT131R1_AFFINE_ZS);
            }
        };
    }
}


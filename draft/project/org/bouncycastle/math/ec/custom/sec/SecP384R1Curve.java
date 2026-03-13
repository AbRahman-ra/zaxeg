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
import org.bouncycastle.math.ec.custom.sec.SecP384R1Field;
import org.bouncycastle.math.ec.custom.sec.SecP384R1FieldElement;
import org.bouncycastle.math.ec.custom.sec.SecP384R1Point;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.util.encoders.Hex;

public class SecP384R1Curve
extends ECCurve.AbstractFp {
    public static final BigInteger q = SecP384R1FieldElement.Q;
    private static final int SECP384R1_DEFAULT_COORDS = 2;
    private static final ECFieldElement[] SECP384R1_AFFINE_ZS = new ECFieldElement[]{new SecP384R1FieldElement(ECConstants.ONE)};
    protected SecP384R1Point infinity = new SecP384R1Point(this, null, null);

    public SecP384R1Curve() {
        super(q);
        this.a = this.fromBigInteger(new BigInteger(1, Hex.decodeStrict("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFC")));
        this.b = this.fromBigInteger(new BigInteger(1, Hex.decodeStrict("B3312FA7E23EE7E4988E056BE3F82D19181D9C6EFE8141120314088F5013875AC656398D8A2ED19D2A85C8EDD3EC2AEF")));
        this.order = new BigInteger(1, Hex.decodeStrict("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC7634D81F4372DDF581A0DB248B0A77AECEC196ACCC52973"));
        this.cofactor = BigInteger.valueOf(1L);
        this.coord = 2;
    }

    protected ECCurve cloneCurve() {
        return new SecP384R1Curve();
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
        return new SecP384R1FieldElement(bigInteger);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return new SecP384R1Point(this, eCFieldElement, eCFieldElement2);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArray) {
        return new SecP384R1Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArray);
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArray, int n, int n2) {
        int[] nArray = new int[n2 * 12 * 2];
        int n3 = 0;
        for (int i = 0; i < n2; ++i) {
            ECPoint eCPoint = eCPointArray[n + i];
            Nat.copy(12, ((SecP384R1FieldElement)eCPoint.getRawXCoord()).x, 0, nArray, n3);
            Nat.copy(12, ((SecP384R1FieldElement)eCPoint.getRawYCoord()).x, 0, nArray, n3 += 12);
            n3 += 12;
        }
        return new AbstractECLookupTable(){

            public int getSize() {
                return n2;
            }

            public ECPoint lookup(int n) {
                int[] nArray3 = Nat.create(12);
                int[] nArray2 = Nat.create(12);
                int n22 = 0;
                for (int i = 0; i < n2; ++i) {
                    int n3 = (i ^ n) - 1 >> 31;
                    for (int j = 0; j < 12; ++j) {
                        int n4 = j;
                        nArray3[n4] = nArray3[n4] ^ nArray[n22 + j] & n3;
                        int n5 = j;
                        nArray2[n5] = nArray2[n5] ^ nArray[n22 + 12 + j] & n3;
                    }
                    n22 += 24;
                }
                return this.createPoint(nArray3, nArray2);
            }

            public ECPoint lookupVar(int n) {
                int[] nArray3 = Nat.create(12);
                int[] nArray2 = Nat.create(12);
                int n22 = n * 12 * 2;
                for (int i = 0; i < 12; ++i) {
                    nArray3[i] = nArray[n22 + i];
                    nArray2[i] = nArray[n22 + 12 + i];
                }
                return this.createPoint(nArray3, nArray2);
            }

            private ECPoint createPoint(int[] nArray3, int[] nArray2) {
                return SecP384R1Curve.this.createRawPoint(new SecP384R1FieldElement(nArray3), new SecP384R1FieldElement(nArray2), SECP384R1_AFFINE_ZS);
            }
        };
    }

    public ECFieldElement randomFieldElement(SecureRandom secureRandom) {
        int[] nArray = Nat.create(12);
        SecP384R1Field.random(secureRandom, nArray);
        return new SecP384R1FieldElement(nArray);
    }

    public ECFieldElement randomFieldElementMult(SecureRandom secureRandom) {
        int[] nArray = Nat.create(12);
        SecP384R1Field.randomMult(secureRandom, nArray);
        return new SecP384R1FieldElement(nArray);
    }
}


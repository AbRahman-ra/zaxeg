/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Random;
import org.bouncycastle.math.ec.AbstractECLookupTable;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECMultiplier;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.GLVMultiplier;
import org.bouncycastle.math.ec.LongArray;
import org.bouncycastle.math.ec.PreCompCallback;
import org.bouncycastle.math.ec.PreCompInfo;
import org.bouncycastle.math.ec.Tnaf;
import org.bouncycastle.math.ec.WNafL2RMultiplier;
import org.bouncycastle.math.ec.WTauNafMultiplier;
import org.bouncycastle.math.ec.endo.ECEndomorphism;
import org.bouncycastle.math.ec.endo.GLVEndomorphism;
import org.bouncycastle.math.field.FiniteField;
import org.bouncycastle.math.field.FiniteFields;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Integers;

public abstract class ECCurve {
    public static final int COORD_AFFINE = 0;
    public static final int COORD_HOMOGENEOUS = 1;
    public static final int COORD_JACOBIAN = 2;
    public static final int COORD_JACOBIAN_CHUDNOVSKY = 3;
    public static final int COORD_JACOBIAN_MODIFIED = 4;
    public static final int COORD_LAMBDA_AFFINE = 5;
    public static final int COORD_LAMBDA_PROJECTIVE = 6;
    public static final int COORD_SKEWED = 7;
    protected FiniteField field;
    protected ECFieldElement a;
    protected ECFieldElement b;
    protected BigInteger order;
    protected BigInteger cofactor;
    protected int coord = 0;
    protected ECEndomorphism endomorphism = null;
    protected ECMultiplier multiplier = null;

    public static int[] getAllCoordinateSystems() {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    }

    protected ECCurve(FiniteField finiteField) {
        this.field = finiteField;
    }

    public abstract int getFieldSize();

    public abstract ECFieldElement fromBigInteger(BigInteger var1);

    public abstract boolean isValidFieldElement(BigInteger var1);

    public abstract ECFieldElement randomFieldElement(SecureRandom var1);

    public abstract ECFieldElement randomFieldElementMult(SecureRandom var1);

    public synchronized Config configure() {
        return new Config(this.coord, this.endomorphism, this.multiplier);
    }

    public ECPoint validatePoint(BigInteger bigInteger, BigInteger bigInteger2) {
        ECPoint eCPoint = this.createPoint(bigInteger, bigInteger2);
        if (!eCPoint.isValid()) {
            throw new IllegalArgumentException("Invalid point coordinates");
        }
        return eCPoint;
    }

    public ECPoint createPoint(BigInteger bigInteger, BigInteger bigInteger2) {
        return this.createRawPoint(this.fromBigInteger(bigInteger), this.fromBigInteger(bigInteger2));
    }

    protected abstract ECCurve cloneCurve();

    protected abstract ECPoint createRawPoint(ECFieldElement var1, ECFieldElement var2);

    protected abstract ECPoint createRawPoint(ECFieldElement var1, ECFieldElement var2, ECFieldElement[] var3);

    protected ECMultiplier createDefaultMultiplier() {
        if (this.endomorphism instanceof GLVEndomorphism) {
            return new GLVMultiplier(this, (GLVEndomorphism)this.endomorphism);
        }
        return new WNafL2RMultiplier();
    }

    public boolean supportsCoordinateSystem(int n) {
        return n == 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PreCompInfo getPreCompInfo(ECPoint eCPoint, String string) {
        Hashtable hashtable;
        this.checkPoint(eCPoint);
        Object object = eCPoint;
        synchronized (object) {
            hashtable = eCPoint.preCompTable;
        }
        if (null == hashtable) {
            return null;
        }
        object = hashtable;
        synchronized (object) {
            return (PreCompInfo)hashtable.get(string);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PreCompInfo precompute(ECPoint eCPoint, String string, PreCompCallback preCompCallback) {
        Hashtable<String, PreCompInfo> hashtable;
        this.checkPoint(eCPoint);
        Object object = eCPoint;
        synchronized (object) {
            hashtable = eCPoint.preCompTable;
            if (null == hashtable) {
                eCPoint.preCompTable = hashtable = new Hashtable<String, PreCompInfo>(4);
            }
        }
        object = hashtable;
        synchronized (object) {
            PreCompInfo preCompInfo = (PreCompInfo)hashtable.get(string);
            PreCompInfo preCompInfo2 = preCompCallback.precompute(preCompInfo);
            if (preCompInfo2 != preCompInfo) {
                hashtable.put(string, preCompInfo2);
            }
            return preCompInfo2;
        }
    }

    public ECPoint importPoint(ECPoint eCPoint) {
        if (this == eCPoint.getCurve()) {
            return eCPoint;
        }
        if (eCPoint.isInfinity()) {
            return this.getInfinity();
        }
        eCPoint = eCPoint.normalize();
        return this.createPoint(eCPoint.getXCoord().toBigInteger(), eCPoint.getYCoord().toBigInteger());
    }

    public void normalizeAll(ECPoint[] eCPointArray) {
        this.normalizeAll(eCPointArray, 0, eCPointArray.length, null);
    }

    public void normalizeAll(ECPoint[] eCPointArray, int n, int n2, ECFieldElement eCFieldElement) {
        int n3;
        this.checkPoints(eCPointArray, n, n2);
        switch (this.getCoordinateSystem()) {
            case 0: 
            case 5: {
                if (eCFieldElement != null) {
                    throw new IllegalArgumentException("'iso' not valid for affine coordinates");
                }
                return;
            }
        }
        ECFieldElement[] eCFieldElementArray = new ECFieldElement[n2];
        int[] nArray = new int[n2];
        int n4 = 0;
        for (n3 = 0; n3 < n2; ++n3) {
            ECPoint eCPoint = eCPointArray[n + n3];
            if (null == eCPoint || eCFieldElement == null && eCPoint.isNormalized()) continue;
            eCFieldElementArray[n4] = eCPoint.getZCoord(0);
            nArray[n4++] = n + n3;
        }
        if (n4 == 0) {
            return;
        }
        ECAlgorithms.montgomeryTrick(eCFieldElementArray, 0, n4, eCFieldElement);
        for (n3 = 0; n3 < n4; ++n3) {
            int n5 = nArray[n3];
            eCPointArray[n5] = eCPointArray[n5].normalize(eCFieldElementArray[n3]);
        }
    }

    public abstract ECPoint getInfinity();

    public FiniteField getField() {
        return this.field;
    }

    public ECFieldElement getA() {
        return this.a;
    }

    public ECFieldElement getB() {
        return this.b;
    }

    public BigInteger getOrder() {
        return this.order;
    }

    public BigInteger getCofactor() {
        return this.cofactor;
    }

    public int getCoordinateSystem() {
        return this.coord;
    }

    protected abstract ECPoint decompressPoint(int var1, BigInteger var2);

    public ECEndomorphism getEndomorphism() {
        return this.endomorphism;
    }

    public ECMultiplier getMultiplier() {
        if (this.multiplier == null) {
            this.multiplier = this.createDefaultMultiplier();
        }
        return this.multiplier;
    }

    public ECPoint decodePoint(byte[] byArray) {
        ECPoint eCPoint = null;
        int n = (this.getFieldSize() + 7) / 8;
        byte by = byArray[0];
        switch (by) {
            case 0: {
                if (byArray.length != 1) {
                    throw new IllegalArgumentException("Incorrect length for infinity encoding");
                }
                eCPoint = this.getInfinity();
                break;
            }
            case 2: 
            case 3: {
                if (byArray.length != n + 1) {
                    throw new IllegalArgumentException("Incorrect length for compressed encoding");
                }
                int n2 = by & 1;
                BigInteger bigInteger = BigIntegers.fromUnsignedByteArray(byArray, 1, n);
                eCPoint = this.decompressPoint(n2, bigInteger);
                if (eCPoint.implIsValid(true, true)) break;
                throw new IllegalArgumentException("Invalid point");
            }
            case 4: {
                if (byArray.length != 2 * n + 1) {
                    throw new IllegalArgumentException("Incorrect length for uncompressed encoding");
                }
                BigInteger bigInteger = BigIntegers.fromUnsignedByteArray(byArray, 1, n);
                BigInteger bigInteger2 = BigIntegers.fromUnsignedByteArray(byArray, 1 + n, n);
                eCPoint = this.validatePoint(bigInteger, bigInteger2);
                break;
            }
            case 6: 
            case 7: {
                if (byArray.length != 2 * n + 1) {
                    throw new IllegalArgumentException("Incorrect length for hybrid encoding");
                }
                BigInteger bigInteger = BigIntegers.fromUnsignedByteArray(byArray, 1, n);
                BigInteger bigInteger3 = BigIntegers.fromUnsignedByteArray(byArray, 1 + n, n);
                if (bigInteger3.testBit(0) != (by == 7)) {
                    throw new IllegalArgumentException("Inconsistent Y coordinate in hybrid encoding");
                }
                eCPoint = this.validatePoint(bigInteger, bigInteger3);
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid point encoding 0x" + Integer.toString(by, 16));
            }
        }
        if (by != 0 && eCPoint.isInfinity()) {
            throw new IllegalArgumentException("Invalid infinity encoding");
        }
        return eCPoint;
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArray, int n, int n2) {
        int n3 = this.getFieldSize() + 7 >>> 3;
        byte[] byArray = new byte[n2 * n3 * 2];
        int n4 = 0;
        for (int i = 0; i < n2; ++i) {
            ECPoint eCPoint = eCPointArray[n + i];
            byte[] byArray2 = eCPoint.getRawXCoord().toBigInteger().toByteArray();
            byte[] byArray3 = eCPoint.getRawYCoord().toBigInteger().toByteArray();
            int n5 = byArray2.length > n3 ? 1 : 0;
            int n6 = byArray2.length - n5;
            int n7 = byArray3.length > n3 ? 1 : 0;
            int n8 = byArray3.length - n7;
            System.arraycopy(byArray2, n5, byArray, n4 + n3 - n6, n6);
            System.arraycopy(byArray3, n7, byArray, (n4 += n3) + n3 - n8, n8);
            n4 += n3;
        }
        return new AbstractECLookupTable(){

            public int getSize() {
                return n2;
            }

            public ECPoint lookup(int n) {
                byte[] byArray3 = new byte[n3];
                byte[] byArray2 = new byte[n3];
                int n22 = 0;
                for (int i = 0; i < n2; ++i) {
                    int n32 = (i ^ n) - 1 >> 31;
                    for (int j = 0; j < n3; ++j) {
                        int n4 = j;
                        byArray3[n4] = (byte)(byArray3[n4] ^ byArray[n22 + j] & n32);
                        int n5 = j;
                        byArray2[n5] = (byte)(byArray2[n5] ^ byArray[n22 + n3 + j] & n32);
                    }
                    n22 += n3 * 2;
                }
                return this.createPoint(byArray3, byArray2);
            }

            public ECPoint lookupVar(int n) {
                byte[] byArray3 = new byte[n3];
                byte[] byArray2 = new byte[n3];
                int n22 = n * n3 * 2;
                for (int i = 0; i < n3; ++i) {
                    byArray3[i] = byArray[n22 + i];
                    byArray2[i] = byArray[n22 + n3 + i];
                }
                return this.createPoint(byArray3, byArray2);
            }

            private ECPoint createPoint(byte[] byArray3, byte[] byArray2) {
                return ECCurve.this.createRawPoint(ECCurve.this.fromBigInteger(new BigInteger(1, byArray3)), ECCurve.this.fromBigInteger(new BigInteger(1, byArray2)));
            }
        };
    }

    protected void checkPoint(ECPoint eCPoint) {
        if (null == eCPoint || this != eCPoint.getCurve()) {
            throw new IllegalArgumentException("'point' must be non-null and on this curve");
        }
    }

    protected void checkPoints(ECPoint[] eCPointArray) {
        this.checkPoints(eCPointArray, 0, eCPointArray.length);
    }

    protected void checkPoints(ECPoint[] eCPointArray, int n, int n2) {
        if (eCPointArray == null) {
            throw new IllegalArgumentException("'points' cannot be null");
        }
        if (n < 0 || n2 < 0 || n > eCPointArray.length - n2) {
            throw new IllegalArgumentException("invalid range specified for 'points'");
        }
        for (int i = 0; i < n2; ++i) {
            ECPoint eCPoint = eCPointArray[n + i];
            if (null == eCPoint || this == eCPoint.getCurve()) continue;
            throw new IllegalArgumentException("'points' entries must be null or on this curve");
        }
    }

    public boolean equals(ECCurve eCCurve) {
        return this == eCCurve || null != eCCurve && this.getField().equals(eCCurve.getField()) && this.getA().toBigInteger().equals(eCCurve.getA().toBigInteger()) && this.getB().toBigInteger().equals(eCCurve.getB().toBigInteger());
    }

    public boolean equals(Object object) {
        return this == object || object instanceof ECCurve && this.equals((ECCurve)object);
    }

    public int hashCode() {
        return this.getField().hashCode() ^ Integers.rotateLeft(this.getA().toBigInteger().hashCode(), 8) ^ Integers.rotateLeft(this.getB().toBigInteger().hashCode(), 16);
    }

    public static abstract class AbstractF2m
    extends ECCurve {
        private BigInteger[] si = null;

        public static BigInteger inverse(int n, int[] nArray, BigInteger bigInteger) {
            return new LongArray(bigInteger).modInverse(n, nArray).toBigInteger();
        }

        private static FiniteField buildField(int n, int n2, int n3, int n4) {
            if (n2 == 0) {
                throw new IllegalArgumentException("k1 must be > 0");
            }
            if (n3 == 0) {
                if (n4 != 0) {
                    throw new IllegalArgumentException("k3 must be 0 if k2 == 0");
                }
                return FiniteFields.getBinaryExtensionField(new int[]{0, n2, n});
            }
            if (n3 <= n2) {
                throw new IllegalArgumentException("k2 must be > k1");
            }
            if (n4 <= n3) {
                throw new IllegalArgumentException("k3 must be > k2");
            }
            return FiniteFields.getBinaryExtensionField(new int[]{0, n2, n3, n4, n});
        }

        protected AbstractF2m(int n, int n2, int n3, int n4) {
            super(AbstractF2m.buildField(n, n2, n3, n4));
        }

        public ECPoint createPoint(BigInteger bigInteger, BigInteger bigInteger2) {
            ECFieldElement eCFieldElement = this.fromBigInteger(bigInteger);
            ECFieldElement eCFieldElement2 = this.fromBigInteger(bigInteger2);
            int n = this.getCoordinateSystem();
            switch (n) {
                case 5: 
                case 6: {
                    if (eCFieldElement.isZero()) {
                        if (eCFieldElement2.square().equals(this.getB())) break;
                        throw new IllegalArgumentException();
                    }
                    eCFieldElement2 = eCFieldElement2.divide(eCFieldElement).add(eCFieldElement);
                    break;
                }
            }
            return this.createRawPoint(eCFieldElement, eCFieldElement2);
        }

        public boolean isValidFieldElement(BigInteger bigInteger) {
            return bigInteger != null && bigInteger.signum() >= 0 && bigInteger.bitLength() <= this.getFieldSize();
        }

        public ECFieldElement randomFieldElement(SecureRandom secureRandom) {
            int n = this.getFieldSize();
            return this.fromBigInteger(BigIntegers.createRandomBigInteger(n, secureRandom));
        }

        public ECFieldElement randomFieldElementMult(SecureRandom secureRandom) {
            int n = this.getFieldSize();
            ECFieldElement eCFieldElement = this.fromBigInteger(AbstractF2m.implRandomFieldElementMult(secureRandom, n));
            ECFieldElement eCFieldElement2 = this.fromBigInteger(AbstractF2m.implRandomFieldElementMult(secureRandom, n));
            return eCFieldElement.multiply(eCFieldElement2);
        }

        protected ECPoint decompressPoint(int n, BigInteger bigInteger) {
            ECFieldElement eCFieldElement = this.fromBigInteger(bigInteger);
            ECFieldElement eCFieldElement2 = null;
            if (eCFieldElement.isZero()) {
                eCFieldElement2 = this.getB().sqrt();
            } else {
                ECFieldElement eCFieldElement3 = eCFieldElement.square().invert().multiply(this.getB()).add(this.getA()).add(eCFieldElement);
                ECFieldElement eCFieldElement4 = this.solveQuadraticEquation(eCFieldElement3);
                if (eCFieldElement4 != null) {
                    if (eCFieldElement4.testBitZero() != (n == 1)) {
                        eCFieldElement4 = eCFieldElement4.addOne();
                    }
                    switch (this.getCoordinateSystem()) {
                        case 5: 
                        case 6: {
                            eCFieldElement2 = eCFieldElement4.add(eCFieldElement);
                            break;
                        }
                        default: {
                            eCFieldElement2 = eCFieldElement4.multiply(eCFieldElement);
                        }
                    }
                }
            }
            if (eCFieldElement2 == null) {
                throw new IllegalArgumentException("Invalid point compression");
            }
            return this.createRawPoint(eCFieldElement, eCFieldElement2);
        }

        protected ECFieldElement solveQuadraticEquation(ECFieldElement eCFieldElement) {
            ECFieldElement eCFieldElement2;
            ECFieldElement eCFieldElement3;
            ECFieldElement.AbstractF2m abstractF2m = (ECFieldElement.AbstractF2m)eCFieldElement;
            boolean bl = abstractF2m.hasFastTrace();
            if (bl && 0 != abstractF2m.trace()) {
                return null;
            }
            int n = this.getFieldSize();
            if (0 != (n & 1)) {
                ECFieldElement eCFieldElement4 = abstractF2m.halfTrace();
                if (bl || eCFieldElement4.square().add(eCFieldElement4).add(eCFieldElement).isZero()) {
                    return eCFieldElement4;
                }
                return null;
            }
            if (eCFieldElement.isZero()) {
                return eCFieldElement;
            }
            ECFieldElement eCFieldElement5 = this.fromBigInteger(ECConstants.ZERO);
            Random random = new Random();
            do {
                ECFieldElement eCFieldElement6 = this.fromBigInteger(new BigInteger(n, random));
                eCFieldElement2 = eCFieldElement5;
                ECFieldElement eCFieldElement7 = eCFieldElement;
                for (int i = 1; i < n; ++i) {
                    ECFieldElement eCFieldElement8 = eCFieldElement7.square();
                    eCFieldElement2 = eCFieldElement2.square().add(eCFieldElement8.multiply(eCFieldElement6));
                    eCFieldElement7 = eCFieldElement8.add(eCFieldElement);
                }
                if (eCFieldElement7.isZero()) continue;
                return null;
            } while ((eCFieldElement3 = eCFieldElement2.square().add(eCFieldElement2)).isZero());
            return eCFieldElement2;
        }

        synchronized BigInteger[] getSi() {
            if (this.si == null) {
                this.si = Tnaf.getSi(this);
            }
            return this.si;
        }

        public boolean isKoblitz() {
            return this.order != null && this.cofactor != null && this.b.isOne() && (this.a.isZero() || this.a.isOne());
        }

        private static BigInteger implRandomFieldElementMult(SecureRandom secureRandom, int n) {
            BigInteger bigInteger;
            while ((bigInteger = BigIntegers.createRandomBigInteger(n, secureRandom)).signum() <= 0) {
            }
            return bigInteger;
        }
    }

    public static abstract class AbstractFp
    extends ECCurve {
        protected AbstractFp(BigInteger bigInteger) {
            super(FiniteFields.getPrimeField(bigInteger));
        }

        public boolean isValidFieldElement(BigInteger bigInteger) {
            return bigInteger != null && bigInteger.signum() >= 0 && bigInteger.compareTo(this.getField().getCharacteristic()) < 0;
        }

        public ECFieldElement randomFieldElement(SecureRandom secureRandom) {
            BigInteger bigInteger = this.getField().getCharacteristic();
            ECFieldElement eCFieldElement = this.fromBigInteger(AbstractFp.implRandomFieldElement(secureRandom, bigInteger));
            ECFieldElement eCFieldElement2 = this.fromBigInteger(AbstractFp.implRandomFieldElement(secureRandom, bigInteger));
            return eCFieldElement.multiply(eCFieldElement2);
        }

        public ECFieldElement randomFieldElementMult(SecureRandom secureRandom) {
            BigInteger bigInteger = this.getField().getCharacteristic();
            ECFieldElement eCFieldElement = this.fromBigInteger(AbstractFp.implRandomFieldElementMult(secureRandom, bigInteger));
            ECFieldElement eCFieldElement2 = this.fromBigInteger(AbstractFp.implRandomFieldElementMult(secureRandom, bigInteger));
            return eCFieldElement.multiply(eCFieldElement2);
        }

        protected ECPoint decompressPoint(int n, BigInteger bigInteger) {
            ECFieldElement eCFieldElement = this.fromBigInteger(bigInteger);
            ECFieldElement eCFieldElement2 = eCFieldElement.square().add(this.a).multiply(eCFieldElement).add(this.b);
            ECFieldElement eCFieldElement3 = eCFieldElement2.sqrt();
            if (eCFieldElement3 == null) {
                throw new IllegalArgumentException("Invalid point compression");
            }
            if (eCFieldElement3.testBitZero() != (n == 1)) {
                eCFieldElement3 = eCFieldElement3.negate();
            }
            return this.createRawPoint(eCFieldElement, eCFieldElement3);
        }

        private static BigInteger implRandomFieldElement(SecureRandom secureRandom, BigInteger bigInteger) {
            BigInteger bigInteger2;
            while ((bigInteger2 = BigIntegers.createRandomBigInteger(bigInteger.bitLength(), secureRandom)).compareTo(bigInteger) >= 0) {
            }
            return bigInteger2;
        }

        private static BigInteger implRandomFieldElementMult(SecureRandom secureRandom, BigInteger bigInteger) {
            BigInteger bigInteger2;
            while ((bigInteger2 = BigIntegers.createRandomBigInteger(bigInteger.bitLength(), secureRandom)).signum() <= 0 || bigInteger2.compareTo(bigInteger) >= 0) {
            }
            return bigInteger2;
        }
    }

    public class Config {
        protected int coord;
        protected ECEndomorphism endomorphism;
        protected ECMultiplier multiplier;

        Config(int n, ECEndomorphism eCEndomorphism, ECMultiplier eCMultiplier) {
            this.coord = n;
            this.endomorphism = eCEndomorphism;
            this.multiplier = eCMultiplier;
        }

        public Config setCoordinateSystem(int n) {
            this.coord = n;
            return this;
        }

        public Config setEndomorphism(ECEndomorphism eCEndomorphism) {
            this.endomorphism = eCEndomorphism;
            return this;
        }

        public Config setMultiplier(ECMultiplier eCMultiplier) {
            this.multiplier = eCMultiplier;
            return this;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public ECCurve create() {
            if (!ECCurve.this.supportsCoordinateSystem(this.coord)) {
                throw new IllegalStateException("unsupported coordinate system");
            }
            ECCurve eCCurve = ECCurve.this.cloneCurve();
            if (eCCurve == ECCurve.this) {
                throw new IllegalStateException("implementation returned current curve");
            }
            ECCurve eCCurve2 = eCCurve;
            synchronized (eCCurve2) {
                eCCurve.coord = this.coord;
                eCCurve.endomorphism = this.endomorphism;
                eCCurve.multiplier = this.multiplier;
            }
            return eCCurve;
        }
    }

    public static class F2m
    extends AbstractF2m {
        private static final int F2M_DEFAULT_COORDS = 6;
        private int m;
        private int k1;
        private int k2;
        private int k3;
        private ECPoint.F2m infinity;

        public F2m(int n, int n2, BigInteger bigInteger, BigInteger bigInteger2) {
            this(n, n2, 0, 0, bigInteger, bigInteger2, null, null);
        }

        public F2m(int n, int n2, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
            this(n, n2, 0, 0, bigInteger, bigInteger2, bigInteger3, bigInteger4);
        }

        public F2m(int n, int n2, int n3, int n4, BigInteger bigInteger, BigInteger bigInteger2) {
            this(n, n2, n3, n4, bigInteger, bigInteger2, null, null);
        }

        public F2m(int n, int n2, int n3, int n4, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
            super(n, n2, n3, n4);
            this.m = n;
            this.k1 = n2;
            this.k2 = n3;
            this.k3 = n4;
            this.order = bigInteger3;
            this.cofactor = bigInteger4;
            this.infinity = new ECPoint.F2m(this, null, null);
            this.a = this.fromBigInteger(bigInteger);
            this.b = this.fromBigInteger(bigInteger2);
            this.coord = 6;
        }

        protected F2m(int n, int n2, int n3, int n4, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, BigInteger bigInteger, BigInteger bigInteger2) {
            super(n, n2, n3, n4);
            this.m = n;
            this.k1 = n2;
            this.k2 = n3;
            this.k3 = n4;
            this.order = bigInteger;
            this.cofactor = bigInteger2;
            this.infinity = new ECPoint.F2m(this, null, null);
            this.a = eCFieldElement;
            this.b = eCFieldElement2;
            this.coord = 6;
        }

        protected ECCurve cloneCurve() {
            return new F2m(this.m, this.k1, this.k2, this.k3, this.a, this.b, this.order, this.cofactor);
        }

        public boolean supportsCoordinateSystem(int n) {
            switch (n) {
                case 0: 
                case 1: 
                case 6: {
                    return true;
                }
            }
            return false;
        }

        protected ECMultiplier createDefaultMultiplier() {
            if (this.isKoblitz()) {
                return new WTauNafMultiplier();
            }
            return super.createDefaultMultiplier();
        }

        public int getFieldSize() {
            return this.m;
        }

        public ECFieldElement fromBigInteger(BigInteger bigInteger) {
            return new ECFieldElement.F2m(this.m, this.k1, this.k2, this.k3, bigInteger);
        }

        protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
            return new ECPoint.F2m(this, eCFieldElement, eCFieldElement2);
        }

        protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArray) {
            return new ECPoint.F2m(this, eCFieldElement, eCFieldElement2, eCFieldElementArray);
        }

        public ECPoint getInfinity() {
            return this.infinity;
        }

        public int getM() {
            return this.m;
        }

        public boolean isTrinomial() {
            return this.k2 == 0 && this.k3 == 0;
        }

        public int getK1() {
            return this.k1;
        }

        public int getK2() {
            return this.k2;
        }

        public int getK3() {
            return this.k3;
        }

        public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArray, int n, final int n2) {
            int[] nArray;
            final int n3 = this.m + 63 >>> 6;
            if (this.isTrinomial()) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = this.k1;
            } else {
                int[] nArray3 = new int[3];
                nArray3[0] = this.k1;
                nArray3[1] = this.k2;
                nArray = nArray3;
                nArray3[2] = this.k3;
            }
            final int[] nArray4 = nArray;
            final long[] lArray = new long[n2 * n3 * 2];
            int n4 = 0;
            for (int i = 0; i < n2; ++i) {
                ECPoint eCPoint = eCPointArray[n + i];
                ((ECFieldElement.F2m)eCPoint.getRawXCoord()).x.copyTo(lArray, n4);
                ((ECFieldElement.F2m)eCPoint.getRawYCoord()).x.copyTo(lArray, n4 += n3);
                n4 += n3;
            }
            return new AbstractECLookupTable(){

                public int getSize() {
                    return n2;
                }

                public ECPoint lookup(int n) {
                    long[] lArray3 = Nat.create64(n3);
                    long[] lArray2 = Nat.create64(n3);
                    int n22 = 0;
                    for (int i = 0; i < n2; ++i) {
                        long l = (i ^ n) - 1 >> 31;
                        for (int j = 0; j < n3; ++j) {
                            int n32 = j;
                            lArray3[n32] = lArray3[n32] ^ lArray[n22 + j] & l;
                            int n4 = j;
                            lArray2[n4] = lArray2[n4] ^ lArray[n22 + n3 + j] & l;
                        }
                        n22 += n3 * 2;
                    }
                    return this.createPoint(lArray3, lArray2);
                }

                public ECPoint lookupVar(int n) {
                    long[] lArray3 = Nat.create64(n3);
                    long[] lArray2 = Nat.create64(n3);
                    int n22 = n * n3 * 2;
                    for (int i = 0; i < n3; ++i) {
                        lArray3[i] = lArray[n22 + i];
                        lArray2[i] = lArray[n22 + n3 + i];
                    }
                    return this.createPoint(lArray3, lArray2);
                }

                private ECPoint createPoint(long[] lArray3, long[] lArray2) {
                    ECFieldElement.F2m f2m = new ECFieldElement.F2m(F2m.this.m, nArray4, new LongArray(lArray3));
                    ECFieldElement.F2m f2m2 = new ECFieldElement.F2m(F2m.this.m, nArray4, new LongArray(lArray2));
                    return F2m.this.createRawPoint(f2m, f2m2);
                }
            };
        }
    }

    public static class Fp
    extends AbstractFp {
        private static final int FP_DEFAULT_COORDS = 4;
        BigInteger q;
        BigInteger r;
        ECPoint.Fp infinity;

        public Fp(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) {
            this(bigInteger, bigInteger2, bigInteger3, null, null);
        }

        public Fp(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4, BigInteger bigInteger5) {
            super(bigInteger);
            this.q = bigInteger;
            this.r = ECFieldElement.Fp.calculateResidue(bigInteger);
            this.infinity = new ECPoint.Fp(this, null, null);
            this.a = this.fromBigInteger(bigInteger2);
            this.b = this.fromBigInteger(bigInteger3);
            this.order = bigInteger4;
            this.cofactor = bigInteger5;
            this.coord = 4;
        }

        protected Fp(BigInteger bigInteger, BigInteger bigInteger2, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, BigInteger bigInteger3, BigInteger bigInteger4) {
            super(bigInteger);
            this.q = bigInteger;
            this.r = bigInteger2;
            this.infinity = new ECPoint.Fp(this, null, null);
            this.a = eCFieldElement;
            this.b = eCFieldElement2;
            this.order = bigInteger3;
            this.cofactor = bigInteger4;
            this.coord = 4;
        }

        protected ECCurve cloneCurve() {
            return new Fp(this.q, this.r, this.a, this.b, this.order, this.cofactor);
        }

        public boolean supportsCoordinateSystem(int n) {
            switch (n) {
                case 0: 
                case 1: 
                case 2: 
                case 4: {
                    return true;
                }
            }
            return false;
        }

        public BigInteger getQ() {
            return this.q;
        }

        public int getFieldSize() {
            return this.q.bitLength();
        }

        public ECFieldElement fromBigInteger(BigInteger bigInteger) {
            return new ECFieldElement.Fp(this.q, this.r, bigInteger);
        }

        protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
            return new ECPoint.Fp(this, eCFieldElement, eCFieldElement2);
        }

        protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArray) {
            return new ECPoint.Fp(this, eCFieldElement, eCFieldElement2, eCFieldElementArray);
        }

        public ECPoint importPoint(ECPoint eCPoint) {
            if (this != eCPoint.getCurve() && this.getCoordinateSystem() == 2 && !eCPoint.isInfinity()) {
                switch (eCPoint.getCurve().getCoordinateSystem()) {
                    case 2: 
                    case 3: 
                    case 4: {
                        return new ECPoint.Fp(this, this.fromBigInteger(eCPoint.x.toBigInteger()), this.fromBigInteger(eCPoint.y.toBigInteger()), new ECFieldElement[]{this.fromBigInteger(eCPoint.zs[0].toBigInteger())});
                    }
                }
            }
            return super.importPoint(eCPoint);
        }

        public ECPoint getInfinity() {
            return this.infinity;
        }
    }
}


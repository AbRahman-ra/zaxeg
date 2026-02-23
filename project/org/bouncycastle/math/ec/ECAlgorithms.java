/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.ECPointMap;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.math.ec.FixedPointPreCompInfo;
import org.bouncycastle.math.ec.FixedPointUtil;
import org.bouncycastle.math.ec.WNafPreCompInfo;
import org.bouncycastle.math.ec.WNafUtil;
import org.bouncycastle.math.ec.endo.ECEndomorphism;
import org.bouncycastle.math.ec.endo.EndoUtil;
import org.bouncycastle.math.ec.endo.GLVEndomorphism;
import org.bouncycastle.math.field.FiniteField;
import org.bouncycastle.math.field.PolynomialExtensionField;
import org.bouncycastle.math.raw.Nat;

public class ECAlgorithms {
    public static boolean isF2mCurve(ECCurve eCCurve) {
        return ECAlgorithms.isF2mField(eCCurve.getField());
    }

    public static boolean isF2mField(FiniteField finiteField) {
        return finiteField.getDimension() > 1 && finiteField.getCharacteristic().equals(ECConstants.TWO) && finiteField instanceof PolynomialExtensionField;
    }

    public static boolean isFpCurve(ECCurve eCCurve) {
        return ECAlgorithms.isFpField(eCCurve.getField());
    }

    public static boolean isFpField(FiniteField finiteField) {
        return finiteField.getDimension() == 1;
    }

    public static ECPoint sumOfMultiplies(ECPoint[] eCPointArray, BigInteger[] bigIntegerArray) {
        if (eCPointArray == null || bigIntegerArray == null || eCPointArray.length != bigIntegerArray.length || eCPointArray.length < 1) {
            throw new IllegalArgumentException("point and scalar arrays should be non-null, and of equal, non-zero, length");
        }
        int n = eCPointArray.length;
        switch (n) {
            case 1: {
                return eCPointArray[0].multiply(bigIntegerArray[0]);
            }
            case 2: {
                return ECAlgorithms.sumOfTwoMultiplies(eCPointArray[0], bigIntegerArray[0], eCPointArray[1], bigIntegerArray[1]);
            }
        }
        ECPoint eCPoint = eCPointArray[0];
        ECCurve eCCurve = eCPoint.getCurve();
        ECPoint[] eCPointArray2 = new ECPoint[n];
        eCPointArray2[0] = eCPoint;
        for (int i = 1; i < n; ++i) {
            eCPointArray2[i] = ECAlgorithms.importPoint(eCCurve, eCPointArray[i]);
        }
        ECEndomorphism eCEndomorphism = eCCurve.getEndomorphism();
        if (eCEndomorphism instanceof GLVEndomorphism) {
            return ECAlgorithms.implCheckResult(ECAlgorithms.implSumOfMultipliesGLV(eCPointArray2, bigIntegerArray, (GLVEndomorphism)eCEndomorphism));
        }
        return ECAlgorithms.implCheckResult(ECAlgorithms.implSumOfMultiplies(eCPointArray2, bigIntegerArray));
    }

    public static ECPoint sumOfTwoMultiplies(ECPoint eCPoint, BigInteger bigInteger, ECPoint eCPoint2, BigInteger bigInteger2) {
        Object object;
        ECCurve eCCurve = eCPoint.getCurve();
        eCPoint2 = ECAlgorithms.importPoint(eCCurve, eCPoint2);
        if (eCCurve instanceof ECCurve.AbstractF2m && ((ECCurve.AbstractF2m)(object = (ECCurve.AbstractF2m)eCCurve)).isKoblitz()) {
            return ECAlgorithms.implCheckResult(eCPoint.multiply(bigInteger).add(eCPoint2.multiply(bigInteger2)));
        }
        object = eCCurve.getEndomorphism();
        if (object instanceof GLVEndomorphism) {
            return ECAlgorithms.implCheckResult(ECAlgorithms.implSumOfMultipliesGLV(new ECPoint[]{eCPoint, eCPoint2}, new BigInteger[]{bigInteger, bigInteger2}, (GLVEndomorphism)object));
        }
        return ECAlgorithms.implCheckResult(ECAlgorithms.implShamirsTrickWNaf(eCPoint, bigInteger, eCPoint2, bigInteger2));
    }

    public static ECPoint shamirsTrick(ECPoint eCPoint, BigInteger bigInteger, ECPoint eCPoint2, BigInteger bigInteger2) {
        ECCurve eCCurve = eCPoint.getCurve();
        eCPoint2 = ECAlgorithms.importPoint(eCCurve, eCPoint2);
        return ECAlgorithms.implCheckResult(ECAlgorithms.implShamirsTrickJsf(eCPoint, bigInteger, eCPoint2, bigInteger2));
    }

    public static ECPoint importPoint(ECCurve eCCurve, ECPoint eCPoint) {
        ECCurve eCCurve2 = eCPoint.getCurve();
        if (!eCCurve.equals(eCCurve2)) {
            throw new IllegalArgumentException("Point must be on the same curve");
        }
        return eCCurve.importPoint(eCPoint);
    }

    public static void montgomeryTrick(ECFieldElement[] eCFieldElementArray, int n, int n2) {
        ECAlgorithms.montgomeryTrick(eCFieldElementArray, n, n2, null);
    }

    public static void montgomeryTrick(ECFieldElement[] eCFieldElementArray, int n, int n2, ECFieldElement eCFieldElement) {
        ECFieldElement[] eCFieldElementArray2 = new ECFieldElement[n2];
        eCFieldElementArray2[0] = eCFieldElementArray[n];
        int n3 = 0;
        while (++n3 < n2) {
            eCFieldElementArray2[n3] = eCFieldElementArray2[n3 - 1].multiply(eCFieldElementArray[n + n3]);
        }
        --n3;
        if (eCFieldElement != null) {
            eCFieldElementArray2[n3] = eCFieldElementArray2[n3].multiply(eCFieldElement);
        }
        ECFieldElement eCFieldElement2 = eCFieldElementArray2[n3].invert();
        while (n3 > 0) {
            int n4 = n + n3--;
            ECFieldElement eCFieldElement3 = eCFieldElementArray[n4];
            eCFieldElementArray[n4] = eCFieldElementArray2[n3].multiply(eCFieldElement2);
            eCFieldElement2 = eCFieldElement2.multiply(eCFieldElement3);
        }
        eCFieldElementArray[n] = eCFieldElement2;
    }

    public static ECPoint referenceMultiply(ECPoint eCPoint, BigInteger bigInteger) {
        BigInteger bigInteger2 = bigInteger.abs();
        ECPoint eCPoint2 = eCPoint.getCurve().getInfinity();
        int n = bigInteger2.bitLength();
        if (n > 0) {
            if (bigInteger2.testBit(0)) {
                eCPoint2 = eCPoint;
            }
            for (int i = 1; i < n; ++i) {
                eCPoint = eCPoint.twice();
                if (!bigInteger2.testBit(i)) continue;
                eCPoint2 = eCPoint2.add(eCPoint);
            }
        }
        return bigInteger.signum() < 0 ? eCPoint2.negate() : eCPoint2;
    }

    public static ECPoint validatePoint(ECPoint eCPoint) {
        if (!eCPoint.isValid()) {
            throw new IllegalStateException("Invalid point");
        }
        return eCPoint;
    }

    public static ECPoint cleanPoint(ECCurve eCCurve, ECPoint eCPoint) {
        ECCurve eCCurve2 = eCPoint.getCurve();
        if (!eCCurve.equals(eCCurve2)) {
            throw new IllegalArgumentException("Point must be on the same curve");
        }
        return eCCurve.decodePoint(eCPoint.getEncoded(false));
    }

    static ECPoint implCheckResult(ECPoint eCPoint) {
        if (!eCPoint.isValidPartial()) {
            throw new IllegalStateException("Invalid result");
        }
        return eCPoint;
    }

    static ECPoint implShamirsTrickJsf(ECPoint eCPoint, BigInteger bigInteger, ECPoint eCPoint2, BigInteger bigInteger2) {
        ECCurve eCCurve = eCPoint.getCurve();
        ECPoint eCPoint3 = eCCurve.getInfinity();
        ECPoint eCPoint4 = eCPoint.add(eCPoint2);
        ECPoint eCPoint5 = eCPoint.subtract(eCPoint2);
        ECPoint[] eCPointArray = new ECPoint[]{eCPoint2, eCPoint5, eCPoint, eCPoint4};
        eCCurve.normalizeAll(eCPointArray);
        ECPoint[] eCPointArray2 = new ECPoint[]{eCPointArray[3].negate(), eCPointArray[2].negate(), eCPointArray[1].negate(), eCPointArray[0].negate(), eCPoint3, eCPointArray[0], eCPointArray[1], eCPointArray[2], eCPointArray[3]};
        byte[] byArray = WNafUtil.generateJSF(bigInteger, bigInteger2);
        ECPoint eCPoint6 = eCPoint3;
        int n = byArray.length;
        while (--n >= 0) {
            byte by = byArray[n];
            int n2 = by << 24 >> 28;
            int n3 = by << 28 >> 28;
            int n4 = 4 + n2 * 3 + n3;
            eCPoint6 = eCPoint6.twicePlus(eCPointArray2[n4]);
        }
        return eCPoint6;
    }

    static ECPoint implShamirsTrickWNaf(ECPoint eCPoint, BigInteger bigInteger, ECPoint eCPoint2, BigInteger bigInteger2) {
        boolean bl = bigInteger.signum() < 0;
        boolean bl2 = bigInteger2.signum() < 0;
        BigInteger bigInteger3 = bigInteger.abs();
        BigInteger bigInteger4 = bigInteger2.abs();
        int n = WNafUtil.getWindowSize(bigInteger3.bitLength(), 8);
        int n2 = WNafUtil.getWindowSize(bigInteger4.bitLength(), 8);
        WNafPreCompInfo wNafPreCompInfo = WNafUtil.precompute(eCPoint, n, true);
        WNafPreCompInfo wNafPreCompInfo2 = WNafUtil.precompute(eCPoint2, n2, true);
        ECCurve eCCurve = eCPoint.getCurve();
        int n3 = FixedPointUtil.getCombSize(eCCurve);
        if (!bl && !bl2 && bigInteger.bitLength() <= n3 && bigInteger2.bitLength() <= n3 && wNafPreCompInfo.isPromoted() && wNafPreCompInfo2.isPromoted()) {
            return ECAlgorithms.implShamirsTrickFixedPoint(eCPoint, bigInteger, eCPoint2, bigInteger2);
        }
        int n4 = Math.min(8, wNafPreCompInfo.getWidth());
        n3 = Math.min(8, wNafPreCompInfo2.getWidth());
        ECPoint[] eCPointArray = bl ? wNafPreCompInfo.getPreCompNeg() : wNafPreCompInfo.getPreComp();
        ECPoint[] eCPointArray2 = bl2 ? wNafPreCompInfo2.getPreCompNeg() : wNafPreCompInfo2.getPreComp();
        ECPoint[] eCPointArray3 = bl ? wNafPreCompInfo.getPreComp() : wNafPreCompInfo.getPreCompNeg();
        ECPoint[] eCPointArray4 = bl2 ? wNafPreCompInfo2.getPreComp() : wNafPreCompInfo2.getPreCompNeg();
        byte[] byArray = WNafUtil.generateWindowNaf(n4, bigInteger3);
        byte[] byArray2 = WNafUtil.generateWindowNaf(n3, bigInteger4);
        return ECAlgorithms.implShamirsTrickWNaf(eCPointArray, eCPointArray3, byArray, eCPointArray2, eCPointArray4, byArray2);
    }

    static ECPoint implShamirsTrickWNaf(ECEndomorphism eCEndomorphism, ECPoint eCPoint, BigInteger bigInteger, BigInteger bigInteger2) {
        boolean bl = bigInteger.signum() < 0;
        boolean bl2 = bigInteger2.signum() < 0;
        bigInteger = bigInteger.abs();
        bigInteger2 = bigInteger2.abs();
        int n = WNafUtil.getWindowSize(Math.max(bigInteger.bitLength(), bigInteger2.bitLength()), 8);
        WNafPreCompInfo wNafPreCompInfo = WNafUtil.precompute(eCPoint, n, true);
        ECPoint eCPoint2 = EndoUtil.mapPoint(eCEndomorphism, eCPoint);
        WNafPreCompInfo wNafPreCompInfo2 = WNafUtil.precomputeWithPointMap(eCPoint2, eCEndomorphism.getPointMap(), wNafPreCompInfo, true);
        int n2 = Math.min(8, wNafPreCompInfo.getWidth());
        int n3 = Math.min(8, wNafPreCompInfo2.getWidth());
        ECPoint[] eCPointArray = bl ? wNafPreCompInfo.getPreCompNeg() : wNafPreCompInfo.getPreComp();
        ECPoint[] eCPointArray2 = bl2 ? wNafPreCompInfo2.getPreCompNeg() : wNafPreCompInfo2.getPreComp();
        ECPoint[] eCPointArray3 = bl ? wNafPreCompInfo.getPreComp() : wNafPreCompInfo.getPreCompNeg();
        ECPoint[] eCPointArray4 = bl2 ? wNafPreCompInfo2.getPreComp() : wNafPreCompInfo2.getPreCompNeg();
        byte[] byArray = WNafUtil.generateWindowNaf(n2, bigInteger);
        byte[] byArray2 = WNafUtil.generateWindowNaf(n3, bigInteger2);
        return ECAlgorithms.implShamirsTrickWNaf(eCPointArray, eCPointArray3, byArray, eCPointArray2, eCPointArray4, byArray2);
    }

    private static ECPoint implShamirsTrickWNaf(ECPoint[] eCPointArray, ECPoint[] eCPointArray2, byte[] byArray, ECPoint[] eCPointArray3, ECPoint[] eCPointArray4, byte[] byArray2) {
        ECPoint eCPoint;
        int n = Math.max(byArray.length, byArray2.length);
        ECCurve eCCurve = eCPointArray[0].getCurve();
        ECPoint eCPoint2 = eCPoint = eCCurve.getInfinity();
        int n2 = 0;
        for (int i = n - 1; i >= 0; --i) {
            ECPoint[] eCPointArray5;
            int n3;
            byte by;
            byte by2 = i < byArray.length ? byArray[i] : (byte)0;
            byte by3 = by = i < byArray2.length ? byArray2[i] : (byte)0;
            if ((by2 | by) == 0) {
                ++n2;
                continue;
            }
            ECPoint eCPoint3 = eCPoint;
            if (by2 != 0) {
                n3 = Math.abs(by2);
                eCPointArray5 = by2 < 0 ? eCPointArray2 : eCPointArray;
                eCPoint3 = eCPoint3.add(eCPointArray5[n3 >>> 1]);
            }
            if (by != 0) {
                n3 = Math.abs(by);
                eCPointArray5 = by < 0 ? eCPointArray4 : eCPointArray3;
                eCPoint3 = eCPoint3.add(eCPointArray5[n3 >>> 1]);
            }
            if (n2 > 0) {
                eCPoint2 = eCPoint2.timesPow2(n2);
                n2 = 0;
            }
            eCPoint2 = eCPoint2.twicePlus(eCPoint3);
        }
        if (n2 > 0) {
            eCPoint2 = eCPoint2.timesPow2(n2);
        }
        return eCPoint2;
    }

    static ECPoint implSumOfMultiplies(ECPoint[] eCPointArray, BigInteger[] bigIntegerArray) {
        int n = eCPointArray.length;
        boolean[] blArray = new boolean[n];
        WNafPreCompInfo[] wNafPreCompInfoArray = new WNafPreCompInfo[n];
        byte[][] byArrayArray = new byte[n][];
        for (int i = 0; i < n; ++i) {
            BigInteger bigInteger = bigIntegerArray[i];
            blArray[i] = bigInteger.signum() < 0;
            bigInteger = bigInteger.abs();
            int n2 = WNafUtil.getWindowSize(bigInteger.bitLength(), 8);
            WNafPreCompInfo wNafPreCompInfo = WNafUtil.precompute(eCPointArray[i], n2, true);
            int n3 = Math.min(8, wNafPreCompInfo.getWidth());
            wNafPreCompInfoArray[i] = wNafPreCompInfo;
            byArrayArray[i] = WNafUtil.generateWindowNaf(n3, bigInteger);
        }
        return ECAlgorithms.implSumOfMultiplies(blArray, wNafPreCompInfoArray, byArrayArray);
    }

    static ECPoint implSumOfMultipliesGLV(ECPoint[] eCPointArray, BigInteger[] bigIntegerArray, GLVEndomorphism gLVEndomorphism) {
        BigInteger bigInteger = eCPointArray[0].getCurve().getOrder();
        int n = eCPointArray.length;
        BigInteger[] bigIntegerArray2 = new BigInteger[n << 1];
        int n2 = 0;
        for (int i = 0; i < n; ++i) {
            BigInteger[] bigIntegerArray3 = gLVEndomorphism.decomposeScalar(bigIntegerArray[i].mod(bigInteger));
            bigIntegerArray2[n2++] = bigIntegerArray3[0];
            bigIntegerArray2[n2++] = bigIntegerArray3[1];
        }
        if (gLVEndomorphism.hasEfficientPointMap()) {
            return ECAlgorithms.implSumOfMultiplies(gLVEndomorphism, eCPointArray, bigIntegerArray2);
        }
        ECPoint[] eCPointArray2 = new ECPoint[n << 1];
        int n3 = 0;
        for (n2 = 0; n2 < n; ++n2) {
            ECPoint eCPoint = eCPointArray[n2];
            ECPoint eCPoint2 = EndoUtil.mapPoint(gLVEndomorphism, eCPoint);
            eCPointArray2[n3++] = eCPoint;
            eCPointArray2[n3++] = eCPoint2;
        }
        return ECAlgorithms.implSumOfMultiplies(eCPointArray2, bigIntegerArray2);
    }

    static ECPoint implSumOfMultiplies(ECEndomorphism eCEndomorphism, ECPoint[] eCPointArray, BigInteger[] bigIntegerArray) {
        int n = eCPointArray.length;
        int n2 = n << 1;
        boolean[] blArray = new boolean[n2];
        WNafPreCompInfo[] wNafPreCompInfoArray = new WNafPreCompInfo[n2];
        byte[][] byArrayArray = new byte[n2][];
        ECPointMap eCPointMap = eCEndomorphism.getPointMap();
        for (int i = 0; i < n; ++i) {
            int n3 = i << 1;
            int n4 = n3 + 1;
            BigInteger bigInteger = bigIntegerArray[n3];
            blArray[n3] = bigInteger.signum() < 0;
            bigInteger = bigInteger.abs();
            BigInteger bigInteger2 = bigIntegerArray[n4];
            blArray[n4] = bigInteger2.signum() < 0;
            bigInteger2 = bigInteger2.abs();
            int n5 = WNafUtil.getWindowSize(Math.max(bigInteger.bitLength(), bigInteger2.bitLength()), 8);
            ECPoint eCPoint = eCPointArray[i];
            WNafPreCompInfo wNafPreCompInfo = WNafUtil.precompute(eCPoint, n5, true);
            ECPoint eCPoint2 = EndoUtil.mapPoint(eCEndomorphism, eCPoint);
            WNafPreCompInfo wNafPreCompInfo2 = WNafUtil.precomputeWithPointMap(eCPoint2, eCPointMap, wNafPreCompInfo, true);
            int n6 = Math.min(8, wNafPreCompInfo.getWidth());
            int n7 = Math.min(8, wNafPreCompInfo2.getWidth());
            wNafPreCompInfoArray[n3] = wNafPreCompInfo;
            wNafPreCompInfoArray[n4] = wNafPreCompInfo2;
            byArrayArray[n3] = WNafUtil.generateWindowNaf(n6, bigInteger);
            byArrayArray[n4] = WNafUtil.generateWindowNaf(n7, bigInteger2);
        }
        return ECAlgorithms.implSumOfMultiplies(blArray, wNafPreCompInfoArray, byArrayArray);
    }

    private static ECPoint implSumOfMultiplies(boolean[] blArray, WNafPreCompInfo[] wNafPreCompInfoArray, byte[][] byArray) {
        ECPoint eCPoint;
        int n = 0;
        int n2 = byArray.length;
        for (int i = 0; i < n2; ++i) {
            n = Math.max(n, byArray[i].length);
        }
        ECCurve eCCurve = wNafPreCompInfoArray[0].getPreComp()[0].getCurve();
        ECPoint eCPoint2 = eCPoint = eCCurve.getInfinity();
        int n3 = 0;
        for (int i = n - 1; i >= 0; --i) {
            ECPoint eCPoint3 = eCPoint;
            for (int j = 0; j < n2; ++j) {
                byte by;
                byte[] byArray2 = byArray[j];
                byte by2 = by = i < byArray2.length ? byArray2[i] : (byte)0;
                if (by == 0) continue;
                int n4 = Math.abs(by);
                WNafPreCompInfo wNafPreCompInfo = wNafPreCompInfoArray[j];
                ECPoint[] eCPointArray = by < 0 == blArray[j] ? wNafPreCompInfo.getPreComp() : wNafPreCompInfo.getPreCompNeg();
                eCPoint3 = eCPoint3.add(eCPointArray[n4 >>> 1]);
            }
            if (eCPoint3 == eCPoint) {
                ++n3;
                continue;
            }
            if (n3 > 0) {
                eCPoint2 = eCPoint2.timesPow2(n3);
                n3 = 0;
            }
            eCPoint2 = eCPoint2.twicePlus(eCPoint3);
        }
        if (n3 > 0) {
            eCPoint2 = eCPoint2.timesPow2(n3);
        }
        return eCPoint2;
    }

    private static ECPoint implShamirsTrickFixedPoint(ECPoint eCPoint, BigInteger bigInteger, ECPoint eCPoint2, BigInteger bigInteger2) {
        int n;
        ECCurve eCCurve = eCPoint.getCurve();
        int n2 = FixedPointUtil.getCombSize(eCCurve);
        if (bigInteger.bitLength() > n2 || bigInteger2.bitLength() > n2) {
            throw new IllegalStateException("fixed-point comb doesn't support scalars larger than the curve order");
        }
        FixedPointPreCompInfo fixedPointPreCompInfo = FixedPointUtil.precompute(eCPoint);
        FixedPointPreCompInfo fixedPointPreCompInfo2 = FixedPointUtil.precompute(eCPoint2);
        ECLookupTable eCLookupTable = fixedPointPreCompInfo.getLookupTable();
        ECLookupTable eCLookupTable2 = fixedPointPreCompInfo2.getLookupTable();
        int n3 = fixedPointPreCompInfo.getWidth();
        if (n3 != (n = fixedPointPreCompInfo2.getWidth())) {
            FixedPointCombMultiplier fixedPointCombMultiplier = new FixedPointCombMultiplier();
            ECPoint eCPoint3 = fixedPointCombMultiplier.multiply(eCPoint, bigInteger);
            ECPoint eCPoint4 = fixedPointCombMultiplier.multiply(eCPoint2, bigInteger2);
            return eCPoint3.add(eCPoint4);
        }
        int n4 = n3;
        int n5 = (n2 + n4 - 1) / n4;
        ECPoint eCPoint5 = eCCurve.getInfinity();
        int n6 = n5 * n4;
        int[] nArray = Nat.fromBigInteger(n6, bigInteger);
        int[] nArray2 = Nat.fromBigInteger(n6, bigInteger2);
        int n7 = n6 - 1;
        for (int i = 0; i < n5; ++i) {
            int n8 = 0;
            int n9 = 0;
            for (int j = n7 - i; j >= 0; j -= n5) {
                int n10 = nArray[j >>> 5] >>> (j & 0x1F);
                n8 ^= n10 >>> 1;
                n8 <<= 1;
                n8 ^= n10;
                int n11 = nArray2[j >>> 5] >>> (j & 0x1F);
                n9 ^= n11 >>> 1;
                n9 <<= 1;
                n9 ^= n11;
            }
            ECPoint eCPoint6 = eCLookupTable.lookupVar(n8);
            ECPoint eCPoint7 = eCLookupTable2.lookupVar(n9);
            ECPoint eCPoint8 = eCPoint6.add(eCPoint7);
            eCPoint5 = eCPoint5.twicePlus(eCPoint8);
        }
        return eCPoint5.add(fixedPointPreCompInfo.getOffset()).add(fixedPointPreCompInfo2.getOffset());
    }
}


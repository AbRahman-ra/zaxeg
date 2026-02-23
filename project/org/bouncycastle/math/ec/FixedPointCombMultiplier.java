/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.AbstractECMultiplier;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointPreCompInfo;
import org.bouncycastle.math.ec.FixedPointUtil;
import org.bouncycastle.math.raw.Nat;

public class FixedPointCombMultiplier
extends AbstractECMultiplier {
    protected ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECCurve eCCurve = eCPoint.getCurve();
        int n = FixedPointUtil.getCombSize(eCCurve);
        if (bigInteger.bitLength() > n) {
            throw new IllegalStateException("fixed-point comb doesn't support scalars larger than the curve order");
        }
        FixedPointPreCompInfo fixedPointPreCompInfo = FixedPointUtil.precompute(eCPoint);
        ECLookupTable eCLookupTable = fixedPointPreCompInfo.getLookupTable();
        int n2 = fixedPointPreCompInfo.getWidth();
        int n3 = (n + n2 - 1) / n2;
        ECPoint eCPoint2 = eCCurve.getInfinity();
        int n4 = n3 * n2;
        int[] nArray = Nat.fromBigInteger(n4, bigInteger);
        int n5 = n4 - 1;
        for (int i = 0; i < n3; ++i) {
            int n6 = 0;
            for (int j = n5 - i; j >= 0; j -= n3) {
                int n7 = nArray[j >>> 5] >>> (j & 0x1F);
                n6 ^= n7 >>> 1;
                n6 <<= 1;
                n6 ^= n7;
            }
            ECPoint eCPoint3 = eCLookupTable.lookup(n6);
            eCPoint2 = eCPoint2.twicePlus(eCPoint3);
        }
        return eCPoint2.add(fixedPointPreCompInfo.getOffset());
    }
}


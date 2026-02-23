/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.AbstractECMultiplier;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.WNafPreCompInfo;
import org.bouncycastle.math.ec.WNafUtil;
import org.bouncycastle.util.Integers;

public class WNafL2RMultiplier
extends AbstractECMultiplier {
    protected ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECPoint[] eCPointArray;
        int n;
        int n2;
        int n3;
        int n4;
        int n5 = WNafUtil.getWindowSize(bigInteger.bitLength());
        WNafPreCompInfo wNafPreCompInfo = WNafUtil.precompute(eCPoint, n5, true);
        ECPoint[] eCPointArray2 = wNafPreCompInfo.getPreComp();
        ECPoint[] eCPointArray3 = wNafPreCompInfo.getPreCompNeg();
        int n6 = wNafPreCompInfo.getWidth();
        int[] nArray = WNafUtil.generateCompactWindowNaf(n6, bigInteger);
        ECPoint eCPoint2 = eCPoint.getCurve().getInfinity();
        int n7 = nArray.length;
        if (n7 > 1) {
            n4 = nArray[--n7];
            n3 = n4 >> 16;
            n2 = n4 & 0xFFFF;
            n = Math.abs(n3);
            ECPoint[] eCPointArray4 = eCPointArray = n3 < 0 ? eCPointArray3 : eCPointArray2;
            if (n << 2 < 1 << n6) {
                int n8 = 32 - Integers.numberOfLeadingZeros(n);
                int n9 = n6 - n8;
                int n10 = n ^ 1 << n8 - 1;
                int n11 = (1 << n6 - 1) - 1;
                int n12 = (n10 << n9) + 1;
                eCPoint2 = eCPointArray[n11 >>> 1].add(eCPointArray[n12 >>> 1]);
                n2 -= n9;
            } else {
                eCPoint2 = eCPointArray[n >>> 1];
            }
            eCPoint2 = eCPoint2.timesPow2(n2);
        }
        while (n7 > 0) {
            n4 = nArray[--n7];
            n3 = n4 >> 16;
            n2 = n4 & 0xFFFF;
            n = Math.abs(n3);
            eCPointArray = n3 < 0 ? eCPointArray3 : eCPointArray2;
            ECPoint eCPoint3 = eCPointArray[n >>> 1];
            eCPoint2 = eCPoint2.twicePlus(eCPoint3);
            eCPoint2 = eCPoint2.timesPow2(n2);
        }
        return eCPoint2;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointPreCompInfo;
import org.bouncycastle.math.ec.PreCompCallback;
import org.bouncycastle.math.ec.PreCompInfo;

public class FixedPointUtil {
    public static final String PRECOMP_NAME = "bc_fixed_point";

    public static int getCombSize(ECCurve eCCurve) {
        BigInteger bigInteger = eCCurve.getOrder();
        return bigInteger == null ? eCCurve.getFieldSize() + 1 : bigInteger.bitLength();
    }

    public static FixedPointPreCompInfo getFixedPointPreCompInfo(PreCompInfo preCompInfo) {
        return preCompInfo instanceof FixedPointPreCompInfo ? (FixedPointPreCompInfo)preCompInfo : null;
    }

    public static FixedPointPreCompInfo precompute(ECPoint eCPoint) {
        ECCurve eCCurve = eCPoint.getCurve();
        return (FixedPointPreCompInfo)eCCurve.precompute(eCPoint, PRECOMP_NAME, new PreCompCallback(){

            public PreCompInfo precompute(PreCompInfo preCompInfo) {
                int n;
                int n2;
                int n3;
                FixedPointPreCompInfo fixedPointPreCompInfo = preCompInfo instanceof FixedPointPreCompInfo ? (FixedPointPreCompInfo)preCompInfo : null;
                if (this.checkExisting(fixedPointPreCompInfo, n3 = 1 << (n2 = (n = FixedPointUtil.getCombSize(eCCurve)) > 250 ? 6 : 5))) {
                    return fixedPointPreCompInfo;
                }
                int n4 = (n + n2 - 1) / n2;
                ECPoint[] eCPointArray = new ECPoint[n2 + 1];
                eCPointArray[0] = eCPoint;
                for (int i = 1; i < n2; ++i) {
                    eCPointArray[i] = eCPointArray[i - 1].timesPow2(n4);
                }
                eCPointArray[n2] = eCPointArray[0].subtract(eCPointArray[1]);
                eCCurve.normalizeAll(eCPointArray);
                ECPoint[] eCPointArray2 = new ECPoint[n3];
                eCPointArray2[0] = eCPointArray[0];
                for (int i = n2 - 1; i >= 0; --i) {
                    int n5;
                    ECPoint eCPoint2 = eCPointArray[i];
                    for (int j = n5 = 1 << i; j < n3; j += n5 << 1) {
                        eCPointArray2[j] = eCPointArray2[j - n5].add(eCPoint2);
                    }
                }
                eCCurve.normalizeAll(eCPointArray2);
                FixedPointPreCompInfo fixedPointPreCompInfo2 = new FixedPointPreCompInfo();
                fixedPointPreCompInfo2.setLookupTable(eCCurve.createCacheSafeLookupTable(eCPointArray2, 0, eCPointArray2.length));
                fixedPointPreCompInfo2.setOffset(eCPointArray[n2]);
                fixedPointPreCompInfo2.setWidth(n2);
                return fixedPointPreCompInfo2;
            }

            private boolean checkExisting(FixedPointPreCompInfo fixedPointPreCompInfo, int n) {
                return fixedPointPreCompInfo != null && this.checkTable(fixedPointPreCompInfo.getLookupTable(), n);
            }

            private boolean checkTable(ECLookupTable eCLookupTable, int n) {
                return eCLookupTable != null && eCLookupTable.getSize() >= n;
            }
        });
    }
}


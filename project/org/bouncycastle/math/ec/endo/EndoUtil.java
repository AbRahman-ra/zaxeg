/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec.endo;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.PreCompCallback;
import org.bouncycastle.math.ec.PreCompInfo;
import org.bouncycastle.math.ec.endo.ECEndomorphism;
import org.bouncycastle.math.ec.endo.EndoPreCompInfo;
import org.bouncycastle.math.ec.endo.ScalarSplitParameters;

public abstract class EndoUtil {
    public static final String PRECOMP_NAME = "bc_endo";

    public static BigInteger[] decomposeScalar(ScalarSplitParameters scalarSplitParameters, BigInteger bigInteger) {
        int n = scalarSplitParameters.getBits();
        BigInteger bigInteger2 = EndoUtil.calculateB(bigInteger, scalarSplitParameters.getG1(), n);
        BigInteger bigInteger3 = EndoUtil.calculateB(bigInteger, scalarSplitParameters.getG2(), n);
        BigInteger bigInteger4 = bigInteger.subtract(bigInteger2.multiply(scalarSplitParameters.getV1A()).add(bigInteger3.multiply(scalarSplitParameters.getV2A())));
        BigInteger bigInteger5 = bigInteger2.multiply(scalarSplitParameters.getV1B()).add(bigInteger3.multiply(scalarSplitParameters.getV2B())).negate();
        return new BigInteger[]{bigInteger4, bigInteger5};
    }

    public static ECPoint mapPoint(ECEndomorphism eCEndomorphism, ECPoint eCPoint) {
        ECCurve eCCurve = eCPoint.getCurve();
        EndoPreCompInfo endoPreCompInfo = (EndoPreCompInfo)eCCurve.precompute(eCPoint, PRECOMP_NAME, new PreCompCallback(){

            public PreCompInfo precompute(PreCompInfo preCompInfo) {
                EndoPreCompInfo endoPreCompInfo;
                EndoPreCompInfo endoPreCompInfo2 = endoPreCompInfo = preCompInfo instanceof EndoPreCompInfo ? (EndoPreCompInfo)preCompInfo : null;
                if (this.checkExisting(endoPreCompInfo, eCEndomorphism)) {
                    return endoPreCompInfo;
                }
                ECPoint eCPoint2 = eCEndomorphism.getPointMap().map(eCPoint);
                EndoPreCompInfo endoPreCompInfo3 = new EndoPreCompInfo();
                endoPreCompInfo3.setEndomorphism(eCEndomorphism);
                endoPreCompInfo3.setMappedPoint(eCPoint2);
                return endoPreCompInfo3;
            }

            private boolean checkExisting(EndoPreCompInfo endoPreCompInfo, ECEndomorphism eCEndomorphism2) {
                return null != endoPreCompInfo && endoPreCompInfo.getEndomorphism() == eCEndomorphism2 && endoPreCompInfo.getMappedPoint() != null;
            }
        });
        return endoPreCompInfo.getMappedPoint();
    }

    private static BigInteger calculateB(BigInteger bigInteger, BigInteger bigInteger2, int n) {
        boolean bl = bigInteger2.signum() < 0;
        BigInteger bigInteger3 = bigInteger.multiply(bigInteger2.abs());
        boolean bl2 = bigInteger3.testBit(n - 1);
        bigInteger3 = bigInteger3.shiftRight(n);
        if (bl2) {
            bigInteger3 = bigInteger3.add(ECConstants.ONE);
        }
        return bl ? bigInteger3.negate() : bigInteger3;
    }
}


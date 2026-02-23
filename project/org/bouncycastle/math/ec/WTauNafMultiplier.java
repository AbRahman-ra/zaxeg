/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.AbstractECMultiplier;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.PreCompCallback;
import org.bouncycastle.math.ec.PreCompInfo;
import org.bouncycastle.math.ec.Tnaf;
import org.bouncycastle.math.ec.WTauNafPreCompInfo;
import org.bouncycastle.math.ec.ZTauElement;

public class WTauNafMultiplier
extends AbstractECMultiplier {
    static final String PRECOMP_NAME = "bc_wtnaf";

    protected ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        if (!(eCPoint instanceof ECPoint.AbstractF2m)) {
            throw new IllegalArgumentException("Only ECPoint.AbstractF2m can be used in WTauNafMultiplier");
        }
        ECPoint.AbstractF2m abstractF2m = (ECPoint.AbstractF2m)eCPoint;
        ECCurve.AbstractF2m abstractF2m2 = (ECCurve.AbstractF2m)abstractF2m.getCurve();
        int n = abstractF2m2.getFieldSize();
        byte by = abstractF2m2.getA().toBigInteger().byteValue();
        byte by2 = Tnaf.getMu(by);
        BigInteger[] bigIntegerArray = abstractF2m2.getSi();
        ZTauElement zTauElement = Tnaf.partModReduction(bigInteger, n, by, bigIntegerArray, by2, (byte)10);
        return this.multiplyWTnaf(abstractF2m, zTauElement, by, by2);
    }

    private ECPoint.AbstractF2m multiplyWTnaf(ECPoint.AbstractF2m abstractF2m, ZTauElement zTauElement, byte by, byte by2) {
        ZTauElement[] zTauElementArray = by == 0 ? Tnaf.alpha0 : Tnaf.alpha1;
        BigInteger bigInteger = Tnaf.getTw(by2, 4);
        byte[] byArray = Tnaf.tauAdicWNaf(by2, zTauElement, (byte)4, BigInteger.valueOf(16L), bigInteger, zTauElementArray);
        return WTauNafMultiplier.multiplyFromWTnaf(abstractF2m, byArray);
    }

    private static ECPoint.AbstractF2m multiplyFromWTnaf(ECPoint.AbstractF2m abstractF2m, byte[] byArray) {
        ECCurve.AbstractF2m abstractF2m2 = (ECCurve.AbstractF2m)abstractF2m.getCurve();
        byte by = abstractF2m2.getA().toBigInteger().byteValue();
        WTauNafPreCompInfo wTauNafPreCompInfo = (WTauNafPreCompInfo)abstractF2m2.precompute(abstractF2m, PRECOMP_NAME, new PreCompCallback(){

            public PreCompInfo precompute(PreCompInfo preCompInfo) {
                if (preCompInfo instanceof WTauNafPreCompInfo) {
                    return preCompInfo;
                }
                WTauNafPreCompInfo wTauNafPreCompInfo = new WTauNafPreCompInfo();
                wTauNafPreCompInfo.setPreComp(Tnaf.getPreComp(abstractF2m, by));
                return wTauNafPreCompInfo;
            }
        });
        ECPoint.AbstractF2m[] abstractF2mArray = wTauNafPreCompInfo.getPreComp();
        ECPoint.AbstractF2m[] abstractF2mArray2 = new ECPoint.AbstractF2m[abstractF2mArray.length];
        for (int i = 0; i < abstractF2mArray.length; ++i) {
            abstractF2mArray2[i] = (ECPoint.AbstractF2m)abstractF2mArray[i].negate();
        }
        ECPoint.AbstractF2m abstractF2m3 = (ECPoint.AbstractF2m)abstractF2m.getCurve().getInfinity();
        int n = 0;
        for (int i = byArray.length - 1; i >= 0; --i) {
            ++n;
            byte by2 = byArray[i];
            if (by2 == 0) continue;
            abstractF2m3 = abstractF2m3.tauPow(n);
            n = 0;
            ECPoint.AbstractF2m abstractF2m4 = by2 > 0 ? abstractF2mArray[by2 >>> 1] : abstractF2mArray2[-by2 >>> 1];
            abstractF2m3 = (ECPoint.AbstractF2m)abstractF2m3.add(abstractF2m4);
        }
        if (n > 0) {
            abstractF2m3 = abstractF2m3.tauPow(n);
        }
        return abstractF2m3;
    }
}


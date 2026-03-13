/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce;

import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.math.ec.ECCurve;

public class ECPointUtil {
    public static ECPoint decodePoint(EllipticCurve ellipticCurve, byte[] byArray) {
        int[] nArray;
        ECCurve eCCurve = null;
        eCCurve = ellipticCurve.getField() instanceof ECFieldFp ? new ECCurve.Fp(((ECFieldFp)ellipticCurve.getField()).getP(), ellipticCurve.getA(), ellipticCurve.getB()) : ((nArray = ((ECFieldF2m)ellipticCurve.getField()).getMidTermsOfReductionPolynomial()).length == 3 ? new ECCurve.F2m(((ECFieldF2m)ellipticCurve.getField()).getM(), nArray[2], nArray[1], nArray[0], ellipticCurve.getA(), ellipticCurve.getB()) : new ECCurve.F2m(((ECFieldF2m)ellipticCurve.getField()).getM(), nArray[0], ellipticCurve.getA(), ellipticCurve.getB()));
        return EC5Util.convertPoint(eCCurve.decodePoint(byArray));
    }
}


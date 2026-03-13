/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

public class ECGOST3410NamedCurveTable {
    public static ECNamedCurveParameterSpec getParameterSpec(String string) {
        X9ECParameters x9ECParameters = ECGOST3410NamedCurves.getByNameX9(string);
        if (x9ECParameters == null) {
            try {
                x9ECParameters = ECGOST3410NamedCurves.getByOIDX9(new ASN1ObjectIdentifier(string));
            } catch (IllegalArgumentException illegalArgumentException) {
                return null;
            }
        }
        if (x9ECParameters == null) {
            return null;
        }
        return new ECNamedCurveParameterSpec(string, x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN(), x9ECParameters.getH(), x9ECParameters.getSeed());
    }

    public static Enumeration getNames() {
        return ECGOST3410NamedCurves.getNames();
    }
}


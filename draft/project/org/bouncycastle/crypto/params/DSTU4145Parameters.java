/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.util.Arrays;

public class DSTU4145Parameters
extends ECDomainParameters {
    private final byte[] dke;

    public DSTU4145Parameters(ECDomainParameters eCDomainParameters, byte[] byArray) {
        super(eCDomainParameters.getCurve(), eCDomainParameters.getG(), eCDomainParameters.getN(), eCDomainParameters.getH(), eCDomainParameters.getSeed());
        this.dke = Arrays.clone(byArray);
    }

    public byte[] getDKE() {
        return Arrays.clone(this.dke);
    }
}


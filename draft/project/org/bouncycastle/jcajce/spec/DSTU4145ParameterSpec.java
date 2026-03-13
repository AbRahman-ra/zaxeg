/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.spec;

import java.security.spec.ECParameterSpec;
import org.bouncycastle.asn1.ua.DSTU4145Params;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.util.Arrays;

public class DSTU4145ParameterSpec
extends ECParameterSpec {
    private final byte[] dke;
    private final ECDomainParameters parameters;

    public DSTU4145ParameterSpec(ECDomainParameters eCDomainParameters) {
        this(eCDomainParameters, EC5Util.convertToSpec(eCDomainParameters), DSTU4145Params.getDefaultDKE());
    }

    private DSTU4145ParameterSpec(ECDomainParameters eCDomainParameters, ECParameterSpec eCParameterSpec, byte[] byArray) {
        super(eCParameterSpec.getCurve(), eCParameterSpec.getGenerator(), eCParameterSpec.getOrder(), eCParameterSpec.getCofactor());
        this.parameters = eCDomainParameters;
        this.dke = Arrays.clone(byArray);
    }

    public byte[] getDKE() {
        return Arrays.clone(this.dke);
    }

    public boolean equals(Object object) {
        if (object instanceof DSTU4145ParameterSpec) {
            DSTU4145ParameterSpec dSTU4145ParameterSpec = (DSTU4145ParameterSpec)object;
            return this.parameters.equals(dSTU4145ParameterSpec.parameters);
        }
        return false;
    }

    public int hashCode() {
        return this.parameters.hashCode();
    }
}


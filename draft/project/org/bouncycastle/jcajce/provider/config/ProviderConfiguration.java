/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.config;

import java.security.spec.DSAParameterSpec;
import java.util.Map;
import java.util.Set;
import javax.crypto.spec.DHParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;

public interface ProviderConfiguration {
    public ECParameterSpec getEcImplicitlyCa();

    public DHParameterSpec getDHDefaultParameters(int var1);

    public DSAParameterSpec getDSADefaultParameters(int var1);

    public Set getAcceptableNamedCurves();

    public Map getAdditionalECParameters();
}


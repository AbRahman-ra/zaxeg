/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.util.Arrays;

public class SM2ParameterSpec
implements AlgorithmParameterSpec {
    private byte[] id;

    public SM2ParameterSpec(byte[] byArray) {
        if (byArray == null) {
            throw new NullPointerException("id string cannot be null");
        }
        this.id = Arrays.clone(byArray);
    }

    public byte[] getID() {
        return Arrays.clone(this.id);
    }
}


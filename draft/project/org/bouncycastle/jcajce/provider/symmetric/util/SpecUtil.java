/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.symmetric.util;

import java.security.AlgorithmParameters;
import java.security.spec.AlgorithmParameterSpec;

class SpecUtil {
    SpecUtil() {
    }

    static AlgorithmParameterSpec extractSpec(AlgorithmParameters algorithmParameters, Class[] classArray) {
        try {
            return algorithmParameters.getParameterSpec(AlgorithmParameterSpec.class);
        } catch (Exception exception) {
            for (int i = 0; i != classArray.length; ++i) {
                if (classArray[i] == null) continue;
                try {
                    return algorithmParameters.getParameterSpec(classArray[i]);
                } catch (Exception exception2) {
                    // empty catch block
                }
            }
            return null;
        }
    }
}


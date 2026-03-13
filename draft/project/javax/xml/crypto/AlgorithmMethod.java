/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto;

import java.security.spec.AlgorithmParameterSpec;

public interface AlgorithmMethod {
    public String getAlgorithm();

    public AlgorithmParameterSpec getParameterSpec();
}


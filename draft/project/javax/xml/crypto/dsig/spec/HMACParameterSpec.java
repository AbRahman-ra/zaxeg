/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.spec;

import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;

public final class HMACParameterSpec
implements SignatureMethodParameterSpec {
    private int outputLength;

    public HMACParameterSpec(int outputLength) {
        this.outputLength = outputLength;
    }

    public int getOutputLength() {
        return this.outputLength;
    }
}


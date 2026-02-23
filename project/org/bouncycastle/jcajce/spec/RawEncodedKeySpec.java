/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.spec;

import java.security.spec.EncodedKeySpec;

public class RawEncodedKeySpec
extends EncodedKeySpec {
    public RawEncodedKeySpec(byte[] byArray) {
        super(byArray);
    }

    public String getFormat() {
        return "RAW";
    }
}


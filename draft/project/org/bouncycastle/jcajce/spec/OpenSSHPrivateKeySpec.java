/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.spec;

import java.security.spec.EncodedKeySpec;

public class OpenSSHPrivateKeySpec
extends EncodedKeySpec {
    private final String format;

    public OpenSSHPrivateKeySpec(byte[] byArray) {
        super(byArray);
        if (byArray[0] == 48) {
            this.format = "ASN.1";
        } else if (byArray[0] == 111) {
            this.format = "OpenSSH";
        } else {
            throw new IllegalArgumentException("unknown byte encoding");
        }
    }

    public String getFormat() {
        return this.format;
    }
}


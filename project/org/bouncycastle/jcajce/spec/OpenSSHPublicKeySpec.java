/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.spec;

import java.security.spec.EncodedKeySpec;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public class OpenSSHPublicKeySpec
extends EncodedKeySpec {
    private static final String[] allowedTypes = new String[]{"ssh-rsa", "ssh-ed25519", "ssh-dss"};
    private final String type;

    public OpenSSHPublicKeySpec(byte[] byArray) {
        super(byArray);
        int n = 0;
        int n2 = (byArray[n++] & 0xFF) << 24;
        n2 |= (byArray[n++] & 0xFF) << 16;
        n2 |= (byArray[n++] & 0xFF) << 8;
        if (n + (n2 |= byArray[n++] & 0xFF) >= byArray.length) {
            throw new IllegalArgumentException("invalid public key blob: type field longer than blob");
        }
        this.type = Strings.fromByteArray(Arrays.copyOfRange(byArray, n, n + n2));
        if (this.type.startsWith("ecdsa")) {
            return;
        }
        for (int i = 0; i < allowedTypes.length; ++i) {
            if (!allowedTypes[i].equals(this.type)) continue;
            return;
        }
        throw new IllegalArgumentException("unrecognised public key type " + this.type);
    }

    public String getFormat() {
        return "OpenSSH";
    }

    public String getType() {
        return this.type;
    }
}


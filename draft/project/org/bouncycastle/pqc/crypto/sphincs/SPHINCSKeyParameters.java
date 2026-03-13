/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class SPHINCSKeyParameters
extends AsymmetricKeyParameter {
    public static final String SHA512_256 = "SHA-512/256";
    public static final String SHA3_256 = "SHA3-256";
    private final String treeDigest;

    protected SPHINCSKeyParameters(boolean bl, String string) {
        super(bl);
        this.treeDigest = string;
    }

    public String getTreeDigest() {
        return this.treeDigest;
    }
}


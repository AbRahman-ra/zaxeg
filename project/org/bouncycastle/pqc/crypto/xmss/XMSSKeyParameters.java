/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class XMSSKeyParameters
extends AsymmetricKeyParameter {
    public static final String SHA_256 = "SHA-256";
    public static final String SHA_512 = "SHA-512";
    public static final String SHAKE128 = "SHAKE128";
    public static final String SHAKE256 = "SHAKE256";
    private final String treeDigest;

    public XMSSKeyParameters(boolean bl, String string) {
        super(bl);
        this.treeDigest = string;
    }

    public String getTreeDigest() {
        return this.treeDigest;
    }
}


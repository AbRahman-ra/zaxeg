/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.qtesla;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.crypto.qtesla.QTESLASecurityCategory;
import org.bouncycastle.util.Arrays;

public final class QTESLAPublicKeyParameters
extends AsymmetricKeyParameter {
    private int securityCategory;
    private byte[] publicKey;

    public QTESLAPublicKeyParameters(int n, byte[] byArray) {
        super(false);
        if (byArray.length != QTESLASecurityCategory.getPublicSize(n)) {
            throw new IllegalArgumentException("invalid key size for security category");
        }
        this.securityCategory = n;
        this.publicKey = Arrays.clone(byArray);
    }

    public int getSecurityCategory() {
        return this.securityCategory;
    }

    public byte[] getPublicData() {
        return Arrays.clone(this.publicKey);
    }
}


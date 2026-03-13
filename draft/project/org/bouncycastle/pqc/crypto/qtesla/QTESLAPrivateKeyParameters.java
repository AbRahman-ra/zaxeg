/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.qtesla;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.crypto.qtesla.QTESLASecurityCategory;
import org.bouncycastle.util.Arrays;

public final class QTESLAPrivateKeyParameters
extends AsymmetricKeyParameter {
    private int securityCategory;
    private byte[] privateKey;

    public QTESLAPrivateKeyParameters(int n, byte[] byArray) {
        super(true);
        if (byArray.length != QTESLASecurityCategory.getPrivateSize(n)) {
            throw new IllegalArgumentException("invalid key size for security category");
        }
        this.securityCategory = n;
        this.privateKey = Arrays.clone(byArray);
    }

    public int getSecurityCategory() {
        return this.securityCategory;
    }

    public byte[] getSecret() {
        return Arrays.clone(this.privateKey);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto;

import org.bouncycastle.crypto.CipherParameters;

public interface RawAgreement {
    public void init(CipherParameters var1);

    public int getAgreementSize();

    public void calculateAgreement(CipherParameters var1, byte[] var2, int var3);
}


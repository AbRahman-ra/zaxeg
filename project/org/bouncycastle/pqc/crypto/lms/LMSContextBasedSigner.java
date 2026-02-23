/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.pqc.crypto.lms.LMSContext;

public interface LMSContextBasedSigner {
    public LMSContext generateLMSContext();

    public byte[] generateSignature(LMSContext var1);

    public long getUsagesRemaining();
}


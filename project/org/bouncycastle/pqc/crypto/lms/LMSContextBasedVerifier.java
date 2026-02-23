/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.pqc.crypto.lms.LMSContext;

public interface LMSContextBasedVerifier {
    public LMSContext generateLMSContext(byte[] var1);

    public boolean verify(LMSContext var1);
}


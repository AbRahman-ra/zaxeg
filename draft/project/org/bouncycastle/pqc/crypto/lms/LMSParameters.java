/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;

public class LMSParameters {
    private final LMSigParameters lmSigParam;
    private final LMOtsParameters lmOTSParam;

    public LMSParameters(LMSigParameters lMSigParameters, LMOtsParameters lMOtsParameters) {
        this.lmSigParam = lMSigParameters;
        this.lmOTSParam = lMOtsParameters;
    }

    public LMSigParameters getLMSigParam() {
        return this.lmSigParam;
    }

    public LMOtsParameters getLMOTSParam() {
        return this.lmOTSParam;
    }
}


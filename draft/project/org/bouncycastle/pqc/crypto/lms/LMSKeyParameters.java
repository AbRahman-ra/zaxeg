/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.IOException;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.Encodable;

public abstract class LMSKeyParameters
extends AsymmetricKeyParameter
implements Encodable {
    protected LMSKeyParameters(boolean bl) {
        super(bl);
    }

    public abstract byte[] getEncoded() throws IOException;
}


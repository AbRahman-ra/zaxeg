/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import java.security.SecureRandom;
import org.bouncycastle.crypto.KeyGenerationParameters;

public class Ed25519KeyGenerationParameters
extends KeyGenerationParameters {
    public Ed25519KeyGenerationParameters(SecureRandom secureRandom) {
        super(secureRandom, 256);
    }
}


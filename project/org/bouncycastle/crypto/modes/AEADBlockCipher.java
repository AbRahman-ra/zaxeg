/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.modes.AEADCipher;

public interface AEADBlockCipher
extends AEADCipher {
    public BlockCipher getUnderlyingCipher();
}


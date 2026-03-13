/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.interfaces;

import java.security.PrivateKey;
import org.bouncycastle.jcajce.interfaces.EdDSAKey;
import org.bouncycastle.jcajce.interfaces.EdDSAPublicKey;

public interface EdDSAPrivateKey
extends EdDSAKey,
PrivateKey {
    public EdDSAPublicKey getPublicKey();
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.interfaces;

import java.security.PrivateKey;
import org.bouncycastle.jcajce.interfaces.XDHKey;
import org.bouncycastle.jcajce.interfaces.XDHPublicKey;

public interface XDHPrivateKey
extends XDHKey,
PrivateKey {
    public XDHPublicKey getPublicKey();
}


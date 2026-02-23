/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.interfaces;

import java.math.BigInteger;
import java.security.PublicKey;
import org.bouncycastle.jcajce.interfaces.XDHKey;

public interface XDHPublicKey
extends XDHKey,
PublicKey {
    public BigInteger getU();

    public byte[] getUEncoding();
}


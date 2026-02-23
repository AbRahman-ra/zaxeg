/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.jcajce.interfaces;

import java.security.PrivateKey;
import org.bouncycastle.pqc.jcajce.interfaces.XMSSKey;

public interface XMSSPrivateKey
extends XMSSKey,
PrivateKey {
    public long getIndex();

    public long getUsagesRemaining();

    public XMSSPrivateKey extractKeyShard(int var1);
}


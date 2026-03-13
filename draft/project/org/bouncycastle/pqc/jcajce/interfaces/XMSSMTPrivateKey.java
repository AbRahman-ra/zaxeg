/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.jcajce.interfaces;

import java.security.PrivateKey;
import org.bouncycastle.pqc.jcajce.interfaces.XMSSMTKey;

public interface XMSSMTPrivateKey
extends XMSSMTKey,
PrivateKey {
    public long getIndex();

    public long getUsagesRemaining();

    public XMSSMTPrivateKey extractKeyShard(int var1);
}


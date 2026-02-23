/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto;

import java.math.BigInteger;
import org.bouncycastle.crypto.DSA;

public interface DSAExt
extends DSA {
    public BigInteger getOrder();
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec;

import org.bouncycastle.math.ec.ECPoint;

public interface ECLookupTable {
    public int getSize();

    public ECPoint lookup(int var1);

    public ECPoint lookupVar(int var1);
}


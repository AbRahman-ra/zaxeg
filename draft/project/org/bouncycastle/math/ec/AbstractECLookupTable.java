/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec;

import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;

public abstract class AbstractECLookupTable
implements ECLookupTable {
    public ECPoint lookupVar(int n) {
        return this.lookup(n);
    }
}


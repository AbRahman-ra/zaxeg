/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import net.sf.saxon.om.NodeInfo;

public interface NodeWrappingFunction<B, T extends NodeInfo> {
    public T wrap(B var1);
}


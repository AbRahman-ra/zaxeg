/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.wrapper.VirtualNode;

public interface WrappingFunction {
    public VirtualNode makeWrapper(NodeInfo var1, VirtualNode var2);
}


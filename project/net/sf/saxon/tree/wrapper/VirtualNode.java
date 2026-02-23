/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import net.sf.saxon.om.NodeInfo;

public interface VirtualNode
extends NodeInfo {
    public Object getUnderlyingNode();

    public Object getRealNode();
}


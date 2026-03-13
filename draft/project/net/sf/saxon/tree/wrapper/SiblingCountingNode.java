/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import net.sf.saxon.om.NodeInfo;

public interface SiblingCountingNode
extends NodeInfo {
    public int getSiblingPosition();
}


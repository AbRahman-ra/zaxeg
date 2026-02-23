/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

import net.sf.saxon.om.NodeInfo;

public interface SteppingNode<N extends SteppingNode>
extends NodeInfo {
    public N getParent();

    public N getNextSibling();

    public N getPreviousSibling();

    public N getFirstChild();

    public N getSuccessorElement(N var1, String var2, String var3);
}


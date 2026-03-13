/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import net.sf.saxon.om.NodeInfo;

public interface NodeVectorTree {
    default public boolean isTyped() {
        return false;
    }

    public NodeInfo getNode(int var1);

    public int getNodeKind(int var1);

    public int getFingerprint(int var1);

    public byte[] getNodeKindArray();

    public int[] getNameCodeArray();
}


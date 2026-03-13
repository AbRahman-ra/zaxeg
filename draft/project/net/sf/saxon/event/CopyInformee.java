/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.om.NodeInfo;

public interface CopyInformee<T> {
    public T notifyElementNode(NodeInfo var1);
}


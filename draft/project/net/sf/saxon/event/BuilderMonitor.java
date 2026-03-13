/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.NodeInfo;

public abstract class BuilderMonitor
extends ProxyReceiver {
    public BuilderMonitor(Receiver next) {
        super(next);
    }

    public abstract void markNextNode(int var1);

    public abstract NodeInfo getMarkedNode();
}


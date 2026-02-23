/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sapling;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.trans.XPathException;

public abstract class SaplingNode {
    public abstract int getNodeKind();

    protected abstract void sendTo(Receiver var1) throws XPathException;
}


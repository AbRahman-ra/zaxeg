/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public interface SpaceStrippingRule {
    public int isSpacePreserving(NodeName var1, SchemaType var2) throws XPathException;

    public ProxyReceiver makeStripper(Receiver var1);

    public void export(ExpressionPresenter var1) throws XPathException;
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import net.sf.saxon.Controller;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;

public interface UpdateAgent {
    public void update(NodeInfo var1, Controller var2) throws XPathException;
}


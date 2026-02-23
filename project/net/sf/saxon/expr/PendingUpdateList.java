/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Set;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;

public interface PendingUpdateList {
    public void apply(XPathContext var1, int var2) throws XPathException;

    public Set<MutableNodeInfo> getAffectedTrees();

    public void addPutAction(NodeInfo var1, String var2, Expression var3) throws XPathException;
}


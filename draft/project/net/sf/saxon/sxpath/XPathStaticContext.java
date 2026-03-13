/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sxpath;

import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.value.QNameValue;

public interface XPathStaticContext
extends StaticContext {
    public void setDefaultElementNamespace(String var1);

    public void setNamespaceResolver(NamespaceResolver var1);

    public XPathVariable declareVariable(QNameValue var1);

    public XPathVariable declareVariable(String var1, String var2);

    public SlotManager getStackFrameMap();

    public boolean isContextItemParentless();
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;

public interface Resource {
    public String getResourceURI();

    public Item getItem(XPathContext var1) throws XPathException;

    public String getContentType();
}


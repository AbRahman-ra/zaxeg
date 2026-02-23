/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Iterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.trans.XPathException;

public interface ResourceCollection {
    public String getCollectionURI();

    public Iterator<String> getResourceURIs(XPathContext var1) throws XPathException;

    public Iterator<? extends Resource> getResources(XPathContext var1) throws XPathException;

    public boolean isStable(XPathContext var1);

    public boolean stripWhitespace(SpaceStrippingRule var1);
}


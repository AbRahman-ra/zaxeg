/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.trans.XPathException;

@FunctionalInterface
public interface CollectionFinder {
    public ResourceCollection findCollection(XPathContext var1, String var2) throws XPathException;
}


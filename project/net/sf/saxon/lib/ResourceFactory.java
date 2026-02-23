/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.trans.XPathException;

public interface ResourceFactory {
    public Resource makeResource(Configuration var1, AbstractResourceCollection.InputDetails var2) throws XPathException;
}


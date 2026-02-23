/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;

public interface CollationURIResolver {
    public StringCollator resolve(String var1, Configuration var2) throws XPathException;
}


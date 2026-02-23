/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.Reader;
import java.net.URI;
import net.sf.saxon.Configuration;
import net.sf.saxon.trans.XPathException;

public interface UnparsedTextURIResolver {
    public Reader resolve(URI var1, String var2, Configuration var3) throws XPathException;
}


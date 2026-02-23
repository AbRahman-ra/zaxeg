/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.trans.XPathException;

public interface ModuleURIResolver {
    public StreamSource[] resolve(String var1, String var2, String[] var3) throws XPathException;
}


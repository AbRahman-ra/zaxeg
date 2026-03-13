/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.trans.XPathException;

public interface SchemaURIResolver {
    public void setConfiguration(Configuration var1);

    public Source[] resolve(String var1, String var2, String[] var3) throws XPathException;
}


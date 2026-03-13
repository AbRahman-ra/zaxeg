/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.trans.XPathException;

public interface SourceResolver {
    public Source resolveSource(Source var1, Configuration var2) throws XPathException;
}


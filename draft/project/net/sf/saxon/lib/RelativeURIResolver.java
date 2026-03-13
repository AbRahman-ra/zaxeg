/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

public interface RelativeURIResolver
extends URIResolver {
    public String makeAbsolute(String var1, String var2) throws TransformerException;

    public Source dereference(String var1) throws TransformerException;

    @Override
    public Source resolve(String var1, String var2) throws TransformerException;
}


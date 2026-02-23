/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

public interface OutputURIResolver {
    public OutputURIResolver newInstance();

    public Result resolve(String var1, String var2) throws TransformerException;

    public void close(Result var1) throws TransformerException;
}


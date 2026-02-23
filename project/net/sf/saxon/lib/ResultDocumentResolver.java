/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;

public interface ResultDocumentResolver {
    public Receiver resolve(XPathContext var1, String var2, String var3, SerializationProperties var4) throws XPathException;
}


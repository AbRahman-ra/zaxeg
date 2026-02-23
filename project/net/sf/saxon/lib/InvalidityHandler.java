/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.lib.Invalidity;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public interface InvalidityHandler {
    public void startReporting(String var1) throws XPathException;

    public void reportInvalidity(Invalidity var1) throws XPathException;

    public Sequence endReporting() throws XPathException;
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public interface Fold {
    public void processItem(Item var1) throws XPathException;

    public boolean isFinished();

    public Sequence result() throws XPathException;
}


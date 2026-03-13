/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.value.AtomicValue;

public interface AtomicComparer {
    public StringCollator getCollator();

    public AtomicComparer provideContext(XPathContext var1);

    public int compareAtomicValues(AtomicValue var1, AtomicValue var2) throws NoDynamicContextException;

    public boolean comparesEqual(AtomicValue var1, AtomicValue var2) throws NoDynamicContextException;

    public String save();
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;

public interface SortKeyEvaluator {
    public AtomicValue evaluateSortKey(int var1, XPathContext var2) throws XPathException;
}


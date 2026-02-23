/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public interface ExportAgent {
    public void export(ExpressionPresenter var1) throws XPathException;
}


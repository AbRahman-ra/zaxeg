/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.trans.XPathException;

public interface ExpressionAction {
    public boolean process(Expression var1, Object var2) throws XPathException;
}

